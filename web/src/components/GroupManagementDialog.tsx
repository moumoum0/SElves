import { useState } from 'react';
import { MemberAvatar } from './MemberAvatar';
import type { ChatGroup, Member } from '../types/models';
import { Icon } from '../ui/components/Icon';
import { Checkbox } from '../ui/components/Checkbox';
import { TextField } from '../ui/components/TextField';
import { Radio } from '../ui/components/Radio';

interface GroupManagementDialogProps {
  group: ChatGroup;
  currentMember: Member;
  allMembers: Member[];
  onDismiss: () => void;
  onAddMembers: (members: Member[]) => void;
  onRemoveMembers: (members: Member[]) => void;
  onUpdateGroupInfo: (name: string, avatarUrl?: string) => void;
  onDeleteGroup: () => void;
  onTransferOwnership: (newOwner: Member) => void;
}

type SubDialog = 'add' | 'remove' | 'editInfo' | 'delete' | 'transfer' | null;

export function GroupManagementDialog({
  group,
  currentMember,
  allMembers,
  onDismiss,
  onAddMembers,
  onRemoveMembers,
  onUpdateGroupInfo,
  onDeleteGroup,
  onTransferOwnership,
}: GroupManagementDialogProps) {
  const [subDialog, setSubDialog] = useState<SubDialog>(null);
  const isOwner = group.ownerId === currentMember.id;
  const ownerMember = group.members.find((m) => m.id === group.ownerId);

  if (subDialog === 'add') {
    return <AddMemberDialog group={group} allMembers={allMembers} onDismiss={() => setSubDialog(null)} onConfirm={(m) => { setSubDialog(null); onAddMembers(m); }} />;
  }
  if (subDialog === 'remove') {
    return <RemoveMemberDialog group={group} currentMember={currentMember} onDismiss={() => setSubDialog(null)} onConfirm={(m) => { setSubDialog(null); onRemoveMembers(m); }} />;
  }
  if (subDialog === 'editInfo') {
    return <EditGroupInfoDialog group={group} onDismiss={() => setSubDialog(null)} onConfirm={(name, avatarUrl) => { setSubDialog(null); onUpdateGroupInfo(name, avatarUrl); }} />;
  }
  if (subDialog === 'delete') {
    return <DeleteGroupDialog groupName={group.name} onDismiss={() => setSubDialog(null)} onConfirm={() => { setSubDialog(null); onDeleteGroup(); onDismiss(); }} />;
  }
  if (subDialog === 'transfer') {
    return <TransferOwnershipDialog group={group} currentMember={currentMember} onDismiss={() => setSubDialog(null)} onConfirm={(m) => { setSubDialog(null); onTransferOwnership(m); }} />;
  }

  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 200, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)' }}
      onClick={(e) => { if (e.target === e.currentTarget) onDismiss(); }}>
      <div style={{ width: '92%', maxWidth: 400, backgroundColor: 'rgb(var(--mdui-color-surface))', borderRadius: 16, padding: 24, boxShadow: '0 4px 24px rgba(0,0,0,0.2)' }}
        onClick={(e) => e.stopPropagation()}>
        <div style={{ fontSize: 22, fontWeight: 400, color: 'rgb(var(--mdui-color-on-surface))', marginBottom: 4 }}>群组管理</div>
        <div style={{ fontSize: 13, color: 'rgb(var(--mdui-color-on-surface-variant))', marginBottom: 16 }}>
          群主：{ownerMember?.name ?? '未知'}
        </div>

        <ManagementOption icon="group_add" title="添加成员" subtitle="邀请成员加入群组" onClick={() => setSubDialog('add')} />
        {isOwner && <ManagementOption icon="person_remove" title="移除成员" subtitle="将成员移出群组" onClick={() => setSubDialog('remove')} />}
        {isOwner && <ManagementOption icon="edit" title="编辑群信息" subtitle="修改群名称" onClick={() => setSubDialog('editInfo')} />}
        {isOwner && <ManagementOption icon="swap_horiz" title="转让群主" subtitle="将群主转给其他成员" onClick={() => setSubDialog('transfer')} />}
        {isOwner && <ManagementOption icon="delete" title="解散群组" subtitle="永久解散此群组" onClick={() => setSubDialog('delete')} destructive />}

        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 16 }}>
          <button type="button" style={{ padding: '8px 16px', borderRadius: 8, border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14, color: 'rgb(var(--mdui-color-primary))' }} onClick={onDismiss}>关闭</button>
        </div>
      </div>
    </div>
  );
}

