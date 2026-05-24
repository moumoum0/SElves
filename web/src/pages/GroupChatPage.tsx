import { MemberHeader } from '../components/MemberHeader';
import { MemberAvatar } from '../components/MemberAvatar';
import { formatMessageTime, getInitial, groupColorFromName } from '../lib/utils';
import type { ChatGroup, Member, Message } from '../types/models';

interface GroupChatPageProps {
  currentMember: Member;
  groups: ChatGroup[];
  groupMessages: Record<string, Message[]>;
  unreadCounts: Record<string, number>;
  onMemberSwitch: () => void;
  onOpenGroup: (groupId: string) => void;
}

export function GroupChatPage({
  currentMember,
  groups,
  groupMessages,
  unreadCounts,
  onMemberSwitch,
  onOpenGroup,
}: GroupChatPageProps) {
  const sortedGroups = [...groups].sort((a, b) => {
    const aLast = (groupMessages[a.id] ?? []).at(-1)?.timestamp ?? a.createdAt;
    const bLast = (groupMessages[b.id] ?? []).at(-1)?.timestamp ?? b.createdAt;
    return bLast - aLast;
  });

  return (
    <div style={{ minHeight: '100%', display: 'flex', flexDirection: 'column', backgroundColor: 'rgb(var(--mdui-color-surface))', position: 'relative' }}>
      <MemberHeader member={currentMember} onMemberSwitch={onMemberSwitch} />

      {sortedGroups.length === 0 ? (
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'rgb(var(--mdui-color-on-surface))' }}>
          暂无群聊，请点击右下角创建
        </div>
      ) : (
        <div>
          {sortedGroups.map((group, index) => {
            const msgs = groupMessages[group.id] ?? [];
            const latest = msgs.at(-1);
            const sender = group.members.find((m) => m.id === latest?.senderId);
            const unread = unreadCounts[group.id] ?? 0;
            const previewContent = latest
              ? latest.type === 'IMAGE'
                ? '[图片]'
                : latest.content
              : null;
            const preview = latest
              ? sender?.id === currentMember.id
                ? `你: ${previewContent}`
                : `${sender?.name ?? '未知成员'}: ${previewContent}`
              : '暂无消息';
            const previewIsEmpty = latest == null;

            return (
              <div key={group.id}>
                <div
                  onClick={() => onOpenGroup(group.id)}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 12,
                    padding: '12px 16px',
                    cursor: 'pointer',
                    backgroundColor: 'rgb(var(--mdui-color-surface))',
                    transition: 'background-color 0.1s',
                  }}
                >
                  {/* 群头像 */}
                  {group.avatarUrl ? (
                    <MemberAvatar name={group.name} avatarUrl={group.avatarUrl} size={52} />
                  ) : (
                    <div
                      style={{
                        width: 52,
                        height: 52,
                        borderRadius: '50%',
                        flexShrink: 0,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        backgroundColor: groupColorFromName(group.name),
                        color: '#fff',
                        fontWeight: 700,
                        fontSize: 20,
                      }}
                    >
                      {getInitial(group.name)}
                    </div>
                  )}

                  {/* 内容 */}
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontWeight: 600, fontSize: 16, lineHeight: '24px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', color: 'rgb(var(--mdui-color-on-surface))' }}>
                      {group.name}
                    </div>
                    <div style={{ height: 2 }} />
                    <div style={{ fontSize: 14, lineHeight: '20px', color: previewIsEmpty ? 'rgba(var(--mdui-color-on-surface-variant), 0.6)' : 'rgb(var(--mdui-color-on-surface-variant))', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {preview}
                    </div>
                  </div>

                  {/* 右侧：时间 + 未读徽章 */}
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 4, flexShrink: 0, marginLeft: 8 }}>
                    <span style={{ fontSize: 12, lineHeight: '16px', color: 'rgba(var(--mdui-color-on-surface-variant), 0.7)' }}>
                      {formatMessageTime(latest?.timestamp ?? group.createdAt)}
                    </span>
                    {unread > 0 && (
                      <div style={{
                        minWidth: 20,
                        minHeight: 16,
                        padding: '2px 6px',
                        borderRadius: 10,
                        backgroundColor: 'rgb(var(--mdui-color-primary))',
                        color: 'rgb(var(--mdui-color-surface))',
                        fontSize: 11,
                        fontWeight: 700,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        boxSizing: 'border-box',
                      }}>
                        {unread > 99 ? '99+' : unread}
                      </div>
                    )}
                  </div>
                </div>
                {index < sortedGroups.length - 1 && (
                  <div style={{ height: 1, backgroundColor: 'rgba(var(--mdui-color-outline), 0.3)', marginLeft: 72 }} />
                )}
              </div>
            );
          })}
        </div>
      )}

      <mdui-fab icon="add" style={{ position: 'absolute', right: 16, bottom: 16 }}></mdui-fab>
    </div>
  );
}
