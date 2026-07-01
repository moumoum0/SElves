import { useEffect, useState, type ReactNode } from 'react';
import { SubPageScaffold } from './SubPageScaffold';
import { applyAndroidColorScheme, type ColorSchemeName } from '../theme/androidColors';
import { setThemeMode as applyThemeMode } from '../ui/theme/themeManager';
import { ImportBackupWarningDialog } from '../components/BackupDialogs';
import { getApiToken, setApiToken, getApiBaseUrl, setApiBaseUrl } from '../lib/api';
import { Switch } from '../ui/components/Switch';
import { Radio } from '../ui/components/Radio';
import { TextField } from '../ui/components/TextField';
import { CircularProgress } from '../ui/components/Progress';
import { Dialog } from '../ui/components/Dialog';
import { Icon } from '../ui/components/Icon';
import { IconButton } from '../ui/components/IconButton';
import { Card } from '../ui/components/Card';
import { List, ListItem } from '../ui/components/List';

interface SettingsPageProps {
  baseUrl: string;
  onBack: () => void;
  onNavigateToAbout?: () => void;
}

const THEME_LABELS: Record<string, string> = {
  auto: '跟随系统',
  light: '浅色模式',
  dark: '深色模式',
};

const LANG_LABELS: Record<string, string> = {
  zh: '简体中文',
  en: 'English',
};

const COLOR_LABELS: Record<string, string> = {
  default: '应用默认',
  cloud_field: '云野',
};

