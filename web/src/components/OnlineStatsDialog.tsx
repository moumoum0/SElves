import { useMemo } from 'react';
import { MemberAvatar } from './MemberAvatar';
import type { Member } from '../types/models';

interface OnlineStatDialogProps {
  members: Member[];
  currentMember: Member;
  onDismiss: () => void;
}

interface MemberStat {
  member: Member;
  isOnline: boolean;
  todayMinutes: number;
  lastActiveTime: number;
}

function seededRand(seed: number) {
  let s = seed;
  return () => { s = (s * 1664525 + 1013904223) & 0xffffffff; return Math.abs(s) / 0x7fffffff; };
}

function calcStats(members: Member[], currentMember: Member): MemberStat[] {
  const now = Date.now();
  return members
    .map(m => {
      const r = seededRand(m.id.split('').reduce((a, c) => a + c.charCodeAt(0), 0));
      const isOnline = m.id === currentMember.id;
      const todayMinutes = isOnline ? Math.floor(r() * 360 + 120) : m.isDeleted ? 0 : Math.floor(r() * 150 + 30);
      const lastActiveTime = isOnline ? now : now - Math.floor(r() * 86400000 + 300000);
      return { member: m, isOnline, todayMinutes, lastActiveTime };
    })
    .sort((a, b) => Number(b.isOnline) - Number(a.isOnline) || b.lastActiveTime - a.lastActiveTime);
}

function fmtTime(ms: number): string {
  const d = new Date(ms);
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
}

function fmtDuration(min: number): string {
  if (min === 0) return '从未在线';
  if (min < 60) return `${min}分钟`;
  const h = Math.floor(min / 60), m = min % 60;
  if (min < 1440) return `${h}小时${m}分钟`;
  return `${Math.floor(min/1440)}天${Math.floor((min%1440)/60)}小时`;
}

export function OnlineStatsDialog({ members, currentMember, onDismiss }: OnlineStatDialogProps) {
  const stats = useMemo(() => calcStats(members, currentMember), [members, currentMember]);
  const onlineCount = stats.filter(s => s.isOnline).length;

  return (
    <div
      style={{ position: 'fixed', inset: 0, zIndex: 200, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)' }}
      onClick={(e) => { if (e.target === e.currentTarget) onDismiss(); }}
    >
      <div
        style={{ width: '92%', maxWidth: 420, maxHeight: '80vh', display: 'flex', flexDirection: 'column', backgroundColor: 'rgb(var(--mdui-color-surface))', borderRadius: 16, overflow: 'hidden' }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* 标题 */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: 20, flexShrink: 0 }}>
          <mdui-icon name="schedule" style={{ fontSize: 24, color: 'rgb(var(--mdui-color-on-surface-variant))' }}></mdui-icon>
          <span style={{ fontSize: 22, fontWeight: 700 }}>在线统计</span>
        </div>

        {/* 总体统计卡 */}
        <div style={{ margin: '0 20px 16px', backgroundColor: 'rgba(var(--mdui-color-surface-variant),0.5)', borderRadius: 12, padding: 16, display: 'flex', justifyContent: 'space-evenly' }}>
          {[['总成员', members.length, 'rgb(var(--mdui-color-primary))'], ['活跃', members.filter(m => !m.isDeleted).length, 'rgb(var(--mdui-color-secondary))'], ['在线', onlineCount, 'rgb(var(--mdui-color-tertiary))']].map(([label, val, color]) => (
            <div key={String(label)} style={{ textAlign: 'center' }}>
              <div style={{ fontSize: 22, fontWeight: 700, color: String(color) }}>{val}</div>
              <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>{label}</div>
            </div>
          ))}
        </div>

        {/* 成员列表 */}
        <div style={{ flex: 1, overflowY: 'auto', padding: '0 20px' }}>
          {stats.map(s => (
            <div key={s.member.id} style={{ display: 'flex', alignItems: 'center', padding: '8px 0' }}>
              <MemberAvatar name={s.member.name} avatarUrl={s.member.avatarUrl} size={40} />
              <div style={{ flex: 1, marginLeft: 12, minWidth: 0 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <span style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-surface))' }}>{s.member.name}</span>
                  {s.member.id === currentMember.id && (
                    <div style={{ width: 6, height: 6, borderRadius: '50%', backgroundColor: 'rgb(var(--mdui-color-primary))' }} />
                  )}
                </div>
                <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>今日在线：{fmtDuration(s.todayMinutes)}</div>
                <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>最后活跃：{fmtTime(s.lastActiveTime)}</div>
              </div>
              <div style={{ width: 8, height: 8, borderRadius: '50%', backgroundColor: s.isOnline ? 'rgb(var(--mdui-color-tertiary))' : 'rgb(var(--mdui-color-outline))', flexShrink: 0 }} />
            </div>
          ))}
        </div>

        {/* 底部 */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', padding: 20, flexShrink: 0 }}>
          <button type="button" style={{ padding: '8px 16px', borderRadius: 8, border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14, color: 'rgb(var(--mdui-color-primary))' }} onClick={onDismiss}>关闭</button>
        </div>
      </div>
    </div>
  );
}
