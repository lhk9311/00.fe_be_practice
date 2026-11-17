import { useEffect, useState } from 'react';
import { fetchTodos, createTodo, updateTodo, deleteTodo, triggerServerError } from './api.js';

// fe-compare/02-react/src/App.jsx 와 비교해서 보기:
// 배열을 직접 고치던 부분이 전부 "서버에 요청 → 응답으로 상태 갱신"으로 바뀌었다.
export default function App() {
  const [todos, setTodos] = useState([]);       // 처음엔 빈 배열. 서버에서 받아와서 채움
  const [title, setTitle] = useState('');
  const [loading, setLoading] = useState(true); // 로딩 중 표시
  const [error, setError] = useState('');       // 에러 메시지 표시

  // ===== 화면이 처음 뜰 때 1번: 목록 조회 =====
  // useEffect(함수, []) : 빈 배열 = 최초 1회만 실행 (Vue의 onMounted)
  useEffect(() => {
    fetchTodos()
      .then(setTodos)                 // 받은 배열로 상태 교체 → 화면 자동 갱신
      .catch(e => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  // 공통: 요청 실행하고 에러면 메시지 보여주기
  const run = async (job) => {
    setError('');
    try {
      await job();
    } catch (e) {
      setError(e.message);
    }
  };

  // ===== Create =====
  // 프론트에서 빈 값 검사를 하지 않는다! 일부러 서버 @Valid 검증(400)을 보려고. [실습 5]
  // (실무에선 둘 다 한다: 프론트 검사 = 사용자 편의, 서버 검사 = 진짜 방어)
  const handleAdd = () =>
    run(async () => {
      const created = await createTodo(title);   // 서버가 id를 붙여서 돌려준 객체
      setTodos(prev => [...prev, created]);
      setTitle('');
    });

  // ===== Update (완료 토글) =====
  const handleToggle = (todo) =>
    run(async () => {
      const updated = await updateTodo({ ...todo, done: !todo.done });
      // 서버가 돌려준 값으로 화면을 갱신한다 → 서버에 실제로 저장된 값이 화면에 보임
      setTodos(prev => prev.map(t => (t.id === updated.id ? updated : t)));
    });

  // ===== Update (제목 수정) =====
  const handleEdit = (todo) => {
    const newTitle = prompt('수정할 내용', todo.title);
    if (newTitle === null) return; // 취소
    run(async () => {
      const updated = await updateTodo({ ...todo, title: newTitle });
      setTodos(prev => prev.map(t => (t.id === updated.id ? updated : t)));
    });
  };

  // ===== Delete =====
  const handleDelete = (id) =>
    run(async () => {
      await deleteTodo(id);
      setTodos(prev => prev.filter(t => t.id !== id));
    });

  const remain = todos.filter(t => !t.done).length;

  if (loading) return <p style={{ textAlign: 'center', marginTop: 40 }}>불러오는 중...</p>;

  return (
    <div style={{ fontFamily: 'sans-serif', maxWidth: 480, margin: '40px auto' }}>
      <h1>할 일 (React + Spring)</h1>

      <input
        value={title}
        onChange={e => setTitle(e.target.value)}
        onKeyUp={e => e.key === 'Enter' && handleAdd()}
        placeholder="할 일을 입력하고 Enter"
      />
      <button onClick={handleAdd}>추가</button>

      {/* 에러가 있을 때만 빨간 박스 표시. 서버가 준 상태코드와 메시지가 그대로 보인다 */}
      {error && (
        <p style={{ color: '#b00020', background: '#fdecea', padding: 8, borderRadius: 4 }}>{error}</p>
      )}

      <p>남은 할 일: {remain} / 전체 {todos.length}</p>

      <ul style={{ padding: 0 }}>
        {todos.map(todo => (
          <li key={todo.id} style={{ display: 'flex', gap: 8, margin: '4px 0' }}>
            <input type="checkbox" checked={todo.done} onChange={() => handleToggle(todo)} />
            <span style={{ flex: 1, textDecoration: todo.done ? 'line-through' : 'none' }}>
              {todo.title}
            </span>
            <button onClick={() => handleEdit(todo)}>수정</button>
            <button onClick={() => handleDelete(todo.id)}>삭제</button>
          </li>
        ))}
      </ul>

      <hr />
      {/* [실습 4] 서버 500 에러가 Network 탭과 화면에 어떻게 보이는지 */}
      <button onClick={() => run(triggerServerError)}>500 에러 테스트</button>
    </div>
  );
}
