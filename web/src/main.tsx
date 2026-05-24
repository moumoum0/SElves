import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import 'mdui/mdui.css';
import 'mdui';
import './styles.css';
import { applyAndroidColorScheme } from './theme/androidColors';

const savedScheme = window.localStorage.getItem('selves-color-scheme');
if (savedScheme && savedScheme !== '#475D92') {
  import('mdui/functions/setColorScheme.js').then(({ setColorScheme }) => {
    setColorScheme(savedScheme);
  });
} else {
  applyAndroidColorScheme();
}

const savedTheme = window.localStorage.getItem('selves-theme') as 'light' | 'dark' | 'auto' | null;
if (savedTheme) {
  import('mdui/functions/setTheme.js').then(({ setTheme }) => {
    setTheme(savedTheme);
  });
}

createRoot(document.getElementById('root')!).render(
  <BrowserRouter>
    <App />
  </BrowserRouter>,
);
