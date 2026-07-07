import { useRef, useEffect, type CSSProperties, type ReactNode } from 'react';

export interface TabsProps {
  /** 当前激活的 tab 索引（从 0 开始） */
  activeIndex?: number;
  onChange?: (index: number) => void;
  children?: ReactNode;
  className?: string;
  style?: CSSProperties;
}

export interface TabProps {
  children?: ReactNode;
  /** 是否是 secondary 样式（默认 primary） */
  secondary?: boolean;
  disabled?: boolean;
  className?: string;
  style?: CSSProperties;
}

/**
 * Material Web Tabs 适配层。
 * mdui 用字符串 value，@material/web 用数字 activeTabIndex。
 * onChange 回调返回激活的索引号。
 */
export function Tabs({ activeIndex, onChange, children, className, style }: TabsProps) {
  const ref = useRef<HTMLElement>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el || !onChange) return;
    const handler = () => {
      const tabs = el as unknown as { activeTabIndex: number };
      onChange(tabs.activeTabIndex);
    };
    el.addEventListener('change', handler);
    return () => el.removeEventListener('change', handler);
  }, [onChange]);

  return (
    <md-tabs
      ref={ref}
      activeTabIndex={activeIndex}
      className={className}
      style={style}
    >
      {children}
    </md-tabs>
  );
}

/** 单个 Tab 标签（默认 primary 样式） */
export function Tab({ children, secondary, disabled, className, style }: TabProps) {
  if (secondary) {
    return (
      <md-secondary-tab disabled={disabled || undefined} className={className} style={style}>
        {children}
      </md-secondary-tab>
    );
  }
  return (
    <md-primary-tab disabled={disabled || undefined} className={className} style={style}>
      {children}
    </md-primary-tab>
  );
}
