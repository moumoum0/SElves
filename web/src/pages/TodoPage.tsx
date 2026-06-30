import { useState, useCallback, useRef, useEffect } from 'react';
import type { Member, Todo, TodoPriority } from '../types/models';
import { formatTimestamp } from '../lib/utils';

interface TodoPageProps {
  todos: Todo[];
  members: Member[];
  currentMember: Member;
  onBack: () => void;
}

const PRIORITY_LABEL: Record<TodoPriority, string> = {
  HIGH: '高',
  NORMAL: '普通',
  LOW: '低',
};

const PRIORITY_COLOR: Record<TodoPriority, string> = {
  HIGH: 'rgb(var(--mdui-color-error))',
  NORMAL: 'rgb(var(--mdui-color-on-surface-variant))',
  LOW: 'rgb(var(--mdui-color-tertiary))',
};

export function TodoPage({ todos, members, onBack }: TodoPageProps) {
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [showPendingTodos, setShowPendingTodos] = useState(true);
  const [showCompletedTodos, setShowCompletedTodos] = useState(false);
  const [selectedTodo, setSelectedTodo] = useState<Todo | null>(null);
  const [showBottomSheet, setShowBottomSheet] = useState(false);

  const pendingTodos = todos.filter((todo) => !todo.isCompleted);
  const completedTodos = todos.filter((todo) => todo.isCompleted);
  const stats = {
    total: todos.length,
    pending: pendingTodos.length,
    completed: completedTodos.length,
  };

  const handleLongPress = useCallback((todo: Todo) => {
    setSelectedTodo(todo);
    setShowBottomSheet(true);
  }, []);

  return (
    <div style={{ position: 'relative', height: '100%', overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
      {/* TopAppBar */}
      <mdui-top-app-bar
        style={{
          flexShrink: 0,
          zIndex: 10,
          backgroundColor: 'rgb(var(--mdui-color-surface))',
          borderBottom: '1px solid rgba(var(--mdui-color-outline-variant), 0.35)',
        }}
      >
        <mdui-button-icon icon="arrow_back" onClick={onBack}></mdui-button-icon>
        <mdui-top-app-bar-title>
          <span style={{ fontWeight: 400 }}>待办事项</span>
        </mdui-top-app-bar-title>
      </mdui-top-app-bar>

      {/* 主要内容 */}
      <div style={{ flex: 1, minHeight: 0, overflowY: 'auto', overflowX: 'hidden' }}>
      <div style={{ padding: 16 }}>
        {/* 统计卡片 */}
        {stats.total > 0 ? (
          <mdui-card
            variant="filled"
            style={{
              padding: 16,
              borderRadius: 16,
              backgroundColor: 'rgb(var(--mdui-color-surface-container))',
              marginBottom: 16,
              boxShadow: 'none',
            }}
          >
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(3, minmax(0, 1fr))',
                gap: 8,
              }}
            >
              <StatBlock
                title="总计"
                value={stats.total}
                color="rgb(var(--mdui-color-primary))"
              />
              <StatBlock
                title="待完成"
                value={stats.pending}
                color="rgb(var(--mdui-color-secondary))"
              />
              <StatBlock
                title="已完成"
                value={stats.completed}
                color="rgb(var(--mdui-color-tertiary))"
              />
            </div>
          </mdui-card>
        ) : null}

        {/* 空状态 */}
        {pendingTodos.length === 0 && completedTodos.length === 0 ? (
          <div
            style={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              padding: '32px 16px',
              color: 'rgb(var(--mdui-color-on-surface-variant))',
              textAlign: 'center',
            }}
          >
            <mdui-icon
              name="assignment"
              style={{ fontSize: 64, opacity: 0.5, marginBottom: 16 }}
            ></mdui-icon>
            <div style={{ fontSize: 16 }}>暂无待办事项</div>
            <div style={{ fontSize: 14, opacity: 0.6, marginTop: 8 }}>
              点击右下角添加第一条待办
            </div>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {/* 待完成 */}
            {pendingTodos.length > 0 ? (
              <div>
                <div style={{ padding: '4px 0' }}>
                  <mdui-button-icon
                    icon="keyboard_arrow_down"
                    onClick={() => setShowPendingTodos((v) => !v)}
                    style={{
                      width: '100%',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      padding: '8px 0',
                      cursor: 'pointer',
                      transform: showPendingTodos ? 'rotate(0deg)' : 'rotate(-90deg)',
                      transition: 'transform 0.2s ease',
                      fontSize: 16,
                      fontWeight: 600,
                    }}
                  >
                    <span>待完成 ({pendingTodos.length})</span>
                  </mdui-button-icon>
                </div>
                {showPendingTodos && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    {pendingTodos.map((todo) => (
                      <TodoCard
                        key={todo.id}
                        todo={todo}
                        members={members}
                        onLongPress={() => handleLongPress(todo)}
                      />
                    ))}
                  </div>
                )}
              </div>
            ) : null}

            {/* 已完成 */}
            {completedTodos.length > 0 ? (
              <div>
                <div style={{ padding: '4px 0' }}>
                  <mdui-button-icon
                    icon="keyboard_arrow_down"
                    onClick={() => setShowCompletedTodos((v) => !v)}
                    style={{
                      width: '100%',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      padding: '8px 0',
                      cursor: 'pointer',
                      transform: showCompletedTodos ? 'rotate(0deg)' : 'rotate(-90deg)',
                      transition: 'transform 0.2s ease',
                      fontSize: 16,
                      fontWeight: 600,
                    }}
                  >
                    <span>已完成 ({completedTodos.length})</span>
                  </mdui-button-icon>
                </div>
                {showCompletedTodos && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    {completedTodos.map((todo) => (
                      <TodoCard
                        key={todo.id}
                        todo={todo}
                        members={members}
                        onLongPress={() => handleLongPress(todo)}
                      />
                    ))}
                  </div>
                )}
              </div>
            ) : null}
          </div>
        )}
      </div>
      </div>

      {/* FAB */}
      <mdui-fab
        icon="add"
        onClick={() => setShowCreateDialog(true)}
        style={{ position: 'absolute', right: 24, bottom: 24 }}
      ></mdui-fab>

      {/* ─── 创建待办对话框 ─── */}
      {showCreateDialog && (
        <CreateTodoDialog
          onDismiss={() => setShowCreateDialog(false)}
          onConfirm={(title, description, priority) => {
            setShowCreateDialog(false);
            // TODO: 实际创建逻辑
          }}
        />
      )}

      {/* ─── 长按底部弹窗 ─── */}
      {showBottomSheet && selectedTodo && (
        <TodoDetailBottomSheet
          todo={selectedTodo}
          members={members}
          onDismiss={() => {
            setShowBottomSheet(false);
            setSelectedTodo(null);
          }}
        />
      )}
    </div>
  );
}