function ManagementOption({ icon, title, subtitle, onClick, destructive = false }: { icon: string; title: string; subtitle: string; onClick: () => void; destructive?: boolean }) {
  const color = destructive ? 'rgb(var(--mdui-color-error))' : 'rgb(var(--mdui-color-primary))';
  const bg = destructive ? 'rgba(var(--mdui-color-error), 0.08)' : 'rgba(var(--mdui-color-surface-variant), 0.3)';
  return (
    <div onClick={onClick} style={{ display: 'flex', alignItems: 'center', gap: 16, padding: 16, borderRadius: 12, backgroundColor: bg, marginBottom: 8, cursor: 'pointer', transition: 'background-color 0.15s' }}>
      <Icon style={{ fontSize: 24, color, flexShrink: 0 }}>{icon}</Icon>
      <div>
        <div style={{ fontSize: 16, fontWeight: 500, color: destructive ? 'rgb(var(--mdui-color-error))' : 'rgb(var(--mdui-color-on-surface))' }}>{title}</div>
        <div style={{ fontSize: 13, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>{subtitle}</div>
      </div>
    </div>
  );
}

// ── AddMemberDialog ──────────────────────────────────────────────

function AddMemberDialog({ group, allMembers, onDismiss, onConfirm }: { group: ChatGroup; allMembers: Member[]; onDismiss: () => void; onConfirm: (m: Member[]) => void }) {
  const notInGroup = allMembers.filter((m) => !group.members.some((gm) => gm.id === m.id));
  const [selected, setSelected] = useState<Set<string>>(new Set());

  const toggle = (id: string) => setSelected((prev) => { const next = new Set(prev); next.has(id) ? next.delete(id) : next.add(id); return next; });

  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 300, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)' }}
      onClick={(e) => { if (e.target === e.currentTarget) onDismiss(); }}>
      <div style={{ width: '92%', maxWidth: 400, backgroundColor: 'rgb(var(--mdui-color-surface))', borderRadius: 16, padding: 24, boxShadow: '0 4px 24px rgba(0,0,0,0.2)' }} onClick={(e) => e.stopPropagation()}>
        <div style={{ fontSize: 22, fontWeight: 400, color: 'rgb(var(--mdui-color-on-surface))', marginBottom: 16 }}>添加成员</div>
        {notInGroup.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '24px 0', color: 'rgb(var(--mdui-color-on-surface-variant))' }}>所有成员已在群组中</div>
        ) : (
          <div style={{ maxHeight: 320, overflowY: 'auto', marginBottom: 16 }}>
            {notInGroup.map((m) => (
              <div key={m.id} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '10px 0', cursor: 'pointer' }} onClick={() => toggle(m.id)}>
                <MemberAvatar name={m.name} avatarUrl={m.avatarUrl} size={40} />
                <div style={{ flex: 1, fontSize: 15, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface))' }}>{m.name}</div>
                <Checkbox checked={selected.has(m.id)} />
              </div>
            ))}
          </div>
        )}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
          <button type="button" style={{ padding: '8px 16px', borderRadius: 8, border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14, color: 'rgb(var(--mdui-color-primary))' }} onClick={onDismiss}>取消</button>
          <button type="button" disabled={selected.size === 0} style={{ padding: '8px 20px', borderRadius: 8, border: 'none', backgroundColor: selected.size === 0 ? 'rgba(var(--mdui-color-primary),0.38)' : 'rgb(var(--mdui-color-primary))', color: 'rgb(var(--mdui-color-on-primary))', cursor: selected.size === 0 ? 'not-allowed' : 'pointer', fontSize: 14 }}
            onClick={() => onConfirm(notInGroup.filter((m) => selected.has(m.id)))}>确定</button>
        </div>
      </div>
    </div>
  );
}

// ── RemoveMemberDialog ───────────────────────────────────────────

