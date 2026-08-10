import { useEffect, useRef, useState } from 'react';
import { Button } from '../ui/components/Button';
import { Icon } from '../ui/components/Icon';

const PIN_LENGTH = 6;

interface AdminPinDialogProps {
  /** 说明这次提权要做什么，例如「删除成员」「导入备份」 */
  actionLabel: string;
  /** 上一次提交失败的原因，用于回显错误 */
  errorMessage?: string | null;
  submitting?: boolean;
  onSubmit: (pin: string) => void;
  onDismiss: () => void;
}

/**
 * 敏感操作提权用的管理密码输入框。
 *
 * 与手机端一致：6 位数字。PIN 不做本地缓存，每次敏感操作都重新输入
 * —— 服务端的失败锁定是按次计数的，缓存反而会让误输入静默耗尽次数。
 */
export function AdminPinDialog({
  actionLabel,
  errorMessage,
  submitting = false,
  onSubmit,
  onDismiss,
}: AdminPinDialogProps) {
  const [pin, setPin] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    inputRef.current?.focus();
  }, []);

  // 服务端回错后清空，避免用户在错误的旧值上补字符
  useEffect(() => {
    if (errorMessage) setPin('');
  }, [errorMessage]);

  const canSubmit = pin.length === PIN_LENGTH && !submitting;

  const submit = () => {
    if (canSubmit) onSubmit(pin);
  };

  return (
    <div
      className="dialog-overlay"
      style={{
        position: 'fixed', inset: 0, zIndex: 320,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        backgroundColor: 'rgba(var(--mdui-color-scrim), 0.5)',
      }}
      onClick={(e) => { if (e.target === e.currentTarget && !submitting) onDismiss(); }}
    >
      <div
        className="dialog-panel alert-dialog-panel"
        style={{ width: '92%', maxWidth: 380, backgroundColor: 'rgb(var(--mdui-color-surface))', padding: 24 }}
        onClick={(e) => e.stopPropagation()}
      >
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 16 }}>
          <Icon style={{ fontSize: 32, color: 'rgb(var(--mdui-color-primary))' }}>lock</Icon>
        </div>
        <div style={{ fontSize: 18, fontWeight: 500, marginBottom: 8, color: 'rgb(var(--mdui-color-on-surface))' }}>
          需要管理密码
        </div>
        <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))', marginBottom: 20 }}>
          {actionLabel}是敏感操作，请输入 {PIN_LENGTH} 位管理密码。
        </div>

        <input
          ref={inputRef}
          type="password"
          inputMode="numeric"
          autoComplete="off"
          aria-label="管理密码"
          value={pin}
          disabled={submitting}
          onChange={(e) => setPin(e.target.value.replace(/\D/g, '').slice(0, PIN_LENGTH))}
          onKeyDown={(e) => { if (e.key === 'Enter') submit(); }}
          style={{
            width: '100%', boxSizing: 'border-box',
            padding: '12px 16px', fontSize: 20, letterSpacing: 8, textAlign: 'center',
            borderRadius: 8,
            border: `1px solid rgb(var(--mdui-color-${errorMessage ? 'error' : 'outline'}))`,
            backgroundColor: 'rgb(var(--mdui-color-surface-container))',
            color: 'rgb(var(--mdui-color-on-surface))',
          }}
        />

        {errorMessage ? (
          <div style={{ fontSize: 13, color: 'rgb(var(--mdui-color-error))', marginTop: 8 }}>
            {errorMessage}
          </div>
        ) : null}

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 20 }}>
          <Button variant="text" onClick={onDismiss} disabled={submitting}>取消</Button>
          <Button variant="filled" onClick={submit} disabled={!canSubmit}>
            {submitting ? '验证中...' : '确认'}
          </Button>
        </div>
      </div>
    </div>
  );
}
