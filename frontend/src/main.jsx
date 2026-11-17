// 진입점 (Spring의 main() 메서드 같은 곳)
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.jsx';

// index.html의 <div id="root"> 안에 <App/> 컴포넌트를 그려라
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>
);
