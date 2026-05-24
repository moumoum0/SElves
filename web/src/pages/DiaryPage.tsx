import { useCallback, useEffect, useRef, useState } from 'react';
import { formatDetailDateTime, formatTimestamp } from '../lib/utils';
import type { Member, MemberDiary } from '../types/models';
import { SubPageScaffold } from './SubPageScaffold';

interface DiaryPageProps {
  diaries: MemberDiary[];
  currentMember: Member;
  onBack: () => void;
  onCreateDiary?: (title: string, content: string) => void;
  onUpdateDiary?: (id: string, title: string, content: string) => void;
  onDeleteDiary?: (id: string) => void;
}

function useDialogClose<T extends HTMLElement>(open: boolean, onClose: () => void) {
  const ref = useRef<T>(null);
  useEffect(() => {
    const el = ref.current;
    if (!el || !open) return;
    const handler = () => onClose();
    el.addEventListener('close', handler);
    return () => el.removeEventListener('close', handler);
  }, [open, onClose]);
  return ref;
}

export function DiaryPage({
  diaries,
  currentMember,
  onBack,
  onCreateDiary,
  onUpdateDiary,
  onDeleteDiary,
}: DiaryPageProps) {
  const [memberDiaries, setMemberDiaries] = useState<MemberDiary[]>([]);
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [editingDiary, setEditingDiary] = useState<MemberDiary | null>(null);
  const [selectedDiary, setSelectedDiary] = useState<MemberDiary | null>(null);
  const [deletingDiary, setDeletingDiary] = useState<MemberDiary | null>(null);
  const [formTitle, setFormTitle] = useState('');
  const [formContent, setFormContent] = useState('');
  const [contentError, setContentError] = useState(false);
  const longPressTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    setMemberDiaries(diaries.filter((d) => d.memberId === currentMember.id));
  }, [diaries, currentMember.id]);

  const openCreateDialog = () => {
    setFormTitle('');
    setFormContent('');
    setContentError(false);
    setShowCreateDialog(true);
  };

  const openEditDialog = (diary: MemberDiary) => {
    setFormTitle(diary.title);
    setFormContent(diary.content);
    setContentError(false);
    setEditingDiary(diary);
    setSelectedDiary(null);
  };

  const handleConfirm = () => {
    if (!formContent.trim()) {
      setContentError(true);
      return;
    }
    if (editingDiary) {
      const updated: MemberDiary = {
        ...editingDiary,
        title: formTitle,
        content: formContent,
        updatedAt: Date.now(),
      };
      setMemberDiaries((prev) => prev.map((d) => (d.id === updated.id ? updated : d)));
      onUpdateDiary?.(editingDiary.id, formTitle, formContent);
      setEditingDiary(null);
    } else {
      const newDiary: MemberDiary = {
        id: `diary-${Date.now()}`,
        memberId: currentMember.id,
        title: formTitle,
        content: formContent,
        createdAt: Date.now(),
        updatedAt: Date.now(),
      };
      setMemberDiaries((prev) => [newDiary, ...prev]);
      onCreateDiary?.(formTitle, formContent);
      setShowCreateDialog(false);
    }
  };

  const handleDelete = (diary: MemberDiary) => {
    setMemberDiaries((prev) => prev.filter((d) => d.id !== diary.id));
    onDeleteDiary?.(diary.id);
    setDeletingDiary(null);
  };

  const startLongPress = useCallback((diary: MemberDiary) => {
    longPressTimer.current = setTimeout(() => {
      setSelectedDiary(diary);
    }, 500);
  }, []);

  const cancelLongPress = useCallback(() => {
    if (longPressTimer.current) {
      clearTimeout(longPressTimer.current);
      longPressTimer.current = null;
    }
  }, []);

  const isEditing = editingDiary !== null;

  return (
    <div style={{ position: 'relative', minHeight: '100%', display: 'flex', flexDirection: 'column' }}>
      <SubPageScaffold title={`${currentMember.name} 的日记`} onBack={onBack} noPadding>
        <div style={{ padding: '0 16px', flex: 1, display: 'flex', flexDirection: 'column' }}>
          {memberDiaries.length === 0 ? (
            <div
              style={{
                flex: 1,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'rgb(var(--mdui-color-on-surface-variant))',
                padding: '32px 0',
              }}
            >
              <mdui-icon name="edit" style={{ fontSize: 64, opacity: 0.5, marginBottom: 16 }}></mdui-icon>
              <div style={{ fontSize: 16 }}>还没有日记</div>
              <div style={{ fontSize: 14, opacity: 0.6, marginTop: 8 }}>点击右下角按钮新建日记</div>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8, padding: '16px 0' }}>
              {memberDiaries.map((diary) => (
                <mdui-card
                  key={diary.id}
                  variant="filled"
                  style={{
                    padding: 0,
                    borderRadius: 16,
                    backgroundColor: 'rgb(var(--mdui-color-surface-container))',
                    userSelect: 'none',
                  }}
                  onContextMenu={(e) => {
                    e.preventDefault();
                    setSelectedDiary(diary);
                  }}
                  onTouchStart={() => startLongPress(diary)}
                  onTouchEnd={cancelLongPress}
                  onTouchMove={cancelLongPress}
                >
                  <div style={{ padding: 16 }}>
                    {diary.title && (
                      <>
                        <div
                          style={{
                            fontSize: 16,
                            fontWeight: 500,
                            color: 'rgb(var(--mdui-color-on-surface))',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            whiteSpace: 'nowrap',
                          }}
                        >
                          {diary.title}
                        </div>
                        <div style={{ height: 4 }} />
                      </>
                    )}
                    <div
                      style={{
                        fontSize: 14,
                        color: 'rgb(var(--mdui-color-on-surface))',
                        opacity: 0.7,
                        overflow: 'hidden',
                        display: '-webkit-box',
                        WebkitLineClamp: 2,
                        WebkitBoxOrient: 'vertical',
                        lineHeight: 1.5,
                      }}
                    >
                      {diary.content}
                    </div>
                    <div style={{ height: 8 }} />
                    <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
                      {formatTimestamp(diary.createdAt)}
                    </div>
                  </div>
                </mdui-card>
              ))}
            </div>
          )}
        </div>
      </SubPageScaffold>

      {/* FAB */}
      <mdui-fab icon="add" style={{ position: 'fixed', right: 16, bottom: 16 }} onClick={openCreateDialog}></mdui-fab>

      {/* 长按底部弹窗（编辑/删除） */}
      {selectedDiary && (
        <>
          <div
            style={{
              position: 'fixed',
              inset: 0,
              backgroundColor: 'rgba(0,0,0,0.5)',
              zIndex: 50,
            }}
            onClick={() => setSelectedDiary(null)}
          />
          <div
            style={{
              position: 'fixed',
              bottom: 0,
              left: 0,
              right: 0,
              backgroundColor: 'rgb(var(--mdui-color-surface-container))',
              borderRadius: '16px 16px 0 0',
              padding: '16px 16px 32px 16px',
              zIndex: 51,
            }}
          >
            {selectedDiary.title && (
              <div
                style={{
                  fontSize: 16,
                  fontWeight: 600,
                  color: 'rgb(var(--mdui-color-on-surface))',
                  marginBottom: 4,
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace: 'nowrap',
                }}
              >
                {selectedDiary.title}
              </div>
            )}
            <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))', marginBottom: 16 }}>
              {formatDetailDateTime(selectedDiary.createdAt)}
            </div>
            <div
              style={{
                height: 1,
                backgroundColor: 'rgba(var(--mdui-color-outline-variant), 0.35)',
                marginBottom: 8,
              }}
            />
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <mdui-list-item onClick={() => openEditDialog(selectedDiary)}>
                <mdui-icon name="edit" slot="icon" style={{ fontSize: 20 }}></mdui-icon>
                <span>编辑</span>
              </mdui-list-item>
              <mdui-list-item
                onClick={() => {
                  setDeletingDiary(selectedDiary);
                  setSelectedDiary(null);
                }}
              >
                <mdui-icon name="delete" slot="icon" style={{ fontSize: 20, color: 'rgb(var(--mdui-color-error))' }}></mdui-icon>
                <span style={{ color: 'rgb(var(--mdui-color-error))' }}>删除</span>
              </mdui-list-item>
            </div>
          </div>
        </>
      )}

      {/* 删除确认对话框 */}
      {deletingDiary && <DeleteConfirmDialog diary={deletingDiary} onCancel={() => setDeletingDiary(null)} onConfirm={() => handleDelete(deletingDiary)} />}

      {/* 创建/编辑弹窗 */}
      {(showCreateDialog || editingDiary) && (
        <EditDialog
          open
          isEditing={isEditing}
          title={formTitle}
          content={formContent}
          contentError={contentError}
          onTitleChange={setFormTitle}
          onContentChange={(v) => {
            setFormContent(v);
            setContentError(false);
          }}
          onCancel={() => {
            setShowCreateDialog(false);
            setEditingDiary(null);
          }}
          onConfirm={handleConfirm}
        />
      )}
    </div>
  );
}

