import type { CSSProperties, ReactNode } from 'react';

export type CardVariant = 'elevated' | 'filled' | 'outlined';

export interface CardProps {
  children?: ReactNode;
  variant?: CardVariant;
  onClick?: (e: React.MouseEvent) => void;
  onContextMenu?: (e: React.MouseEvent) => void;
  onTouchStart?: () => void;
  onTouchEnd?: () => void;
  onTouchMove?: () => void;
  className?: string;
  style?: CSSProperties;
}

/**
 * Material Web 卡片适配层。
 * variant 映射：elevated → md-elevated-card，filled → md-filled-card，outlined → md-outlined-card。
 */
export function Card({ children, variant = 'elevated', onClick, onContextMenu, onTouchStart, onTouchEnd, onTouchMove, className, style }: CardProps) {
  const props = { onClick, onContextMenu, onTouchStart, onTouchEnd, onTouchMove, className, style } as Record<string, unknown>;
  switch (variant) {
    case 'filled':
      return <md-filled-card {...props}>{children}</md-filled-card>;
    case 'outlined':
      return <md-outlined-card {...props}>{children}</md-outlined-card>;
    default:
      return <md-elevated-card {...props}>{children}</md-elevated-card>;
  }
}
