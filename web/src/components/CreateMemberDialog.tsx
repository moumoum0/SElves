import { useState } from 'react';
import { createPortal } from 'react-dom';
import { MemberAvatar } from './MemberAvatar';
import type { Member } from '../types/models';
import { Icon } from '../ui/components/Icon';
import { TextField } from '../ui/components/TextField';

interface CreateMemberDialogProps {
  existingMembers: Member[];
  existingGroups: string[];
  onDismiss: () => void;
  onConfirm: (name: string, bio: string, pronouns: string, groups: string[]) => void;
}

export function CreateMemberDialog({
  existingMembers,
  existingGroups,
  onDismiss,
  onConfirm,
}: CreateMemberDialogProps) {
  const [name, setName] = useState('');
  const [bio, setBio] = useState('');
  const [pronouns, setPronouns] = useState('');
  const [groups, setGroups] = useState<string[]>([]);
  const [nameError, setNameError] = useState('');
  const [showNewGroupDialog, setShowNewGroupDialog] = useState(false);
  const [newGroupInput, setNewGroupInput] = useState('');

  const availableGroups = existingGroups.filter((g) => !groups.includes(g));

  const handleConfirm = () => {
    const trimmed = name.trim();
    if (!trimmed) { setNameError('成员名不能为空'); return; }
    if (existingMembers.some((m) => m.name === trimmed)) { setNameError('该名称已存在'); return; }
    onConfirm(trimmed, bio.trim(), pronouns.trim(), groups);
  };

  return createPortal(
    <div
      className="dialog-overlay"
      style={{ position: 'fixed', inset: 0, zIndex: 200, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(var(--mdui-color-scrim), 0.5)' }}
      onClick={(e) => { if (e.target === e.currentTarget) onDismiss(); }}
    >
      <div
        className="dialog-panel"
        style={{ width: '92%', maxWidth: 400, maxHeight: '90vh', overflowY: 'auto', backgroundColor: 'rgb(var(--mdui-color-surface))', borderRadius: 16, padding: 24, boxShadow: '0 4px 24px rgba(var(--mdui-color-scrim), 0.2)' }}
        onClick={(e) => e.stopPropagation()}
      >
        <div style={{ fontSize: 22, fontWeight: 400, color: 'rgb(var(--mdui-color-on-surface))', marginBottom: 20 }}>新建成员</div>

        {/* 头像 */}
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 20 }}>
          <div style={{ position: 'relative', cursor: 'pointer' }}>
            <MemberAvatar name={name || '?'} avatarUrl={null} size={80} />
            <div style={{
              position: 'absolute', bottom: 0, right: 0,
              width: 28, height: 28, borderRadius: '50%',
              backgroundColor: 'rgb(var(--mdui-color-primary))',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
            }}>
              <Icon style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-primary))' }}>photo_camera</Icon>
            </div>
          </div>
        </div>

        {/* 名称 */}
        <div style={{ marginBottom: 16 }}>
          <TextField
            label="成员名"
            value={name}
            errorText={nameError}
            style={{ width: '100%' }}
            onChange={(val) => { setName(val); setNameError(''); }}
          />
        </div>

        {/* 简介 */}
        <div style={{ marginBottom: 16 }}>
          <TextField
            label="简介"
            placeholder="介绍一下这位成员..."
            value={bio}
            rows={3}
            style={{ width: '100%' }}
            onChange={(val) => setBio(val)}
          />
        </div>

        {/* 代词 */}
        <div style={{ marginBottom: 16 }}>
          <TextField
            label="代词"
            placeholder="如：TA / 她 / 他"
            value={pronouns}
            style={{ width: '100%' }}
            onChange={(val) => setPronouns(val)}
          />
        </div>

        {/* 已选分组 */}
        {groups.length > 0 && (
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 8 }}>
            {groups.map((g) => (
              <div key={g} style={{
                display: 'inline-flex', alignItems: 'center', gap: 4,
                padding: '4px 8px', borderRadius: 16,
                backgroundColor: 'rgba(var(--mdui-color-primary), 0.12)',
                color: 'rgb(var(--mdui-color-primary))', fontSize: 13,
              }}>
                {g}
                <span style={{ cursor: 'pointer', fontSize: 16, lineHeight: 1 }} onClick={() => setGroups(groups.filter((x) => x !== g))}>×</span>
              </div>
            ))}
          </div>
        )}

        {/* 可选分组 */}
        <div style={{ marginBottom: 16 }}>
          <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))', marginBottom: 6 }}>分组</div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
            {availableGroups.map((g) => (
              <button key={g} type="button"
                style={{ padding: '4px 12px', borderRadius: 16, border: '1px solid rgb(var(--mdui-color-outline))', background: 'transparent', cursor: 'pointer', fontSize: 13, color: 'rgb(var(--mdui-color-on-surface))' }}
                onClick={() => setGroups([...groups, g])}
              >{g}</button>
            ))}
            <button type="button"
              style={{ display: 'inline-flex', alignItems: 'center', gap: 4, padding: '4px 12px', borderRadius: 16, border: '1px solid rgb(var(--mdui-color-outline))', background: 'transparent', cursor: 'pointer', fontSize: 13, color: 'rgb(var(--mdui-color-primary))' }}
              onClick={() => setShowNewGroupDialog(true)}
            >
              <Icon style={{ fontSize: 16 }}>add</Icon>
              新建分组
            </button>
          </div>
        </div>

        {/* 按钮 */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
          <button type="button" style={{ padding: '8px 16px', borderRadius: 8, border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14, color: 'rgb(var(--mdui-color-primary))' }} onClick={onDismiss}>取消</button>
          <button type="button" style={{ padding: '8px 20px', borderRadius: 8, border: 'none', backgroundColor: 'rgb(var(--mdui-color-primary))', color: 'rgb(var(--mdui-color-on-primary))', cursor: 'pointer', fontSize: 14, fontWeight: 600 }} onClick={handleConfirm}>确认</button>
        </div>
      </div>

      {/* 新建分组子弹窗 */}
      {showNewGroupDialog && (
        <div className="dialog-overlay" style={{ position: 'fixed', inset: 0, zIndex: 300, display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(var(--mdui-color-scrim), 0.4)' }} onClick={(e) => e.stopPropagation()}>
          <div className="dialog-panel" style={{ width: '85%', maxWidth: 300, backgroundColor: 'rgb(var(--mdui-color-surface))', borderRadius: 16, padding: 20, boxShadow: '0 4px 24px rgba(var(--mdui-color-scrim), 0.2)' }}>
            <div style={{ fontSize: 16, fontWeight: 700, marginBottom: 16, color: 'rgb(var(--mdui-color-on-surface))' }}>新建分组</div>
            <TextField
              label="分组名称"
              value={newGroupInput}
              style={{ width: '100%', marginBottom: 12 }}
              onChange={(val) => setNewGroupInput(val)}
            />
            <TextField
              label="分组描述"
              placeholder="描述这个分组的用途..."
              value=""
              rows={2}
              style={{ width: '100%', marginBottom: 16 }}
            />
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
              <button type="button" style={{ padding: '8px 16px', borderRadius: 8, border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14, color: 'rgb(var(--mdui-color-primary))' }} onClick={() => { setShowNewGroupDialog(false); setNewGroupInput(''); }}>取消</button>
              <button type="button" style={{ padding: '8px 20px', borderRadius: 8, border: 'none', backgroundColor: 'rgb(var(--mdui-color-primary))', color: 'rgb(var(--mdui-color-on-primary))', cursor: 'pointer', fontSize: 14 }}
                onClick={() => {
                  const t = newGroupInput.trim();
                  if (t && !groups.includes(t)) setGroups([...groups, t]);
                  setShowNewGroupDialog(false);
                  setNewGroupInput('');
                }}
              >确定</button>
            </div>
          </div>
        </div>
      )}
    </div>,
    document.body,
  );
}
