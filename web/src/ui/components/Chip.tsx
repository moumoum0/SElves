import { useRef, useEffect, type CSSProperties, type ReactNode } from 'react';

export type ChipVariant = 'assist' | 'filter' | 'input' | 'suggestion';

export interface ChipProps {
  children?: ReactNode;
  variant?: ChipVariant;
  /** selectable 存在时渲染 md-filter-chip */
  selectable?: boolean;
  selected?: boolean;
  disabled?: boolean;
  elevated?: boolean;
  href?: string;
  target?: string;
  icon?: string;
  onClick?: (e: React.MouseEvent) => void;
  /** filter-chip 专用：选中状态变化回调 */
  onChange?: (selected: boolean) => void;
  className?: string;
  style?: CSSProperties;
}

export function Chip({
  children,
  variant = 'assist',
  selectable,
  selected,
  disabled,
  elevated,
  href,
  target,
  icon,
  onClick,
  onChange,
  className,
  style,
}: ChipProps) {
  const ref = useRef<HTMLElement>(null);

  // filter-chip 的 selected 变化通过 DOM change 事件通知
  useEffect(() => {
    const el = ref.current;
    if (!el || !onChange) return;
    const handler = () => {
      onChange((el as unknown as { selected: boolean }).selected);
    };
    el.addEventListener('change', handler);
    return () => el.removeEventListener('change', handler);
  }, [onChange]);

  const common = {
    disabled: disabled || undefined,
    elevated: elevated || undefined,
    href,
    target,
    onClick,
    className,
    style,
  } as Record<string, unknown>;

  const iconEl = icon ? <md-icon slot="icon">{icon}</md-icon> : null;

  if (selectable || variant === 'filter') {
    return (
      <md-filter-chip ref={ref} {...common} selected={selected || undefined}>
        {iconEl}{children}
      </md-filter-chip>
    );
  }
  if (variant === 'input') {
    return <md-input-chip ref={ref} {...common}>{iconEl}{children}</md-input-chip>;
  }
  if (variant === 'suggestion') {
    return <md-suggestion-chip ref={ref} {...common}>{iconEl}{children}</md-suggestion-chip>;
  }
  // default: assist
  return <md-assist-chip ref={ref} {...common}>{iconEl}{children}</md-assist-chip>;
}
