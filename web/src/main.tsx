import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import 'mdui/mdui.css';
import 'mdui';
import './styles.css';
import { applyAndroidColorScheme, type ColorSchemeName } from './theme/androidColors';

const savedColorScheme = (window.localStorage.getItem('selves-color-scheme') || 'default') as ColorSchemeName;
applyAndroidColorScheme(savedColorScheme);

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
