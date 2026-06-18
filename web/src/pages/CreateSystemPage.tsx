import { useRef, useState } from 'react';

interface CreateSystemPageProps {
  onBack: () => void;
  onConfirm: (name: string, avatarDataUrl: string | null) => void;
}

/**
 * 创建系统页面 - 全屏布局
 * 与安卓 CreateSystemDialog.kt 1:1 对应
 */
export function CreateSystemPage({ onBack, onConfirm }: CreateSystemPageProps) {
  const [name, setName] = useState('');
  const [avatarUrl, setAvatarUrl] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const canCreate = name.trim().length > 0;

  const handleAvatarClick = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (ev) => {
      if (ev.target?.result && typeof ev.target.result === 'string') {
        setAvatarUrl(ev.target.result);
      }
    };
    reader.readAsDataURL(file);
    e.target.value = '';
  };

  const handleCreate = () => {
    if (!canCreate) return;
    onConfirm(name.trim(), avatarUrl);
  };

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        minHeight: '100%',
        backgroundColor: 'rgb(var(--mdui-color-background))',
      }}
    >
      {/* ── TopAppBar ── */}
      <div
        style={{
          position: 'sticky',
          top: 0,
          zIndex: 10,
          backgroundColor: 'rgb(var(--mdui-color-surface))',
          borderBottom: '1px solid rgba(var(--mdui-color-outline-variant), 0.35)',
        }}
      >
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            height: 64,
            padding: '0 4px',
          }}
        >
          <mdui-button-icon icon="close" onClick={onBack} />
          <div style={{ flex: 1, minWidth: 0, padding: '0 8px', overflow: 'hidden' }}>
            <div
              style={{
                fontSize: 20,
                fontWeight: 400,
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                color: 'rgb(var(--mdui-color-on-surface))',
              }}
            >
              创建系统
            </div>
          </div>

          {/* 创建按钮 */}
          <span
            onClick={canCreate ? handleCreate : undefined}
            style={{
              cursor: canCreate ? 'pointer' : 'default',
              fontSize: 14,
              fontWeight: 500,
              color: canCreate
                ? 'rgb(var(--mdui-color-primary))'
                : 'rgba(var(--mdui-color-on-surface), 0.38)',
              padding: '0 12px',
              lineHeight: '36px',
            }}
          >
            创建
          </span>
        </div>
      </div>

      {/* ── 主要内容 ── */}
      <div
        style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          padding: '32px 24px',
          overflowY: 'auto',
        }}
      >
        {/* 系统头像选择区 */}
        <div
          style={{
            position: 'relative',
            cursor: 'pointer',
            marginBottom: 32,
          }}
          onClick={handleAvatarClick}
        >
          {/* 主头像区域 */}
          <div
            style={{
              width: 120,
              height: 120,
              borderRadius: '50%',
              backgroundColor: 'rgb(var(--mdui-color-surface-variant))',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              overflow: 'hidden',
            }}
          >
            {avatarUrl ? (
              <img
                src={avatarUrl}
                alt="系统头像"
                style={{
                  width: '100%',
                  height: '100%',
                  objectFit: 'cover',
                }}
              />
            ) : (
              <>
                <mdui-icon
                  name="account_circle"
                  style={{
                    fontSize: 48,
                    color: 'rgb(var(--mdui-color-on-surface-variant))',
                  }}
                />
                <div
                  style={{
                    fontSize: 12,
                    color: 'rgb(var(--mdui-color-on-surface-variant))',
                    textAlign: 'center',
                    marginTop: 8,
                  }}
                >
                  选择头像
                </div>
              </>
            )}
          </div>

          {/* 相机图标叠加 */}
          <div
            style={{
              position: 'absolute',
              bottom: 4,
              right: 4,
              width: 32,
              height: 32,
              borderRadius: '50%',
              backgroundColor: 'rgb(var(--mdui-color-primary))',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <mdui-icon
              name="photo_camera"
              style={{
                fontSize: 18,
                color: 'rgb(var(--mdui-color-surface))',
              }}
            />
          </div>
        </div>

        {/* 系统名称输入 */}
        <mdui-text-field
          label="系统名称"
          placeholder="请输入系统名称"
          value={name}
          style={{ width: '100%' }}
          onInput={(e: Event) => setName((e.target as HTMLInputElement).value)}
          onKeyDown={(e: KeyboardEvent) => {
            if (e.key === 'Enter' && canCreate) handleCreate();
          }}
        />
      </div>

      {/* Hidden file input */}
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        style={{ display: 'none' }}
        onChange={handleFileChange}
      />
    </div>
  );
}