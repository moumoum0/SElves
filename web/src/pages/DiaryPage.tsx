import { useCallback, useEffect, useRef, useState } from 'react';
import { formatDetailDateTime, formatTimestamp } from '../lib/utils';
import type { Member, MemberDiary } from '../types/models';
import { Button } from '../ui/components/Button';
import { Card } from '../ui/components/Card';
import { Dialog } from '../ui/components/Dialog';
import { FAB } from '../ui/components/FAB';
import { Icon } from '../ui/components/Icon';
import { ListItem } from '../ui/components/List';
import { TextField } from '../ui/components/TextField';
import { SubPageScaffold } from './SubPageScaffold';

interface DiaryPageProps {
  diaries: MemberDiary[];
  currentMember: Member;
  onBack: () => void;
  onCreateDiary?: (title: string, content: string) => void;
  onUpdateDiary?: (id: string, title: string, content: string) => void;
  onDeleteDiary?: (id: string) => void;
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
    <>
      <SubPageScaffold
        title={`${currentMember.name} 的日记`}
        onBack={onBack}
        noPadding
        fab={<FAB icon="add" onClick={openCreateDialog} />}
      >
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
              <Icon style={{ fontSize: 64, opacity: 0.5, marginBottom: 16 }}>edit</Icon>
              <div style={{ fontSize: 16 }}>还没有日记</div>
              <div style={{ fontSize: 14, opacity: 0.6, marginTop: 8 }}>点击右下角按钮新建日记</div>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8, padding: '16px 0' }}>
              {memberDiaries.map((diary) => (
                <Card
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
                </Card>
              ))}
            </div>
          )}
        </div>
      </SubPageScaffold>

      {/* 长按底部弹窗（编辑/删除） */}
      {selectedDiary && (
        <>
          <div
            style={{
              position: 'fixed',
              inset: 0,
              backgroundColor: 'rgba(var(--mdui-color-scrim), 0.5)',
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
              <ListItem onClick={() => openEditDialog(selectedDiary)}>
                <Icon slot="icon" style={{ fontSize: 20 }}>edit</Icon>
                <span>编辑</span>
              </ListItem>
              <ListItem
                onClick={() => {
                  setDeletingDiary(selectedDiary);
                  setSelectedDiary(null);
                }}
              >
                <Icon slot="icon" style={{ fontSize: 20, color: 'rgb(var(--mdui-color-error))' }}>delete</Icon>
                <span style={{ color: 'rgb(var(--mdui-color-error))' }}>删除</span>
              </ListItem>
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
    </>
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
  return (
    <Dialog
      open={true}
      onClose={onCancel}
      headline="删除日记"
      actions={
        <>
          <Button variant="text" onClick={onCancel}>
            取消
          </Button>
          <Button variant="filled" style={{ backgroundColor: 'rgb(var(--mdui-color-error))' }} onClick={onConfirm}>
            删除
          </Button>
        </>
      }
    >
      <div style={{ padding: '0 24px 16px' }}>确定要删除这篇日记吗？</div>
    </Dialog>
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
  if (!open) return null;
  return (
    <Dialog
      open={open}
      onClose={onCancel}
      headline={isEditing ? '编辑日记' : '新建日记'}
      actions={
        <>
          <Button variant="text" onClick={onCancel}>
            取消
          </Button>
          <Button variant="filled" onClick={onConfirm}>
            确定
          </Button>
        </>
      }
    >
      <div style={{ padding: '16px 24px', display: 'flex', flexDirection: 'column', gap: 12 }}>
        <TextField
          label="标题（可选）"
          placeholder="输入日记标题"
          variant="outlined"
          value={title}
          onChange={(val) => onTitleChange(val.replace(/\n/g, ''))}
        />
        <TextField
          label="内容"
          placeholder="写下今天的故事…"
          variant="outlined"
          type="textarea"
          rows={5}
          value={content}
          onChange={(val) => onContentChange(val)}
          error={contentError}
          supportingText={contentError ? '请输入日记内容' : undefined}
        />
      </div>
    </Dialog>
  );
}
