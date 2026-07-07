import { useEffect, useRef } from 'react';
import { Icon } from '../ui/components/Icon';

interface ImageViewerProps {
  src: string;
  senderName?: string;
  timestamp?: number;
  onClose: () => void;
}

function formatTs(ts: number): string {
  const d = new Date(ts);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

export function ImageViewer({ src, senderName, timestamp, onClose }: ImageViewerProps) {
  const backdropRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handler = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, [onClose]);

  return (
    <div
      ref={backdropRef}
      onClick={(e) => { if (e.target === backdropRef.current) onClose(); }}
      style={{
        position: 'fixed', inset: 0, zIndex: 500,
        backgroundColor: 'rgba(var(--mdui-color-scrim), 0.92)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
      }}
    >
      {/* 返回按钮 */}
      <button
        onClick={onClose}
        style={{
          position: 'absolute', top: 16, left: 16,
          width: 48, height: 48, borderRadius: '50%',
          backgroundColor: 'rgba(var(--mdui-color-scrim), 0.5)', border: 'none', cursor: 'pointer',
          display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'rgb(var(--mdui-color-surface))',
        }}
      >
        <Icon style={{ fontSize: 24 }}>arrow_back</Icon>
      </button>

      {/* 发送者信息 */}
      {senderName && timestamp && (
        <div style={{
          position: 'absolute', top: 16, right: 16,
          backgroundColor: 'rgba(var(--mdui-color-scrim), 0.5)', borderRadius: 24,
          padding: '8px 16px', color: 'rgb(var(--mdui-color-surface))', textAlign: 'right',
        }}>
          <div style={{ fontSize: 14 }}>{senderName}</div>
          <div style={{ fontSize: 12, opacity: 0.7 }}>{formatTs(timestamp)}</div>
        </div>
      )}

      {/* 图片 */}
      <img
        src={src}
        alt="全屏预览"
        style={{ maxWidth: '100%', maxHeight: '100%', objectFit: 'contain' }}
        onClick={(e) => e.stopPropagation()}
      />
    </div>
  );
}
