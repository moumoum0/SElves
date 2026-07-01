
// 逗号分隔的 RGB 三元组，与 mdui 原生变量格式（如 --mdui-color-scrim-light: 0,0,0）一致。
// 必须用逗号：rgba(var(--mdui-color-scrim), 0.5) 展开后需为合法的 rgba(0, 0, 0, 0.5)；
// 若用空格会得到 rgba(0 0 0, 0.5)，混用空格分量与逗号 alpha 是非法 CSS，整条声明会被丢弃。
function hexToRgb(hex: string): string {
  const r = parseInt(hex.slice(0, 2), 16);
  const g = parseInt(hex.slice(2, 4), 16);
  const b = parseInt(hex.slice(4, 6), 16);
  return `${r}, ${g}, ${b}`;
}

// 将 RGB 三元组（mdui 格式）转换为完整的 rgb() 颜色字符串（Material Web 格式）
function tripletToRgb(triplet: string): string {
  return `rgb(${triplet})`;
}

// APP_DEFAULT 浅色方案 —— 直接从安卓 Theme.kt LightColorScheme 提取
const DEFAULT_LIGHT: Record<string, string> = {
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

// APP_DEFAULT 深色方案 —— Material3 baseline dark + Purple80/PurpleGrey80/Pink80
const DEFAULT_DARK: Record<string, string> = {
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

// 云野 (Cloud Field) 浅色方案 —— 从安卓 CloudFieldLightColorScheme 提取
const CLOUD_FIELD_LIGHT: Record<string, string> = {
  '--mdui-color-primary':                   hexToRgb('3C6839'),
  '--mdui-color-on-primary':                hexToRgb('FFFFFF'),
  '--mdui-color-primary-container':         hexToRgb('BDF0B3'),
  '--mdui-color-on-primary-container':      hexToRgb('245023'),
  '--mdui-color-secondary':                 hexToRgb('53634F'),
  '--mdui-color-on-secondary':              hexToRgb('FFFFFF'),
  '--mdui-color-secondary-container':       hexToRgb('D6E8CE'),
  '--mdui-color-on-secondary-container':    hexToRgb('3B4B38'),
  '--mdui-color-tertiary':                  hexToRgb('38656A'),
  '--mdui-color-on-tertiary':               hexToRgb('FFFFFF'),
  '--mdui-color-tertiary-container':        hexToRgb('BCEBF0'),
  '--mdui-color-on-tertiary-container':     hexToRgb('1E4D52'),
  '--mdui-color-error':                     hexToRgb('BA1A1A'),
  '--mdui-color-on-error':                  hexToRgb('FFFFFF'),
  '--mdui-color-error-container':           hexToRgb('FFDAD6'),
  '--mdui-color-on-error-container':        hexToRgb('93000A'),
  '--mdui-color-background':                hexToRgb('F7FBF1'),
  '--mdui-color-on-background':             hexToRgb('191D17'),
  '--mdui-color-surface':                   hexToRgb('F7FBF1'),
  '--mdui-color-on-surface':                hexToRgb('191D17'),
  '--mdui-color-surface-variant':           hexToRgb('DEE5D8'),
  '--mdui-color-on-surface-variant':        hexToRgb('424940'),
  '--mdui-color-outline':                   hexToRgb('73796F'),
  '--mdui-color-outline-variant':           hexToRgb('C2C8BD'),
  '--mdui-color-scrim':                     hexToRgb('000000'),
  '--mdui-color-inverse-surface':           hexToRgb('2D322B'),
  '--mdui-color-inverse-on-surface':        hexToRgb('EFF2E9'),
  '--mdui-color-inverse-primary':           hexToRgb('A2D399'),
  '--mdui-color-surface-dim':               hexToRgb('D8DBD2'),
  '--mdui-color-surface-bright':            hexToRgb('F7FBF1'),
  '--mdui-color-surface-container-lowest':  hexToRgb('FFFFFF'),
  '--mdui-color-surface-container-low':     hexToRgb('F1F5EB'),
  '--mdui-color-surface-container':         hexToRgb('ECEFE6'),
  '--mdui-color-surface-container-high':    hexToRgb('E6E9E0'),
  '--mdui-color-surface-container-highest': hexToRgb('E0E4DA'),
};

// 云野 (Cloud Field) 深色方案 —— 从安卓 CloudFieldDarkColorScheme 提取
const CLOUD_FIELD_DARK: Record<string, string> = {
  '--mdui-color-primary':                   hexToRgb('A2D399'),
  '--mdui-color-on-primary':                hexToRgb('0C390E'),
  '--mdui-color-primary-container':         hexToRgb('245023'),
  '--mdui-color-on-primary-container':      hexToRgb('BDF0B3'),
  '--mdui-color-secondary':                 hexToRgb('BACCB3'),
  '--mdui-color-on-secondary':              hexToRgb('253423'),
  '--mdui-color-secondary-container':       hexToRgb('3B4B38'),
  '--mdui-color-on-secondary-container':    hexToRgb('D6E8CE'),
  '--mdui-color-tertiary':                  hexToRgb('A0CFD4'),
  '--mdui-color-on-tertiary':               hexToRgb('00363B'),
  '--mdui-color-tertiary-container':        hexToRgb('1E4D52'),
  '--mdui-color-on-tertiary-container':     hexToRgb('BCEBF0'),
  '--mdui-color-error':                     hexToRgb('FFB4AB'),
  '--mdui-color-on-error':                  hexToRgb('690005'),
  '--mdui-color-error-container':           hexToRgb('93000A'),
  '--mdui-color-on-error-container':        hexToRgb('FFDAD6'),
  '--mdui-color-background':                hexToRgb('10140F'),
  '--mdui-color-on-background':             hexToRgb('E0E4DA'),
  '--mdui-color-surface':                   hexToRgb('10140F'),
  '--mdui-color-on-surface':                hexToRgb('E0E4DA'),
  '--mdui-color-surface-variant':           hexToRgb('424940'),
  '--mdui-color-on-surface-variant':        hexToRgb('C2C8BD'),
  '--mdui-color-outline':                   hexToRgb('8C9388'),
  '--mdui-color-outline-variant':           hexToRgb('424940'),
  '--mdui-color-scrim':                     hexToRgb('000000'),
  '--mdui-color-inverse-surface':           hexToRgb('E0E4DA'),
  '--mdui-color-inverse-on-surface':        hexToRgb('2D322B'),
  '--mdui-color-inverse-primary':           hexToRgb('3C6839'),
  '--mdui-color-surface-dim':               hexToRgb('10140F'),
  '--mdui-color-surface-bright':            hexToRgb('363A34'),
  '--mdui-color-surface-container-lowest':  hexToRgb('0B0F0A'),
  '--mdui-color-surface-container-low':     hexToRgb('191D17'),
  '--mdui-color-surface-container':         hexToRgb('1D211B'),
  '--mdui-color-surface-container-high':    hexToRgb('272B25'),
  '--mdui-color-surface-container-highest': hexToRgb('323630'),
};

export type ColorSchemeName = 'default' | 'cloud_field';

export function applyAndroidColorScheme(scheme: ColorSchemeName = 'default'): void {
  const light = scheme === 'cloud_field' ? CLOUD_FIELD_LIGHT : DEFAULT_LIGHT;
  const dark  = scheme === 'cloud_field' ? CLOUD_FIELD_DARK  : DEFAULT_DARK;

  // --mdui-color-* : 保持空格三元组格式，兼容现有 rgb(var(...)) / rgba(var(...), alpha) 用法
  const toMduiRules = (map: Record<string, string>) =>
    Object.entries(map).map(([k, v]) => `  ${k}: ${v};`).join('\n');

  // --md-sys-color-* : Material Web 组件使用完整的 rgb() 颜色字符串
  const toMdSysRules = (map: Record<string, string>) =>
    Object.entries(map)
      .map(([k, v]) => `  ${k.replace('--mdui-color-', '--md-sys-color-')}: ${tripletToRgb(v)};`)
      .join('\n');

  const lightRules = `${toMduiRules(light)}\n${toMdSysRules(light)}`;
  const darkRules  = `${toMduiRules(dark)}\n${toMdSysRules(dark)}`;

  // 暗色选择器对齐 mdui setTheme 实际添加的类（.mdui-theme-dark / .mdui-theme-auto）
  // 修正原来的 :root[mdui-theme="dark"] 属性选择器（该属性 mdui 从未实际写入）
  const css =
    `:root {\n${lightRules}\n}\n` +
    `.mdui-theme-dark {\n${darkRules}\n}\n` +
    `@media (prefers-color-scheme: dark) {\n  .mdui-theme-auto {\n${darkRules}\n  }\n}`;

  const existing = document.getElementById('selves-android-theme');
  if (existing) existing.remove();

  const style = document.createElement('style');
  style.id = 'selves-android-theme';
  style.textContent = css;
  document.head.appendChild(style);
}
