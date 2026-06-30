export type ThemeMode = 'light' | 'dark' | 'auto';

const STORAGE_KEY = 'selves-theme';
const CLASS_PREFIX = 'mdui-theme-';
const THEME_CLASSES = (['light', 'dark', 'auto'] as const).map(m => CLASS_PREFIX + m);

/** 读取已保存的主题模式，未设置时默认 auto */
export function getThemeMode(): ThemeMode {
  return (window.localStorage.getItem(STORAGE_KEY) as ThemeMode | null) ?? 'auto';
}

/** 将主题模式写入 localStorage 并立即应用到 <html> */
export function setThemeMode(mode: ThemeMode): void {
  window.localStorage.setItem(STORAGE_KEY, mode);
  _applyClass(mode);
}

/** 在应用启动时调用，读取已保存的主题并应用（替代 mdui setTheme）*/
export function initTheme(): void {
  _applyClass(getThemeMode());
}

function _applyClass(mode: ThemeMode): void {
  const root = document.documentElement;
  // 移除全部主题类再添加新类，与 mdui setTheme 行为完全一致
  root.classList.remove(...THEME_CLASSES);
  root.classList.add(CLASS_PREFIX + mode);
}