function RemoveMemberDialog({ group, currentMember, onDismiss, onConfirm }: { group: ChatGroup; currentMember: Member; onDismiss: () => void; onConfirm: (m: Member[]) => void }) {
  const removable = group.members.filter((m) => m.id !== group.ownerId);
  const [selected, setSelected] = useState<Set<string>>(new Set());

  const toggle = (id: string) => setSelected((prev) => { const next = new Set(prev); next.has(id) ? next.delete(id) : next.add(id); return next; });

  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 300, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)' }}
      onClick={(e) => { if (e.target === e.currentTarget) onDismiss(); }}>
      <div style={{ width: '92%', maxWidth: 400, backgroundColor: 'rgb(var(--mdui-color-surface))', borderRadius: 16, padding: 24, boxShadow: '0 4px 24px rgba(0,0,0,0.2)' }} onClick={(e) => e.stopPropagation()}>
        <div style={{ fontSize: 22, fontWeight: 400, color: 'rgb(var(--mdui-color-on-surface))', marginBottom: 16 }}>移除成员</div>
        <div style={{ maxHeight: 320, overflowY: 'auto', marginBottom: 16 }}>
          {removable.map((m) => (
            <div key={m.id} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '10px 0', cursor: 'pointer' }} onClick={() => toggle(m.id)}>
              <MemberAvatar name={m.name} avatarUrl={m.avatarUrl} size={40} />
              <div style={{ flex: 1, fontSize: 15, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface))' }}>{m.name}</div>
              <Checkbox checked={selected.has(m.id)} />
            </div>
          ))}
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
          <button type="button" style={{ padding: '8px 16px', borderRadius: 8, border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14, color: 'rgb(var(--mdui-color-primary))' }} onClick={onDismiss}>取消</button>
          <button type="button" disabled={selected.size === 0} style={{ padding: '8px 20px', borderRadius: 8, border: 'none', backgroundColor: selected.size === 0 ? 'rgba(var(--mdui-color-error),0.38)' : 'rgb(var(--mdui-color-error))', color: 'rgb(var(--mdui-color-on-error))', cursor: selected.size === 0 ? 'not-allowed' : 'pointer', fontSize: 14 }}
            onClick={() => onConfirm(removable.filter((m) => selected.has(m.id)))}>移除</button>
        </div>
      </div>
    </div>
  );
}

// ── EditGroupInfoDialog ──────────────────────────────────────────

function EditGroupInfoDialog({ group, onDismiss, onConfirm }: { group: ChatGroup; onDismiss: () => void; onConfirm: (name: string, avatarUrl?: string) => void }) {
  const [name, setName] = useState(group.name);
  const [nameError, setNameError] = useState('');

  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 300, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)' }}
      onClick={(e) => { if (e.target === e.currentTarget) onDismiss(); }}>
      <div style={{ width: '92%', maxWidth: 400, backgroundColor: 'rgb(var(--mdui-color-surface))', borderRadius: 16, padding: 24, boxShadow: '0 4px 24px rgba(0,0,0,0.2)' }} onClick={(e) => e.stopPropagation()}>
        <div style={{ fontSize: 22, fontWeight: 400, color: 'rgb(var(--mdui-color-on-surface))', marginBottom: 20 }}>编辑群信息</div>
        {/* 群头像 */}
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 20 }}>
          <div style={{
            width: 80, height: 80, borderRadius: '50%',
            backgroundColor: 'rgba(var(--mdui-color-primary), 0.1)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            cursor: 'pointer', position: 'relative',
          }}>
            <span style={{ fontSize: 32, fontWeight: 500, color: 'rgb(var(--mdui-color-primary))' }}>
              {group.name.charAt(0).toUpperCase()}
            </span>
            <Icon style={{ position: 'absolute', bottom: 0, right: 0, fontSize: 20, color: 'rgb(var(--mdui-color-primary))', backgroundColor: 'rgb(var(--mdui-color-surface))', borderRadius: '50%', padding: 2 }}>photo_camera</Icon>
          </div>
        </div>
        <div style={{ marginBottom: 20 }}>
          <TextField label="群名称" value={name} error-text={nameError} style={{ width: '100%' }} onChange={(val) => { setName(val); setNameError(''); }} />
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
          <button type="button" style={{ padding: '8px 16px', borderRadius: 8, border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14, color: 'rgb(var(--mdui-color-primary))' }} onClick={onDismiss}>取消</button>
          <button type="button" style={{ padding: '8px 20px', borderRadius: 8, border: 'none', backgroundColor: 'rgb(var(--mdui-color-primary))', color: 'rgb(var(--mdui-color-on-primary))', cursor: 'pointer', fontSize: 14 }}
            onClick={() => { if (!name.trim()) { setNameError('群名不能为空'); return; } onConfirm(name.trim()); }}>保存</button>
        </div>
      </div>
    </div>
  );
}

