import { useEffect, useState } from 'react';
import type { LocationRecord, Member, TrackingSummary } from '../types/models';
import { getLocationRecords, getLocationSummary } from '../lib/api';
import { SubPageScaffold } from './SubPageScaffold';
import { LocationTrackingConfigDialog } from '../components/LocationTrackingConfigDialog';
import { Icon } from '../ui/components/Icon';
import { Card } from '../ui/components/Card';
import { Chip } from '../ui/components/Chip';
import { LinearProgress } from '../ui/components/Progress';

interface LocationPageProps {
  tracking: TrackingSummary;
  currentMember: Member;
  onBack: () => void;
}

function formatRecordTime(timestamp: string): string {
  const date = new Date(timestamp);
  if (Number.isNaN(date.getTime())) return '未知时间';
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}

function formatLastRecordTime(value: string | null): string {
  if (!value) return '暂无';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return `${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}

export function LocationPage({ tracking, currentMember, onBack }: LocationPageProps) {
  const [summary, setSummary] = useState<TrackingSummary>(tracking);
  const isRecording = summary.status === 'RECORDING';
  const [records, setRecords] = useState<LocationRecord[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [showConfig, setShowConfig] = useState(false);
  const [trackingConfig, setTrackingConfig] = useState({ recordingInterval: 60, enableAutoStart: false, autoRestartDelay: 300 });

  useEffect(() => {
    let cancelled = false;
    setError(null);
    Promise.all([
      getLocationSummary(currentMember.id),
      getLocationRecords({ memberId: currentMember.id, limit: 20 }),
    ])
      .then(([nextSummary, nextRecords]) => {
        if (cancelled) return;
        setSummary(nextSummary);
        setRecords(nextRecords);
      })
      .catch((loadError) => {
        if (cancelled) return;
        setError(loadError instanceof Error ? loadError.message : '位置记录加载失败');
        setRecords([]);
      });
    return () => {
      cancelled = true;
    };
  }, [currentMember.id]);

  return (
    <>
    <SubPageScaffold title="轨迹记录" subtitle="查看轨迹记录与统计概览" onBack={onBack}>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        {/* 状态卡 */}
        <Card
          variant="filled"
          style={{
            padding: 16,
            borderRadius: 16,
            backgroundColor: isRecording ? 'rgb(var(--mdui-color-primary))' : undefined,
            color: isRecording ? 'rgb(var(--mdui-color-on-primary))' : undefined,
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 12 }}>
            <div style={{ width: 52, height: 52, borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: isRecording ? 'rgba(var(--mdui-color-on-primary), 0.16)' : 'rgb(var(--mdui-color-primary))', color: isRecording ? 'inherit' : 'rgb(var(--mdui-color-on-primary))', flexShrink: 0 }}>
              <Icon style={{ fontSize: 28 }}>{isRecording ? 'navigation' : 'location_on'}</Icon>
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ fontWeight: 600, fontSize: 15 }}>{isRecording ? '正在记录轨迹' : '轨迹记录已停止'}</div>
              <div style={{ fontSize: 13, opacity: 0.8 }}>当前成员：{currentMember.name}</div>
            </div>
            <Chip variant="assist" onClick={() => setShowConfig(true)}>{isRecording ? '停止' : '开始'}</Chip>
          </div>
          <LinearProgress value={0.72} style={{ display: 'block' }} />
        </Card>

        {/* 统计数据 */}
        <div style={{ display: 'flex', gap: 12 }}>
          {[{ label: '今日', value: String(summary.todayRecords) }, { label: '总计', value: String(summary.totalRecords) }, { label: '最后', value: formatLastRecordTime(summary.lastRecordTime) }].map((s) => (
            <Card key={s.label} variant="filled" style={{ flex: 1, padding: 12, borderRadius: 12, textAlign: 'center' }}>
              <div style={{ fontSize: 22, fontWeight: 700, color: 'rgb(var(--mdui-color-primary))' }}>{s.value}</div>
              <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>{s.label}</div>
            </Card>
          ))}
        </div>

        {/* 地图占位 */}
        <Card variant="filled" style={{ padding: 16, borderRadius: 16 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
            <Icon style={{ color: 'rgb(var(--mdui-color-primary))' }}>map</Icon>
            <span style={{ fontWeight: 600, fontSize: 15 }}>地图预览</span>
          </div>
          <div style={{ height: 160, borderRadius: 12, backgroundColor: 'rgb(var(--mdui-color-surface-container-high))', position: 'relative', overflow: 'hidden', display: 'flex', alignItems: 'flex-end', justifyContent: 'center', padding: '0 12px 12px' }}>
            <div style={{ position: 'absolute', left: 52, top: 40, width: 12, height: 12, borderRadius: '50%', backgroundColor: 'rgb(var(--mdui-color-primary))' }} />
            <div style={{ position: 'absolute', right: 68, bottom: 52, width: 16, height: 16, borderRadius: '50%', backgroundColor: 'rgb(var(--mdui-color-error))' }} />
            <span style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))', zIndex: 1 }}>Web 预览占位 · 等待地图能力接入</span>
          </div>
        </Card>

        {/* 今日记录 */}
        <Card variant="filled" style={{ padding: 16, borderRadius: 16 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
            <Icon style={{ color: 'rgb(var(--mdui-color-primary))' }}>history</Icon>
            <span style={{ fontWeight: 600, fontSize: 15 }}>今日记录</span>
          </div>
          {error && (
            <div style={{ padding: 12, marginBottom: 12, borderRadius: 12, backgroundColor: 'rgb(var(--mdui-color-error-container))', color: 'rgb(var(--mdui-color-on-error-container))', fontSize: 13 }}>
              {error}
            </div>
          )}
          {records.map((r, i) => (
            <div key={r.id} style={{ display: 'flex', gap: 12, paddingBottom: 12, borderBottom: i < records.length - 1 ? '1px solid rgb(var(--mdui-color-outline-variant))' : 'none', marginBottom: i < records.length - 1 ? 12 : 0 }}>
              <div style={{ paddingTop: 4 }}><div style={{ width: 8, height: 8, borderRadius: '50%', backgroundColor: 'rgb(var(--mdui-color-primary))' }} /></div>
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ fontWeight: 500, fontSize: 14 }}>{r.note || '位置记录'}</span>
                  <span style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>{formatRecordTime(r.timestamp)}</span>
                </div>
                <div style={{ fontSize: 13, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>{r.address || `${r.latitude.toFixed(5)}, ${r.longitude.toFixed(5)}`}</div>
                <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-primary))' }}>{r.accuracy != null ? `精度 ${Math.round(r.accuracy)}m` : '真实记录'}</div>
              </div>
            </div>
          ))}
          {!error && records.length === 0 && (
            <div style={{ padding: 24, textAlign: 'center', color: 'rgb(var(--mdui-color-on-surface-variant))', fontSize: 14 }}>
              暂无位置记录
            </div>
          )}
        </Card>
      </div>
    </SubPageScaffold>
    {showConfig && (
      <LocationTrackingConfigDialog
        config={trackingConfig}
        onConfigUpdate={(c) => { setTrackingConfig(c); setShowConfig(false); }}
        onDismiss={() => setShowConfig(false)}
      />
    )}
  </>
  );
}
