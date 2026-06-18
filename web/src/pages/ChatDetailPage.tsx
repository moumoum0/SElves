import { useRef, useState, useEffect, useCallback, useMemo } from 'react';
import { MemberAvatar } from '../components/MemberAvatar';
import { GroupManagementDialog } from '../components/GroupManagementDialog';
import { formatMessageTime } from '../lib/utils';
import type { ChatGroup, Member, Message } from '../types/models';

interface ChatDetailPageProps {
  currentMember: Member;
  group: ChatGroup;
  messages: Message[];
  members: Member[];
  onBack: () => void;
  onSendMessage?: (content: string) => void;
  onDeleteMessage?: (messageId: string) => void;
  onAddMembers?: (members: Member[]) => void;
  onRemoveMembers?: (members: Member[]) => void;
  onUpdateGroupInfo?: (name: string, avatarUrl?: string) => void;
  onDeleteGroup?: () => void;
  onTransferOwnership?: (member: Member) => void;
}

export function ChatDetailPage({
  currentMember,
  group,
  messages,
  members,
  onBack,
  onSendMessage,
  onDeleteMessage,
  onAddMembers,
  onRemoveMembers,
  onUpdateGroupInfo,
  onDeleteGroup,
  onTransferOwnership,
}: ChatDetailPageProps) {
  const [messageText, setMessageText] = useState('');
  const [showManagement, setShowManagement] = useState(false);
  const listRef = useRef<HTMLDivElement>(null);

  // Auto scroll to bottom on new messages
  useEffect(() => {
    if (listRef.current) {
      listRef.current.scrollTop = listRef.current.scrollHeight;
    }
  }, [messages.length]);

  const handleSend = useCallback(() => {
    if (messageText.trim() && onSendMessage) {
      onSendMessage(messageText.trim());
      setMessageText('');
    }
  }, [messageText, onSendMessage]);

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        handleSend();
      }
    },
    [handleSend]
  );

  return (
    <div
      style={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        backgroundColor: 'rgb(var(--mdui-color-background))',
      }}
    >
      {/* ChatTopBar — 群名 + 成员数 + more_vert */}
      <mdui-top-app-bar
        style={{
          position: 'sticky',
          top: 0,
          zIndex: 10,
          backgroundColor: 'rgb(var(--mdui-color-surface))',
        }}
      >
        <mdui-button-icon icon="arrow_back" onClick={onBack}></mdui-button-icon>
        <mdui-top-app-bar-title>
          <div style={{ display: 'flex', flexDirection: 'column', lineHeight: 1.3 }}>
            <span
              style={{
                fontSize: 16,
                fontWeight: 500,
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
              }}
            >
              {group.name}
            </span>
            <span
              style={{
                fontSize: 12,
                color: 'rgb(var(--mdui-color-on-surface-variant))',
                fontWeight: 400,
              }}
            >
              {group.members.length} 位成员
            </span>
          </div>
        </mdui-top-app-bar-title>
        <mdui-button-icon icon="more_vert" onClick={() => setShowManagement(true)}></mdui-button-icon>
      </mdui-top-app-bar>

      {showManagement && (
        <GroupManagementDialog
          group={group}
          currentMember={currentMember}
          allMembers={members}
          onDismiss={() => setShowManagement(false)}
          onAddMembers={(m) => { setShowManagement(false); onAddMembers?.(m); }}
          onRemoveMembers={(m) => { setShowManagement(false); onRemoveMembers?.(m); }}
          onUpdateGroupInfo={(name, avatarUrl) => { setShowManagement(false); onUpdateGroupInfo?.(name, avatarUrl); }}
          onDeleteGroup={() => { setShowManagement(false); onDeleteGroup?.(); onBack(); }}
          onTransferOwnership={(m) => { setShowManagement(false); onTransferOwnership?.(m); }}
        />
      )}

      {/* 消息列表 */}
      <div
        ref={listRef}
        style={{
          flex: 1,
          overflowY: 'auto',
          padding: '0 16px',
          paddingBottom: 8,
        }}
      >
        {messages.length === 0 ? (
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              height: '100%',
              color: 'rgb(var(--mdui-color-on-surface-variant))',
              fontSize: 14,
            }}
          >
            暂无消息
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8, minHeight: '100%', justifyContent: 'flex-end' }}>
            {messages.map((msg) => (
              <MessageItem
                key={msg.id}
                message={msg}
                isFromCurrentMember={msg.senderId === currentMember.id}
                currentMember={currentMember}
                sender={members.find((m) => m.id === msg.senderId)}
                onDeleteMessage={onDeleteMessage}
              />
            ))}
          </div>
        )}
      </div>

      {/* 输入区域 */}
      <div
        style={{
          borderTop: '1px solid rgb(var(--mdui-color-outline-variant))',
          backgroundColor: 'rgb(var(--mdui-color-surface))',
        }}
      >
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 8,
            padding: 16,
          }}
        >
          {/* 图片按钮 */}
          <mdui-button-icon
            icon="image"
            style={{ color: 'rgb(var(--mdui-color-primary))', flexShrink: 0 }}
          ></mdui-button-icon>

          {/* 文本输入框 */}
          <mdui-text-field
            variant="outlined"
            placeholder="发送消息..."
            value={messageText}
            style={{ flex: 1 }}
            max-rows="5"
            onInput={(e: Event) =>
              setMessageText((e.target as HTMLInputElement).value)
            }
            onKeyDown={handleKeyDown as unknown as (e: Event) => void}
          ></mdui-text-field>

          {/* 发送按钮 */}
          <mdui-button-icon
            icon="send"
            style={{ color: 'rgb(var(--mdui-color-primary))', flexShrink: 0 }}
            disabled={!messageText.trim()}
            onClick={handleSend}
          ></mdui-button-icon>
        </div>
      </div>
    </div>
  );
}