// ── DeleteGroupDialog ────────────────────────────────────────────

function DeleteGroupDialog({ groupName, onDismiss, onConfirm }: { groupName: string; onDismiss: () => void; onConfirm: () => void }) {
  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 300, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)' }}
      onClick={(e) => { if (e.target === e.currentTarget) onDismiss(); }}>
      <div style={{ width: '85%', maxWidth: 320, backgroundColor: 'rgb(var(--mdui-color-surface))', borderRadius: 16, padding: 24, boxShadow: '0 4px 24px rgba(0,0,0,0.2)' }} onClick={(e) => e.stopPropagation()}>
        <div style={{ fontSize: 22, fontWeight: 400, color: 'rgb(var(--mdui-color-on-surface))', marginBottom: 16 }}>解散群组</div>
        <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))', marginBottom: 24 }}>确定要解散「{groupName}」吗？此操作不可撤销。</div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
          <button type="button" style={{ padding: '8px 16px', borderRadius: 8, border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14, color: 'rgb(var(--mdui-color-primary))' }} onClick={onDismiss}>取消</button>
          <button type="button" style={{ padding: '8px 20px', borderRadius: 8, border: 'none', backgroundColor: 'rgb(var(--mdui-color-error))', color: 'rgb(var(--mdui-color-on-error))', cursor: 'pointer', fontSize: 14 }} onClick={onConfirm}>解散</button>
        </div>
      </div>
    </div>
  );
}

// ── TransferOwnershipDialog ──────────────────────────────────────

function TransferOwnershipDialog({ group, currentMember, onDismiss, onConfirm }: { group: ChatGroup; currentMember: Member; onDismiss: () => void; onConfirm: (m: Member) => void }) {
  const candidates = group.members.filter((m) => m.id !== currentMember.id);
  const [selected, setSelected] = useState<string | null>(null);

  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 300, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)' }}
      onClick={(e) => { if (e.target === e.currentTarget) onDismiss(); }}>
      <div style={{ width: '92%', maxWidth: 400, backgroundColor: 'rgb(var(--mdui-color-surface))', borderRadius: 16, padding: 24, boxShadow: '0 4px 24px rgba(0,0,0,0.2)' }} onClick={(e) => e.stopPropagation()}>
        <div style={{ fontSize: 22, fontWeight: 400, color: 'rgb(var(--mdui-color-on-surface))', marginBottom: 16 }}>转让群主</div>
        <div style={{ maxHeight: 280, overflowY: 'auto', marginBottom: 16 }}>
          {candidates.map((m) => (
            <div key={m.id} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '10px 0', cursor: 'pointer' }} onClick={() => setSelected(m.id)}>
              <Radio checked={selected === m.id} style={{ flexShrink: 0 }} />
              <MemberAvatar name={m.name} avatarUrl={m.avatarUrl} size={40} />
              <div style={{ flex: 1, fontSize: 15, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface))' }}>{m.name}</div>
            </div>
          ))}
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
          <button type="button" style={{ padding: '8px 16px', borderRadius: 8, border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14, color: 'rgb(var(--mdui-color-primary))' }} onClick={onDismiss}>取消</button>
          <button type="button" disabled={!selected} style={{ padding: '8px 20px', borderRadius: 8, border: 'none', backgroundColor: selected ? 'rgb(var(--mdui-color-primary))' : 'rgba(var(--mdui-color-primary),0.38)', color: 'rgb(var(--mdui-color-on-primary))', cursor: selected ? 'pointer' : 'not-allowed', fontSize: 14 }}
            onClick={() => { const m = candidates.find((c) => c.id === selected); if (m) onConfirm(m); }}>确认转让</button>
        </div>
      </div>
    </div>
  );
}
