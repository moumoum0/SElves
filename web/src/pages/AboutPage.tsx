import { useCallback, useRef, useState } from 'react';
import { Icon } from '../ui/components/Icon';
import { SubPageScaffold } from './SubPageScaffold';

interface AboutPageProps {
  onBack: () => void;
  onDeveloperModeUnlocked?: () => void;
}

interface ThirdPartyLibrary {
  name: string;
  version: string;
  description: string;
}

const THIRD_PARTY_LIBRARIES: ThirdPartyLibrary[] = [
  { name: 'Jetpack Compose', version: '2024.12.01', description: 'Native Android UI toolkit' },
  { name: 'Material3', version: '2024.12.01', description: 'Material 3 components' },
  { name: 'Material Icons Extended', version: '2024.12.01', description: 'Extended Material icons' },
  { name: 'Navigation Compose', version: '2.8.5', description: 'Jetpack Navigation for Compose' },
  { name: 'Room', version: '2.6.1', description: 'SQLite ORM for local storage' },
  { name: 'Dagger Hilt', version: '2.52', description: 'Dependency injection framework for Android' },
  { name: 'Hilt Navigation Compose', version: '1.2.0', description: 'Hilt + Nav Compose' },
  { name: 'Coil', version: '2.7.0', description: 'Android image loader' },
  { name: 'DataStore', version: '1.1.1', description: 'Replaces SharedPreferences' },
  { name: 'Android Image Cropper', version: '4.3.2', description: 'Image cropping library' },
  { name: 'Accompanist System UI Controller', version: '0.36.0', description: 'System UI control utilities' },
  { name: 'Gson', version: '2.11.0', description: 'Google JSON serialization library' },
  { name: 'TinyPinyin', version: '2.0.3', description: 'Chinese to Pinyin converter' },
  { name: 'Core KTX', version: '1.16.0', description: 'Core Kotlin extensions' },
  { name: 'Lifecycle Runtime KTX', version: '2.8.7', description: 'Lifecycle-aware component extensions' },
  { name: 'Activity Compose', version: '1.10.1', description: 'Activity + Compose integration' },
  { name: 'AppCompat', version: '1.7.0', description: 'Backward compatibility library' },
  { name: 'Splash Screen', version: '1.0.1', description: 'Android 12+ splash screen' },
  { name: 'Kotlin', version: '2.1.0', description: 'Kotlin language and coroutine support' },
];

export function AboutPage({ onBack, onDeveloperModeUnlocked }: AboutPageProps) {
  const [tapCount, setTapCount] = useState(0);
  const unlockedRef = useRef(false);

  const handleIconClick = useCallback(() => {
    if (unlockedRef.current) return;
    setTapCount((prev) => {
      const next = prev + 1;
      if (next >= 7) {
        unlockedRef.current = true;
        onDeveloperModeUnlocked?.();
      }
      return next;
    });
  }, [onDeveloperModeUnlocked]);

  return (
    <SubPageScaffold title="关于" onBack={onBack}>
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '24px 0' }}>
        {/* 应用图标 — 120px, 圆角12, 点击7次解锁开发者模式 */}
        <div
          onClick={handleIconClick}
          style={{
            width: 120,
            height: 120,
            borderRadius: 12,
            overflow: 'hidden',
            cursor: 'pointer',
            position: 'relative',
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <Icon style={{ fontSize: 64, color: '#fff' }}>diversity_3</Icon>
        </div>

        <div style={{ height: 24 }} />

        {/* 应用名称 */}
        <div style={{ fontSize: 28, fontWeight: 700, color: 'rgb(var(--mdui-color-on-surface))' }}>
          selves
        </div>

        <div style={{ height: 8 }} />

        {/* 版本信息 */}
        <div style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
          版本 1.1.1
        </div>

        <div style={{ height: 32 }} />

        {/* 开发者信息卡片 — OutlinedCard, fullWidth */}
        <div
          style={{
            width: '100%',
            borderRadius: 12,
            border: '1px solid rgb(var(--mdui-color-outline))',
            backgroundColor: 'rgb(var(--mdui-color-surface))',
            padding: 20,
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: 16, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface))' }}>
              开发者
            </span>
            <span style={{ fontSize: 16, fontWeight: 500, color: 'rgb(var(--mdui-color-primary))' }}>
              moumoum
            </span>
          </div>

          <div style={{ height: 16 }} />

          <div style={{ height: 1, backgroundColor: 'rgba(var(--mdui-color-outline), 0.5)' }} />

          <div style={{ height: 12 }} />

          {/* Bilibili 链接 */}
          <a
            href="https://b23.tv/59njutf"
            target="_blank"
            rel="noopener noreferrer"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 12,
              padding: '8px 0',
              textDecoration: 'none',
              color: 'inherit',
            }}
          >
            <Icon style={{ fontSize: 24, color: 'rgb(var(--mdui-color-primary))' }}>smart_display</Icon>
            <span style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-surface))' }}>Bilibili</span>
          </a>

          {/* GitHub 链接 */}
          <a
            href="https://github.com/moumoum0/SElves"
            target="_blank"
            rel="noopener noreferrer"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 12,
              padding: '8px 0',
              textDecoration: 'none',
              color: 'inherit',
            }}
          >
            <Icon style={{ fontSize: 24, color: 'rgb(var(--mdui-color-primary))' }}>code</Icon>
            <span style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-surface))' }}>GitHub</span>
          </a>
        </div>

        <div style={{ height: 24 }} />

        {/* 第三方库标题 */}
        <div
          style={{
            width: '100%',
            fontSize: 22,
            fontWeight: 700,
            color: 'rgb(var(--mdui-color-on-surface))',
            paddingBottom: 16,
          }}
        >
          第三方库
        </div>

        {/* 第三方库列表 (19 items) */}
        <div style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 12 }}>
          {THIRD_PARTY_LIBRARIES.map((lib) => (
            <LibraryCard key={lib.name} library={lib} />
          ))}
        </div>

        <div style={{ height: 32 }} />

        {/* 底部致谢 */}
        <div
          style={{
            fontSize: 14,
            color: 'rgb(var(--mdui-color-on-surface-variant))',
            textAlign: 'center',
            width: '100%',
          }}
        >
          Selves — Local tool for plurals
        </div>
      </div>
    </SubPageScaffold>
  );
}

function LibraryCard({ library }: { library: ThirdPartyLibrary }) {
  return (
    <div
      style={{
        width: '100%',
        borderRadius: 12,
        border: '1px solid rgb(var(--mdui-color-outline))',
        backgroundColor: 'rgb(var(--mdui-color-surface))',
        padding: 20,
      }}
    >
      {/* 库名称 */}
      <div style={{ fontSize: 16, fontWeight: 600, color: 'rgb(var(--mdui-color-on-surface))' }}>
        {library.name}
      </div>

      {/* 版本信息 */}
      {library.version ? (
        <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-primary))', paddingTop: 4 }}>
          版本: {library.version}
        </div>
      ) : null}

      <div style={{ height: 8 }} />

      {/* 描述信息 */}
      <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))', lineHeight: 1.5 }}>
        {library.description}
      </div>
    </div>
  );
}