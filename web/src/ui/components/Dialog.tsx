import { useRef, useEffect, type CSSProperties, type ReactNode } from 'react';

export interface DialogProps {
  open?: boolean;
  /** Dialog 关闭时回调（无论何种关闭方式） */
  onClose?: () => void;
  /** 标题行 */
  headline?: ReactNode;
  /** 主体内容 */
  children?: ReactNode;
  /** 底部操作区（slot="actions"） */
  actions?: ReactNode;
  /** 跳过开关动画（快速显隐） */
  quick?: boolean;
  onClick?: (e: React.MouseEvent) => void;
  className?: string;
  style?: CSSProperties;
}

/**
 * Material Web Dialog 适配层。
 * `md-dialog` 用 DOM `close` 事件通知关闭，此处封装为 `onClose()` 回调。
 * 当 `open` 为 false 时不渲染，节省 DOM。
 */
export function Dialog({
  open,
  onClose,
  headline,
  children,
  actions,
  quick,
  onClick,
  className,
  style,
}: DialogProps) {
  const ref = useRef<HTMLElement>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el || !onClose) return;
    const handler = () => onClose();
    el.addEventListener('close', handler);
    return () => el.removeEventListener('close', handler);
  }, [onClose]);

  if (!open) return null;

  return (
    <md-dialog
      ref={ref}
      open
      quick={quick || undefined}
      onClick={onClick}
      className={className}
      style={style}
    >
      {headline && <div slot="headline">{headline}</div>}
      {children && <div slot="content">{children}</div>}
      {actions && <div slot="actions">{actions}</div>}
    </md-dialog>
  );
}
