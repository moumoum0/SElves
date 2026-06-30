import type { CSSProperties } from 'react';

export type FABSize = 'small' | 'medium' | 'large';
export type FABVariant = 'surface' | 'primary' | 'secondary' | 'tertiary';

export interface FABProps {
  /** Material Symbols 图标名 */
  icon: string;
  label?: string;
  size?: FABSize;
  variant?: FABVariant;
  lowered?: boolean;
  onClick?: (e: React.MouseEvent) => void;
  className?: string;
  style?: CSSProperties;
}

/**
 * Material Web FAB（悬浮操作按钮）适配层。
 * 图标通过 slot="icon" 传入 md-icon。
 */
export function FAB({ icon, label, size, variant, lowered, onClick, className, style }: FABProps) {
  return (
    <md-fab
      label={label}
      size={size}
      variant={variant}
      lowered={lowered || undefined}
      onClick={onClick}
      className={className}
      style={style}
    >
      <md-icon slot="icon">{icon}</md-icon>
    </md-fab>
  );
}
