package com.example.todo.controller;

import com.example.todo.domain.Todo;
import com.example.todo.dto.TodoRequest;
import com.example.todo.service.TodoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController = @Controller + @ResponseBody
//   @Controller만 쓰면 return 값을 "뷰 이름"(JSP)으로 해석한다 → SSR
//   @RestController는 return 값을 JSON으로 바꿔서 응답 본문에 넣는다 → CSR(React)과 짝
@RestController
@RequestMapping("/api/todos") // 이 컨트롤러의 모든 URL 앞에 붙는 공통 경로
public class TodoController {

    private static final Logger log = LoggerFactory.getLogger(TodoController.class);

    // ===== 의존성 주입(DI) =====
    // final 필드 + 생성자 하나 → 스프링이 TodoService 빈을 찾아서 생성자에 넣어준다.
    // @Autowired를 안 붙여도 생성자가 하나면 자동으로 주입됨 (요즘 권장 방식)
    // 장점: new를 안 하니 테스트할 때 가짜 서비스로 갈아끼우기 쉽고, final이라 바뀔 일이 없음
    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    // ===== 조회: GET /api/todos =====
    // 반환한 List<Todo>가 JSON 배열로 바뀌어 나간다 → 200 OK
    @GetMapping
    public List<Todo> list() {
        log.debug("목록 조회 요청");
        return todoService.findAll();
    }

    // ===== 추가: POST /api/todos =====
    // @RequestBody: 요청 본문(JSON)을 TodoRequest 객체로 바꿔줌 ★ 빼먹으면 필드가 전부 null (실습 2)
    // @Valid: TodoRequest의 @NotBlank 검사 실행 → 실패하면 400 (실습 5)
    // 201 Created: "새로 만들었다"는 상태코드. 200보다 의미가 정확함
    @PostMapping
    public ResponseEntity<Todo> create(@Valid @RequestBody TodoRequest request) {
        log.debug("추가 요청: title={}", request.getTitle());
        Todo created = todoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ===== 수정: PUT /api/todos/{id} =====
    // @PathVariable: URL의 {id} 자리 값을 꺼냄. /api/todos/3 → id = 3
    @PutMapping("/{id}")
    public Todo update(@PathVariable Long id, @Valid @RequestBody TodoRequest request) {
        log.debug("수정 요청: id={}, title={}, done={}", id, request.getTitle(), request.isDone());
        return todoService.update(id, request);
    }

    // ===== 삭제: DELETE /api/todos/{id} =====
    // 204 No Content: 성공했고 돌려줄 본문은 없다
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ===== 실습 4용: 일부러 500 에러 내기 =====
    // 브라우저에서 화면의 [500 에러 테스트] 버튼이 이걸 호출한다
    @GetMapping("/boom")
    public void boom() {
        throw new IllegalStateException("일부러 낸 서버 에러");
    }
}
