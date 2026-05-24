import { useRef, useState, useEffect } from 'react';
import { MemberAvatar } from '../components/MemberAvatar';
import { formatMessageTime } from '../lib/utils';
import type { ChatGroup, Member, Message } from '../types/models';

interface ChatDetailPageProps {
  currentMember: Member;
  group: ChatGroup;
  messages: Message[];
  onBack: () => void;
}

export function ChatDetailPage({ currentMember, group, messages, onBack }: ChatDetailPageProps) {
  const [input, setInput] = useState('');
  const listRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLElement>(null);

  useEffect(() => {
    if (listRef.current) {
      listRef.current.scrollTop = listRef.current.scrollHeight;
    }
  }, [messages]);

  useEffect(() => {
    const el = inputRef.current;
    if (!el) return;
    const handler = (e: Event) => setInput((e.target as HTMLInputElement).value);
    el.addEventListener('input', handler);
    return () => el.removeEventListener('input', handler);
  }, []);

  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', backgroundColor: 'rgb(var(--mdui-color-surface))' }}>
      <mdui-top-app-bar style={{ position: 'sticky', top: 0, zIndex: 10 }}>
        <mdui-button-icon icon="arrow_back" onClick={onBack}></mdui-button-icon>
        <mdui-top-app-bar-title>{group.name}</mdui-top-app-bar-title>
      </mdui-top-app-bar>

      <div ref={listRef} style={{ flex: 1, overflowY: 'auto', padding: '8px 16px', display: 'flex', flexDirection: 'column', gap: 8 }}>
        {messages.map((msg) => {
          const isMine = msg.senderId === currentMember.id;
          const sender = group.members.find((m) => m.id === msg.senderId);
          return (
            <div key={msg.id} style={{ display: 'flex', flexDirection: isMine ? 'row-reverse' : 'row', gap: 8, alignItems: 'flex-end' }}>
              {!isMine && <MemberAvatar name={sender?.name ?? '?'} avatarUrl={sender?.avatarUrl} size={32} />}
              <div style={{ maxWidth: 240, display: 'flex', flexDirection: 'column', alignItems: isMine ? 'flex-end' : 'flex-start' }}>
                {!isMine && (
                  <span style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))', paddingLeft: 4, marginBottom: 2 }}>
                    {sender?.name ?? '未知'}
                  </span>
                )}
                <div style={{ padding: '8px 12px', borderRadius: isMine ? '16px 4px 16px 16px' : '4px 16px 16px 16px', backgroundColor: isMine ? 'rgb(var(--mdui-color-primary))' : 'rgb(var(--mdui-color-surface-container-high))', color: isMine ? 'rgb(var(--mdui-color-on-primary))' : 'rgb(var(--mdui-color-on-surface))', fontSize: 14, lineHeight: 1.5, wordBreak: 'break-word' }}>
                  {msg.content}
                </div>
                <span style={{ fontSize: 11, color: 'rgb(var(--mdui-color-on-surface-variant))', marginTop: 3, padding: '0 4px' }}>
                  {formatMessageTime(msg.timestamp)}
                </span>
              </div>
            </div>
          );
        })}
      </div>

      <div style={{ display: 'flex', gap: 8, alignItems: 'center', padding: '8px 12px 12px', borderTop: '1px solid rgb(var(--mdui-color-outline-variant))', backgroundColor: 'rgb(var(--mdui-color-surface))' }}>
        <mdui-text-field ref={inputRef} variant="outlined" placeholder="发送消息..." value={input} style={{ flex: 1 }} rows={1}></mdui-text-field>
        <mdui-button-icon icon="send" disabled={!input.trim() || undefined}></mdui-button-icon>
      </div>
    </div>
  );
}
