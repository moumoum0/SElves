import { useMemo, useState } from 'react';
import type { NavigateFunction } from 'react-router-dom';
import { MemberHeader } from '../components/MemberHeader';
import { formatDynamicTime, formatTimestamp, formatVoteRemaining } from '../lib/utils';
import type { AppData, Dynamic, Member, MemberDiary, Todo, Vote } from '../types/models';

interface HomePageProps {
  data: AppData;
  currentMember: Member;
  onMemberSwitch: () => void;
  onNavigate: NavigateFunction;
}

const FUNCTION_MODULES = [
  { id: 'todo', label: '待办', icon: 'assignment' },
  { id: 'dynamic', label: '动态', icon: 'timeline' },
  { id: 'vote', label: '投票', icon: 'poll' },
  { id: 'location', label: '轨迹', icon: 'location_on' },
  { id: 'diary', label: '日记', icon: 'menu_book' },
];

type ModuleId = 'functions' | 'location' | 'todo' | 'dynamic' | 'vote' | 'diary';

const DEFAULT_MODULE_ORDER: ModuleId[] = ['functions', 'todo', 'dynamic', 'vote', 'diary', 'location'];

const MODULE_TITLES: Record<ModuleId, string> = {
  functions: '功能模块',
  location: '轨迹记录',
  todo: '待办事项',
  dynamic: '最新动态',
  vote: '投票活动',
  diary: '成员日记',
};

