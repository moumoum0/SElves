import type { CSSProperties, ReactNode } from 'react';

export type IconButtonVariant = 'standard' | 'filled' | 'tonal' | 'outlined';

export interface IconButtonProps {
  children?: ReactNode;       // icon name or element inside
  variant?: IconButtonVariant;
  disabled?: boolean;
  toggle?: boolean;
  selected?: boolean;
  href?: string;
  target?: string;
  onClick?: (e: React.MouseEvent) => void;
  onMouseDown?: (e: React.MouseEvent) => void;
  className?: string;
  style?: CSSProperties;
  /** aria-label — 无文字按钮必填 */
  label?: string;
  slot?: string;
}

/**
 * Material Web 图标按钮适配层。
 * variant 默认 'standard'。
 */
export function IconButton({
  children,
  variant = 'standard',
  disabled,
  toggle,
  selected,
  href,
  target,
  onClick,
  onMouseDown,
  className,
  style,
  label,
  slot,
}: IconButtonProps) {
  const common = {
    disabled,
    toggle,
    selected,
    href,
    target,
    onClick,
    onMouseDown,
    className,
    style,
    slot,
    'aria-label': label,
  } as Record<string, unknown>;

  switch (variant) {
    case 'filled':
      return <md-filled-icon-button {...common}>{children}</md-filled-icon-button>;
    case 'tonal':
      return <md-filled-tonal-icon-button {...common}>{children}</md-filled-tonal-icon-button>;
    case 'outlined':
      return <md-outlined-icon-button {...common}>{children}</md-outlined-icon-button>;
    default: // 'standard'
      return <md-icon-button {...common}>{children}</md-icon-button>;
  }
}