function DeleteConfirmDialog({
  diary,
  onCancel,
  onConfirm,
}: {
  diary: MemberDiary;
  onCancel: () => void;
  onConfirm: () => void;
}) {
  const ref = useDialogClose<HTMLElement>(true, onCancel);
  return (
    <mdui-dialog ref={ref} open headline="删除日记" close-on-overlay-click close-on-esc>
      <div style={{ padding: '0 24px 16px' }}>确定要删除这篇日记吗？</div>
      <mdui-button slot="action" variant="text" onClick={onCancel}>
        取消
      </mdui-button>
      <mdui-button slot="action" variant="filled" style={{ backgroundColor: 'rgb(var(--mdui-color-error))' }} onClick={onConfirm}>
        删除
      </mdui-button>
    </mdui-dialog>
  );
}

function EditDialog({
  open,
  isEditing,
  title,
  content,
  contentError,
  onTitleChange,
  onContentChange,
  onCancel,
  onConfirm,
}: {
  open: boolean;
  isEditing: boolean;
  title: string;
  content: string;
  contentError: boolean;
  onTitleChange: (v: string) => void;
  onContentChange: (v: string) => void;
  onCancel: () => void;
  onConfirm: () => void;
}) {
  const ref = useDialogClose<HTMLElement>(open, onCancel);
  if (!open) return null;
  return (
    <mdui-dialog ref={ref} open headline={isEditing ? '编辑日记' : '新建日记'} close-on-overlay-click close-on-esc>
      <div style={{ padding: '16px 24px', display: 'flex', flexDirection: 'column', gap: 12 }}>
        <mdui-text-field
          label="标题（可选）"
          placeholder="输入日记标题"
          variant="outlined"
          value={title}
          onInput={(e) => onTitleChange((e.target as HTMLInputElement).value.replace(/\n/g, ''))}
        ></mdui-text-field>
        {React.createElement('mdui-text-field', {
          label: '内容',
          placeholder: '写下今天的故事…',
          variant: 'outlined',
          type: 'textarea',
          rows: 5,
          value: content,
          onInput: (e: React.FormEvent<HTMLInputElement>) => {
            onContentChange(e.currentTarget.value);
          },
          error: contentError,
          helper: contentError ? '请输入日记内容' : undefined,
        })}
      </div>
      <mdui-button slot="action" variant="text" onClick={onCancel}>
        取消
      </mdui-button>
      <mdui-button slot="action" variant="filled" onClick={onConfirm}>
        确定
      </mdui-button>
    </mdui-dialog>
  );
}
