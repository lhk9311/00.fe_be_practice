# React + Spring 연동 실습

할 일 CRUD를 **React(프론트)와 Spring Boot(백엔드)로 나눠서** 붙이고,
F12 Network 탭으로 요청이 오가는 걸 직접 확인하는 실습이다.
정상 동작을 확인한 다음, **일부러 망가뜨려 보는 것**이 핵심이다.

```
fe-be-practice/
├─ backend/    Spring Boot (8080)   → IntelliJ로 연다
└─ frontend/   React + Vite (5173)  → VS Code로 연다
```

## 구조 한눈에

```
[브라우저 5173]  --fetch('/api/todos')-->  [Vite 프록시]  -->  [Spring 8080]
   App.jsx                                                    TodoController   (@RestController)
   api.js      <------------- JSON 응답 -------------------   TodoService      (@Service, 메모리 저장)
                                                              GlobalExceptionHandler (에러 → 상태코드)
```

| API | 메서드 | 성공 코드 | 컨트롤러 메서드 |
|---|---|---|---|
| `/api/todos` | GET | 200 | `list()` |
| `/api/todos` | POST | 201 | `create()` |
| `/api/todos/{id}` | PUT | 200 | `update()` |
| `/api/todos/{id}` | DELETE | 204 | `delete()` |
| `/api/todos/boom` | GET | (항상 500) | `boom()` 실습용 |

## 실행

**1. 백엔드 (IntelliJ)**
1. IntelliJ에서 **File > Open** → `backend/pom.xml` 선택 → **Open as Project**
2. 오른쪽 아래 Maven 의존성 다운로드가 끝날 때까지 기다림 (처음 한 번, 몇 분)
3. `TodoApplication.java` 열고 `main` 옆 ▶ 클릭
4. 콘솔에 `Tomcat started on port 8080` 나오면 성공
5. 브라우저에서 http://localhost:8080/api/todos 열어보기 → JSON 배열이 보이면 OK

> JDK 17 이상 필요. IntelliJ가 JDK를 물어보면 **File > Project Structure > SDK**에서 17 이상 선택 (없으면 거기서 Download JDK).
> Spring Boot 버전은 `pom.xml`의 `3.5.6`. 다운로드가 실패하면 IntelliJ의 Maven 창에서 새로고침(↻)을 한 번 눌러본다.

**2. 프론트 (VS Code 터미널)**
```bash
cd frontend
npm install
npm run dev
```
http://localhost:5173 열기. 목록 2개가 보이면 연동 성공이다.

## 먼저: Network 탭 보는 법

1. F12 → **Network** 탭 → 필터에서 **Fetch/XHR** 선택 (이미지, JS 파일 요청을 숨기고 API 요청만 보기)
2. 화면에서 할 일을 추가하면 `todos` 줄이 하나 생긴다. 클릭하면 오른쪽에 탭이 나온다.

| 탭 | 볼 것 |
|---|---|
| Headers | Request URL, Request Method, **Status Code** |
| Payload | 프론트가 **보낸** JSON |
| Response / Preview | 서버가 **돌려준** JSON |

**정상 확인**: 추가, 체크, 수정, 삭제를 한 번씩 하면서 Method와 Status가 위 API 표와 같은지 본다 (POST 201, PUT 200, DELETE 204).

## 망가뜨리기 실습

하나 해보고 원래대로 되돌린 다음 다음 걸 한다.

### 실습 1-a. JSON 키 이름 틀리기 (검증이 잡아주는 경우)
- **바꿀 곳**: `frontend/src/api.js`의 `createTodo` → `JSON.stringify({ title })` 를 `JSON.stringify({ todoTitle: title })` 로
- **해볼 것**: 할 일 추가
- **보이는 것**: Payload에는 값이 분명히 있는데 Status **400**, 메시지 "할 일을 입력하세요"
- **의미**: 서버 DTO에 `todoTitle` 필드가 없어서 `title`이 null이 됐고, `@NotBlank`가 잡았다. 키 이름이 DTO 필드명과 같아야 한다.

### 실습 1-b. JSON 키 이름 틀리기 (조용히 틀리는 경우) ⭐
- **바꿀 곳**: `api.js`의 `updateTodo` → `done: todo.done` 을 `completed: todo.done` 으로
- **해볼 것**: 체크박스 클릭
- **보이는 것**: Status **200 성공**인데 체크가 안 됨. Response를 보면 `"done": false`
- **의미**: 검증이 없는 필드는 에러 없이 기본값(false)이 된다. 실무에서 "저장은 되는데 값이 안 바뀌어요"가 이거다. **에러가 안 나서 더 무섭다.** Payload와 Response를 나란히 비교하는 습관이 답이다.