// ─── MessageItem ────────────────────────────────────────────────

function MessageItem({
  message,
  isFromCurrentMember,
  currentMember,
  sender,
  onDeleteMessage,
}: {
  message: Message;
  isFromCurrentMember: boolean;
  currentMember: Member;
  sender?: Member;
  onDeleteMessage?: (id: string) => void;
}) {
  const [showMenu, setShowMenu] = useState(false);
  const [menuPosition, setMenuPosition] = useState({ x: 0, y: 0 });
  const menuRef = useRef<HTMLDivElement>(null);

  const handleContextMenu = useCallback(
    (e: React.MouseEvent) => {
      e.preventDefault();
      setMenuPosition({ x: e.clientX, y: e.clientY });
      setShowMenu(true);
    },
    []
  );

  // 点外部关闭菜单
  useEffect(() => {
    if (!showMenu) return;
    const handler = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setShowMenu(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, [showMenu]);

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: isFromCurrentMember ? 'flex-end' : 'flex-start',
        alignItems: 'flex-start',
        padding: '4px 0',
        position: 'relative',
      }}
    >
      {!isFromCurrentMember && (
        <>
          {/* 对方头像 */}
          <MemberAvatar
            name={sender?.name ?? '?'}
            avatarUrl={sender?.avatarUrl}
            size={40}
          />
          <div style={{ width: 8 }} />
          {/* 成员名 + 消息气泡 */}
          <div
            style={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'flex-start',
              maxWidth: 240,
            }}
          >
            <span
              style={{
                fontSize: 12,
                color: 'rgb(var(--mdui-color-on-surface-variant))',
                lineHeight: 1.4,
                marginBottom: 4,
              }}
            >
              {sender?.name ?? '未知'}
            </span>
            <div onContextMenu={handleContextMenu}>
              <MessageBubble
                message={message}
                isFromCurrentMember={false}
              />
            </div>
          </div>
        </>
      )}

      {isFromCurrentMember && (
        <>
          <div onContextMenu={handleContextMenu}>
            <MessageBubble
              message={message}
              isFromCurrentMember={true}
            />
          </div>
          <div style={{ width: 8 }} />
          <MemberAvatar
            name={sender?.name ?? currentMember.name}
            avatarUrl={sender?.avatarUrl ?? currentMember.avatarUrl}
            size={40}
          />
        </>
      )}

      {/* 长按/右键菜单 */}
      {showMenu && (
        <div
          ref={menuRef}
          style={{
            position: 'fixed',
            left: menuPosition.x,
            top: menuPosition.y,
            zIndex: 1000,
            backgroundColor: 'rgb(var(--mdui-color-surface-container))',
            borderRadius: 8,
            boxShadow: '0 2px 12px rgba(0,0,0,0.15)',
            padding: '4px 0',
            minWidth: 120,
          }}
        >
          {/* 复制 */}
          <button
            type="button"
            onClick={() => {
              navigator.clipboard.writeText(message.content);
              setShowMenu(false);
            }}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              width: '100%',
              padding: '8px 16px',
              border: 'none',
              background: 'transparent',
              cursor: 'pointer',
              fontSize: 14,
              color: 'rgb(var(--mdui-color-on-surface))',
            }}
            onMouseEnter={(e) => {
              (e.target as HTMLElement).style.backgroundColor =
                'rgba(var(--mdui-color-on-surface), 0.04)';
            }}
            onMouseLeave={(e) => {
              (e.target as HTMLElement).style.backgroundColor = 'transparent';
            }}
          >
            <mdui-icon name="content_copy" style={{ fontSize: 18 }}></mdui-icon>
            复制
          </button>
          {/* 删除 */}
          <button
            type="button"
            onClick={() => {
              if (onDeleteMessage) onDeleteMessage(message.id);
              setShowMenu(false);
            }}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              width: '100%',
              padding: '8px 16px',
              border: 'none',
              background: 'transparent',
              cursor: 'pointer',
              fontSize: 14,
              color: 'rgb(var(--mdui-color-error))',
            }}
            onMouseEnter={(e) => {
              (e.target as HTMLElement).style.backgroundColor =
                'rgba(var(--mdui-color-on-surface), 0.04)';
            }}
            onMouseLeave={(e) => {
              (e.target as HTMLElement).style.backgroundColor = 'transparent';
            }}
          >
            <mdui-icon name="delete" style={{ fontSize: 18 }}></mdui-icon>
            删除
          </button>
        </div>
      )}
    </div>
  );
}

