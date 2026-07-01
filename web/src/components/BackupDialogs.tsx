import { Button } from '../ui/components/Button';
import { Icon } from '../ui/components/Icon';
import { CircularProgress, LinearProgress } from '../ui/components/Progress';

interface BackupProgressDialogProps {
  title?: string;
  message?: string;
  progress?: number | null;
}

export function BackupProgressDialog({ title = '备份中', message = '正在处理，请稍候...', progress = null }: BackupProgressDialogProps) {
  return (
    <div className="dialog-overlay" style={{ position: 'fixed', inset: 0, zIndex: 300, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(var(--mdui-color-scrim), 0.5)' }}>
      <div className="dialog-panel" style={{
        width: '88%', maxWidth: 360,
        backgroundColor: 'rgb(var(--mdui-color-surface-container))',
        borderRadius: 16, padding: 24,
        display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 16,
      }}>
        <div style={{ fontSize: 18, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface))', alignSelf: 'flex-start' }}>{title}</div>

        {progress != null ? (
          <>
            <LinearProgress value={progress} style={{ display: 'block', width: '100%', height: 6, borderRadius: 3 }} />
            <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>{Math.round(progress * 100)}%</div>
          </>
        ) : (
          <CircularProgress style={{ width: 40, height: 40, color: 'rgb(var(--mdui-color-primary))' }} />
        )}

        <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))', textAlign: 'center' }}>{message}</div>
      </div>
    </div>
  );
}

interface ImportBackupWarningDialogProps {
  onConfirm: () => void;
  onDismiss: () => void;
}

export function ImportBackupWarningDialog({ onConfirm, onDismiss }: ImportBackupWarningDialogProps) {
  return (
    <div
      className="dialog-overlay"
      style={{ position: 'fixed', inset: 0, zIndex: 300, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(var(--mdui-color-scrim), 0.5)' }}
      onClick={(e) => { if (e.target === e.currentTarget) onDismiss(); }}
    >
      <div
        className="dialog-panel alert-dialog-panel"
        style={{ width: '92%', maxWidth: 400, backgroundColor: 'rgb(var(--mdui-color-surface))', padding: 24 }}
        onClick={(e) => e.stopPropagation()}
      >
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 16 }}>
          <Icon style={{ fontSize: 32, color: 'rgb(var(--mdui-color-error))' }}>warning</Icon>
        </div>
        <div style={{ fontSize: 18, fontWeight: 500, marginBottom: 16, color: 'rgb(var(--mdui-color-on-surface))' }}>导入备份</div>
        <div style={{ fontSize: 14, fontWeight: 500, marginBottom: 8, color: 'rgb(var(--mdui-color-on-surface))' }}>导入备份将会：</div>
        <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-error))', marginBottom: 4 }}>· 清空所有成员数据</div>
        <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-error))', marginBottom: 4 }}>· 清空所有聊天记录</div>
        <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-error))', marginBottom: 4 }}>· 清空所有待办事项</div>
        <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-error))', marginBottom: 12 }}>· 清空所有动态内容</div>
        <div style={{ fontSize: 14, fontWeight: 500, color: 'rgb(var(--mdui-color-error))', marginBottom: 8 }}>此操作不可撤销！</div>
        <div style={{ fontSize: 14, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface))', marginBottom: 20 }}>确定要继续导入吗？</div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
          <Button variant="text" onClick={onDismiss}>取消</Button>
          <Button
            variant="filled"
            onClick={onConfirm}
            style={{
              '--md-filled-button-container-color': 'rgb(var(--mdui-color-error))',
              '--md-filled-button-label-text-color': 'rgb(var(--mdui-color-on-error))',
              '--md-filled-button-hover-state-layer-color': 'rgb(var(--mdui-color-on-error))',
            } as React.CSSProperties}
          >
            确认导入
          </Button>
        </div>
      </div>
    </div>
  );
}
