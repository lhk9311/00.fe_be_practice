// 서버 통신만 모아둔 파일 (백엔드의 Repository / Service 계층처럼 역할을 분리)
// 화면 코드(App.jsx)는 "어떤 URL로 어떻게 보내는지" 몰라도 되게 한다.

// 프록시를 쓰니까 앞에 주소 없이 '/api' 로 시작
// [실습 3] 프록시를 끄면 이걸 'http://localhost:8080/api/todos' 로 바꿔서 CORS 에러를 직접 본다
const BASE_URL = '/api/todos';

// 공통 요청 함수: 성공이면 JSON을, 실패면 서버가 준 에러 메시지로 예외를 던진다
// ★ fetch는 4xx/5xx가 와도 에러로 안 친다! res.ok 로 직접 확인해야 함 (실무 단골 함정)
async function request(url, options = {}) {
  const res = await fetch(url, {
    ...options,
    // 본문이 있을 때만 "본문이 JSON이다"라고 서버에 알린다.
    // (GET에도 붙이면 브라우저가 OPTIONS 사전 요청(preflight)을 먼저 보내서 실습 3이 헷갈려짐)
    headers: options.body ? { 'Content-Type': 'application/json' } : {},
  });

  if (!res.ok) {
    // GlobalExceptionHandler가 { status, message } 형식으로 내려준다
    const body = await res.json().catch(() => ({}));
    throw new Error(`[${res.status}] ${body.message ?? res.statusText}`);
  }

  // 204 No Content(삭제 성공)는 본문이 없어서 json()을 부르면 에러가 난다
  if (res.status === 204) return null;
  return res.json();
}

// GET /api/todos → TodoController.list()
export function fetchTodos() {
  return request(BASE_URL);
}

// POST /api/todos → TodoController.create()
// JSON.stringify: JS 객체를 JSON 문자열로 바꿈. 이 키 이름이 TodoRequest 필드 이름과 같아야 한다
// [실습 1-a] title 을 todoTitle 로 바꿔보기
export function createTodo(title) {
  return request(BASE_URL, {
    method: 'POST',
    body: JSON.stringify({ title }),
  });
}

// PUT /api/todos/{id} → TodoController.update()
// [실습 1-b] done 을 completed 로 바꿔보기
export function updateTodo(todo) {
  return request(`${BASE_URL}/${todo.id}`, {
    method: 'PUT',
    body: JSON.stringify({ title: todo.title, done: todo.done }),
  });
}

// DELETE /api/todos/{id} → TodoController.delete()
export function deleteTodo(id) {
  return request(`${BASE_URL}/${id}`, { method: 'DELETE' });
}

// GET /api/todos/boom → 일부러 500 에러 [실습 4]
export function triggerServerError() {
  return request(`${BASE_URL}/boom`);
}

/*
  [실무 메모] 실무에서는 fetch 대신 axios를 많이 쓴다 (npm install axios)
  - 4xx/5xx면 자동으로 에러로 던져줌 (위의 res.ok 검사가 필요 없음)
  - JSON 변환 자동
  - 공통 헤더(로그인 토큰 등)를 한 곳에서 설정 가능 (interceptor)
  예) const res = await axios.post('/api/todos', { title });  res.data 가 결과
*/