// ─── StatBlock ──────────────────────────────────────────────

function StatBlock({
  title,
  value,
  color,
}: {
  title: string;
  value: number;
  color: string;
}) {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 2,
      }}
    >
      <span
        style={{
          fontSize: 22,
          lineHeight: 1.2,
          fontWeight: 700,
          color,
        }}
      >
        {value}
      </span>
      <span
        style={{
          fontSize: 12,
          color: 'rgb(var(--mdui-color-on-surface-variant))',
        }}
      >
        {title}
      </span>
    </div>
  );
}

// ─── TodoCard ──────────────────────────────────────────────

function TodoCard({
  todo,
  members,
  onLongPress,
}: {
  todo: Todo;
  members: Member[];
  onLongPress: () => void;
}) {
  const creator = members.find((m) => m.id === todo.createdBy);
  const timeLabel = todo.isCompleted
    ? `完成于 ${formatTimestamp(todo.completedAt ?? todo.createdAt)}`
    : `创建于 ${formatTimestamp(todo.createdAt)}`;

  return (
    <mdui-card
      variant="filled"
      style={{ borderRadius: 12, padding: 0, boxShadow: 'none' }}
      onContextMenu={(e) => {
        e.preventDefault();
        onLongPress();
      }}
    >
      <div
        style={{
          display: 'flex',
          alignItems: 'flex-start',
          padding: 16,
          cursor: 'context-menu',
        }}
        onContextMenu={(e: React.MouseEvent) => {
          e.preventDefault();
          onLongPress();
        }}
      >
        {/* 左侧内容 */}
        <div style={{ flex: 1, minWidth: 0 }}>
          {/* 标题和优先级 */}
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <div
              style={{
                flex: 1,
                minWidth: 0,
                fontSize: 16,
                lineHeight: 1.4,
                fontWeight: 500,
                color: todo.isCompleted
                  ? 'rgba(var(--mdui-color-on-surface), 0.6)'
                  : 'rgb(var(--mdui-color-on-surface))',
                textDecoration: todo.isCompleted ? 'line-through' : 'none',
              }}
            >
              {todo.title}
            </div>

            {/* 优先级标识 — padding 2x6, borderRadius 12, fontSize 10 */}
            {todo.priority !== 'NORMAL' && !todo.isCompleted && (
              <span
                style={{
                  padding: '2px 6px',
                  borderRadius: 12,
                  fontSize: 10,
                  lineHeight: 1.2,
                  color:
                    todo.priority === 'HIGH'
                      ? 'rgb(var(--mdui-color-on-error))'
                      : 'rgb(var(--mdui-color-on-tertiary-container))',
                  backgroundColor:
                    todo.priority === 'HIGH'
                      ? 'rgb(var(--mdui-color-error))'
                      : 'rgb(var(--mdui-color-tertiary-container))',
                  whiteSpace: 'nowrap',
                }}
              >
                {PRIORITY_LABEL[todo.priority]}
              </span>
            )}
          </div>

          {/* 描述 */}
          {todo.description ? (
            <div
              style={{
                marginTop: 4,
                fontSize: 14,
                lineHeight: 1.45,
                color: todo.isCompleted
                  ? 'rgba(var(--mdui-color-on-surface), 0.4)'
                  : 'rgba(var(--mdui-color-on-surface), 0.7)',
                display: '-webkit-box',
                WebkitLineClamp: 3,
                WebkitBoxOrient: 'vertical',
                overflow: 'hidden',
              }}
            >
              {todo.description}
            </div>
          ) : null}

          {/* 时间 */}
          <div
            style={{
              marginTop: 8,
              fontSize: 12,
              color: 'rgb(var(--mdui-color-on-surface-variant))',
            }}
          >
            {timeLabel}
          </div>
          <div
            style={{
              marginTop: 4,
              fontSize: 12,
              color: 'rgb(var(--mdui-color-on-surface-variant))',
            }}
          >
            {creator?.name ?? '未知'}
            {!todo.isCompleted ? (
              <span style={{ color: PRIORITY_COLOR[todo.priority] }}>
                {' '}· {PRIORITY_LABEL[todo.priority]}
              </span>
            ) : null}
          </div>
        </div>

        {/* 右侧间距 + Checkbox */}
        <div style={{ width: 16 }} />
        <mdui-checkbox
          checked={todo.isCompleted}
          disabled
          style={{ flexShrink: 0 }}
        ></mdui-checkbox>
      </div>
    </mdui-card>
  );
}

