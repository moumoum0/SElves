import { useState } from 'react';
import { MemberAvatar } from './MemberAvatar';
import type { SystemInfo } from '../types/models';

interface SystemEditDialogProps {
  system: SystemInfo;
  onDismiss: () => void;
  onConfirm: (name: string, description: string) => void;
}

export function SystemEditDialog({ system, onDismiss, onConfirm }: SystemEditDialogProps) {
  const [name, setName] = useState(system.name);
  const [description, setDescription] = useState(system.description ?? '');
  const [nameError, setNameError] = useState('');

  const handleConfirm = () => {
    const trimmed = name.trim();
    if (!trimmed) { setNameError('系统名称不能为空'); return; }
    onConfirm(trimmed, description.trim());
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
        <div style={{ fontSize: 20, fontWeight: 700, color: 'rgb(var(--mdui-color-on-surface))', marginBottom: 20 }}>编辑系统</div>

        {/* 头像 */}
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 20 }}>
          <div style={{ position: 'relative', cursor: 'pointer' }}>
            <MemberAvatar name={system.name} avatarUrl={system.avatarUrl} size={80} />
            <div style={{
              position: 'absolute', bottom: 0, right: 0,
              width: 28, height: 28, borderRadius: '50%',
              backgroundColor: 'rgb(var(--mdui-color-primary))',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
            }}>
              <mdui-icon name="photo_camera" style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-primary))' }}></mdui-icon>
            </div>
          </div>
        </div>

        {/* 系统名称 */}
        <div style={{ marginBottom: 16 }}>
          <mdui-text-field
            label="系统名称"
            value={name}
            error-text={nameError}
            style={{ width: '100%' }}
            onInput={(e: Event) => { setName((e.target as HTMLInputElement).value); setNameError(''); }}
          ></mdui-text-field>
        </div>

        {/* 系统简介 */}
        <div style={{ marginBottom: 20 }}>
          <mdui-text-field
            label="系统简介"
            placeholder="添加系统描述..."
            value={description}
            rows={4}
            style={{ width: '100%' }}
            onInput={(e: Event) => setDescription((e.target as HTMLInputElement).value)}
          ></mdui-text-field>
        </div>

        {/* 按钮 */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
          <button type="button" style={{ padding: '8px 16px', borderRadius: 8, border: 'none', background: 'transparent', cursor: 'pointer', fontSize: 14, color: 'rgb(var(--mdui-color-primary))' }} onClick={onDismiss}>取消</button>
          <button type="button" style={{ padding: '8px 20px', borderRadius: 8, border: 'none', backgroundColor: 'rgb(var(--mdui-color-primary))', color: 'rgb(var(--mdui-color-on-primary))', cursor: 'pointer', fontSize: 14, fontWeight: 600 }} onClick={handleConfirm}>保存</button>
        </div>
      </div>
    </div>
  );
}