export function HomePage({ data, currentMember, onMemberSwitch, onNavigate }: HomePageProps) {
  const [isEditMode, setIsEditMode] = useState(false);
  const [moduleVisibility, setModuleVisibility] = useState<Record<ModuleId, boolean>>({
    functions: true, location: true, todo: true, dynamic: true, vote: true, diary: true,
  });
  const [moduleOrder, setModuleOrder] = useState<ModuleId[]>(DEFAULT_MODULE_ORDER);
  const [enabledFunctionModules, setEnabledFunctionModules] = useState<Record<string, boolean>>({
    todo: true, dynamic: true, vote: true, location: true, diary: true,
  });

  const visibleModules = useMemo(
    () => moduleOrder.filter((m) => moduleVisibility[m]),
    [moduleOrder, moduleVisibility],
  );

  const pendingTodos = data.todos.filter((t) => !t.isCompleted);
  const todoStats = useMemo(() => {
    const total = data.todos.length;
    const completed = data.todos.filter((t) => t.isCompleted).length;
    const pending = total - completed;
    return { total, completed, pending };
  }, [data.todos]);

  const recentDynamics = data.dynamics.slice(0, 3);
  const activeVotes = data.votes.slice(0, 2);
  const recentDiaries = data.diaries.filter((d) => d.memberId === currentMember.id).slice(0, 3);
  const tracking = data.tracking;

  const handleToggleModule = (id: ModuleId) => {
    setModuleVisibility((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  const handleToggleFunctionModule = (id: string) => {
    setEnabledFunctionModules((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  return (
    <div style={{ backgroundColor: 'rgb(var(--mdui-color-surface))', minHeight: '100%', position: 'relative' }}>
      {/* 编辑模式横幅 */}
      {isEditMode && (
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '8px 16px',
            backgroundColor: 'rgb(var(--mdui-color-primary-container))',
            color: 'rgb(var(--mdui-color-on-primary-container))',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 14 }}>
            <mdui-icon name="edit" style={{ fontSize: 18 }}></mdui-icon>
            <span>编辑模式</span>
          </div>
          <mdui-button variant="text" onClick={() => setIsEditMode(false)} style={{ color: 'rgb(var(--mdui-color-on-primary-container))' }}>
            完成
          </mdui-button>
        </div>
      )}

      <MemberHeader member={currentMember} onMemberSwitch={onMemberSwitch} />

      <div style={{ display: 'flex', flexDirection: 'column', gap: 16, padding: 16 }}>
        {visibleModules.map((moduleId) => {
          switch (moduleId) {
            case 'functions':
              return (
                <FunctionModulesSection
                  key="functions"
                  onNavigate={onNavigate}
                  isEditMode={isEditMode}
                  onEditClick={() => { /* 功能模块编辑弹窗占位 */ }}
                  onModuleToggle={handleToggleFunctionModule}
                  enabledModules={enabledFunctionModules}
                />
              );
            case 'location':
              return (
                <LocationTrackingSection
                  key="location"
                  tracking={tracking}
                  onNavigateToLocation={() => onNavigate('/location')}
                  isEditMode={isEditMode}
                  onEditClick={() => handleToggleModule('location')}
                />
              );
            case 'todo':
              return (
                <TodoSection
                  key="todo"
                  onNavigateToTodo={() => onNavigate('/todo')}
                  pendingTodos={pendingTodos}
                  todoStats={todoStats}
                  isEditMode={isEditMode}
                  onEditClick={() => handleToggleModule('todo')}
                />
              );
            case 'dynamic':
              return (
                <DynamicSection
                  key="dynamic"
                  dynamics={recentDynamics}
                  onNavigateToDynamic={() => onNavigate('/dynamic')}
                  isEditMode={isEditMode}
                  onEditClick={() => handleToggleModule('dynamic')}
                />
              );
            case 'vote':
              return (
                <VoteSection
                  key="vote"
                  votes={activeVotes}
                  onNavigateToVote={() => onNavigate('/vote')}
                  isEditMode={isEditMode}
                  onEditClick={() => handleToggleModule('vote')}
                />
              );
            case 'diary':
              return (
                <DiarySection
                  key="diary"
                  recentDiaries={recentDiaries}
                  onNavigateToDiary={() => onNavigate('/diary')}
                  isEditMode={isEditMode}
                  onEditClick={() => handleToggleModule('diary')}
                />
              );
            default:
              return null;
          }
        })}

        {/* FAB 空白间距 */}
        <div style={{ height: 80 }} />
      </div>

      {/* 编辑模式 FAB */}
      {isEditMode && (
        <div style={{ position: 'fixed', right: 16, bottom: 80, zIndex: 10 }}>
          <mdui-fab icon="edit" onClick={() => { /* 编辑首页布局弹窗占位 */ }}></mdui-fab>
        </div>
      )}
    </div>
  );
}

/* ========== 功能模块区 ========== */
function FunctionModulesSection({
  onNavigate,
  isEditMode,
  onEditClick,
  onModuleToggle,
  enabledModules,
}: {
  onNavigate: NavigateFunction;
  isEditMode: boolean;
  onEditClick: () => void;
  onModuleToggle: (id: string) => void;
  enabledModules: Record<string, boolean>;
}) {
  return (
    <mdui-card
      variant="filled"
      style={{
        padding: 16,
        borderRadius: 12,
        backgroundColor: isEditMode
          ? 'rgba(var(--mdui-color-surface-variant), 0.7)'
          : 'rgb(var(--mdui-color-surface-container))',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 }}>
        <span style={{ fontSize: 18, fontWeight: 600, color: 'rgb(var(--mdui-color-on-surface))' }}>
          {MODULE_TITLES.functions}
        </span>
        {isEditMode && (
          <mdui-button variant="text" onClick={onEditClick} style={{ fontSize: 14, padding: '0 8px' }}>
            <mdui-icon name="settings" style={{ fontSize: 18, marginRight: 4 }}></mdui-icon>
            编辑
          </mdui-button>
        )}
      </div>
      <div style={{ display: 'flex', gap: 16, overflowX: 'auto', paddingBottom: 4, scrollbarWidth: 'none' }}>
        {FUNCTION_MODULES.filter((m) => enabledModules[m.id] !== false).map((m) => (
          <div
            key={m.id}
            onClick={() => {
              if (!isEditMode) onNavigate(`/${m.id}`);
            }}
            style={{
              width: 100,
              minWidth: 100,
              height: 100,
              borderRadius: 12,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 8,
              cursor: isEditMode ? 'default' : 'pointer',
              backgroundColor: 'rgb(var(--mdui-color-surface-container))',
              border: '1px solid rgb(var(--mdui-color-outline-variant))',
              boxSizing: 'border-box',
              flexShrink: 0,
              position: 'relative',
            }}
          >
            <mdui-icon name={m.icon} style={{ fontSize: 32, color: 'rgb(var(--mdui-color-on-surface))' }}></mdui-icon>
            <span style={{ fontSize: 14, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface))' }}>{m.label}</span>
            {isEditMode && (
              <div
                onClick={(e) => {
                  e.stopPropagation();
                  onModuleToggle(m.id);
                }}
                style={{
                  position: 'absolute',
                  top: 4,
                  right: 4,
                  width: 20,
                  height: 20,
                  borderRadius: '50%',
                  backgroundColor: 'rgb(var(--mdui-color-error))',
                  color: 'rgb(var(--mdui-color-on-error))',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  cursor: 'pointer',
                  fontSize: 12,
                }}
              >
                <mdui-icon name="close" style={{ fontSize: 14 }}></mdui-icon>
              </div>
            )}
          </div>
        ))}
      </div>
    </mdui-card>
  );
}

/* ========== 轨迹记录区 ========== */
function LocationTrackingSection({
  tracking,
  onNavigateToLocation,
  isEditMode,
  onEditClick,
}: {
  tracking: AppData['tracking'];
  onNavigateToLocation: () => void;
  isEditMode: boolean;
  onEditClick: () => void;
}) {
  const statusColor = tracking.status === 'RECORDING'
    ? 'rgb(var(--mdui-color-primary))'
    : 'rgb(var(--mdui-color-outline))';
  const statusText = tracking.status === 'RECORDING' ? '记录中' : tracking.status === 'STOPPED' ? '已停止' : '未知';
  const lastTime = tracking.lastRecordTime
    ? new Date(tracking.lastRecordTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    : '--:--';

  return (
    <mdui-card
      variant="filled"
      style={{
        padding: 16,
        borderRadius: 12,
        backgroundColor: isEditMode
          ? 'rgba(var(--mdui-color-surface-variant), 0.7)'
          : 'rgb(var(--mdui-color-surface-container))',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <span style={{ fontSize: 18, fontWeight: 600, color: 'rgb(var(--mdui-color-on-surface))' }}>
          {MODULE_TITLES.location}
        </span>
        <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
          {isEditMode ? (
            <mdui-button-icon icon="close" onClick={onEditClick} style={{ color: 'rgb(var(--mdui-color-error))' }}></mdui-button-icon>
          ) : (
            <>
              <div
                style={{
                  width: 8,
                  height: 8,
                  borderRadius: 4,
                  backgroundColor: statusColor,
                }}
              />
              <span style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>{statusText}</span>
              <mdui-button-icon icon="chevron_right" onClick={onNavigateToLocation} style={{ color: 'rgb(var(--mdui-color-on-surface-variant))' }}></mdui-button-icon>
            </>
          )}
        </div>
      </div>

      {!isEditMode && (
        <div style={{ display: 'flex', justifyContent: 'space-evenly', marginTop: 12 }}>
          <LocationStatItem label="今日" value={String(tracking.todayRecords)} icon="today" />
          <LocationStatItem label="总计" value={String(tracking.totalRecords)} icon="timeline" />
          <LocationStatItem label="最近" value={lastTime} icon="schedule" />
        </div>
      )}
    </mdui-card>
  );
}

function LocationStatItem({ label, value, icon }: { label: string; value: string; icon: string }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4 }}>
      <mdui-icon name={icon} style={{ fontSize: 20, color: 'rgb(var(--mdui-color-primary))' }}></mdui-icon>
      <span style={{ fontSize: 16, fontWeight: 700, color: 'rgb(var(--mdui-color-on-surface))' }}>{value}</span>
      <span style={{ fontSize: 11, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>{label}</span>
    </div>
  );
}

/* ========== 待办事项区 ========== */
function TodoSection({
  onNavigateToTodo,
  pendingTodos,
  todoStats,
  isEditMode,
  onEditClick,
}: {
  onNavigateToTodo: () => void;
  pendingTodos: Todo[];
  todoStats: { total: number; completed: number; pending: number };
  isEditMode: boolean;
  onEditClick: () => void;
}) {
  return (
    <mdui-card
      variant="filled"
      style={{
        padding: 16,
        borderRadius: 12,
        backgroundColor: isEditMode
          ? 'rgba(var(--mdui-color-surface-variant), 0.7)'
          : 'rgb(var(--mdui-color-surface-container))',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <span style={{ fontSize: 18, fontWeight: 600, color: 'rgb(var(--mdui-color-on-surface))' }}>
          {MODULE_TITLES.todo}
        </span>
        {isEditMode ? (
          <mdui-button-icon icon="close" onClick={onEditClick} style={{ color: 'rgb(var(--mdui-color-error))' }}></mdui-button-icon>
        ) : (
          <mdui-button-icon icon="chevron_right" onClick={onNavigateToTodo} style={{ color: 'rgb(var(--mdui-color-on-surface-variant))' }}></mdui-button-icon>
        )}
      </div>

      {!isEditMode && (
        <div style={{ marginTop: 8 }}>
          {pendingTodos.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '24px 0', color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
              <mdui-icon name="assignment" style={{ fontSize: 48, opacity: 0.5, display: 'block', margin: '0 auto 12px' }}></mdui-icon>
              <div style={{ fontSize: 15 }}>暂无待办</div>
              <div style={{ fontSize: 13, opacity: 0.6, marginTop: 6 }}>点击创建待办事项</div>
            </div>
          ) : (
            <>
              {pendingTodos.slice(0, 2).map((todo) => (
                <TodoItem key={todo.id} todo={todo} />
              ))}
              {pendingTodos.length > 2 && (
                <div
                  onClick={onNavigateToTodo}
                  style={{ fontSize: 14, color: 'rgb(var(--mdui-color-primary))', marginTop: 8, cursor: 'pointer' }}
                >
                  还有 {pendingTodos.length - 2} 项待办
                </div>
              )}
            </>
          )}
        </div>
      )}
    </mdui-card>
  );
}

function TodoItem({ todo }: { todo: Todo }) {
  const priorityBg =
    todo.priority === 'HIGH'
      ? 'rgb(var(--mdui-color-error))'
      : todo.priority === 'LOW'
        ? 'rgb(var(--mdui-color-tertiary))'
        : 'transparent';
  const priorityText =
    todo.priority === 'HIGH'
      ? 'rgb(var(--mdui-color-on-error))'
      : todo.priority === 'LOW'
        ? 'rgb(var(--mdui-color-on-tertiary))'
        : 'rgb(var(--mdui-color-on-surface))';
  const priorityLabel = todo.priority === 'HIGH' ? '高' : todo.priority === 'LOW' ? '低' : '普通';

  return (
    <div style={{ display: 'flex', alignItems: 'center', padding: '8px 0' }}>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div
          style={{
            fontSize: 16,
            color: todo.isCompleted
              ? 'rgba(var(--mdui-color-on-surface), 0.6)'
              : 'rgb(var(--mdui-color-on-surface))',
            textDecoration: todo.isCompleted ? 'line-through' : 'none',
          }}
        >
          {todo.title}
        </div>
        {todo.description && (
          <div
            style={{
              fontSize: 14,
              color: 'rgba(var(--mdui-color-on-surface-variant), 0.7)',
              marginTop: 2,
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
            }}
          >
            {todo.description}
          </div>
        )}
      </div>

      {todo.priority !== 'NORMAL' && (
        <div
          style={{
            backgroundColor: priorityBg,
            color: priorityText,
            fontSize: 12,
            padding: '2px 6px',
            borderRadius: 4,
            marginLeft: 8,
            flexShrink: 0,
          }}
        >
          {priorityLabel}
        </div>
      )}

      <mdui-checkbox checked={todo.isCompleted} disabled style={{ flexShrink: 0 }}></mdui-checkbox>
    </div>
  );
}

/* ========== 最新动态区 ========== */
function DynamicSection({
  dynamics,
  onNavigateToDynamic,
  isEditMode,
  onEditClick,
}: {
  dynamics: Dynamic[];
  onNavigateToDynamic: () => void;
  isEditMode: boolean;
  onEditClick: () => void;
}) {
  return (
    <mdui-card
      variant="filled"
      style={{
        padding: 16,
        borderRadius: 12,
        backgroundColor: isEditMode
          ? 'rgba(var(--mdui-color-surface-variant), 0.7)'
          : 'rgb(var(--mdui-color-surface-container))',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <span style={{ fontSize: 18, fontWeight: 600, color: 'rgb(var(--mdui-color-on-surface))' }}>
          {MODULE_TITLES.dynamic}
        </span>
        {isEditMode ? (
          <mdui-button-icon icon="close" onClick={onEditClick} style={{ color: 'rgb(var(--mdui-color-error))' }}></mdui-button-icon>
        ) : (
          <mdui-button-icon icon="chevron_right" onClick={onNavigateToDynamic} style={{ color: 'rgb(var(--mdui-color-on-surface-variant))' }}></mdui-button-icon>
        )}
      </div>

      {!isEditMode && (
        <div style={{ marginTop: 8 }}>
          {dynamics.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '24px 0', color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
              <mdui-icon name="timeline" style={{ fontSize: 48, opacity: 0.5, display: 'block', margin: '0 auto 12px' }}></mdui-icon>
              <div style={{ fontSize: 15 }}>暂无动态</div>
              <div style={{ fontSize: 13, opacity: 0.6, marginTop: 6 }}>点击创建动态</div>
            </div>
          ) : (
            <>
              {dynamics.map((dynamic) => (
                <DynamicItem key={dynamic.id} dynamic={dynamic} />
              ))}
              {dynamics.length >= 3 && (
                <div
                  onClick={onNavigateToDynamic}
                  style={{ fontSize: 14, color: 'rgb(var(--mdui-color-primary))', marginTop: 8, cursor: 'pointer' }}
                >
                  查看更多
                </div>
              )}
            </>
          )}
        </div>
      )}
    </mdui-card>
  );
}

function DynamicItem({ dynamic }: { dynamic: Dynamic }) {
  const timeText = formatDynamicTime(dynamic.createdAt) ?? '--';
  return (
    <div style={{ padding: '8px 0' }}>
      <div style={{ fontSize: 16, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface))' }}>
        {dynamic.title || '无标题'}
      </div>
      <div style={{ fontSize: 14, color: 'rgba(var(--mdui-color-on-surface), 0.7)', marginTop: 4 }}>
        {dynamic.content}
      </div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: 4 }}>
        <span style={{ fontSize: 12, color: 'rgba(var(--mdui-color-on-surface), 0.5)' }}>{timeText}</span>
        {dynamic.authorName && (
          <span style={{ fontSize: 12, color: 'rgb(var(--mdui-color-primary))' }}>
            作者: {dynamic.authorName}
          </span>
        )}
      </div>
    </div>
  );
}

/* ========== 投票活动区 ========== */
function VoteSection({
  votes,
  onNavigateToVote,
  isEditMode,
  onEditClick,
}: {
  votes: Vote[];
  onNavigateToVote: () => void;
  isEditMode: boolean;
  onEditClick: () => void;
}) {
  return (
    <mdui-card
      variant="filled"
      style={{
        padding: 16,
        borderRadius: 12,
        backgroundColor: isEditMode
          ? 'rgba(var(--mdui-color-surface-variant), 0.7)'
          : 'rgb(var(--mdui-color-surface-container))',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <span style={{ fontSize: 18, fontWeight: 600, color: 'rgb(var(--mdui-color-on-surface))' }}>
          {MODULE_TITLES.vote}
        </span>
        {isEditMode ? (
          <mdui-button-icon icon="close" onClick={onEditClick} style={{ color: 'rgb(var(--mdui-color-error))' }}></mdui-button-icon>
        ) : (
          <mdui-button-icon icon="chevron_right" onClick={onNavigateToVote} style={{ color: 'rgb(var(--mdui-color-on-surface-variant))' }}></mdui-button-icon>
        )}
      </div>

      {!isEditMode && (
        <div style={{ marginTop: 8 }}>
          {votes.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '24px 0', color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
              <mdui-icon name="poll" style={{ fontSize: 48, opacity: 0.5, display: 'block', margin: '0 auto 12px' }}></mdui-icon>
              <div style={{ fontSize: 15 }}>暂无投票</div>
              <div style={{ fontSize: 13, opacity: 0.6, marginTop: 6 }}>点击创建投票</div>
            </div>
          ) : (
            <>
              {votes.map((vote) => (
                <VoteItem key={vote.id} vote={vote} />
              ))}
              {votes.length >= 2 && (
                <div
                  onClick={onNavigateToVote}
                  style={{ fontSize: 14, color: 'rgb(var(--mdui-color-primary))', marginTop: 8, cursor: 'pointer' }}
                >
                  查看更多
                </div>
              )}
            </>
          )}
        </div>
      )}
    </mdui-card>
  );
}

function VoteItem({ vote }: { vote: Vote }) {
  const endTimeText = vote.endTime ? formatVoteRemaining(vote.endTime) : '无截止时间';
  return (
    <div style={{ padding: '8px 0' }}>
      <div style={{ fontSize: 16, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface))' }}>{vote.title}</div>
      <div
        style={{
          fontSize: 14,
          color: 'rgba(var(--mdui-color-on-surface), 0.7)',
          marginTop: 4,
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          display: '-webkit-box',
          WebkitLineClamp: 2,
          WebkitBoxOrient: 'vertical',
        }}
      >
        {vote.description}
      </div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: 4 }}>
        <span style={{ fontSize: 12, color: 'rgb(var(--mdui-color-primary))' }}>{endTimeText}</span>
        <span style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
          总票数: {vote.totalVotes}
        </span>
      </div>
    </div>
  );
}

/* ========== 成员日记区 ========== */
function DiarySection({
  recentDiaries,
  onNavigateToDiary,
  isEditMode,
  onEditClick,
}: {
  recentDiaries: MemberDiary[];
  onNavigateToDiary: () => void;
  isEditMode: boolean;
  onEditClick: () => void;
}) {
  return (
    <mdui-card
      variant="filled"
      style={{
        padding: 16,
        borderRadius: 12,
        backgroundColor: isEditMode
          ? 'rgba(var(--mdui-color-surface-variant), 0.7)'
          : 'rgb(var(--mdui-color-surface-container))',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <span style={{ fontSize: 18, fontWeight: 600, color: 'rgb(var(--mdui-color-on-surface))' }}>
          {MODULE_TITLES.diary}
        </span>
        {isEditMode ? (
          <mdui-button-icon icon="close" onClick={onEditClick} style={{ color: 'rgb(var(--mdui-color-error))' }}></mdui-button-icon>
        ) : (
          <mdui-button-icon icon="chevron_right" onClick={onNavigateToDiary} style={{ color: 'rgb(var(--mdui-color-on-surface-variant))' }}></mdui-button-icon>
        )}
      </div>

      {!isEditMode && (
        <div style={{ marginTop: 8 }}>
          {recentDiaries.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '24px 0', color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
              <mdui-icon name="menu_book" style={{ fontSize: 48, opacity: 0.5, display: 'block', margin: '0 auto 12px' }}></mdui-icon>
              <div style={{ fontSize: 15 }}>暂无日记</div>
              <div style={{ fontSize: 13, opacity: 0.6, marginTop: 6 }}>点击新建日记</div>
            </div>
          ) : (
            <>
              {recentDiaries.map((diary, index) => (
                <div key={diary.id}>
                  <div style={{ padding: '8px 0' }}>
                    {diary.title && (
                      <div
                        style={{
                          fontSize: 15,
                          fontWeight: 500,
                          color: 'rgb(var(--mdui-color-on-surface))',
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          whiteSpace: 'nowrap',
                        }}
                      >
                        {diary.title}
                      </div>
                    )}
                    <div
                      style={{
                        fontSize: 14,
                        color: 'rgba(var(--mdui-color-on-surface), 0.7)',
                        marginTop: diary.title ? 2 : 0,
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        display: '-webkit-box',
                        WebkitLineClamp: 2,
                        WebkitBoxOrient: 'vertical',
                      }}
                    >
                      {diary.content}
                    </div>
                    <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))', marginTop: 4 }}>
                      {formatTimestamp(diary.createdAt)}
                    </div>
                  </div>
                  {index < recentDiaries.length - 1 && (
                    <div
                      style={{
                        height: 1,
                        backgroundColor: 'rgb(var(--mdui-color-outline-variant))',
                      }}
                    />
                  )}
                </div>
              ))}
              {recentDiaries.length >= 2 && (
                <div
                  onClick={onNavigateToDiary}
                  style={{ fontSize: 14, color: 'rgb(var(--mdui-color-primary))', marginTop: 8, cursor: 'pointer' }}
                >
                  查看更多
                </div>
              )}
            </>
          )}
        </div>
      )}
    </mdui-card>
  );
}