// ─── CreateTodoDialog ──────────────────────────────────────

function CreateTodoDialog({
  onDismiss,
  onConfirm,
}: {
  onDismiss: () => void;
  onConfirm: (title: string, description: string, priority: TodoPriority) => void;
}) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<TodoPriority>('NORMAL');
  const [showTitleError, setShowTitleError] = useState(false);
  const [showPriorityDropdown, setShowPriorityDropdown] = useState(false);
  const dialogRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (dialogRef.current && !dialogRef.current.contains(e.target as Node)) {
        onDismiss();
      }
    };
    setTimeout(() => document.addEventListener('mousedown', handler), 0);
    return () => document.removeEventListener('mousedown', handler);
  }, [onDismiss]);

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 2000,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'rgba(0,0,0,0.32)',
      }}
    >
      <div
        ref={dialogRef}
        style={{
          width: '90%',
          maxWidth: 400,
          backgroundColor: 'rgb(var(--mdui-color-surface))',
          borderRadius: 16,
          padding: 24,
        }}
      >
        <div
          style={{
            fontSize: 20,
            fontWeight: 500,
            marginBottom: 16,
            color: 'rgb(var(--mdui-color-on-surface))',
          }}
        >
          创建待办
        </div>

        {/* 标题输入 */}
        <mdui-text-field
          variant="outlined"
          label="标题"
          value={title}
          style={{ width: '100%', marginBottom: 8 }}
          onInput={(e) => {
            setTitle((e.target as HTMLInputElement).value);
            setShowTitleError(false);
          }}
        ></mdui-text-field>
        {showTitleError && (
          <div
            style={{
              fontSize: 12,
              color: 'rgb(var(--mdui-color-error))',
              marginBottom: 8,
              marginTop: -4,
            }}
          >
            标题不能为空
          </div>
        )}

        {/* 描述输入 */}
        <mdui-text-field
          variant="outlined"
          label="描述（可选）"
          value={description}
          style={{ width: '100%', marginBottom: 8 }}
          rows={3}
          onInput={(e) =>
            setDescription((e.target as HTMLInputElement).value)
          }
        ></mdui-text-field>

        {/* 优先级选择 */}
        <div style={{ position: 'relative', marginBottom: 16 }}>
          <mdui-text-field
            variant="outlined"
            label="优先级"
            value={getPriorityLabel(priority)}
            readonly
            style={{ width: '100%', color: getPriorityWebColor(priority) }}
            onClick={() => setShowPriorityDropdown((v) => !v)}
          ></mdui-text-field>

          {showPriorityDropdown && (
            <div
              style={{
                position: 'absolute',
                top: '100%',
                left: 0,
                right: 0,
                zIndex: 10,
                backgroundColor: 'rgb(var(--mdui-color-surface-container))',
                borderRadius: 8,
                boxShadow: '0 4px 16px rgba(0,0,0,0.12)',
                marginTop: 4,
                overflow: 'hidden',
              }}
            >
              {(['LOW', 'NORMAL', 'HIGH'] as TodoPriority[]).map((opt) => (
                <button
                  key={opt}
                  type="button"
                  onClick={() => {
                    setPriority(opt);
                    setShowPriorityDropdown(false);
                  }}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 8,
                    width: '100%',
                    padding: '10px 16px',
                    border: 'none',
                    background: 'transparent',
                    cursor: 'pointer',
                    fontSize: 14,
                    color: getPriorityWebColor(opt),
                    textAlign: 'left',
                  }}
                  onMouseEnter={(e) => {
                    (e.target as HTMLElement).style.backgroundColor =
                      'rgba(var(--mdui-color-on-surface), 0.04)';
                  }}
                  onMouseLeave={(e) => {
                    (e.target as HTMLElement).style.backgroundColor =
                      'transparent';
                  }}
                >
                  {getPriorityLabel(opt)}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* 按钮行 */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'flex-end',
            gap: 8,
          }}
        >
          <mdui-button variant="text" onClick={onDismiss}>
            取消
          </mdui-button>
          <mdui-button
            variant="filled"
            onClick={() => {
              if (title.trim()) {
                onConfirm(title.trim(), description.trim(), priority);
              } else {
                setShowTitleError(true);
              }
            }}
          >
            创建
          </mdui-button>
        </div>
      </div>
    </div>
  );
}

