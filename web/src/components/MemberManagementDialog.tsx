import { useState } from 'react';
import { MemberAvatar } from './MemberAvatar';
import { EditMemberDialog } from './EditMemberDialog';
import type { Member } from '../types/models';

interface MemberManagementDialogProps {
  members: Member[];
  currentMember: Member;
  onDismiss: () => void;
  onCreateNewMember: () => void;
  onDeleteMember: (member: Member) => void;
  onEditMember: (member: Member, name: string, bio: string, pronouns: string, groups: string[]) => void;
}

export function MemberManagementDialog({
  members, currentMember, onDismiss, onCreateNewMember, onDeleteMember, onEditMember,
}: MemberManagementDialogProps) {
  const [deleteTarget, setDeleteTarget] = useState<Member | null>(null);
  const [editTarget, setEditTarget] = useState<Member | null>(null);
  const [menuOpen, setMenuOpen] = useState<string | null>(null);

  const allGroups = Array.from(new Set(members.flatMap(m => m.groups ?? []))).sort();

  return (
    <>
      <div
        style={{ position: 'fixed', inset: 0, zIndex: 200, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)' }}
        onClick={(e) => { if (e.target === e.currentTarget) onDismiss(); }}
      >
        <div
          style={{ width: '92%', maxWidth: 420, maxHeight: '80vh', display: 'flex', flexDirection: 'column', backgroundColor: 'rgb(var(--mdui-color-surface))', borderRadius: 16, overflow: 'hidden' }}
          onClick={(e) => e.stopPropagation()}
        >
          {/* 标题栏 */}
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: 20, flexShrink: 0 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <mdui-icon name="group" style={{ fontSize: 24, color: 'rgb(var(--mdui-color-on-surface-variant))' }}></mdui-icon>
              <span style={{ fontSize: 22, fontWeight: 700, color: 'rgb(var(--mdui-color-on-surface))' }}>成员管理</span>
            </div>
            <button
              type="button"
              onClick={onCreateNewMember}
              style={{ width: 40, height: 40, borderRadius: '50%', border: 'none', backgroundColor: 'rgb(var(--mdui-color-primary))', color: 'rgb(var(--mdui-color-on-primary))', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
            >
              <mdui-icon name="add" style={{ fontSize: 20 }}></mdui-icon>
            </button>
          </div>

          {/* 成员列表 */}
          <div style={{ flex: 1, overflowY: 'auto', padding: '0 20px' }}>
            {members.map(member => (
              <div key={member.id} style={{ display: 'flex', alignItems: 'center', padding: '12px 4px', borderBottom: 'none' }}>
                <MemberAvatar name={member.name} avatarUrl={member.avatarUrl} size={40} />
                <div style={{ flex: 1, marginLeft: 12, minWidth: 0 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <span style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-surface))' }}>{member.name}</span>
                    {member.id === currentMember.id && (
                      <span style={{ fontSize: 11, padding: '2px 6px', borderRadius: 4, backgroundColor: 'rgb(var(--mdui-color-primary))', color: 'rgb(var(--mdui-color-on-primary))' }}>当前</span>
                    )}
                  </div>
                  <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {member.bio?.trim() || '暂无简介'}
                  </div>
                  {member.pronouns?.trim() && (
                    <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>{member.pronouns}</div>
                  )}
                </div>
                {/* more_vert 菜单 */}
                <div style={{ position: 'relative' }}>
                  <button type="button" onClick={() => setMenuOpen(menuOpen === member.id ? null : member.id)} style={{ border: 'none', background: 'transparent', cursor: 'pointer', padding: 4 }}>
                    <mdui-icon name="more_vert" style={{ fontSize: 20, color: 'rgb(var(--mdui-color-on-surface-variant))' }}></mdui-icon>
                  </button>
                  {menuOpen === member.id && (
                    <>
                      <div style={{ position: 'fixed', inset: 0, zIndex: 10 }} onClick={() => setMenuOpen(null)} />
                      <div style={{ position: 'absolute', right: 0, top: 32, zIndex: 11, backgroundColor: 'rgb(var(--mdui-color-surface))', borderRadius: 8, boxShadow: '0 4px 16px rgba(0,0,0,0.2)', minWidth: 120, overflow: 'hidden' }}>
                        <button type="button" style={{ display: 'flex', alignItems: 'center', gap: 8, width: '100%', padding: '10px 16px', border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14 }}
                          onClick={() => { setMenuOpen(null); setEditTarget(member); }}>
                          <mdui-icon name="edit" style={{ fontSize: 16 }}></mdui-icon>编辑
                        </button>
                        {member.id !== currentMember.id && (
                          <button type="button" style={{ display: 'flex', alignItems: 'center', gap: 8, width: '100%', padding: '10px 16px', border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14, color: 'rgb(var(--mdui-color-error))' }}
                            onClick={() => { setMenuOpen(null); setDeleteTarget(member); }}>
                            <mdui-icon name="delete" style={{ fontSize: 16, color: 'rgb(var(--mdui-color-error))' }}></mdui-icon>删除
                          </button>
                        )}
                      </div>
                    </>
                  )}
                </div>
              </div>
            ))}
          </div>

          {/* 底部按钮 */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', padding: 20, flexShrink: 0 }}>
            <button type="button" style={{ padding: '8px 16px', borderRadius: 8, border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14, color: 'rgb(var(--mdui-color-primary))' }} onClick={onDismiss}>关闭</button>
          </div>
        </div>
      </div>

      {/* 删除确认 */}
      {deleteTarget && (
        <div style={{ position: 'fixed', inset: 0, zIndex: 300, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.4)' }}>
          <div style={{ width: '88%', maxWidth: 340, backgroundColor: 'rgb(var(--mdui-color-surface))', borderRadius: 16, padding: 24 }}>
            <div style={{ fontSize: 18, fontWeight: 600, marginBottom: 12 }}>删除成员</div>
            <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))', marginBottom: 20 }}>确定要删除成员「{deleteTarget.name}」吗？</div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
              <button type="button" style={{ padding: '8px 16px', borderRadius: 8, border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14, color: 'rgb(var(--mdui-color-primary))' }} onClick={() => setDeleteTarget(null)}>取消</button>
              <button type="button" style={{ padding: '8px 20px', borderRadius: 8, border: 'none', backgroundColor: 'rgb(var(--mdui-color-error))', color: 'rgb(var(--mdui-color-on-error))', cursor: 'pointer', fontSize: 14 }}
                onClick={() => { onDeleteMember(deleteTarget); setDeleteTarget(null); }}>删除</button>
            </div>
          </div>
        </div>
      )}

      {/* 编辑成员 */}
      {editTarget && (
        <EditMemberDialog
          member={editTarget}
          existingMembers={members}
          existingGroups={allGroups}
          onDismiss={() => setEditTarget(null)}
          onConfirm={(name, bio, pronouns, groups) => {
            onEditMember(editTarget, name, bio, pronouns, groups);
            setEditTarget(null);
          }}
        />
      )}
    </>
  );
}
