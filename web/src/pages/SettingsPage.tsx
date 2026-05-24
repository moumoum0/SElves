import { useEffect, useRef, useState, type ReactNode } from 'react';
import { SubPageScaffold } from './SubPageScaffold';

function useDialogClose<T extends HTMLElement>(open: boolean, onClose: () => void) {
  const ref = useRef<T>(null);
  useEffect(() => {
    const el = ref.current;
    if (!el || !open) return;
    const handler = () => onClose();
    el.addEventListener('close', handler);
    return () => el.removeEventListener('close', handler);
  }, [open, onClose]);
  return ref;
}

interface SettingsPageProps {
  baseUrl: string;
  onBack: () => void;
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

export function SettingsPage({ baseUrl, onBack }: SettingsPageProps) {
  const [language, setLanguage] = useState('zh');
  const [themeMode, setThemeMode] = useState('auto');
  const [colorScheme, setColorScheme] = useState('default');
  const [quickSwitch, setQuickSwitch] = useState(false);
  const [webServerEnabled, setWebServerEnabled] = useState(false);
  const [showLangDialog, setShowLangDialog] = useState(false);
  const [showThemeDialog, setShowThemeDialog] = useState(false);
  const [showColorDialog, setShowColorDialog] = useState(false);
  const [isBackupLoading, setIsBackupLoading] = useState(false);
  const [isSpImportLoading, setIsSpImportLoading] = useState(false);

  return (
    <SubPageScaffold title="设置" onBack={onBack}>
      <div style={{ padding: 16, display: 'flex', flexDirection: 'column', gap: 4 }}>
        {/* 通用设置分组 */}
        <SettingsGroupTitle>通用</SettingsGroupTitle>
        <SettingsItem icon="language" title="语言" subtitle={LANG_LABELS[language] ?? '跟随系统'} onClick={() => setShowLangDialog(true)} />
        <SettingsItem icon="dark_mode" title="深色模式" subtitle={THEME_LABELS[themeMode] ?? '跟随系统'} onClick={() => setShowThemeDialog(true)} />
        <SettingsItem icon="palette" title="颜色与个性化" subtitle={COLOR_LABELS[colorScheme] ?? '应用默认'} onClick={() => setShowColorDialog(true)} />
        <SettingsSwitchItem icon="swap_horiz" title="快捷切换成员" subtitle="在投票和聊天界面显示快捷成员切换" checked={quickSwitch} onChange={setQuickSwitch} />

        {/* 数据与备份分组 */}
        <div style={{ height: 16 }} />
        <SettingsGroupTitle>数据与备份</SettingsGroupTitle>
        <SettingsItem icon="schedule" title="定时备份" subtitle="设置自动备份频率和时间" onClick={() => {}} />
        <SettingsItemWithProgress icon="file_upload" title="导出备份" subtitle="备份应用数据到文件" isLoading={isBackupLoading} onClick={() => setIsBackupLoading(true)} />
        <SettingsItemWithProgress icon="file_download" title="导入备份" subtitle="从文件恢复应用数据" isLoading={isBackupLoading} onClick={() => {}} />
        <SettingsItemWithProgress icon="file_download" title="从 SimplyPlural 导入" subtitle="导入 SimplyPlural 导出的 JSON 文件" isLoading={isSpImportLoading} onClick={() => setIsSpImportLoading(true)} />

        {/* Web 访问分组 */}
        <div style={{ height: 16 }} />
        <SettingsGroupTitle>Web 访问</SettingsGroupTitle>
        <SettingsSwitchItem icon="wifi" title="开启 Web 访问" subtitle="通过局域网浏览器访问 Selves 数据" checked={webServerEnabled} onChange={setWebServerEnabled} />
        {webServerEnabled && <WebAccessInfoCard url={baseUrl} />}

        {/* 其他分组 */}
        <div style={{ height: 16 }} />
        <SettingsGroupTitle>其他</SettingsGroupTitle>
        <SettingsItem icon="info" title="关于" subtitle="应用信息和版本" onClick={() => {}} />
      </div>

      {/* 语言选择弹窗 */}
      <DialogHook open={showLangDialog} onClose={() => setShowLangDialog(false)} headline="选择语言">
        <mdui-list>
          <mdui-list-item active={language === 'zh'} onClick={() => { setLanguage('zh'); setShowLangDialog(false); }}>
            简体中文
          </mdui-list-item>
          <mdui-list-item active={language === 'en'} onClick={() => { setLanguage('en'); setShowLangDialog(false); }}>
            English
          </mdui-list-item>
        </mdui-list>
      </DialogHook>

      {/* 主题模式弹窗 */}
      <DialogHook open={showThemeDialog} onClose={() => setShowThemeDialog(false)} headline="选择主题模式">
        <mdui-list>
          {Object.entries(THEME_LABELS).map(([k, v]) => (
            <mdui-list-item key={k} active={themeMode === k} onClick={() => { setThemeMode(k); setShowThemeDialog(false); }}>
              {v}
            </mdui-list-item>
          ))}
        </mdui-list>
      </DialogHook>

      {/* 配色方案弹窗 */}
      <DialogHook open={showColorDialog} onClose={() => setShowColorDialog(false)} headline="选择配色方案">
        <mdui-list>
          {Object.entries(COLOR_LABELS).map(([k, v]) => (
            <mdui-list-item key={k} active={colorScheme === k} onClick={() => { setColorScheme(k); setShowColorDialog(false); }}>
              {v}
            </mdui-list-item>
          ))}
        </mdui-list>
      </DialogHook>
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
      <mdui-icon name={icon} style={{ fontSize: 24, color: 'rgb(var(--mdui-color-on-surface-variant))', flexShrink: 0 }} />
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
      <mdui-icon name={icon} style={{ fontSize: 24, color: 'rgb(var(--mdui-color-on-surface-variant))', flexShrink: 0 }} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-surface))', lineHeight: 1.5 }}>{title}</div>
        <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))', lineHeight: 1.5 }}>{subtitle}</div>
      </div>
      <mdui-switch
        checked={checked}
        onClick={(e) => {
          e.stopPropagation();
          onChange(!checked);
        }}
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
      <mdui-icon name={icon} style={{ fontSize: 24, color: 'rgb(var(--mdui-color-on-surface-variant))', flexShrink: 0 }} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-surface))', lineHeight: 1.5 }}>{title}</div>
        <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))', lineHeight: 1.5 }}>{subtitle}</div>
      </div>
      {isLoading && <mdui-circular-progress style={{ width: 20, height: 20 }} />}
    </div>
  );
}

function WebAccessInfoCard({ url }: { url: string }) {
  const [copied, setCopied] = useState(false);

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
    <mdui-card
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
        <mdui-button-icon icon="content_copy" onClick={handleCopy} />
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
          }}
        >
          <mdui-icon name="qr_code_2" style={{ fontSize: 80, color: 'rgb(var(--mdui-color-primary))' }} />
        </div>
        <div style={{ fontSize: 11, color: 'rgb(var(--mdui-color-on-surface-variant))', marginTop: 8 }}>
          确保设备与手机在同一局域网
        </div>
      </div>
    </mdui-card>
  );
}

function DialogHook({
  open,
  onClose,
  headline,
  children,
}: {
  open: boolean;
  onClose: () => void;
  headline: string;
  children: ReactNode;
}) {
  const ref = useDialogClose<HTMLElement>(open, onClose);
  if (!open) return null;
  return (
    <mdui-dialog ref={ref} open headline={headline} close-on-overlay-click close-on-esc>
      {children}
    </mdui-dialog>
  );
}
