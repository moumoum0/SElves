import { useState } from 'react';
import { MemberAvatar } from './MemberAvatar';
import type { Member } from '../types/models';
import { Icon } from '../ui/components/Icon';
import { TextField } from '../ui/components/TextField';
import { Checkbox } from '../ui/components/Checkbox';

interface CreateGroupDialogProps {
  availableMembers: Member[];
  currentMember: Member;
  onDismiss: () => void;
  onConfirm: (name: string, members: Member[]) => void;
}

export function CreateGroupDialog({
  availableMembers,
  currentMember,
  onDismiss,
  onConfirm,
}: CreateGroupDialogProps) {
  const [step, setStep] = useState<'info' | 'members'>('info');
  const [groupName, setGroupName] = useState('');
  const [nameError, setNameError] = useState('');
  // currentMember always pre-selected
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set([currentMember.id]));

  const handleNext = () => {
    if (!groupName.trim()) { setNameError('群名不能为空'); return; }
    setStep('members');
  };

  const toggleMember = (id: string) => {
    if (id === currentMember.id) return; // cannot deselect self
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id); else next.add(id);
      return next;
    });
  };

  const handleConfirm = () => {
    const selected = availableMembers.filter((m) => selectedIds.has(m.id));
    // always include currentMember
    const members = selectedIds.has(currentMember.id)
      ? selected
      : [currentMember, ...selected];
    onConfirm(groupName.trim(), members);
  };

  return (
    <div
      style={{ position: 'fixed', inset: 0, zIndex: 200, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0,0,0,0.5)' }}
      onClick={(e) => { if (e.target === e.currentTarget) onDismiss(); }}
    >
      <div
        style={{ width: '92%', maxWidth: 400, backgroundColor: 'rgb(var(--mdui-color-surface))', borderRadius: 16, padding: 24, boxShadow: '0 4px 24px rgba(0,0,0,0.2)' }}
        onClick={(e) => e.stopPropagation()}
      >
        {step === 'info' ? (
          <>
            <div style={{ fontSize: 22, fontWeight: 400, color: 'rgb(var(--mdui-color-on-surface))', marginBottom: 20 }}>创建群聊</div>

            {/* 群头像 */}
            <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 20 }}>
              <div style={{
                width: 80, height: 80, borderRadius: '50%',
                backgroundColor: 'rgba(var(--mdui-color-primary), 0.1)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                cursor: 'pointer',
              }}>
                <Icon style={{ fontSize: 32, color: 'rgb(var(--mdui-color-primary))' }}>photo_camera</Icon>
              </div>
            </div>
            <div style={{ textAlign: 'center', fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))', marginBottom: 20 }}>
              选择群头像（可选）
            </div>

            {/* 群名 */}
            <div style={{ marginBottom: 20 }}>
              <TextField
                label="群名称"
                value={groupName}
                supportingText={nameError}
                style={{ width: '100%' }}
                onChange={(val) => { setGroupName(val); setNameError(''); }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
              <button type="button" style={{ padding: '8px 16px', borderRadius: 8, border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14, color: 'rgb(var(--mdui-color-primary))' }} onClick={onDismiss}>取消</button>
              <button type="button" style={{ padding: '8px 20px', borderRadius: 8, border: 'none', backgroundColor: 'rgb(var(--mdui-color-primary))', color: 'rgb(var(--mdui-color-on-primary))', cursor: 'pointer', fontSize: 14, fontWeight: 600 }} onClick={handleNext}>下一步</button>
            </div>
          </>
        ) : (
          <>
            <div style={{ fontSize: 22, fontWeight: 400, color: 'rgb(var(--mdui-color-on-surface))', marginBottom: 4 }}>选择成员</div>
            <div style={{ fontSize: 13, color: 'rgb(var(--mdui-color-on-surface-variant))', marginBottom: 16 }}>群主将自动包含在群聊中</div>

            <div style={{ maxHeight: 320, overflowY: 'auto', marginBottom: 16 }}>
              {availableMembers.map((m) => {
                const checked = selectedIds.has(m.id);
                const isSelf = m.id === currentMember.id;
                return (
                  <div
                    key={m.id}
                    style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '10px 0', cursor: isSelf ? 'default' : 'pointer' }}
                    onClick={() => toggleMember(m.id)}
                  >
                    <Checkbox checked={checked} disabled={isSelf}></Checkbox>
                    <MemberAvatar name={m.name} avatarUrl={m.avatarUrl} size={40} />
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <div style={{ fontSize: 15, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface))' }}>{m.name}</div>
                      {isSelf && <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-primary))' }}>群主（你）</div>}
                    </div>
                  </div>
                );
              })}
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
              <button type="button" style={{ padding: '8px 16px', borderRadius: 8, border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14, color: 'rgb(var(--mdui-color-primary))' }} onClick={() => setStep('info')}>取消</button>
              <button type="button" style={{ padding: '8px 20px', borderRadius: 8, border: 'none', backgroundColor: 'rgb(var(--mdui-color-primary))', color: 'rgb(var(--mdui-color-on-primary))', cursor: 'pointer', fontSize: 14, fontWeight: 600 }} onClick={handleConfirm}>确认</button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