// ─── MessageBubble ──────────────────────────────────────────────

function MessageBubble({
  message,
  isFromCurrentMember,
}: {
  message: Message;
  isFromCurrentMember: boolean;
}) {
  const bubbleColor = isFromCurrentMember
    ? 'rgb(var(--mdui-color-primary))'
    : 'rgb(var(--mdui-color-surface-container-high))';

  const contentColor = isFromCurrentMember
    ? 'rgb(var(--mdui-color-on-primary))'
    : 'rgb(var(--mdui-color-on-surface))';

  // 气泡圆角：current → topStart=16, topEnd=0, bottomStart=16, bottomEnd=16
  // other → topStart=0, topEnd=16, bottomStart=16, bottomEnd=16
  const bubbleRadius = isFromCurrentMember
    ? '16px 0 16px 16px'
    : '0 16px 16px 16px';

  return (
    <div
      style={{
        padding: '8px 12px',
        borderRadius: bubbleRadius,
        backgroundColor: bubbleColor,
        color: contentColor,
        fontSize: 14,
        lineHeight: 1.5,
        wordBreak: 'break-word',
      }}
    >
      {message.type === 'IMAGE' && message.imagePath ? (
        <div>
          <img
            src={message.imagePath}
            alt="图片消息"
            style={{
              maxWidth: 240,
              maxHeight: 360,
              borderRadius: 8,
              objectFit: 'contain',
              display: 'block',
            }}
          />
          {message.content ? (
            <div style={{ marginTop: 8, fontSize: 14, lineHeight: 1.5 }}>
              {message.content}
            </div>
          ) : null}
        </div>
      ) : (
        <div>{message.content}</div>
      )}

      {/* 时间戳 — 在气泡内 */}
      <div
        style={{
          fontSize: 11,
          lineHeight: 1.3,
          color: isFromCurrentMember
            ? 'rgba(255,255,255,0.7)'
            : 'rgba(var(--mdui-color-on-surface), 0.5)',
          marginTop: 4,
          textAlign: 'right',
        }}
      >
        {formatMessageTime(message.timestamp)}
      </div>
    </div>
  );
}