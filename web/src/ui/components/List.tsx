import type { CSSProperties, ReactNode } from 'react';

export interface ListItemProps {
  /** 主文本（或直接传 children） */
  children?: ReactNode;
  headline?: string;
  /** 副文本 */
  supportingText?: string;
  /** leading slot（图标或头像） */
  leading?: ReactNode;
  /** trailing slot（操作按钮等） */
  trailing?: ReactNode;
  disabled?: boolean;
  selected?: boolean;
  active?: boolean;
  href?: string;
  target?: string;
  type?: 'text' | 'button' | 'link';
  multiLineSupport?: boolean;
  onClick?: (e: React.MouseEvent) => void;
  className?: string;
  style?: CSSProperties;
}

export interface ListProps {
  children?: ReactNode;
  className?: string;
  style?: CSSProperties;
}

/** Material Web List 容器 */
export function List({ children, className, style }: ListProps) {
  return (
    <md-list className={className} style={style}>
      {children}
    </md-list>
  );
}

/**
 * Material Web ListItem 适配层。
 * leading / trailing 通过 slot 插入；
 * headline 和 supportingText 使用 slot="headline" / slot="supporting-text"。
 */
export function ListItem({
  children,
  headline,
  supportingText,
  leading,
  trailing,
  disabled,
  selected,
  active,
  href,
  target,
  type,
  multiLineSupport,
  onClick,
  className,
  style,
}: ListItemProps) {
  return (
    <md-list-item
      disabled={disabled || undefined}
      selected={selected || undefined}
      active={active || undefined}
      href={href}
      target={target}
      type={type}
      multi-line-support={multiLineSupport || undefined}
      onClick={onClick}
      className={className}
      style={style}
    >
      {leading && <span slot="start">{leading}</span>}
      {headline && <span slot="headline">{headline}</span>}
      {supportingText && <span slot="supporting-text">{supportingText}</span>}
      {children}
      {trailing && <span slot="end">{trailing}</span>}
    </md-list-item>
  );
}
