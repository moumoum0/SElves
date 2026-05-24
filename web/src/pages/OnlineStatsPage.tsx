import { useEffect, useMemo, useRef, useState } from 'react';
import { MemberAvatar } from '../components/MemberAvatar';
import type { Member } from '../types/models';
import { SubPageScaffold } from './SubPageScaffold';

interface OnlineStatsPageProps {
  members: Member[];
  currentMember: Member;
  onBack: () => void;
}

interface MemberOnlineStat {
  member: Member;
  isOnline: boolean;
  todayOnlineMinutes: number;
  lastActiveTime: number;
}

interface OnlineStats {
  onlineCount: number;
  memberStats: MemberOnlineStat[];
}

interface LoginLog {
  memberName: string;
  memberAvatar: string | null;
  isOnline: boolean;
  loginTime: number;
  logoutTime?: number | null;
  duration?: number;
}

interface LoginLogSummary {
  totalLogins: number;
  todayLogins: number;
  currentOnlineCount: number;
  averageOnlineTime: number;
}

function generateMockData(members: Member[]): {
  onlineStats: OnlineStats;
  logs: LoginLog[];
  summary: LoginLogSummary;
} {
  const memberStats = members.map((m, i) => ({
    member: m,
    isOnline: i === 0,
    todayOnlineMinutes: Math.max(0, 120 - i * 25),
    lastActiveTime: Date.now() - (i === 0 ? 0 : i * 3600000),
  }));
  const logs: LoginLog[] = members.slice(0, 4).map((m, i) => ({
    memberName: m.name,
    memberAvatar: m.avatarUrl,
    isOnline: i === 0,
    loginTime: Date.now() - (i + 1) * 7200000,
    logoutTime: i === 0 ? undefined : Date.now() - i * 3600000,
    duration: i === 0 ? undefined : 3600000,
  }));
  const summary: LoginLogSummary = {
    totalLogins: logs.length,
    todayLogins: logs.filter((_, i) => i < 2).length,
    currentOnlineCount: memberStats.filter(s => s.isOnline).length,
    averageOnlineTime: 1800000,
  };
  return {
    onlineStats: { onlineCount: memberStats.filter(s => s.isOnline).length, memberStats },
    logs,
    summary,
  };
}

function formatOnlineTime(minutes: number): string {
  if (minutes === 0) return '从未在线';
  if (minutes < 60) return `${minutes} 分钟`;
  const hours = Math.floor(minutes / 60);
  const rem = minutes % 60;
  if (minutes < 1440) {
    if (rem === 0) return `${hours} 小时`;
    return `${hours} 小时 ${rem} 分钟`;
  }
  const days = Math.floor(minutes / 1440);
  const remHours = Math.floor((minutes % 1440) / 60);
  if (remHours === 0) return `${days} 天`;
  return `${days} 天 ${remHours} 小时`;
}

function formatDuration(ms: number): string {
  const minutes = Math.floor(ms / 60000);
  const hours = Math.floor(minutes / 60);
  const rem = minutes % 60;
  if (hours > 0) return `${hours} 小时 ${rem} 分钟`;
  if (minutes > 0) return `${minutes} 分钟`;
  return '不到1分钟';
}

