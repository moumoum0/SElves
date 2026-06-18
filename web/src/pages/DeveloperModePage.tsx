import { SubPageScaffold } from './SubPageScaffold';

interface DeveloperModePageProps {
  onBack: () => void;
}

export function DeveloperModePage({ onBack }: DeveloperModePageProps) {
  return (
    <SubPageScaffold title="开发者模式" onBack={onBack}>
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: 'calc(100% - 32px)',
          padding: '0 24px',
        }}
      >
        <div style={{ fontSize: 24, fontWeight: 700, color: 'rgb(var(--mdui-color-on-surface))' }}>
          开发者模式已启用
        </div>
        <div
          style={{
            fontSize: 16,
            color: 'rgb(var(--mdui-color-on-surface-variant))',
            paddingTop: 12,
          }}
        >
          功能开发中，敬请期待。
        </div>
      </div>
    </SubPageScaffold>
  );
}