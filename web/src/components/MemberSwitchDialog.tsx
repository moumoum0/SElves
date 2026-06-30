import { useState, useRef, useEffect, useCallback } from 'react';
import { MemberAvatar } from './MemberAvatar';
import type { Member } from '../types/models';
import { Icon } from '../ui/components/Icon';

interface MemberSwitchDialogProps {
  members: Member[];
  currentMemberId: string;
  onDismiss: () => void;
  onMemberSelected: (member: Member) => void;
  onCreateNewMember: () => void;
  onDeleteMember?: (member: Member) => void;
}

export function MemberSwitchDialog({
  members,
  currentMemberId,
  onDismiss,
  onMemberSelected,
  onCreateNewMember,
  onDeleteMember,
}: MemberSwitchDialogProps) {
  const [menuTarget, setMenuTarget] = useState<Member | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Member | null>(null);
  const [countdown, setCountdown] = useState(3);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const menuRef = useRef<HTMLDivElement>(null);

  // 长按处理
  const longPressTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const startLongPress = useCallback((member: Member) => {
    if (member.id === currentMemberId) return;
    longPressTimerRef.current = setTimeout(() => {
      setMenuTarget(member);
    }, 500);
  }, [currentMemberId]);

  const cancelLongPress = useCallback(() => {
    if (longPressTimerRef.current) clearTimeout(longPressTimerRef.current);
  }, []);

  // 关闭菜单（点外部）
  useEffect(() => {
    if (!menuTarget) return;
    const handler = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuTarget(null);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, [menuTarget]);

  // 删除倒计时
  useEffect(() => {
    if (!deleteTarget) {
      if (timerRef.current) clearInterval(timerRef.current);
      setCountdown(3);
      return;
    }
    setCountdown(3);
    timerRef.current = setInterval(() => {
      setCountdown((c) => {
        if (c <= 1) {
          clearInterval(timerRef.current!);
          return 0;
        }
        return c - 1;
      });
    }, 1000);
    return () => { if (timerRef.current) clearInterval(timerRef.current); };
  }, [deleteTarget]);

  return (
    <div
      style={{
        position: 'fixed', inset: 0, zIndex: 200,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        backgroundColor: 'rgba(0,0,0,0.5)',
      }}
      onClick={(e) => { if (e.target === e.currentTarget) onDismiss(); }}
    >
      <div
        style={{
          width: '90%', maxWidth: 360,
          backgroundColor: 'rgb(var(--mdui-color-surface))',
          borderRadius: 16,
          overflow: 'hidden',
          boxShadow: '0 4px 24px rgba(0,0,0,0.2)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* 成员列表 */}
        <div style={{ padding: '8px 0' }}>
          {members.map((member) => {
            const isCurrent = member.id === currentMemberId;
            return (
              <div
                key={member.id}
                style={{ position: 'relative' }}
              >
                <div
                  style={{
                    display: 'flex', alignItems: 'center', gap: 12,
                    padding: '10px 16px', cursor: 'pointer',
                    backgroundColor: isCurrent ? 'rgba(var(--mdui-color-primary), 0.08)' : 'transparent',
                    transition: 'background-color 0.15s',
                  }}
                  onClick={() => { if (!isCurrent) onMemberSelected(member); else onDismiss(); }}
                  onMouseDown={() => startLongPress(member)}
                  onMouseUp={cancelLongPress}
                  onMouseLeave={cancelLongPress}
                  onTouchStart={() => startLongPress(member)}
                  onTouchEnd={cancelLongPress}
                  onContextMenu={(e) => {
                    e.preventDefault();
                    if (!isCurrent) setMenuTarget(member);
                  }}
                >
                  <MemberAvatar name={member.name} avatarUrl={member.avatarUrl} size={40} />
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontSize: 16, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface))', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {member.name}
                    </div>
                  </div>
                  {isCurrent && (
                    <Icon style={{ color: 'rgb(var(--mdui-color-primary))', fontSize: 20 }}>check</Icon>
                  )}
                </div>

                {/* 长按菜单 */}
                {menuTarget?.id === member.id && (
                  <div
                    ref={menuRef}
                    style={{
                      position: 'absolute', right: 12, top: '50%', transform: 'translateY(-50%)',
                      zIndex: 10, minWidth: 120,
                      backgroundColor: 'rgb(var(--mdui-color-surface-container))',
                      borderRadius: 8, boxShadow: '0 2px 12px rgba(0,0,0,0.2)',
                      padding: '4px 0',
                    }}
                  >
                    <button
                      type="button"
                      style={{
                        display: 'flex', alignItems: 'center', gap: 8,
                        width: '100%', padding: '10px 16px', border: 'none',
                        background: 'transparent', cursor: 'pointer',
                        fontSize: 14, color: 'rgb(var(--mdui-color-error))',
                      }}
                      onClick={() => { setMenuTarget(null); setDeleteTarget(member); }}
                    >
                      <Icon style={{ fontSize: 18 }}>delete</Icon>
                      删除
                    </button>
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {/* 新建成员按钮 */}
        <div style={{ borderTop: '1px solid rgba(var(--mdui-color-outline-variant), 0.5)', padding: '4px 0' }}>
          <button
            type="button"
            style={{
              display: 'flex', alignItems: 'center', gap: 12,
              width: '100%', padding: '12px 16px', border: 'none',
              background: 'transparent', cursor: 'pointer',
              fontSize: 16, color: 'rgb(var(--mdui-color-primary))',
            }}
            onClick={() => { onDismiss(); onCreateNewMember(); }}
          >
            <div style={{
              width: 40, height: 40, borderRadius: '50%',
              backgroundColor: 'rgba(var(--mdui-color-primary), 0.1)',
              display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
            }}>
              <Icon style={{ fontSize: 24, color: 'rgb(var(--mdui-color-primary))' }}>add</Icon>
            </div>
            新建成员
          </button>
        </div>
      </div>

      {/* 删除确认弹窗 */}
      {deleteTarget && (
        <div
          style={{
            position: 'fixed', inset: 0, zIndex: 300,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            backgroundColor: 'rgba(0,0,0,0.5)',
          }}
          onClick={(e) => e.stopPropagation()}
        >
          <div
            style={{
              width: '85%', maxWidth: 320,
              backgroundColor: 'rgb(var(--mdui-color-surface))',
              borderRadius: 16, padding: 24,
              boxShadow: '0 4px 24px rgba(0,0,0,0.2)',
            }}
          >
            <div style={{ fontSize: 22, fontWeight: 400, color: 'rgb(var(--mdui-color-on-surface))', marginBottom: 12 }}>
              删除成员
            </div>
            <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))', marginBottom: 24 }}>
              确定要删除成员「{deleteTarget.name}」？此操作不可撤销。
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
              <button
                type="button"
                style={{ padding: '8px 16px', borderRadius: 8, border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14, color: 'rgb(var(--mdui-color-primary))' }}
                onClick={() => setDeleteTarget(null)}
              >
                取消
              </button>
              <button
                type="button"
                disabled={countdown > 0}
                style={{
                  padding: '8px 16px', borderRadius: 8, border: 'none', cursor: countdown > 0 ? 'not-allowed' : 'pointer',
                  fontSize: 14,
                  backgroundColor: countdown > 0 ? 'rgba(var(--mdui-color-error), 0.38)' : 'rgb(var(--mdui-color-error))',
                  color: 'rgb(var(--mdui-color-on-error))',
                }}
                onClick={() => {
                  if (countdown > 0) return;
                  onDeleteMember?.(deleteTarget);
                  setDeleteTarget(null);
                  onDismiss();
                }}
              >
                {countdown > 0 ? `删除 (${countdown})` : '删除'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