// ─── TodoDetailBottomSheet ─────────────────────────────────

function TodoDetailBottomSheet({
  todo,
  members,
  onDismiss,
}: {
  todo: Todo;
  members: Member[];
  onDismiss: () => void;
}) {
  const creator = members.find((m) => m.id === todo.createdBy);

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 2000,
        display: 'flex',
        alignItems: 'flex-end',
        justifyContent: 'center',
        backgroundColor: 'rgba(0,0,0,0.32)',
      }}
      onClick={onDismiss}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          width: '100%',
          maxWidth: 480,
          backgroundColor: 'rgb(var(--mdui-color-surface-container))',
          borderRadius: '16px 16px 0 0',
          padding: 24,
          paddingBottom: 32,
        }}
      >
        {/* 标题 */}
        <div
          style={{
            fontSize: 20,
            fontWeight: 700,
            marginBottom: 16,
            color: 'rgb(var(--mdui-color-on-surface))',
          }}
        >
          待办详情
        </div>

        {/* 待办标题 */}
        <div
          style={{
            fontSize: 16,
            fontWeight: 500,
            marginBottom: 8,
            color: 'rgb(var(--mdui-color-on-surface))',
          }}
        >
          {todo.title}
        </div>

        {/* 描述 */}
        {todo.description ? (
          <div
            style={{
              fontSize: 14,
              color: 'rgb(var(--mdui-color-on-surface-variant))',
              marginBottom: 16,
            }}
          >
            {todo.description}
          </div>
        ) : null}

        {/* 优先级 */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 8,
            marginBottom: 12,
          }}
        >
          <span
            style={{
              fontSize: 14,
              color: 'rgb(var(--mdui-color-on-surface-variant))',
            }}
          >
            优先级
          </span>
          <span
            style={{
              padding: '4px 8px',
              borderRadius: 12,
              fontSize: 12,
              color:
                todo.priority === 'HIGH'
                  ? 'rgb(var(--mdui-color-on-error))'
                  : todo.priority === 'LOW'
                  ? 'rgb(var(--mdui-color-on-tertiary-container))'
                  : 'rgb(var(--mdui-color-on-secondary-container))',
              backgroundColor:
                todo.priority === 'HIGH'
                  ? 'rgb(var(--mdui-color-error))'
                  : todo.priority === 'LOW'
                  ? 'rgb(var(--mdui-color-tertiary-container))'
                  : 'rgb(var(--mdui-color-secondary-container))',
            }}
          >
            {PRIORITY_LABEL[todo.priority]}
          </span>
        </div>

        {/* 状态 */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 8,
            marginBottom: 16,
          }}
        >
          <span
            style={{
              fontSize: 14,
              color: 'rgb(var(--mdui-color-on-surface-variant))',
            }}
          >
            状态
          </span>
          <span
            style={{
              fontSize: 14,
              fontWeight: 500,
              color: todo.isCompleted
                ? 'rgb(var(--mdui-color-tertiary))'
                : 'rgb(var(--mdui-color-primary))',
            }}
          >
            {todo.isCompleted ? '已完成' : '待完成'}
          </span>
        </div>

        {/* 创建人 */}
        {creator && (
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              marginBottom: 12,
            }}
          >
            <span
              style={{
                fontSize: 14,
                color: 'rgb(var(--mdui-color-on-surface-variant))',
              }}
            >
              创建人
            </span>
            <div
              style={{
                width: 24,
                height: 24,
                borderRadius: '50%',
                overflow: 'hidden',
                backgroundColor: creator.avatarUrl
                  ? 'transparent'
                  : 'rgb(var(--mdui-color-primary))',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'rgb(var(--mdui-color-on-primary))',
                fontSize: 10,
                fontWeight: 700,
              }}
            >
              {creator.avatarUrl ? (
                <img
                  src={creator.avatarUrl}
                  alt={creator.name}
                  style={{
                    width: '100%',
                    height: '100%',
                    objectFit: 'cover',
                  }}
                />
              ) : (
                creator.name.charAt(0)
              )}
            </div>
            <span
              style={{
                fontSize: 14,
                fontWeight: 500,
                color: 'rgb(var(--mdui-color-on-surface))',
              }}
            >
              {creator.name}
            </span>
          </div>
        )}

        {/* 创建时间 */}
        <div
          style={{
            fontSize: 12,
            color: 'rgb(var(--mdui-color-on-surface-variant))',
            marginBottom: 8,
          }}
        >
          创建于 {formatTimestamp(todo.createdAt)}
        </div>

        {/* 完成时间 */}
        {todo.isCompleted && todo.completedAt && (
          <div
            style={{
              fontSize: 12,
              color: 'rgb(var(--mdui-color-on-surface-variant))',
              marginBottom: 16,
            }}
          >
            完成于 {formatTimestamp(todo.completedAt)}
          </div>
        )}

        {!todo.isCompleted && <div style={{ height: 16 }} />}

        {/* 关闭按钮 */}
        <mdui-button
          variant="filled"
          onClick={onDismiss}
          style={{ width: '100%' }}
        >
          关闭
        </mdui-button>
      </div>
    </div>
  );
}

// ─── 辅助函数 ──────────────────────────────────────────────

function getPriorityLabel(priority: TodoPriority): string {
  const map: Record<TodoPriority, string> = {
    LOW: '低优先级',
    NORMAL: '普通优先级',
    HIGH: '高优先级',
  };
  return map[priority];
}

function getPriorityWebColor(priority: TodoPriority): string {
  const map: Record<TodoPriority, string> = {
    LOW: 'rgb(var(--mdui-color-outline))',
    NORMAL: 'rgb(var(--mdui-color-on-surface))',
    HIGH: 'rgb(var(--mdui-color-error))',
  };
  return map[priority];
}