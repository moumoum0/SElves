import type { CSSProperties } from 'react';

export interface CircularProgressProps {
  /** 0–1 之间的进度值；不传则显示不确定态 */
  value?: number;
  indeterminate?: boolean;
  className?: string;
  style?: CSSProperties;
}

/** Material Web 圆形进度指示器 */
export function CircularProgress({ value, indeterminate, className, style }: CircularProgressProps) {
  const isIndeterminate = indeterminate ?? value === undefined;
  return (
    <md-circular-progress
      value={isIndeterminate ? undefined : value}
      indeterminate={isIndeterminate || undefined}
      className={className}
      style={style}
    />
  );
}

export interface LinearProgressProps {
  /** 0–1 之间的进度值；不传则显示不确定态 */
  value?: number;
  buffer?: number;
  indeterminate?: boolean;
  className?: string;
  style?: CSSProperties;
}

/** Material Web 线性进度指示器 */
export function LinearProgress({ value, buffer, indeterminate, className, style }: LinearProgressProps) {
  const isIndeterminate = indeterminate ?? value === undefined;
  return (
    <md-linear-progress
      value={isIndeterminate ? undefined : value}
      buffer={buffer}
      indeterminate={isIndeterminate || undefined}
      className={className}
      style={style}
    />
  );
}
