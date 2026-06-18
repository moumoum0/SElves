import { useState } from 'react';

interface TrackingConfig {
  recordingInterval: number;
  enableAutoStart: boolean;
  autoRestartDelay: number;
}

interface LocationTrackingConfigDialogProps {
  config: TrackingConfig;
  onConfigUpdate: (config: TrackingConfig) => void;
  onDismiss: () => void;
}

const INTERVALS: [number, string][] = [
  [30, '30 秒'],
  [60, '1 分钟'],
  [300, '5 分钟'],
  [600, '10 分钟'],
  [1800, '30 分钟'],
];

const DELAYS: [number, string][] = [
  [60, '1 分钟'],
  [300, '5 分钟'],
  [600, '10 分钟'],
  [1800, '30 分钟'],
  [3600, '1 小时'],
];

export function LocationTrackingConfigDialog({ config, onConfigUpdate, onDismiss }: LocationTrackingConfigDialogProps) {
  const [temp, setTemp] = useState<TrackingConfig>(config);

  return (
    <div
      style={{ position: 'fixed', inset: 0, zIndex: 200, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)' }}
      onClick={(e) => { if (e.target === e.currentTarget) onDismiss(); }}
    >
      <div
        style={{ width: '92%', maxWidth: 420, maxHeight: '88vh', overflowY: 'auto', backgroundColor: 'rgb(var(--mdui-color-surface-container))', borderRadius: 16, padding: 20 }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* 标题栏 */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 }}>
          <span style={{ fontSize: 16, fontWeight: 700, color: 'rgb(var(--mdui-color-on-surface))' }}>记录设置</span>
          <button type="button" onClick={onDismiss} style={{ border: 'none', background: 'transparent', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <mdui-icon name="close" style={{ fontSize: 20, color: 'rgb(var(--mdui-color-on-surface-variant))' }}></mdui-icon>
          </button>
        </div>

        {/* 记录间隔 */}
        <div style={{ fontSize: 14, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface))', marginBottom: 8 }}>记录频率</div>
        {INTERVALS.map(([sec, label]) => (
          <div
            key={sec}
            onClick={() => setTemp(t => ({ ...t, recordingInterval: sec }))}
            style={{ display: 'flex', alignItems: 'center', height: 48, padding: '0 8px', cursor: 'pointer', gap: 8 }}
          >
            <div style={{
              width: 18, height: 18, borderRadius: '50%', border: `2px solid ${temp.recordingInterval === sec ? 'rgb(var(--mdui-color-primary))' : 'rgb(var(--mdui-color-outline))'}`,
              display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
            }}>
              {temp.recordingInterval === sec && <div style={{ width: 10, height: 10, borderRadius: '50%', backgroundColor: 'rgb(var(--mdui-color-primary))' }} />}
            </div>
            <span style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-surface))' }}>{label}</span>
          </div>
        ))}

        <div style={{ height: 16 }} />

        {/* 自动开始 */}
        <div
          onClick={() => setTemp(t => ({ ...t, enableAutoStart: !t.enableAutoStart }))}
          style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8, cursor: 'pointer' }}
        >
          <mdui-switch
            checked={temp.enableAutoStart || undefined}
          ></mdui-switch>
          <span style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface))' }}>启动时自动开始记录</span>
        </div>

        {temp.enableAutoStart && (
          <>
            <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))', marginBottom: 8 }}>自动开始延迟</div>
            {DELAYS.map(([sec, label]) => (
              <div
                key={sec}
                onClick={() => setTemp(t => ({ ...t, autoRestartDelay: sec }))}
                style={{ display: 'flex', alignItems: 'center', height: 40, padding: '0 8px', cursor: 'pointer', gap: 8 }}
              >
                <div style={{
                  width: 18, height: 18, borderRadius: '50%', border: `2px solid ${temp.autoRestartDelay === sec ? 'rgb(var(--mdui-color-primary))' : 'rgb(var(--mdui-color-outline))'}`,
                  display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
                }}>
                  {temp.autoRestartDelay === sec && <div style={{ width: 10, height: 10, borderRadius: '50%', backgroundColor: 'rgb(var(--mdui-color-primary))' }} />}
                </div>
                <span style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface))' }}>{label}</span>
              </div>
            ))}
          </>
        )}

        <div style={{ height: 20 }} />

        {/* 按钮 */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
          <button type="button" style={{ padding: '8px 16px', borderRadius: 8, border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14, color: 'rgb(var(--mdui-color-primary))' }} onClick={onDismiss}>取消</button>
          <button type="button" style={{ padding: '8px 20px', borderRadius: 8, border: 'none', backgroundColor: 'rgb(var(--mdui-color-primary))', color: 'rgb(var(--mdui-color-on-primary))', cursor: 'pointer', fontSize: 14, fontWeight: 600 }} onClick={() => onConfigUpdate(temp)}>保存</button>
        </div>
      </div>
    </div>
  );
}
