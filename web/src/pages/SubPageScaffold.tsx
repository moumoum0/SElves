import type { ReactNode } from 'react';
import { IconButton } from '../ui/components/IconButton';

interface SubPageScaffoldProps {
  title: string;
  subtitle?: string;
  onBack: () => void;
  actions?: ReactNode;
  children: ReactNode;
  noPadding?: boolean;
  /** 固定在右下角的悬浮按钮（FAB）。由 Scaffold 统一 absolute 定位，不随内容滚动 */
  fab?: ReactNode;
}

export function SubPageScaffold({ title, subtitle, onBack, actions, children, noPadding, fab }: SubPageScaffoldProps) {
  return (
    // 页面根：固定高度 = "屏幕"边界。overflow:hidden 防止内容溢出，
    // 内部内容区独立滚动；FAB 以此为 absolute 锚点，稳定在右下角不随内容滚动。
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', position: 'relative', overflow: 'hidden', backgroundColor: 'rgb(var(--mdui-color-surface))' }}>
      <mdui-top-app-bar
        style={{
          flexShrink: 0,
          backgroundColor: 'rgb(var(--mdui-color-surface))',
          borderBottom: '1px solid rgba(var(--mdui-color-outline-variant), 0.35)',
        }}
      >
        <IconButton onClick={onBack}><md-icon>arrow_back</md-icon></IconButton>
        <mdui-top-app-bar-title>
          <div>
            <div style={{ fontSize: 16, fontWeight: 400, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              {title}
            </div>
            {subtitle ? (
              <div style={{ fontSize: 12, opacity: 0.7, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {subtitle}
              </div>
            ) : null}
          </div>
        </mdui-top-app-bar-title>
        {actions}
      </mdui-top-app-bar>
      {/* 内容区：唯一滚动层。min-height:0 让 flex 子项可正常收缩并滚动 */}
      <div style={{ flex: 1, minHeight: 0, overflowY: 'auto', overflowX: 'hidden', padding: noPadding ? 0 : '16px' }}>
        {children}
      </div>
      {fab}
    </div>
  );
}