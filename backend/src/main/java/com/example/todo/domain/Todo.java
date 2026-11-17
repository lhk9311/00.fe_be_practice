package com.example.todo.domain;

// 저장되는 데이터 (DB가 있다면 테이블 한 행, JPA라면 @Entity 자리)
// 응답으로 나갈 때 Jackson이 getter를 보고 JSON을 만든다:
//   getId() → "id", getTitle() → "title", isDone() → "done"
// → 그래서 프론트에서 todo.id, todo.title, todo.done 으로 꺼낼 수 있는 것
public class Todo {

    private Long id;
    private String title;
    private boolean done;

    public Todo(Long id, String title, boolean done) {
        this.id = id;
        this.title = title;
        this.done = done;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public boolean isDone() { return done; }

    public void setTitle(String title) { this.title = title; }
    public void setDone(boolean done) { this.done = done; }
}
