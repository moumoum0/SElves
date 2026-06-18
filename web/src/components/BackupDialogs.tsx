interface BackupProgressDialogProps {
  title?: string;
  message?: string;
  progress?: number | null;
}

export function BackupProgressDialog({ title = '备份中', message = '正在处理，请稍候...', progress = null }: BackupProgressDialogProps) {
  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 300, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div style={{
        width: '88%', maxWidth: 360,
        backgroundColor: 'rgb(var(--mdui-color-surface-container))',
        borderRadius: 16, padding: 24,
        display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 16,
      }}>
        <div style={{ fontSize: 18, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface))', alignSelf: 'flex-start' }}>{title}</div>

        {progress != null ? (
          <>
            <mdui-linear-progress value={progress} style={{ display: 'block', width: '100%', height: 6, borderRadius: 3 }}></mdui-linear-progress>
            <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>{Math.round(progress * 100)}%</div>
          </>
        ) : (
          <mdui-circular-progress style={{ width: 40, height: 40, color: 'rgb(var(--mdui-color-primary))' }}></mdui-circular-progress>
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
      style={{ position: 'fixed', inset: 0, zIndex: 300, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)' }}
      onClick={(e) => { if (e.target === e.currentTarget) onDismiss(); }}
    >
      <div
        style={{ width: '92%', maxWidth: 400, backgroundColor: 'rgb(var(--mdui-color-surface))', borderRadius: 16, padding: 24 }}
        onClick={(e) => e.stopPropagation()}
      >
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 16 }}>
          <mdui-icon name="warning" style={{ fontSize: 32, color: 'rgb(var(--mdui-color-error))' }}></mdui-icon>
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
          <button type="button" style={{ padding: '8px 16px', borderRadius: 8, border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14, color: 'rgb(var(--mdui-color-primary))' }} onClick={onDismiss}>取消</button>
          <button type="button" style={{ padding: '8px 20px', borderRadius: 8, border: 'none', backgroundColor: 'rgb(var(--mdui-color-error))', color: 'rgb(var(--mdui-color-on-error))', cursor: 'pointer', fontSize: 14, fontWeight: 500 }} onClick={onConfirm}>确认导入</button>
        </div>
      </div>
    </div>
  );
}
