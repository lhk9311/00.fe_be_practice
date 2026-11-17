import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,

    // ★ 프록시: 프론트에서 fetch('/api/todos') 하면
    //   Vite 개발 서버가 대신 http://localhost:8080/api/todos 로 보내준다.
    //
    //   왜 필요하냐면: 브라우저 입장에서 5173(프론트)과 8080(백엔드)은 "다른 출처"라서,
    //   서버가 허락(CORS 헤더)하지 않으면 응답을 막아버린다.
    //   프록시를 쓰면 브라우저는 5173에만 요청한 것처럼 보이니까 CORS 문제가 안 생김.
    //
    //   [실습 3] 아래 proxy 부분을 주석 처리하고 저장 → 브라우저에서 무슨 일이 생기는지 확인
    //   (주석 처리할 때 api.js의 BASE_URL도 같이 바꿔야 함. README 참고)
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
});
