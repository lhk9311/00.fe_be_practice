package com.example.todo.exception;

// 없는 id를 수정/삭제하려 할 때 던지는 예외 → 404로 응답됨
public class TodoNotFoundException extends RuntimeException {
    public TodoNotFoundException(Long id) {
        super("할 일을 찾을 수 없습니다. id=" + id);
    }
}
