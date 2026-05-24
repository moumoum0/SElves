import type { Member, TrackingSummary } from '../types/models';
import { SubPageScaffold } from './SubPageScaffold';

interface LocationPageProps {
  tracking: TrackingSummary;
  currentMember: Member;
  onBack: () => void;
}

const MOCK_RECORDS = [
  { id: '1', title: '晨间记录', address: '系统空间 · 起点', time: '08:12', distance: '0.8 km' },
  { id: '2', title: '午间记录', address: '系统空间 · 中段', time: '12:35', distance: '2.4 km' },
  { id: '3', title: '最近记录', address: '系统空间 · 当前附近', time: '09:42', distance: '4.6 km' },
];

export function LocationPage({ tracking, currentMember, onBack }: LocationPageProps) {
  const isRecording = tracking.status === 'RECORDING';

  return (
    <SubPageScaffold title="轨迹记录" subtitle="查看轨迹记录与统计概览" onBack={onBack}>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        {/* 状态卡 */}
        <mdui-card
          variant="filled"
          style={{
            padding: 16,
            borderRadius: 16,
            backgroundColor: isRecording ? 'rgb(var(--mdui-color-primary))' : undefined,
            color: isRecording ? 'rgb(var(--mdui-color-on-primary))' : undefined,
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 12 }}>
            <div style={{ width: 52, height: 52, borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: isRecording ? 'rgba(255,255,255,0.16)' : 'rgb(var(--mdui-color-primary))', color: isRecording ? 'inherit' : 'rgb(var(--mdui-color-on-primary))', flexShrink: 0 }}>
              <mdui-icon name={isRecording ? 'navigation' : 'location_on'} style={{ fontSize: 28 }}></mdui-icon>
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ fontWeight: 600, fontSize: 15 }}>{isRecording ? '正在记录轨迹' : '轨迹记录已停止'}</div>
              <div style={{ fontSize: 13, opacity: 0.8 }}>当前成员：{currentMember.name}</div>
            </div>
            <mdui-chip variant="assist">{isRecording ? '停止' : '开始'}</mdui-chip>
          </div>
          <mdui-linear-progress value={0.72} style={{ display: 'block' }}></mdui-linear-progress>
        </mdui-card>

        {/* 统计数据 */}
        <div style={{ display: 'flex', gap: 12 }}>
          {[{ label: '今日', value: String(tracking.todayRecords) }, { label: '总计', value: String(tracking.totalRecords) }, { label: '最后', value: tracking.lastRecordTime }].map((s) => (
            <mdui-card key={s.label} variant="filled" style={{ flex: 1, padding: 12, borderRadius: 12, textAlign: 'center' }}>
              <div style={{ fontSize: 22, fontWeight: 700, color: 'rgb(var(--mdui-color-primary))' }}>{s.value}</div>
              <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>{s.label}</div>
            </mdui-card>
          ))}
        </div>

        {/* 地图占位 */}
        <mdui-card variant="filled" style={{ padding: 16, borderRadius: 16 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
            <mdui-icon name="map" style={{ color: 'rgb(var(--mdui-color-primary))' }}></mdui-icon>
            <span style={{ fontWeight: 600, fontSize: 15 }}>地图预览</span>
          </div>
          <div style={{ height: 160, borderRadius: 12, backgroundColor: 'rgb(var(--mdui-color-surface-container-high))', position: 'relative', overflow: 'hidden', display: 'flex', alignItems: 'flex-end', justifyContent: 'center', padding: '0 12px 12px' }}>
            <div style={{ position: 'absolute', left: 52, top: 40, width: 12, height: 12, borderRadius: '50%', backgroundColor: 'rgb(var(--mdui-color-primary))' }} />
            <div style={{ position: 'absolute', right: 68, bottom: 52, width: 16, height: 16, borderRadius: '50%', backgroundColor: 'rgb(var(--mdui-color-error))' }} />
            <span style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))', zIndex: 1 }}>Web 预览占位 · 等待地图能力接入</span>
          </div>
        </mdui-card>

        {/* 今日记录 */}
        <mdui-card variant="filled" style={{ padding: 16, borderRadius: 16 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
            <mdui-icon name="history" style={{ color: 'rgb(var(--mdui-color-primary))' }}></mdui-icon>
            <span style={{ fontWeight: 600, fontSize: 15 }}>今日记录</span>
          </div>
          {MOCK_RECORDS.map((r, i) => (
            <div key={r.id} style={{ display: 'flex', gap: 12, paddingBottom: 12, borderBottom: i < MOCK_RECORDS.length - 1 ? '1px solid rgb(var(--mdui-color-outline-variant))' : 'none', marginBottom: i < MOCK_RECORDS.length - 1 ? 12 : 0 }}>
              <div style={{ paddingTop: 4 }}><div style={{ width: 8, height: 8, borderRadius: '50%', backgroundColor: 'rgb(var(--mdui-color-primary))' }} /></div>
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ fontWeight: 500, fontSize: 14 }}>{r.title}</span>
                  <span style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>{r.time}</span>
                </div>
                <div style={{ fontSize: 13, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>{r.address}</div>
                <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-primary))' }}>{r.distance}</div>
              </div>
            </div>
          ))}
        </mdui-card>
      </div>
    </SubPageScaffold>
  );
}
