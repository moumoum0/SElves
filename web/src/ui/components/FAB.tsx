import { useEffect, useRef, type CSSProperties } from 'react';

export type FABSize = 'small' | 'medium' | 'large';
export type FABVariant = 'surface' | 'primary' | 'secondary' | 'tertiary';

export interface FABProps {
  /** Material Symbols 图标名 */
  icon: string;
  label?: string;
  size?: FABSize;
  variant?: FABVariant;
  lowered?: boolean;
  onClick?: (e: MouseEvent) => void;
  className?: string;
  style?: CSSProperties;
}

/**
 * Material Web FAB（悬浮操作按钮）适配层。
 * 图标通过 slot="icon" 传入 md-icon。
 */
export function FAB({ icon, label, size, variant = 'primary', lowered, onClick, className, style }: FABProps) {
  const ref = useRef<HTMLElement>(null);

  // md-fab 内部是 Shadow DOM button，React 合成 onClick 不可靠，改绑原生 click。
  useEffect(() => {
    const el = ref.current;
    if (!el || !onClick) return;
    el.addEventListener('click', onClick);
    return () => el.removeEventListener('click', onClick);
  }, [onClick]);

  return (
    <md-fab
      ref={ref}
      label={label}
      size={size}
      variant={variant}
      lowered={lowered || undefined}
      className={className}
      style={style}
    >
      <md-icon slot="icon">{icon}</md-icon>
    </md-fab>
  );
}
