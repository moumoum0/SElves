import { useCallback, useState } from 'react';
import { AdminPinError } from '../lib/api';

interface PendingAction {
  /** 展示给用户的操作名，如「删除成员」 */
  label: string;
  run: (pin: string) => Promise<unknown>;
}

/**
 * 管理提权弹窗的状态机：发起敏感操作 → 弹窗输 PIN → 带 PIN 重试。
 *
 * 服务端是唯一的判定方，这里不做任何本地权限判断，只负责把 PIN 交上去
 * 并把服务端的拒绝理由回显给用户。
 */
export function useAdminPinGate() {
  const [pending, setPending] = useState<PendingAction | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  /**
   * 执行一个可能需要提权的操作。
   * 先不带 PIN 试一次，服务端要求提权时再弹窗 —— 这样操作若无需提权就不打扰用户。
   */
  const run = useCallback(async (label: string, action: (pin: string) => Promise<unknown>) => {
    try {
      await action('');
    } catch (e) {
      if (e instanceof AdminPinError && e.reason === 'required') {
        setError(null);
        setPending({ label, run: action });
        return;
      }
      throw e;
    }
  }, []);

  const submitPin = useCallback(async (pin: string) => {
    if (!pending) return;
    setSubmitting(true);
    try {
      await pending.run(pin);
      setPending(null);
      setError(null);
    } catch (e) {
      if (e instanceof AdminPinError) {
        setError(
          e.reason === 'locked' && e.retryAfterSeconds
            ? `${e.message}（${e.retryAfterSeconds} 秒后可重试）`
            : e.message,
        );
      } else {
        setError(e instanceof Error ? e.message : '操作失败');
      }
    } finally {
      setSubmitting(false);
    }
  }, [pending]);

  const dismiss = useCallback(() => {
    setPending(null);
    setError(null);
  }, []);

  return {
    /** 非 null 时应渲染 AdminPinDialog */
    pendingLabel: pending?.label ?? null,
    error,
    submitting,
    run,
    submitPin,
    dismiss,
  };
}
