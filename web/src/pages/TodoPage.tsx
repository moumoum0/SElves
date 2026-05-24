import { useState } from 'react';
import { SubPageScaffold } from './SubPageScaffold';
import type { Member, Todo, TodoPriority } from '../types/models';
import { formatTimestamp } from '../lib/utils';

interface TodoPageProps {
  todos: Todo[];
  members: Member[];
  currentMember: Member;
  onBack: () => void;
}

const PRIORITY_LABEL: Record<TodoPriority, string> = { HIGH: '高', NORMAL: '普通', LOW: '低' };
const PRIORITY_COLOR: Record<TodoPriority, string> = {
  HIGH: 'rgb(var(--mdui-color-error))',
  NORMAL: 'rgb(var(--mdui-color-on-surface-variant))',
  LOW: 'rgb(var(--mdui-color-tertiary))',
};

export function TodoPage({ todos, members, onBack }: TodoPageProps) {
  const [showPendingTodos, setShowPendingTodos] = useState(true);
  const [showCompletedTodos, setShowCompletedTodos] = useState(false);

  const pendingTodos = todos.filter((todo) => !todo.isCompleted);
  const completedTodos = todos.filter((todo) => todo.isCompleted);
  const stats = {
    total: todos.length,
    pending: pendingTodos.length,
    completed: completedTodos.length,
  };

  return (
    <SubPageScaffold title="待办事项" onBack={onBack}>
      {stats.total > 0 ? (
        <mdui-card
          variant="filled"
          style={{
            padding: 16,
            borderRadius: 16,
            backgroundColor: 'rgb(var(--mdui-color-surface-container))',
            marginBottom: 16,
          }}
        >
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, minmax(0, 1fr))', gap: 8 }}>
            <StatBlock title="总计" value={stats.total} color="rgb(var(--mdui-color-primary))" />
            <StatBlock title="待完成" value={stats.pending} color="rgb(var(--mdui-color-secondary))" />
            <StatBlock title="已完成" value={stats.completed} color="rgb(var(--mdui-color-tertiary))" />
          </div>
        </mdui-card>
      ) : null}

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
          <mdui-icon name="assignment" style={{ fontSize: 64, opacity: 0.5, marginBottom: 16 }}></mdui-icon>
          <div style={{ fontSize: 16 }}>暂无待办事项</div>
          <div style={{ fontSize: 14, opacity: 0.6, marginTop: 8 }}>点击右下角添加第一条待办</div>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          {pendingTodos.length > 0 ? (
            <div>
              <button
                type="button"
                onClick={() => setShowPendingTodos((value) => !value)}
                style={{
                  width: '100%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  padding: '8px 0',
                  background: 'transparent',
                  border: 'none',
                  cursor: 'pointer',
                }}
              >
                <span style={{ fontSize: 16, fontWeight: 600, color: 'rgb(var(--mdui-color-on-surface))' }}>
                  待完成 ({pendingTodos.length})
                </span>
                <mdui-icon
                  name="keyboard_arrow_down"
                  style={{
                    color: 'rgb(var(--mdui-color-primary))',
                    transform: showPendingTodos ? 'rotate(180deg)' : 'rotate(0deg)',
                    transition: 'transform 0.2s ease',
                  }}
                ></mdui-icon>
              </button>
              {showPendingTodos ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  {pendingTodos.map((todo) => (
                    <TodoCard key={todo.id} todo={todo} members={members} />
                  ))}
                </div>
              ) : null}
            </div>
          ) : null}

          {completedTodos.length > 0 ? (
            <div>
              <button
                type="button"
                onClick={() => setShowCompletedTodos((value) => !value)}
                style={{
                  width: '100%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  padding: '8px 0',
                  background: 'transparent',
                  border: 'none',
                  cursor: 'pointer',
                }}
              >
                <span style={{ fontSize: 16, fontWeight: 600, color: 'rgb(var(--mdui-color-on-surface))' }}>
                  已完成 ({completedTodos.length})
                </span>
                <mdui-icon
                  name="keyboard_arrow_down"
                  style={{
                    color: 'rgb(var(--mdui-color-primary))',
                    transform: showCompletedTodos ? 'rotate(180deg)' : 'rotate(0deg)',
                    transition: 'transform 0.2s ease',
                  }}
                ></mdui-icon>
              </button>
              {showCompletedTodos ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  {completedTodos.map((todo) => (
                    <TodoCard key={todo.id} todo={todo} members={members} />
                  ))}
                </div>
              ) : null}
            </div>
          ) : null}
        </div>
      )}

      <mdui-fab icon="add" style={{ position: 'fixed', right: 24, bottom: 24 }}></mdui-fab>
    </SubPageScaffold>
  );
}

function StatBlock({ title, value, color }: { title: string; value: number; color: string }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
      <span style={{ fontSize: 22, lineHeight: 1.2, fontWeight: 700, color }}>{value}</span>
      <span style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>{title}</span>
    </div>
  );
}

function TodoCard({ todo, members }: { todo: Todo; members: Member[] }) {
  const creator = members.find((member) => member.id === todo.createdBy);
  const timeLabel = todo.isCompleted
    ? `完成于 ${formatTimestamp(todo.completedAt ?? todo.createdAt)}`
    : `创建于 ${formatTimestamp(todo.createdAt)}`;

  return (
    <mdui-card variant="filled" style={{ borderRadius: 12, padding: 16, boxShadow: 'none' }}>
      <div style={{ display: 'flex', alignItems: 'flex-start', width: '100%' }}>
        <div style={{ flex: 1, minWidth: 0 }}>
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
            {todo.priority !== 'NORMAL' && !todo.isCompleted ? (
              <span
                style={{
                  padding: '2px 6px',
                  borderRadius: 12,
                  fontSize: 10,
                  lineHeight: 1.2,
                  color: todo.priority === 'HIGH' ? 'rgb(var(--mdui-color-on-error))' : 'rgb(var(--mdui-color-on-tertiary-container))',
                  backgroundColor:
                    todo.priority === 'HIGH'
                      ? 'rgb(var(--mdui-color-error))'
                      : 'rgb(var(--mdui-color-tertiary-container))',
                }}
              >
                {PRIORITY_LABEL[todo.priority]}
              </span>
            ) : null}
          </div>

          {todo.description ? (
            <div
              style={{
                marginTop: 4,
                fontSize: 14,
                lineHeight: 1.45,
                color: todo.isCompleted
                  ? 'rgba(var(--mdui-color-on-surface), 0.4)'
                  : 'rgba(var(--mdui-color-on-surface), 0.7)',
              }}
            >
              {todo.description}
            </div>
          ) : null}

          <div style={{ marginTop: 8, fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
            {timeLabel}
          </div>
          <div style={{ marginTop: 4, fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
            {creator?.name ?? '未知'}
            {!todo.isCompleted ? (
              <span style={{ color: PRIORITY_COLOR[todo.priority] }}> · {PRIORITY_LABEL[todo.priority]}</span>
            ) : null}
          </div>
        </div>

        <div style={{ width: 16 }} />
        <mdui-checkbox checked={todo.isCompleted} disabled style={{ flexShrink: 0 }}></mdui-checkbox>
      </div>
    </mdui-card>
  );
}