export function SettingsPage({ baseUrl, onBack, onNavigateToAbout }: SettingsPageProps) {
  const [language, setLanguage] = useState(() => window.localStorage.getItem('selves-language') || 'zh');
  const [themeMode, setThemeMode] = useState(() => window.localStorage.getItem('selves-theme') || 'auto');
  const [colorScheme, setColorScheme] = useState<ColorSchemeName>(() => (window.localStorage.getItem('selves-color-scheme') || 'default') as ColorSchemeName);
  const [quickSwitch, setQuickSwitch] = useState(false);
  const [webServerEnabled, setWebServerEnabled] = useState(false);
  const [showLangDialog, setShowLangDialog] = useState(false);
  const [showThemeDialog, setShowThemeDialog] = useState(false);
  const [showColorDialog, setShowColorDialog] = useState(false);
  const [isBackupLoading, setIsBackupLoading] = useState(false);
  const [showImportWarning, setShowImportWarning] = useState(false);
  const [isSpImportLoading, setIsSpImportLoading] = useState(false);
  const [apiToken, setApiTokenState] = useState(() => getApiToken());
  const [apiUrl, setApiUrlState] = useState(() => getApiBaseUrl());

  const handleThemeChange = (mode: string) => {
    setThemeMode(mode);
    applyThemeMode(mode as 'light' | 'dark' | 'auto');
    setShowThemeDialog(false);
  };

  const handleColorChange = (scheme: ColorSchemeName) => {
    setColorScheme(scheme);
    window.localStorage.setItem('selves-color-scheme', scheme);
    applyAndroidColorScheme(scheme);
    setShowColorDialog(false);
  };

  const handleLanguageChange = (lang: string) => {
    setLanguage(lang);
    window.localStorage.setItem('selves-language', lang);
    setShowLangDialog(false);
  };

  return (
    <SubPageScaffold title="设置" onBack={onBack}>
      <div style={{ padding: 16, display: 'flex', flexDirection: 'column', gap: 4 }}>
        {/* 通用设置分组 */}
        <SettingsGroupTitle>通用</SettingsGroupTitle>
        <SettingsItem icon="language" title="语言" subtitle={LANG_LABELS[language] ?? '跟随系统'} onClick={() => setShowLangDialog(true)} />
        <SettingsItem icon="dark_mode" title="深色模式" subtitle={THEME_LABELS[themeMode] ?? '跟随系统'} onClick={() => setShowThemeDialog(true)} />
        <SettingsItem icon="palette" title="颜色与个性化" subtitle={COLOR_LABELS[colorScheme] ?? '应用默认'} onClick={() => setShowColorDialog(true)} />
        <SettingsSwitchItem icon="swap_horiz" title="快捷切换成员" subtitle="在投票和聊天界面显示快捷成员切换" checked={quickSwitch} onChange={setQuickSwitch} />

        {/* ===== 数据与备份（已注释） ===== */}
        {/* <div style={{ height: 16 }} />
        <SettingsGroupTitle>数据与备份</SettingsGroupTitle>
        <SettingsItem icon="schedule" title="定时备份" subtitle="设置自动备份频率和时间" onClick={() => {}} />
        <SettingsItemWithProgress icon="file_upload" title="导出备份" subtitle="备份应用数据到文件" isLoading={isBackupLoading} onClick={() => setIsBackupLoading(true)} />
        <SettingsItemWithProgress icon="file_download" title="导入备份" subtitle="从文件恢复应用数据" isLoading={isBackupLoading} onClick={() => setShowImportWarning(true)} />
        <SettingsItemWithProgress icon="file_download" title="从 SimplyPlural 导入" subtitle="导入 SimplyPlural 导出的 JSON 文件" isLoading={isSpImportLoading} onClick={() => setIsSpImportLoading(true)} /> */}

        {/* ===== Web 访问（已注释） ===== */}
        {/* <div style={{ height: 16 }} />
        <SettingsGroupTitle>Web 访问</SettingsGroupTitle>
        <SettingsSwitchItem icon="wifi" title="开启 Web 访问" subtitle="通过局域网浏览器访问 Selves 数据" checked={webServerEnabled} onChange={setWebServerEnabled} />
        {webServerEnabled && (
          <>
            <div style={{ padding: '8px 4px', display: 'flex', flexDirection: 'column', gap: 12 }}>
              <TextField
                label="API 地址"
                value={apiUrl}
                onChange={(val) => { setApiUrlState(val); setApiBaseUrl(val); }}
                placeholder="http://192.168.x.x:8080"
                style={{ width: '100%' }}
              />
              <TextField
                label="访问令牌 (Token)"
                value={apiToken}
                onChange={(val) => {
                  const upper = val.toUpperCase();
                  setApiTokenState(upper);
                  setApiToken(upper);
                  if (upper.length === 6) {
                    localStorage.setItem('selves-token-configured', 'true');
                  }
                }}
                placeholder="例如：A3B7K9"
                supportingText="6位字母+数字组合"
                style={{ width: '100%' }}
              />
            </div>
            <WebAccessInfoCard url={apiUrl} />
          </>
        )} */}

        {/* 其他分组 */}
        <div style={{ height: 16 }} />
        <SettingsGroupTitle>其他</SettingsGroupTitle>
        <SettingsItem icon="info" title="关于" subtitle="应用信息和版本" onClick={() => onNavigateToAbout?.()} />
      </div>

      {/* 语言选择弹窗 */}
      <Dialog open={showLangDialog} onClose={() => setShowLangDialog(false)} headline="选择语言">
        <List>
          <ListItem active={language === 'zh'} leading={<Radio checked={language === 'zh'} />} onClick={() => handleLanguageChange('zh')}>
            简体中文
          </ListItem>
          <ListItem active={language === 'en'} leading={<Radio checked={language === 'en'} />} onClick={() => handleLanguageChange('en')}>
            English
          </ListItem>
        </List>
      </Dialog>

      {/* 主题模式弹窗 */}
      <Dialog open={showThemeDialog} onClose={() => setShowThemeDialog(false)} headline="选择主题模式">
        <List>
          {Object.entries(THEME_LABELS).map(([k, v]) => (
            <ListItem key={k} active={themeMode === k} leading={<Radio checked={themeMode === k} />} onClick={() => handleThemeChange(k)}>
              {v}
            </ListItem>
          ))}
        </List>
      </Dialog>

      {/* 配色方案弹窗 */}
      <Dialog open={showColorDialog} onClose={() => setShowColorDialog(false)} headline="选择配色方案">
        <List>
          {Object.entries(COLOR_LABELS).map(([k, v]) => (
            <ListItem key={k} active={colorScheme === k} leading={<Radio checked={colorScheme === k} />} onClick={() => handleColorChange(k as ColorSchemeName)}>
              {v}
            </ListItem>
          ))}
        </List>
      </Dialog>
      {showImportWarning && (
        <ImportBackupWarningDialog
          onConfirm={() => { setShowImportWarning(false); setIsBackupLoading(true); }}
          onDismiss={() => setShowImportWarning(false)}
        />
      )}
    </SubPageScaffold>
  );
}

function SettingsGroupTitle({ children }: { children: ReactNode }) {
  return (
    <div style={{ fontSize: 16, fontWeight: 500, color: 'rgb(var(--mdui-color-primary))', padding: '8px 4px' }}>
      {children}
    </div>
  );
}

