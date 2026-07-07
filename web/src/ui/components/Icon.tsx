import type { CSSProperties } from 'react';

export interface IconProps {
  children: string;
  slot?: string;
  className?: string;
  style?: CSSProperties;
}

/**
 * Material Symbols 图标，对应 @material/web md-icon。
 *
 * md-icon 大小由 --md-icon-size 控制，不继承父元素 font-size。
 * 这里自动把 style.fontSize 映射到 --md-icon-size，保持调用方接口不变。
 */
export function Icon({ children, slot, className, style }: IconProps) {
  const { fontSize, ...restStyle } = style ?? {};
  const iconStyle = {
    ...restStyle,
    ...(fontSize != null
      ? { '--md-icon-size': typeof fontSize === 'number' ? `${fontSize}px` : fontSize }
      : {}),
  } as CSSProperties;

  return <md-icon slot={slot} className={className} style={iconStyle}>{children}</md-icon>;
}
