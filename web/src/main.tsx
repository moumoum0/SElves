import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import 'mdui/mdui.css';
import 'mdui';
import './styles.css';
import { applyAndroidColorScheme, type ColorSchemeName } from './theme/androidColors';
import { initTheme } from './ui/theme/themeManager';
import './ui/material/register';

// 调色板优先于主题类，避免 FOUC
const savedColorScheme = (window.localStorage.getItem('selves-color-scheme') || 'default') as ColorSchemeName;
applyAndroidColorScheme(savedColorScheme);

// 用 themeManager 替代 mdui setTheme；未设置时默认 auto（跟随系统），与 Settings UI 一致
initTheme();

createRoot(document.getElementById('root')!).render(
  <BrowserRouter>
    <App />
  </BrowserRouter>,
);
