package com.example.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 이 클래스가 있는 패키지(com.example.todo)와 그 하위 패키지를 스캔해서
// @RestController, @Service 같은 클래스를 찾아 스프링 빈으로 등록한다.
// → 그래서 controller, service 패키지를 이 아래에 두는 것
@SpringBootApplication
public class TodoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TodoApplication.class, args);
    }
}