function SettingsItem({ icon, title, subtitle, onClick }: { icon: string; title: string; subtitle: string; onClick: () => void }) {
  return (
    <div
      onClick={onClick}
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 16,
        padding: '12px 4px',
        cursor: 'pointer',
        borderRadius: 4,
      }}
    >
      <Icon style={{ fontSize: 24, color: 'rgb(var(--mdui-color-on-surface-variant))', flexShrink: 0 }}>{icon}</Icon>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-surface))', lineHeight: 1.5 }}>{title}</div>
        <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))', lineHeight: 1.5 }}>{subtitle}</div>
      </div>
    </div>
  );
}

function SettingsSwitchItem({
  icon,
  title,
  subtitle,
  checked,
  onChange,
}: {
  icon: string;
  title: string;
  subtitle: string;
  checked: boolean;
  onChange: (v: boolean) => void;
}) {
  return (
    <div
      onClick={() => onChange(!checked)}
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 16,
        padding: '12px 4px',
        cursor: 'pointer',
        borderRadius: 4,
      }}
    >
      <Icon style={{ fontSize: 24, color: 'rgb(var(--mdui-color-on-surface-variant))', flexShrink: 0 }}>{icon}</Icon>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-surface))', lineHeight: 1.5 }}>{title}</div>
        <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))', lineHeight: 1.5 }}>{subtitle}</div>
      </div>
      <Switch
        selected={checked}
        onChange={onChange}
        onClick={(e) => e.stopPropagation()}
      />
    </div>
  );
}

function SettingsItemWithProgress({
  icon,
  title,
  subtitle,
  isLoading,
  onClick,
}: {
  icon: string;
  title: string;
  subtitle: string;
  isLoading: boolean;
  onClick: () => void;
}) {
  return (
    <div
      onClick={() => {
        if (!isLoading) onClick();
      }}
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 16,
        padding: '12px 4px',
        cursor: isLoading ? 'default' : 'pointer',
        borderRadius: 4,
        opacity: isLoading ? 0.5 : 1,
      }}
    >
      <Icon style={{ fontSize: 24, color: 'rgb(var(--mdui-color-on-surface-variant))', flexShrink: 0 }}>{icon}</Icon>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-surface))', lineHeight: 1.5 }}>{title}</div>
        <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))', lineHeight: 1.5 }}>{subtitle}</div>
      </div>
      {isLoading && <CircularProgress style={{ width: 20, height: 20 }} />}
    </div>
  );
}

function WebAccessInfoCard({ url }: { url: string }) {
  const [copied, setCopied] = useState(false);
  const [qrDataUrl, setQrDataUrl] = useState<string | null>(null);

  useEffect(() => {
    if (!url) { setQrDataUrl(null); return; }
    let cancelled = false;
    import('qrcode').then((QRCode) => {
      if (cancelled) return;
      QRCode.toDataURL(url, { width: 180, margin: 1, color: { dark: '#000000', light: '#ffffff' } })
        .then((dataUrl: string) => { if (!cancelled) setQrDataUrl(dataUrl); })
        .catch(() => { if (!cancelled) setQrDataUrl(null); });
    });
    return () => { cancelled = true; };
  }, [url]);

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(url);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // ignore
    }
  };

  return (
    <Card
      variant="filled"
      style={{
        borderRadius: 12,
        padding: 16,
        backgroundColor: 'rgb(var(--mdui-color-surface-variant))',
        margin: '8px 4px',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))', lineHeight: 1.5 }}>访问地址</div>
          <div
            style={{
              fontSize: 14,
              fontWeight: 500,
              color: 'rgb(var(--mdui-color-on-surface))',
              lineHeight: 1.5,
              wordBreak: 'break-all',
            }}
          >
            {url || '未配置接口地址'}
          </div>
        </div>
        <IconButton onClick={handleCopy}><md-icon>{copied ? 'check' : 'content_copy'}</md-icon></IconButton>
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginTop: 12 }}>
        <div style={{ fontSize: 11, color: 'rgb(var(--mdui-color-on-surface-variant))', marginBottom: 8 }}>
          扫描二维码快速访问
        </div>
        <div
          style={{
            width: 180,
            height: 180,
            borderRadius: 8,
            backgroundColor: 'rgb(var(--mdui-color-surface))',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            overflow: 'hidden',
          }}
        >
          {qrDataUrl
            ? <img src={qrDataUrl} alt="QR Code" style={{ width: 180, height: 180 }} />
            : <Icon style={{ fontSize: 80, color: 'rgb(var(--mdui-color-primary))' }}>qr_code_2</Icon>
          }
        </div>
        <div style={{ fontSize: 11, color: 'rgb(var(--mdui-color-on-surface-variant))', marginTop: 8 }}>
          确保设备与手机在同一局域网
        </div>
      </div>
    </Card>
  );
}
