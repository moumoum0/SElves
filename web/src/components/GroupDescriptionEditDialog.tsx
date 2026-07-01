import { useState } from 'react';
import { TextField } from '../ui/components/TextField';

interface MemberGroup {
  name: string;
  description: string;
  parentName?: string | null;
}

interface GroupDescriptionEditDialogProps {
  group: MemberGroup;
  existingGroupNames: string[];
  allMemberGroups: MemberGroup[];
  onDismiss: () => void;
  onConfirm: (name: string, description: string, parentName: string | null) => void;
}

/**
 * 成员分组编辑对话框
 * 与安卓 GroupDescriptionEditDialog.kt / GroupEditDialog 1:1 对应
 */
export function GroupDescriptionEditDialog({
  group,
  existingGroupNames,
  allMemberGroups,
  onDismiss,
  onConfirm,
}: GroupDescriptionEditDialogProps) {
  const [name, setName] = useState(group.name);
  const [description, setDescription] = useState(group.description);
  const [selectedParentName, setSelectedParentName] = useState<string | null>(
    group.parentName ?? null
  );
  const [nameError, setNameError] = useState('');
  const [showParentDropdown, setShowParentDropdown] = useState(false);

  // 计算不可选的父级（自身及后代）
  const ineligibleNames = new Set<string>();
  ineligibleNames.add(group.name);
  const addDescendants = (parentName: string) => {
    allMemberGroups
      .filter((g) => g.parentName === parentName)
      .forEach((child) => {
        ineligibleNames.add(child.name);
        addDescendants(child.name);
      });
  };
  addDescendants(group.name);

  const eligibleParents = allMemberGroups
    .filter((g) => !ineligibleNames.has(g.name))
    .sort((a, b) => a.name.localeCompare(b.name));

  const handleConfirm = () => {
    const trimmedName = name.trim();
    if (!trimmedName) {
      setNameError('分组名称不能为空');
      return;
    }
    if (trimmedName !== group.name && existingGroupNames.includes(trimmedName)) {
      setNameError('该分组名称已存在');
      return;
    }
    onConfirm(trimmedName, description.trim(), selectedParentName);
  };

  return (
    <div
      className="dialog-overlay"
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 200,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'rgba(var(--mdui-color-scrim), 0.5)',
      }}
      onClick={(e) => { if (e.target === e.currentTarget) onDismiss(); }}
    >
      <div
        className="dialog-panel"
        style={{
          width: '92%',
          maxWidth: 400,
          backgroundColor: 'rgb(var(--mdui-color-surface))',
          borderRadius: 12,
          padding: 24,
          boxShadow: '0 4px 24px rgba(var(--mdui-color-scrim), 0.2)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* 标题 */}
        <div
          style={{
            fontSize: 22,
            fontWeight: 400,
            color: 'rgb(var(--mdui-color-on-surface))',
            marginBottom: 16,
          }}
        >
          编辑分组
        </div>

        {/* 分组名称 */}
        <div style={{ marginBottom: 16 }}>
          <TextField
            label="分组名称"
            placeholder="请输入分组名称"
            value={name}
            error-text={nameError}
            style={{ width: '100%' }}
            onChange={(val) => {
              setName(val.replace(/\n/g, ''));
              setNameError('');
            }}
          />
        </div>

        {/* 分组描述 */}
        <div style={{ marginBottom: 16 }}>
          <TextField
            label="分组描述"
            placeholder="添加分组描述..."
            value={description}
            rows={4}
            style={{ width: '100%' }}
            onChange={(val) => setDescription(val)}
          />
        </div>

        {/* 父级分组选择 */}
        <div style={{ position: 'relative', marginBottom: 16 }}>
          <TextField
            label="父级分组"
            value={selectedParentName ?? '无'}
            readonly
            style={{ width: '100%' }}
            onClick={() => setShowParentDropdown((v) => !v)}
          />

          {showParentDropdown && (
            <>
              <div
                style={{ position: 'fixed', inset: 0, zIndex: 10 }}
                onClick={() => setShowParentDropdown(false)}
              />
              <div
                style={{
                  position: 'absolute',
                  top: '100%',
                  left: 0,
                  right: 0,
                  zIndex: 11,
                  backgroundColor: 'rgb(var(--mdui-color-surface-container))',
                  borderRadius: 8,
                  boxShadow: '0 4px 16px rgba(var(--mdui-color-scrim), 0.2)',
                  marginTop: 4,
                  maxHeight: 200,
                  overflowY: 'auto',
                }}
              >
                <button
                  type="button"
                  onClick={() => {
                    setSelectedParentName(null);
                    setShowParentDropdown(false);
                  }}
                  style={{
                    display: 'block',
                    width: '100%',
                    padding: '10px 16px',
                    border: 'none',
                    background: selectedParentName === null
                      ? 'rgba(var(--mdui-color-primary), 0.12)'
                      : 'transparent',
                    cursor: 'pointer',
                    fontSize: 14,
                    textAlign: 'left',
                    color: 'rgb(var(--mdui-color-on-surface))',
                  }}
                >
                  无
                </button>
                {eligibleParents.map((parent) => (
                  <button
                    key={parent.name}
                    type="button"
                    onClick={() => {
                      setSelectedParentName(parent.name);
                      setShowParentDropdown(false);
                    }}
                    style={{
                      display: 'block',
                      width: '100%',
                      padding: '10px 16px',
                      border: 'none',
                      background: selectedParentName === parent.name
                        ? 'rgba(var(--mdui-color-primary), 0.12)'
                        : 'transparent',
                      cursor: 'pointer',
                      fontSize: 14,
                      textAlign: 'left',
                      color: 'rgb(var(--mdui-color-on-surface))',
                    }}
                  >
                    {parent.name}
                  </button>
                ))}
              </div>
            </>
          )}
        </div>

        {/* 按钮 */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
          <button
            type="button"
            style={{
              padding: '8px 16px',
              borderRadius: 8,
              border: 'none',
              background: 'transparent',
              cursor: 'pointer',
              fontSize: 14,
              color: 'rgb(var(--mdui-color-primary))',
            }}
            onClick={onDismiss}
          >
            取消
          </button>
          <button
            type="button"
            style={{
              padding: '8px 20px',
              borderRadius: 8,
              border: 'none',
              backgroundColor: 'rgb(var(--mdui-color-primary))',
              color: 'rgb(var(--mdui-color-on-primary))',
              cursor: 'pointer',
              fontSize: 14,
              fontWeight: 600,
            }}
            onClick={handleConfirm}
          >
            确认
          </button>
        </div>
      </div>
    </div>
  );
}