### 실습 2. `@RequestBody` 빼먹기
- **바꿀 곳**: `TodoController.create()`의 `@Valid @RequestBody TodoRequest request` 에서 `@RequestBody`만 지우고 서버 재시작
- **해볼 것**: 할 일 추가
- **보이는 것**: Payload에 title이 있는데 **400** "할 일을 입력하세요"
- **의미**: `@RequestBody`가 없으면 스프링이 요청 **본문(JSON)을 안 읽는다**. 프론트는 제대로 보냈으니 이건 백엔드 문제다. "보낸 건 맞는데 서버가 못 받았다"를 Payload로 증명하는 연습.

### 실습 3. CORS 에러 만들고 고치기
- **바꿀 곳 (2군데)**:
  1. `frontend/vite.config.js`의 `proxy: { ... }` 세 줄을 주석 처리
  2. `api.js`의 `BASE_URL`을 `'http://localhost:8080/api/todos'` 로
- **보이는 것**: 화면에 목록이 안 나오고, **Console**에 빨간 `blocked by CORS policy`. Network에서는 해당 요청이 `CORS error`로 표시됨
- **재밌는 점**: IntelliJ 콘솔에 `목록 조회 요청` 로그가 찍혀 있다. 서버는 요청을 **받았고 정상 응답도 했다**. 응답을 막은 건 브라우저다.
- 참고: 추가(POST)나 수정(PUT)을 누르면 Network에 `OPTIONS` 요청이 먼저 보인다. JSON을 보내는 요청은 브라우저가 "보내도 돼?"라고 사전 요청(preflight)부터 하는데, 여기서 거절당해서 본 요청은 아예 안 나간다.
- **고치는 법 (서버 쪽)**: `TodoController` 클래스 위에 `@CrossOrigin(origins = "http://localhost:5173")` 추가하고 재시작 → 정상 동작
- **실무 정리**: 개발 중엔 프록시, 운영에선 같은 도메인으로 배포하거나 서버에서 허용 출처를 설정한다. 확인 후 원래대로 되돌리기.

### 실습 4. 서버 500 에러
- **해볼 것**: 화면 아래 **[500 에러 테스트]** 버튼 클릭
- **보이는 것**: Status **500**, Response는 `"서버 오류가 발생했습니다"` 한 줄뿐. IntelliJ 콘솔에는 **빨간 스택트레이스 전체**
- **의미**: 프론트에는 짧은 메시지만, 진짜 원인은 서버 로그에 있다 (`GlobalExceptionHandler`가 그렇게 나눠둠). 화면 개발자가 "500 떠요" 하면 서버 로그를 보는 게 백엔드 개발자 일이다.

### 실습 5. 입력 검증 (400)
- **해볼 것**: 입력창을 비워둔 채 추가 (스페이스만 넣어도 됨)
- **보이는 것**: Status **400**, "할 일을 입력하세요"
- **의미**: 프론트에서 일부러 빈 값 검사를 안 했다. 그래도 서버 `@Valid` + `@NotBlank`가 막는다. 프론트 검사는 F12나 Postman으로 쉽게 우회되니까 **서버 검증이 진짜 방어선**이다.

### 보너스. URL 오타 (404)
- `api.js`의 `BASE_URL`을 `'/api/todo'` (s 빠짐)로 → Status **404**

## 정리: 증상으로 어느 쪽 문제인지 가르기

| Network에서 보이는 것 | 어느 쪽 | 실습 |
|---|---|---|
| 요청 줄이 아예 안 생김 | 프론트 (이벤트, JS 에러 → Console 확인) | - |
| Payload가 이상함 | 프론트 | 1-a, 1-b |
| Payload는 맞는데 4xx/5xx | 백엔드 (서버 로그 확인) | 2, 4 |
| 200인데 Response 값이 기대와 다름 | 매핑 문제 (Payload와 Response 비교) | 1-b |
| CORS error | 설정 (프록시 / `@CrossOrigin`) | 3 |
| 404 | URL 또는 매핑 경로 | 보너스 |

## 코드 읽는 순서 추천

1. `backend/.../controller/TodoController.java` — `@RestController`, DI, `@RequestBody`, 상태코드
2. `backend/.../dto/TodoRequest.java` — JSON ↔ 필드 매핑 규칙, `@NotBlank`
3. `backend/.../exception/GlobalExceptionHandler.java` — 예외를 상태코드로 바꾸는 곳
4. `frontend/src/api.js` — fetch, `res.ok` 함정, 204 처리
5. `frontend/src/App.jsx` — `fe-compare/02-react/src/App.jsx`와 비교 (배열 직접 수정 → 서버 요청 후 갱신)

## GitHub에 올리기 (선택)

GitHub에서 빈 레포 `fe-be-practice`를 만든 다음:
```bash
cd fe-be-practice
git init
git add .
git commit -m "React + Spring 연동 실습"
git branch -M main
git remote add origin https://github.com/<내 아이디>/fe-be-practice.git
git push -u origin main
```
`.gitignore`에 `node_modules/`, `target/`, `.idea/`가 들어 있어서 무거운 폴더는 안 올라간다.
