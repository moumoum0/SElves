import type { ReactNode } from 'react';

interface SubPageScaffoldProps {
  title: string;
  subtitle?: string;
  onBack: () => void;
  actions?: ReactNode;
  children: ReactNode;
  noPadding?: boolean;
}

export function SubPageScaffold({ title, subtitle, onBack, actions, children, noPadding }: SubPageScaffoldProps) {
  return (
    <div style={{ minHeight: '100%', display: 'flex', flexDirection: 'column', backgroundColor: 'rgb(var(--mdui-color-surface))' }}>
      <mdui-top-app-bar
        style={{
          position: 'sticky',
          top: 0,
          zIndex: 10,
          backgroundColor: 'rgb(var(--mdui-color-surface))',
          borderBottom: '1px solid rgba(var(--mdui-color-outline-variant), 0.35)',
        }}
      >
        <mdui-button-icon icon="arrow_back" onClick={onBack}></mdui-button-icon>
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
      <div style={{ flex: 1, padding: noPadding ? 0 : '16px' }}>
        {children}
      </div>
    </div>
  );
}
