
function hexToRgb(hex: string): string {
  const r = parseInt(hex.slice(0, 2), 16);
  const g = parseInt(hex.slice(2, 4), 16);
  const b = parseInt(hex.slice(4, 6), 16);
  return `${r} ${g} ${b}`;
}

// APP_DEFAULT 浅色方案 —— 直接从安卓 Theme.kt LightColorScheme 提取
const LIGHT: Record<string, string> = {
  '--mdui-color-primary':                   hexToRgb('475D92'),
  '--mdui-color-on-primary':                hexToRgb('FFFFFF'),
  '--mdui-color-primary-container':         hexToRgb('D9E2FF'),
  '--mdui-color-on-primary-container':      hexToRgb('001945'),
  '--mdui-color-secondary':                 hexToRgb('575E71'),
  '--mdui-color-on-secondary':              hexToRgb('FFFFFF'),
  '--mdui-color-secondary-container':       hexToRgb('DCE2F9'),
  '--mdui-color-on-secondary-container':    hexToRgb('151B2C'),
  '--mdui-color-tertiary':                  hexToRgb('725572'),
  '--mdui-color-on-tertiary':               hexToRgb('FFFFFF'),
  '--mdui-color-tertiary-container':        hexToRgb('FDD7FA'),
  '--mdui-color-on-tertiary-container':     hexToRgb('2A122C'),
  '--mdui-color-error':                     hexToRgb('B3261E'),
  '--mdui-color-on-error':                  hexToRgb('FFFFFF'),
  '--mdui-color-error-container':           hexToRgb('F9DEDC'),
  '--mdui-color-on-error-container':        hexToRgb('410E0B'),
  '--mdui-color-background':                hexToRgb('FEFBFF'),
  '--mdui-color-on-background':             hexToRgb('1A1B20'),
  '--mdui-color-surface':                   hexToRgb('FEFBFF'),
  '--mdui-color-on-surface':                hexToRgb('1A1B20'),
  '--mdui-color-surface-variant':           hexToRgb('E1E2EC'),
  '--mdui-color-on-surface-variant':        hexToRgb('44464F'),
  '--mdui-color-outline':                   hexToRgb('757780'),
  '--mdui-color-outline-variant':           hexToRgb('CAC4D0'),
  '--mdui-color-scrim':                     hexToRgb('000000'),
  '--mdui-color-inverse-surface':           hexToRgb('2F3036'),
  '--mdui-color-inverse-on-surface':        hexToRgb('F1F0F7'),
  '--mdui-color-inverse-primary':           hexToRgb('B0C6FF'),
  '--mdui-color-surface-dim':               hexToRgb('DAD9E0'),
  '--mdui-color-surface-bright':            hexToRgb('FEFBFF'),
  '--mdui-color-surface-container-lowest':  hexToRgb('FFFFFF'),
  '--mdui-color-surface-container-low':     hexToRgb('F8F7FE'),
  '--mdui-color-surface-container':         hexToRgb('F2F1F8'),
  '--mdui-color-surface-container-high':    hexToRgb('ECEBF2'),
  '--mdui-color-surface-container-highest': hexToRgb('E6E5ED'),
};

// APP_DEFAULT 深色方案 —— 安卓 DarkColorScheme 仅定义 primary/secondary/tertiary，
// 其余使用 Material3 baseline dark 默认值
const DARK: Record<string, string> = {
  '--mdui-color-primary':                   hexToRgb('D0BCFF'),
  '--mdui-color-on-primary':                hexToRgb('381E72'),
  '--mdui-color-primary-container':         hexToRgb('4F378B'),
  '--mdui-color-on-primary-container':      hexToRgb('EADDFF'),
  '--mdui-color-secondary':                 hexToRgb('CCC2DC'),
  '--mdui-color-on-secondary':              hexToRgb('332D41'),
  '--mdui-color-secondary-container':       hexToRgb('4A4458'),
  '--mdui-color-on-secondary-container':    hexToRgb('E8DEF8'),
  '--mdui-color-tertiary':                  hexToRgb('EFB8C8'),
  '--mdui-color-on-tertiary':               hexToRgb('492532'),
  '--mdui-color-tertiary-container':        hexToRgb('633B48'),
  '--mdui-color-on-tertiary-container':     hexToRgb('FFD8E4'),
  '--mdui-color-error':                     hexToRgb('F2B8B5'),
  '--mdui-color-on-error':                  hexToRgb('601410'),
  '--mdui-color-error-container':           hexToRgb('8C1D18'),
  '--mdui-color-on-error-container':        hexToRgb('F9DEDC'),
  '--mdui-color-background':                hexToRgb('1C1B1F'),
  '--mdui-color-on-background':             hexToRgb('E6E1E5'),
  '--mdui-color-surface':                   hexToRgb('1C1B1F'),
  '--mdui-color-on-surface':                hexToRgb('E6E1E5'),
  '--mdui-color-surface-variant':           hexToRgb('49454F'),
  '--mdui-color-on-surface-variant':        hexToRgb('CAC4D0'),
  '--mdui-color-outline':                   hexToRgb('938F99'),
  '--mdui-color-outline-variant':           hexToRgb('49454F'),
  '--mdui-color-scrim':                     hexToRgb('000000'),
  '--mdui-color-inverse-surface':           hexToRgb('E6E1E5'),
  '--mdui-color-inverse-on-surface':        hexToRgb('313033'),
  '--mdui-color-inverse-primary':           hexToRgb('6750A4'),
  '--mdui-color-surface-dim':               hexToRgb('141218'),
  '--mdui-color-surface-bright':            hexToRgb('3B383E'),
  '--mdui-color-surface-container-lowest':  hexToRgb('0F0D13'),
  '--mdui-color-surface-container-low':     hexToRgb('1D1B20'),
  '--mdui-color-surface-container':         hexToRgb('211F26'),
  '--mdui-color-surface-container-high':    hexToRgb('2B2930'),
  '--mdui-color-surface-container-highest': hexToRgb('36343B'),
};

export function applyAndroidColorScheme(): void {
  const toRules = (map: Record<string, string>) =>
    Object.entries(map).map(([k, v]) => `  ${k}: ${v};`).join('\n');

  const css =
    `:root {\n${toRules(LIGHT)}\n}\n` +
    `:root[mdui-theme="dark"] {\n${toRules(DARK)}\n}`;

  const existing = document.getElementById('selves-android-theme');
  if (existing) existing.remove();

  const style = document.createElement('style');
  style.id = 'selves-android-theme';
  style.textContent = css;
  document.head.appendChild(style);
}
