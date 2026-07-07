import { useRef, useEffect, type CSSProperties, type ReactNode } from 'react';

export type TextFieldVariant = 'filled' | 'outlined';

export interface TextFieldProps {
  variant?: TextFieldVariant;
  label?: string;
  value?: string;
  defaultValue?: string;
  placeholder?: string;
  disabled?: boolean;
  readonly?: boolean;
  required?: boolean;
  /** HTML input type，例如 'text' | 'password' | 'email' | 'number' */
  type?: string;
  error?: boolean;
  errorText?: string;
  supportingText?: string;
  name?: string;
  autocomplete?: string;
  pattern?: string;
  maxlength?: number;
  minlength?: number;
  rows?: number;
  maxRows?: number;
  /** 值变化时触发，参数为最新字符串 */
  onChange?: (value: string) => void;
  onBlur?: (e: FocusEvent) => void;
  onFocus?: (e: FocusEvent) => void;
  onKeyDown?: (e: React.KeyboardEvent) => void;
  onClick?: (e: React.MouseEvent) => void;
  /** slot 子元素，用于 leading-icon / end-icon slot 内容 */
  children?: ReactNode;
  className?: string;
  style?: CSSProperties;
  id?: string;
}

/**
 * Material Web 文本输入框适配层。
 * 将 Web Component 的 `input` 事件封装为 `onChange(value: string)`，
 * 保留受控模式（value prop）。
 */
export function TextField({
  variant = 'outlined',
  label,
  value,
  defaultValue,
  placeholder,
  disabled,
  readonly,
  required,
  type = 'text',
  error,
  errorText,
  supportingText,
  name,
  autocomplete,
  pattern,
  maxlength,
  minlength,
  rows,
  maxRows,
  onChange,
  onBlur,
  onFocus,
  onKeyDown,
  onClick,
  children,
  className,
  style,
  id,
}: TextFieldProps) {
  const ref = useRef<HTMLElement>(null);

  // 用 DOM 事件监听而非 React 合成事件，保证 Web Component 兼容性
  useEffect(() => {
    const el = ref.current;
    if (!el) return;

    const handleInput = () => {
      const input = el as unknown as HTMLInputElement;
      onChange?.(input.value);
    };
    const handleBlur = (e: Event) => onBlur?.(e as FocusEvent);
    const handleFocus = (e: Event) => onFocus?.(e as FocusEvent);

    el.addEventListener('input', handleInput);
    el.addEventListener('blur', handleBlur);
    el.addEventListener('focus', handleFocus);
    return () => {
      el.removeEventListener('input', handleInput);
      el.removeEventListener('blur', handleBlur);
      el.removeEventListener('focus', handleFocus);
    };
  }, [onChange, onBlur, onFocus]);

  const props = {
    ref,
    label,
    value,
    placeholder,
    disabled: disabled || undefined,
    readonly: readonly || undefined,
    required: required || undefined,
    type,
    error: error || undefined,
    'error-text': errorText,
    'supporting-text': supportingText,
    name,
    autocomplete,
    pattern,
    maxlength,
    minlength,
    rows,
    'max-rows': maxRows,
    onKeyDown,
    onClick,
    className,
    style,
    id,
    // defaultValue 只在非受控时设置初始值
    ...(value === undefined && defaultValue !== undefined ? { value: defaultValue } : {}),
  } as Record<string, unknown>;

  if (variant === 'outlined') {
    return <md-outlined-text-field {...props}>{children}</md-outlined-text-field>;
  }
  return <md-filled-text-field {...props}>{children}</md-filled-text-field>;
}
