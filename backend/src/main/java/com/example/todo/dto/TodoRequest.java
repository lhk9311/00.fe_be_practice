package com.example.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 요청 DTO: 프론트가 보낸 JSON을 담는 그릇
//
// ★ 매핑 규칙: JSON의 키 이름 == 이 클래스의 필드(setter) 이름
//   프론트가 { "title": "공부" } 를 보내면 → setTitle("공부") 가 호출됨
//   프론트가 { "todoTitle": "공부" } 를 보내면 → 맞는 필드가 없어서 title은 null
//   (에러가 안 나고 조용히 null이 된다는 게 함정. 실습 1에서 확인)
//
// 왜 Todo(도메인)를 그대로 안 받고 DTO를 따로 두나?
//   → 클라이언트가 id 같은 값을 마음대로 보내서 덮어쓰는 걸 막고,
//     요청마다 필요한 필드와 검증 규칙만 따로 정의하려고
public class TodoRequest {

    // @Valid가 붙은 곳에서 검사됨. 빈 문자열이나 공백만 있으면 400 에러
    @NotBlank(message = "할 일을 입력하세요")
    @Size(max = 100, message = "할 일은 100자 이하로 입력하세요")
    private String title;

    private boolean done;

    // Jackson이 객체를 만들 때 기본 생성자가 필요하다
    public TodoRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
}
