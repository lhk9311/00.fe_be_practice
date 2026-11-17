package com.example.todo.service;

import com.example.todo.domain.Todo;
import com.example.todo.dto.TodoRequest;
import com.example.todo.exception.TodoNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

// @Service: 스프링이 이 클래스의 객체를 "딱 하나" 만들어서 관리한다 (싱글톤 빈)
// 컨트롤러는 new TodoService() 를 하지 않고, 스프링이 만들어둔 걸 주입받아 쓴다 (DI)
@Service
public class TodoService {

    // DB 대신 메모리 리스트. 서버 재시작하면 초기화됨
    // CopyOnWriteArrayList: 여러 요청이 동시에 와도 안전한 리스트 (싱글톤이라 여러 스레드가 같이 씀)
    private final List<Todo> todos = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong(0); // DB 시퀀스 / auto_increment 역할

    public TodoService() {
        todos.add(new Todo(sequence.incrementAndGet(), "DOM 개념 정리", true));
        todos.add(new Todo(sequence.incrementAndGet(), "Vite 정리", false));
    }

    // SELECT * FROM todo
    public List<Todo> findAll() {
        return new ArrayList<>(todos);
    }

    // INSERT
    public Todo create(TodoRequest request) {
        Todo todo = new Todo(sequence.incrementAndGet(), request.getTitle().trim(), false);
        todos.add(todo);
        return todo;
    }

    // UPDATE ... WHERE id = ?
    public Todo update(Long id, TodoRequest request) {
        Todo todo = findById(id);
        todo.setTitle(request.getTitle().trim());
        todo.setDone(request.isDone());
        return todo;
    }

    // DELETE ... WHERE id = ?
    public void delete(Long id) {
        Todo todo = findById(id);
        todos.remove(todo);
    }

    // 없으면 예외 → GlobalExceptionHandler가 404로 바꿔서 응답
    private Todo findById(Long id) {
        return todos.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new TodoNotFoundException(id));
    }
}
