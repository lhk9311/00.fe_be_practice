package com.example.todo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// @RestControllerAdvice: 모든 컨트롤러에서 터진 예외를 여기서 한 번에 받아서
// "상태코드 + 에러 JSON"으로 바꿔 응답한다.
// 이게 없으면 스프링 기본 에러 JSON이 나가는데, 프론트가 메시지를 꺼내 쓰기 불편하다.
// → 실무에서는 팀마다 이 에러 응답 형식을 프론트와 미리 약속해둔다
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404: 없는 데이터
    @ExceptionHandler(TodoNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(TodoNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 400: @Valid 검증 실패 (빈 제목 등). 첫 번째 검증 메시지를 꺼내서 돌려준다
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("입력값이 올바르지 않습니다");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    // 400: JSON 문법이 깨져서 읽을 수 없을 때 (쉼표 빠짐, 따옴표 안 닫힘 등)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "요청 JSON 형식이 올바르지 않습니다");
    }

    // 500: 그 밖의 모든 예외. 서버 로그에는 스택트레이스를 남기고, 프론트에는 짧은 메시지만
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleEtc(Exception e) {
        // 스프링이 원래 상태코드를 정해둔 예외(없는 URL → 404, 지원 안 하는 메서드 → 405 등)는 그 코드 그대로 돌려줌
        // 이걸 안 하면 URL 오타까지 전부 500으로 보여서 원인을 헷갈리게 된다
        if (e instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
            return error(status, status.getReasonPhrase());
        }
        log.error("서버 에러", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다");
    }

    // 응답 형식: { "status": 404, "message": "..." }
    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = Map.of("status", status.value(), "message", message);
        return ResponseEntity.status(status).body(body);
    }
}
