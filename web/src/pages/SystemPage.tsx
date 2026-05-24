import { useState } from 'react';
import { MemberAvatar } from '../components/MemberAvatar';
import type { SystemInfo } from '../types/models';

export interface SystemPageProps {
  system: SystemInfo;
  onNavigateMemberManagement: () => void;
  onNavigateOnlineStats: () => void;
  onNavigateSettings: () => void;
}

interface ManagementItemProps {
  icon: string;
  title: string;
  subtitle: string;
  onClick: () => void;
}

function ManagementListItem({ icon, title, subtitle, onClick }: ManagementItemProps) {
  return (
    <div
      onClick={onClick}
      className="system-mgmt-item"
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 16,
        padding: '12px 4px',
        cursor: 'pointer',
        borderRadius: 8,
      }}
    >
      <mdui-icon name={icon} style={{ color: 'rgb(var(--mdui-color-on-surface-variant))', fontSize: 24, flexShrink: 0 }}></mdui-icon>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-surface))', lineHeight: '24px' }}>{title}</div>
        <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))', lineHeight: '20px' }}>{subtitle}</div>
      </div>
    </div>
  );
}

export function SystemPage({ system, onNavigateMemberManagement, onNavigateOnlineStats, onNavigateSettings }: SystemPageProps) {
  const [showDesc, setShowDesc] = useState(false);

  return (
    <div style={{ minHeight: '100%', backgroundColor: 'rgb(var(--mdui-color-background))', padding: 16, display: 'flex', flexDirection: 'column', gap: 16 }}>
      {/* 系统信息卡片：填充 surfaceContainer，圆角 16，内边距 20 */}
      <div style={{ backgroundColor: 'rgb(var(--mdui-color-surface-container))', borderRadius: 16, padding: 20 }}>
        <div style={{ display: 'flex', alignItems: showDesc ? 'flex-start' : 'center', gap: 16, transition: 'all 0.2s' }}>
          <MemberAvatar name={system.name} avatarUrl={system.avatarUrl} size={60} />
          <div style={{ flex: 1, minWidth: 0 }}>
            <div
              onClick={() => setShowDesc((v) => !v)}
              style={{
                fontSize: 24,
                fontWeight: 700,
                color: 'rgb(var(--mdui-color-on-surface))',
                cursor: 'pointer',
                transform: showDesc ? 'scale(0.9)' : 'scale(1)',
                transformOrigin: 'left center',
                transition: 'transform 0.2s',
                lineHeight: '32px',
                display: 'inline-block',
              }}
            >
              {system.name}
            </div>
            {showDesc && (
              <div style={{ marginTop: 8, fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))', lineHeight: '20px' }}>
                {system.description || '还没有系统简介'}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 系统管理 分组标题 + 列表项（裸行，无卡片包裹） */}
      <div style={{ fontSize: 16, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface-variant))', padding: '8px 4px' }}>系统管理</div>
      <ManagementListItem icon="edit" title="编辑系统" subtitle="修改系统名称和头像" onClick={() => {}} />
      <ManagementListItem icon="group" title="成员管理" subtitle="管理系统成员" onClick={onNavigateMemberManagement} />
      <ManagementListItem icon="schedule" title="在线统计" subtitle="查看成员活跃度和在线时间" onClick={onNavigateOnlineStats} />

      <div style={{ fontSize: 16, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface-variant))', padding: '8px 4px' }}>其它</div>
      <ManagementListItem icon="settings" title="系统设置" subtitle="应用设置和偏好" onClick={onNavigateSettings} />
    </div>
  );
}
