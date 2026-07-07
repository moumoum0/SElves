import { useRef, useEffect, type CSSProperties } from 'react';

export interface SwitchProps {
  /** 是否选中（受控） */
  selected?: boolean;
  disabled?: boolean;
  /** 显示图标 */
  icons?: boolean;
  showOnlySelectedIcon?: boolean;
  name?: string;
  value?: string;
  required?: boolean;
  onChange?: (selected: boolean) => void;
  onClick?: (e: React.MouseEvent) => void;
  className?: string;
  style?: CSSProperties;
}

/**
 * Material Web Switch 适配层。
 * Web Component 通过 DOM `change` 事件通知状态变更，
 * 这里封装成 `onChange(selected: boolean)` 回调。
 */
export function Switch({
  selected,
  disabled,
  icons,
  showOnlySelectedIcon,
  name,
  value,
  required,
  onChange,
  onClick,
  className,
  style,
}: SwitchProps) {
  const ref = useRef<HTMLElement>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el || !onChange) return;
    const handler = () => {
      const sw = el as unknown as { selected: boolean };
      onChange(sw.selected);
    };
    el.addEventListener('change', handler);
    return () => el.removeEventListener('change', handler);
  }, [onChange]);

  return (
    <md-switch
      ref={ref}
      selected={selected || undefined}
      disabled={disabled || undefined}
      icons={icons || undefined}
      show-only-selected-icon={showOnlySelectedIcon || undefined}
      name={name}
      value={value}
      required={required || undefined}
      onClick={onClick}
      className={className}
      style={style}
    />
  );
}
