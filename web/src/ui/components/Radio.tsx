import { useRef, useEffect, type CSSProperties } from 'react';

export interface RadioProps {
  checked?: boolean;
  disabled?: boolean;
  name?: string;
  value?: string;
  required?: boolean;
  onChange?: (checked: boolean) => void;
  className?: string;
  style?: CSSProperties;
}

/** Material Web Radio 适配层 */
export function Radio({ checked, disabled, name, value, required, onChange, className, style }: RadioProps) {
  const ref = useRef<HTMLElement>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el || !onChange) return;
    const handler = () => {
      const r = el as unknown as HTMLInputElement;
      onChange(r.checked);
    };
    el.addEventListener('change', handler);
    return () => el.removeEventListener('change', handler);
  }, [onChange]);

  return (
    <md-radio
      ref={ref}
      checked={checked || undefined}
      disabled={disabled || undefined}
      name={name}
      value={value}
      required={required || undefined}
      className={className}
      style={style}
    />
  );
}