function formatDetailDateTime(ts: number): string {
  const d = new Date(ts);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

function formatLastActiveTime(ts: number): string {
  const diff = Date.now() - ts;
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`;
  return `${Math.floor(diff / 86400000)} 天前`;
}

export function OnlineStatsPage({ members, currentMember, onBack }: OnlineStatsPageProps) {
  const [tab, setTab] = useState(0);
  const [filter, setFilter] = useState<'ALL' | 'TODAY'>('ALL');
  const { onlineStats, logs, summary } = useMemo(() => generateMockData(members), [members]);
  const isLoading = false;
  const isLoadingLogs = false;

  const filteredLogs = useMemo(
    () => (filter === 'TODAY' ? logs.filter(l => Date.now() - l.loginTime < 86400000) : logs),
    [filter, logs]
  );

  const tabsRef = useRef<HTMLElement>(null);
  useEffect(() => {
    const el = tabsRef.current;
    if (!el) return;
    const handler = (e: Event) => {
      const val = Number((e.target as any).value);
      if (!Number.isNaN(val)) setTab(val);
    };
    el.addEventListener('change', handler);
    return () => el.removeEventListener('change', handler);
  }, []);

  return (
    <SubPageScaffold title="在线统计" onBack={onBack}>
      <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
        <mdui-tabs ref={tabsRef} value={String(tab)}>
          <mdui-tab value="0">在线状态</mdui-tab>
          <mdui-tab value="1">在线时长</mdui-tab>
          <mdui-tab value="2">登录日志</mdui-tab>
        </mdui-tabs>

        <div style={{ flex: 1, overflowY: 'auto' }}>
          {isLoading ? (
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
              <mdui-circular-progress />
            </div>
          ) : (
            <div style={{ padding: 16 }}>
              {tab === 0 && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                  {onlineStats.memberStats.map(stat => (
                    <OnlineStatItem key={stat.member.id} stat={stat} isCurrent={stat.member.id === currentMember.id} />
                  ))}
                </div>
              )}

              {tab === 1 && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                  {[...onlineStats.memberStats]
                    .sort((a, b) => b.todayOnlineMinutes - a.todayOnlineMinutes)
                    .map(stat => (
                      <OnlineTimeStatItem key={stat.member.id} stat={stat} isCurrent={stat.member.id === currentMember.id} />
                    ))}
                </div>
              )}

              {tab === 2 && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                  <LoginLogSummaryCard summary={summary} />
                  <FilterChips selected={filter} onChange={setFilter} />
                  {isLoadingLogs ? (
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: 200 }}>
                      <mdui-circular-progress />
                    </div>
                  ) : (
                    <>
                      {filteredLogs.map((log, idx) => (
                        <LoginLogItem key={idx} log={log} />
                      ))}
                      {filteredLogs.length === 0 && (
                        <mdui-card variant="filled" style={{ borderRadius: 16, backgroundColor: 'rgb(var(--mdui-color-surface-container))' }}>
                          <div style={{ padding: 32, textAlign: 'center', color: 'rgb(var(--mdui-color-on-surface-variant))', fontSize: 14 }}>
                            暂无登录日志
                          </div>
                        </mdui-card>
                      )}
                    </>
                  )}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </SubPageScaffold>
  );
}

function OnlineStatItem({ stat, isCurrent }: { stat: MemberOnlineStat; isCurrent: boolean }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '8px 0' }}>
      <MemberAvatar name={stat.member.name} avatarUrl={stat.member.avatarUrl} size={40} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-surface))', lineHeight: 1.5 }}>{stat.member.name}</span>
          {isCurrent && (
            <span
              style={{
                width: 6,
                height: 6,
                borderRadius: '50%',
                backgroundColor: 'rgb(var(--mdui-color-primary))',
                display: 'inline-block',
              }}
            />
          )}
        </div>
        <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))', lineHeight: 1.5 }}>
          今日在线 {formatOnlineTime(stat.todayOnlineMinutes)}
        </div>
        <div
          style={{
            fontSize: 12,
            color: stat.isOnline ? 'rgb(var(--mdui-color-tertiary))' : 'rgb(var(--mdui-color-on-surface-variant))',
            lineHeight: 1.5,
          }}
        >
          {stat.isOnline ? '在线' : `上次活跃 ${formatLastActiveTime(stat.lastActiveTime)}`}
        </div>
      </div>
      <span
        style={{
          width: 8,
          height: 8,
          borderRadius: '50%',
          backgroundColor: stat.isOnline ? 'rgb(var(--mdui-color-tertiary))' : 'rgb(var(--mdui-color-outline))',
          flexShrink: 0,
        }}
      />
    </div>
  );
}

function OnlineTimeStatItem({ stat, isCurrent }: { stat: MemberOnlineStat; isCurrent: boolean }) {
  return (
    <mdui-card variant="filled" style={{ borderRadius: 16, backgroundColor: 'rgb(var(--mdui-color-surface-container))' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: 16 }}>
        <MemberAvatar name={stat.member.name} avatarUrl={stat.member.avatarUrl} size={40} />
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <span style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-surface))', lineHeight: 1.5 }}>{stat.member.name}</span>
            {isCurrent && (
              <span
                style={{
                  width: 6,
                  height: 6,
                  borderRadius: '50%',
                  backgroundColor: 'rgb(var(--mdui-color-primary))',
                  display: 'inline-block',
                }}
              />
            )}
          </div>
          <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))', lineHeight: 1.5 }}>
            今日在线时长
          </div>
        </div>
        <span style={{ fontSize: 16, fontWeight: 700, color: 'rgb(var(--mdui-color-primary))' }}>
          {formatOnlineTime(stat.todayOnlineMinutes)}
        </span>
      </div>
    </mdui-card>
  );
}

function LoginLogSummaryCard({ summary }: { summary: LoginLogSummary }) {
  return (
    <mdui-card variant="filled" style={{ borderRadius: 16, backgroundColor: 'rgb(var(--mdui-color-surface-container))', padding: 16 }}>
      <div style={{ fontSize: 16, fontWeight: 700, color: 'rgb(var(--mdui-color-on-surface))', marginBottom: 12 }}>登录统计</div>
      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
        <SummaryItem title="总登录" value={String(summary.totalLogins)} color="rgb(var(--mdui-color-primary))" />
        <SummaryItem title="今日" value={String(summary.todayLogins)} color="rgb(var(--mdui-color-secondary))" />
        <SummaryItem title="当前在线" value={String(summary.currentOnlineCount)} color="rgb(var(--mdui-color-tertiary))" />
      </div>
      <div style={{ marginTop: 8, fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
        今日平均在线时长: {formatDuration(summary.averageOnlineTime)}
      </div>
    </mdui-card>
  );
}

function SummaryItem({ title, value, color }: { title: string; value: string; color: string }) {
  return (
    <div style={{ textAlign: 'center' }}>
      <div style={{ fontSize: 24, fontWeight: 700, color }}>{value}</div>
      <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>{title}</div>
    </div>
  );
}

function FilterChips({ selected, onChange }: { selected: 'ALL' | 'TODAY'; onChange: (v: 'ALL' | 'TODAY') => void }) {
  return (
    <div style={{ display: 'flex', gap: 8 }}>
      <mdui-chip selectable selected={selected === 'ALL'} onClick={() => onChange('ALL')}>全部</mdui-chip>
      <mdui-chip selectable selected={selected === 'TODAY'} onClick={() => onChange('TODAY')}>今天</mdui-chip>
    </div>
  );
}

function LoginLogItem({ log }: { log: LoginLog }) {
  return (
    <mdui-card variant="filled" style={{ borderRadius: 16, backgroundColor: 'rgb(var(--mdui-color-surface-container))' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: 16 }}>
        <MemberAvatar name={log.memberName} avatarUrl={log.memberAvatar} size={40} />
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ fontSize: 14, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface))' }}>{log.memberName}</span>
            {log.isOnline && (
              <span
                style={{
                  width: 8,
                  height: 8,
                  borderRadius: '50%',
                  backgroundColor: 'rgb(var(--mdui-color-primary))',
                }}
              />
            )}
          </div>
          <div style={{ marginTop: 4, fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
            登录: {formatDetailDateTime(log.loginTime)}
          </div>
          {log.isOnline ? (
            <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-primary))' }}>在线中</div>
          ) : log.logoutTime ? (
            <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
              登出: {formatDetailDateTime(log.logoutTime)} • 时长: {formatDuration(log.duration ?? 0)}
            </div>
          ) : null}
        </div>
      </div>
    </mdui-card>
  );
}
