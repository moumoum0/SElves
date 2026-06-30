import { useRef, useEffect, type CSSProperties } from 'react';

export interface CheckboxProps {
  checked?: boolean;
  indeterminate?: boolean;
  disabled?: boolean;
  name?: string;
  value?: string;
  required?: boolean;
  onChange?: (checked: boolean) => void;
  className?: string;
  style?: CSSProperties;
}

/** Material Web Checkbox 适配层 */
export function Checkbox({
  checked,
  indeterminate,
  disabled,
  name,
  value,
  required,
  onChange,
  className,
  style,
}: CheckboxProps) {
  const ref = useRef<HTMLElement>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el || !onChange) return;
    const handler = () => {
      const cb = el as unknown as HTMLInputElement;
      onChange(cb.checked);
    };
    el.addEventListener('change', handler);
    return () => el.removeEventListener('change', handler);
  }, [onChange]);

  return (
    <md-checkbox
      ref={ref}
      checked={checked || undefined}
      indeterminate={indeterminate || undefined}
      disabled={disabled || undefined}
      name={name}
      value={value}
      required={required || undefined}
      className={className}
      style={style}
    />
  );
}
