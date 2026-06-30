import { Icon } from '../ui/components/Icon';

interface SystemSettingsDialogProps {
  onDismiss: () => void;
}

/**
 * 系统设置对话框
 * 与安卓 SystemSettingsDialog.kt 1:1 对应
 */
export function SystemSettingsDialog({ onDismiss }: SystemSettingsDialogProps) {
  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 200,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'rgba(0,0,0,0.5)',
      }}
      onClick={(e) => { if (e.target === e.currentTarget) onDismiss(); }}
    >
      <div
        style={{
          width: '92%',
          maxWidth: 400,
          backgroundColor: 'rgb(var(--mdui-color-surface))',
          borderRadius: 16,
          padding: 20,
          boxShadow: '0 4px 24px rgba(0,0,0,0.2)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* 标题 */}
        <div
          style={{
            fontSize: 22,
            fontWeight: 700,
            color: 'rgb(var(--mdui-color-on-surface))',
            marginBottom: 20,
          }}
        >
          设置
        </div>

        {/* 设置项列表 */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
          <SettingsItem
            icon="info"
            title="关于"
            subtitle="查看系统信息"
            onClick={() => { /* TODO: 显示关于信息 */ }}
          />
        </div>

        {/* 底部按钮 */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 20 }}>
          <button
            type="button"
            style={{
              padding: '8px 16px',
              borderRadius: 8,
              border: 'none',
              background: 'transparent',
              cursor: 'pointer',
              fontSize: 14,
              color: 'rgb(var(--mdui-color-primary))',
            }}
            onClick={onDismiss}
          >
            关闭
          </button>
        </div>
      </div>
    </div>
  );
}

interface SettingsItemProps {
  icon: string;
  title: string;
  subtitle: string;
  onClick: () => void;
}

function SettingsItem({ icon, title, subtitle, onClick }: SettingsItemProps) {
  return (
    <div
      onClick={onClick}
      style={{
        display: 'flex',
        alignItems: 'center',
        padding: '12px 4px',
        cursor: 'pointer',
        borderRadius: 4,
      }}
    >
      <Icon
        style={{
          fontSize: 24,
          color: 'rgb(var(--mdui-color-on-surface-variant))',
          flexShrink: 0,
        }}
      >{icon}</Icon>
      <div style={{ flex: 1, minWidth: 0, marginLeft: 16 }}>
        <div style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-surface))' }}>
          {title}
        </div>
        <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
          {subtitle}
        </div>
      </div>
    </div>
  );
}