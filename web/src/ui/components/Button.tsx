import type { CSSProperties, ReactNode } from 'react';

export type ButtonVariant = 'filled' | 'outlined' | 'text' | 'tonal' | 'elevated';

export interface ButtonProps {
  children?: ReactNode;
  variant?: ButtonVariant;
  disabled?: boolean;
  href?: string;
  target?: string;
  onClick?: (e: React.MouseEvent) => void;
  className?: string;
  style?: CSSProperties;
  /** leading icon name（Material Symbols） */
  icon?: ReactNode;
  trailingIcon?: boolean;
}

/**
 * Material Web 按钮适配层。
 * variant 默认 'filled'。
 */
export function Button({
  children,
  variant = 'filled',
  disabled,
  href,
  target,
  onClick,
  className,
  style,
  icon,
  trailingIcon,
}: ButtonProps) {
  const common = { disabled, href, target, onClick, className, style } as Record<string, unknown>;

  switch (variant) {
    case 'outlined':
      return (
        <md-outlined-button {...common}>
          {icon && <md-icon slot={trailingIcon ? 'trailing-icon' : 'icon'}>{icon}</md-icon>}
          {children}
        </md-outlined-button>
      );
    case 'text':
      return (
        <md-text-button {...common}>
          {icon && <md-icon slot={trailingIcon ? 'trailing-icon' : 'icon'}>{icon}</md-icon>}
          {children}
        </md-text-button>
      );
    case 'tonal':
      return (
        <md-filled-tonal-button {...common}>
          {icon && <md-icon slot={trailingIcon ? 'trailing-icon' : 'icon'}>{icon}</md-icon>}
          {children}
        </md-filled-tonal-button>
      );
    case 'elevated':
      return (
        <md-elevated-button {...common}>
          {icon && <md-icon slot={trailingIcon ? 'trailing-icon' : 'icon'}>{icon}</md-icon>}
          {children}
        </md-elevated-button>
      );
    default: // 'filled'
      return (
        <md-filled-button {...common}>
          {icon && <md-icon slot={trailingIcon ? 'trailing-icon' : 'icon'}>{icon}</md-icon>}
          {children}
        </md-filled-button>
      );
  }
}
