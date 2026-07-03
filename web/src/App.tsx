import { useRef, useEffect, useMemo, useState, useCallback } from 'react';
import { Navigate, Route, Routes, useLocation, useNavigate, useParams } from 'react-router-dom';
import { useSelvesData } from './hooks/useSelvesData';
import {
  fetchJson,
  postJson,
  putJson,
  deleteApi,
  getApiToken,
  setApiToken,
  createTodo,
  updateTodoStatus,
  deleteTodo,
  getDynamicComments,
  createDynamicComment,
  deleteDynamicComment,
  getVoteRecords,
  castVote,
} from './lib/api';
import { ChatDetailPage } from './pages/ChatDetailPage';
import { CreateDynamicPage } from './pages/CreateDynamicPage';
import { DiaryPage } from './pages/DiaryPage';
import { DynamicDetailPage } from './pages/DynamicDetailPage';
import { DynamicPage } from './pages/DynamicPage';
import { GroupChatPage } from './pages/GroupChatPage';
import { HomePage } from './pages/HomePage';
import { LocationPage } from './pages/LocationPage';
import { MemberManagementPage } from './pages/MemberManagementPage';
import { OnlineStatsPage } from './pages/OnlineStatsPage';
import { SettingsPage } from './pages/SettingsPage';
import { SystemPage } from './pages/SystemPage';
import { AboutPage } from './pages/AboutPage';
import { DeveloperModePage } from './pages/DeveloperModePage';
import { WelcomeGuidePage } from './pages/WelcomeGuidePage';
import { TodoPage } from './pages/TodoPage';
import { VotePage } from './pages/VotePage';
import { CreateVotePage } from './pages/CreateVotePage';
import { VoteDetailPage } from './pages/VoteDetailPage';
import type { AppData, DynamicComment, Member, VoteRecord } from './types/models';

const MAIN_TABS = ['/', '/chat', '/system'];

// 判断路径是否为主 Tab
function isMainTab(path: string) {
  return MAIN_TABS.includes(path);
}

// 路径深度（用于启发式判断导航方向）
function pathDepth(path: string) {
  return path.split('/').filter(Boolean).length;
}

type NavDirection = 'forward' | 'back' | 'tab-forward' | 'tab-back' | 'none';

// Tab index 映射（与安卓 getTabIndex 对齐）
const TAB_INDEX: Record<string, number> = { '/': 0, '/chat': 1, '/system': 2 };

// 推断导航方向
function inferDirection(prev: string, next: string): NavDirection {
  if (prev === next) return 'none';
  const prevIsMain = isMainTab(prev);
  const nextIsMain = isMainTab(next);
  // 主 Tab 之间：基于 index 方向滑动
  if (prevIsMain && nextIsMain) {
    return (TAB_INDEX[next] ?? 0) > (TAB_INDEX[prev] ?? 0) ? 'tab-forward' : 'tab-back';
  }
  // 从主 Tab 进入子页面
  if (prevIsMain && !nextIsMain) return 'forward';
  // 从子页面回到主 Tab
  if (!prevIsMain && nextIsMain) return 'back';
  // 两个子页面之间：比较深度
  const d = pathDepth(next) - pathDepth(prev);
  if (d > 0) return 'forward';
  if (d < 0) return 'back';
  // 同级跳转（如 /dynamic → /vote），视为 forward
  return 'forward';
}

export default function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const navBarRef = useRef<HTMLElement>(null);
  const { data, loading, error, baseUrl, currentMember, setCurrentMember, reload } = useSelvesData();
  const [showTokenDialog, setShowTokenDialog] = useState(false);
  const [tokenInput, setTokenInput] = useState('');

  const currentTab = useMemo(() => {
    if (location.pathname.startsWith('/chat')) return '/chat';
    if (location.pathname === '/system') return '/system';
    return '/';
  }, [location.pathname]);

  // --- 页面过渡动画状态 ---
  const prevLocationRef = useRef(location);
  const [displayLocation, setDisplayLocation] = useState(location);
  const [exitLocation, setExitLocation] = useState(location);
  const [transitionDirection, setTransitionDirection] = useState<NavDirection>('none');
  const [isTransitioning, setIsTransitioning] = useState(false);

  useEffect(() => {
    const prev = prevLocationRef.current;
    if (prev.pathname !== location.pathname) {
      const dir = inferDirection(prev.pathname, location.pathname);
      if (dir === 'none') {
        // 无动画，直接切换
        setDisplayLocation(location);
        setIsTransitioning(false);
        setTransitionDirection('none');
      } else {
        // 触发过渡动画
        setExitLocation(prev);
        setTransitionDirection(dir);
        setIsTransitioning(true);
        setDisplayLocation(location);
      }
      prevLocationRef.current = location;
    }
  }, [location]);

  const handleTransitionEnd = useCallback(() => {
    setIsTransitioning(false);
    setTransitionDirection('none');
  }, []);

  const showNavBar = isMainTab(location.pathname);

  const [developerModeArmed, setDeveloperModeArmed] = useState(false);

  const handleMemberSwitch = () => {
    if (!data || !currentMember) return;
    const idx = data.members.findIndex((m: Member) => m.id === currentMember.id);
    const next = data.members[(idx + 1) % data.members.length];
    setCurrentMember(next.id);
  };

  const handleCreateMember = useCallback((name: string, bio: string, pronouns: string, _groups: string[]) => {
    postJson('/api/members', { name, bio, pronouns }).then(() => void reload()).catch(() => {});
  }, [reload]);

  // 检查 token 是否存在，首次访问时弹窗提示
  useEffect(() => {
    const token = getApiToken();
    const hasConfiguredToken = localStorage.getItem('selves-token-configured');

    // 只有当用户已配置过有效 token 时才不再提示
    if (!token && !hasConfiguredToken && !loading) {
      setShowTokenDialog(true);
    }
  }, [loading]);

  const handleTokenSubmit = () => {
    const trimmedToken = tokenInput.trim().toUpperCase();
    if (trimmedToken.length === 6) {
      setApiToken(trimmedToken);
      // 标记用户已配置过 token
      localStorage.setItem('selves-token-configured', 'true');
      setShowTokenDialog(false);
      setTokenInput('');
      // 刷新数据以测试 token
      reload();
    }
  };

  const handleTokenSkip = () => {
    setShowTokenDialog(false);
    setTokenInput('');
  };

  useEffect(() => {
    const el = navBarRef.current;
    if (!el) return;
    const handler = (e: Event) => {
      const val = (e.target as HTMLElement & { value: string }).value;
      navigate(val);
    };
    el.addEventListener('change', handler);
    return () => el.removeEventListener('change', handler);
  }, [navigate, loading, showNavBar]);

  const routeContent = (loc: typeof location) => (
    <Routes location={loc}>
      <Route
        path="/"
        element={
          currentMember && data ? (
            <HomePage
              data={data}
              currentMember={currentMember}
              onMemberSwitch={handleMemberSwitch}
              onNavigate={navigate}
              onCreateMember={handleCreateMember}
            />
          ) : <div />
        }
      />
      <Route
        path="/chat"
        element={
          currentMember && data ? (
            <GroupChatPage
              currentMember={currentMember}
              members={data.members}
              groups={data.groups}
              groupMessages={data.groupMessages}
              unreadCounts={data.unreadCounts}
              onMemberSwitch={handleMemberSwitch}
              onMemberSelected={(m) => setCurrentMember(m.id)}
              onOpenGroup={(id: string) => navigate(`/chat/${id}`)}
              onCreateMember={handleCreateMember}
            />
          ) : <div />
        }
      />
      <Route
        path="/chat/:groupId"
        element={
          currentMember && data ? (
            <ChatDetailRoute
              data={data}
              currentMember={currentMember}
              onBack={() => navigate('/chat')}
              developerModeArmed={developerModeArmed}
              onDeveloperModeArm={() => setDeveloperModeArmed(false)}
              onNavigateDeveloper={() => navigate('/developer-mode')}
            />
          ) : <div />
        }
      />
      <Route
        path="/system"
        element={
          data ? (
            <SystemPage
              system={data.system}
              onNavigateMemberManagement={() => navigate('/member-management')}
              onNavigateOnlineStats={() => navigate('/online-stats')}
              onNavigateSettings={() => navigate('/settings')}
            />
          ) : <div />
        }
      />
      <Route
        path="/todo"
        element={
          data && currentMember ? (
            <TodoPage
              todos={data.todos}
              members={data.members}
              currentMember={currentMember}
              onBack={() => navigate('/')}
              onCreateTodo={(title, description, priority) => {
                createTodo({ title, description, priority, createdBy: currentMember.id })
                  .then(() => void reload())
                  .catch(() => {});
              }}
              onToggleTodo={(todo, isCompleted) => {
                updateTodoStatus(todo.id, isCompleted)
                  .then(() => void reload())
                  .catch(() => {});
              }}
              onDeleteTodo={(todo) => {
                deleteTodo(todo.id)
                  .then(() => void reload())
                  .catch(() => {});
              }}
            />
          ) : <div />
        }
      />
      <Route path="/dynamic" element={data && currentMember ? <DynamicPage dynamics={data.dynamics} currentMember={currentMember} onBack={() => navigate('/')} onDynamicClick={(id: string) => navigate(`/dynamic/${id}`)} onNavigateToCreateDynamic={() => navigate('/dynamic/create')} /> : <div />} />
      <Route path="/dynamic/create" element={data && currentMember ? <CreateDynamicPage currentMember={currentMember} onBack={() => navigate('/dynamic')} onSubmit={(params) => { postJson('/api/dynamics', { title: params.title, content: params.content, authorId: currentMember.id, authorName: params.authorName, authorAvatar: params.authorAvatar, images: params.images, tags: params.tags }).then(() => navigate('/dynamic')).catch(() => {}); }} /> : <div />} />
      <Route path="/dynamic/:dynamicId" element={data && currentMember ? <DynamicDetailRoute data={data} currentMember={currentMember} onBack={() => navigate('/dynamic')} reload={reload} /> : <div />} />
      <Route path="/vote" element={data && currentMember ? <VotePage votes={data.votes} currentMember={currentMember} onBack={() => navigate('/')} onVoteClick={(id: string) => navigate(`/vote/${id}`)} onNavigateToCreateVote={() => navigate('/vote/create')} /> : <div />} />
      <Route path="/vote/create" element={data && currentMember ? <CreateVotePage currentMember={currentMember} onBack={() => navigate('/vote')} onSubmit={(params) => { postJson('/api/votes', { title: params.title, description: params.description, authorId: currentMember.id, authorName: params.authorName, authorAvatar: params.authorAvatar, options: params.options, allowMultipleChoice: params.allowMultipleChoice, isAnonymous: params.isAnonymous }).then(() => navigate('/vote')).catch(() => {}); }} /> : <div />} />
      <Route path="/vote/:voteId" element={data && currentMember ? <VoteDetailRoute data={data} currentMember={currentMember} onBack={() => navigate('/vote')} reload={reload} /> : <div />} />
      <Route path="/diary" element={data && currentMember ? <DiaryPage diaries={data.diaries} currentMember={currentMember} onBack={() => navigate('/')} onCreateDiary={(title, content) => { postJson('/api/diaries', { memberId: currentMember.id, title, content }).catch(() => {}); }} onDeleteDiary={(id) => { deleteApi(`/api/diaries/${id}`).then(() => void reload()).catch(() => {}); }} /> : <div />} />
      <Route path="/location" element={data && currentMember ? <LocationPage tracking={data.tracking} currentMember={currentMember} onBack={() => navigate('/')} /> : <div />} />
      <Route path="/member-management" element={data && currentMember ? <MemberManagementPage members={data.members} currentMember={currentMember} onBack={() => navigate('/system')} onCreateMember={handleCreateMember} onEditMember={(id, name, bio, pronouns) => { putJson(`/api/members/${id}`, { name, bio, pronouns }).then(() => void reload()).catch(() => {}); }} onDeleteMember={(id) => { deleteApi(`/api/members/${id}`).then(() => void reload()).catch(() => {}); }} /> : <div />} />
      <Route path="/online-stats" element={data && currentMember ? <OnlineStatsPage members={data.members} currentMember={currentMember} onBack={() => navigate('/system')} /> : <div />} />
      <Route path="/settings" element={<SettingsPage baseUrl={baseUrl} onBack={() => navigate('/system')} onNavigateToAbout={() => navigate('/about')} />} />
      <Route path="/about" element={<AboutPage onBack={() => navigate('/system')} onDeveloperModeUnlocked={() => setDeveloperModeArmed(true)} />} />
      <Route path="/developer-mode" element={<DeveloperModePage onBack={() => navigate('/about')} />} />
      <Route
        path="/welcome"
        element={
          <WelcomeGuidePage
            onCreateSystem={(name, avatarUrl) => {
              // TODO: 调用 API 创建系统
              console.log('Create system:', name, avatarUrl);
            }}
            onCreateMember={(name, avatarUrl) => {
              // TODO: 调用 API 创建成员
              console.log('Create member:', name, avatarUrl);
            }}
            onImportBackup={() => {
              // TODO: 触发备份导入
              console.log('Import backup');
            }}
            onCompleteGuide={() => {
              navigate('/');
            }}
          />
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );

  return (
    <div className="app-shell">
      <div className="app-card">
        {loading ? (
          <div
            style={{
              flex: 1,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              backgroundColor: 'rgb(var(--mdui-color-background))',
            }}
          >
            <img
              src="/selves-app-icon.png"
              alt="Selves App Icon"
              style={{
                width: 120,
                height: 120,
                borderRadius: 12,
                display: 'block',
              }}
            />
          </div>
        ) : (
          <>
            {error && (
              <div style={{
                display: 'flex', alignItems: 'center', gap: 8, padding: '10px 16px',
                backgroundColor: 'rgb(var(--mdui-color-error-container))',
                color: 'rgb(var(--mdui-color-on-error-container))',
                fontSize: 13,
              }}>
                <mdui-icon name="error" style={{ fontSize: 18 }}></mdui-icon>
                <span style={{ flex: 1 }}>{`数据加载异常：${error}`}</span>
                <mdui-button-icon icon="refresh" onClick={() => void reload()}></mdui-button-icon>
              </div>
            )}

            <div className="page-transition-container">
              {isTransitioning && transitionDirection !== 'none' ? (
                <>
                  {/* 新页面层 */}
                  <div
                    className={`page-transition-layer enter-${transitionDirection}`}
                    onAnimationEnd={transitionDirection === 'forward' || transitionDirection === 'tab-forward' || transitionDirection === 'tab-back' ? handleTransitionEnd : undefined}
                  >
                    {routeContent(displayLocation)}
                  </div>
                  {/* 旧页面层（退出动画） */}
                  <div
                    className={`page-transition-layer exit-${transitionDirection}`}
                    onAnimationEnd={transitionDirection === 'back' ? handleTransitionEnd : undefined}
                  >
                    {routeContent(exitLocation)}
                  </div>
                  {/* Scrim 暗化层（仅前进时显示在旧页面上方、新页面下方） */}
                  {transitionDirection === 'forward' && (
                    <div className="page-scrim scrim-enter" style={{ zIndex: 2 }} />
                  )}
                </>
              ) : (
                <div className="page-transition-layer no-transition">
                  {routeContent(displayLocation)}
                </div>
              )}
            </div>

            {showNavBar && (
              <mdui-navigation-bar ref={navBarRef} value={currentTab} label-visibility="selected">
                <mdui-navigation-bar-item icon="home" value="/">首页</mdui-navigation-bar-item>
                <mdui-navigation-bar-item icon="chat" value="/chat">群聊</mdui-navigation-bar-item>
                <mdui-navigation-bar-item icon="manage_accounts" value="/system">系统</mdui-navigation-bar-item>
              </mdui-navigation-bar>
            )}

            {/* Token 配置弹窗 */}
            {showTokenDialog && (
              <mdui-dialog open headline="配置访问令牌" close-on-esc={false} close-on-overlay-click={false}>
                <div style={{ padding: '0 24px 20px', display: 'flex', flexDirection: 'column', gap: 20 }}>
                  {/* 说明文字 */}
                  <div style={{ display: 'flex', gap: 12, alignItems: 'flex-start' }}>
                    <mdui-icon name="lock" style={{ fontSize: 20, color: 'rgb(var(--mdui-color-primary))', marginTop: 2, flexShrink: 0 }} />
                    <div style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))', lineHeight: 1.6 }}>
                      访问令牌用于验证身份，必须配置后才能使用
                    </div>
                  </div>

                  {/* Token 输入框 */}
                  <mdui-text-field
                    label="访问令牌"
                    placeholder="A3B7K9"
                    value={tokenInput}
                    onInput={(e) => setTokenInput((e.target as HTMLInputElement).value.trim().toUpperCase())}
                    maxlength={6}
                    style={{ width: '100%', textTransform: 'uppercase', letterSpacing: '2px', fontWeight: 600 }}
                  />

                  {/* 获取步骤卡片 */}
                  <mdui-card variant="outlined" style={{ padding: 16, backgroundColor: 'rgb(var(--mdui-color-surface-variant) / 0.4)' }}>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                      <div style={{ fontSize: 13, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface))', display: 'flex', alignItems: 'center', gap: 8 }}>
                        <mdui-icon name="phone_android" style={{ fontSize: 18 }} />
                        如何获取令牌
                      </div>
                      <div style={{ fontSize: 13, color: 'rgb(var(--mdui-color-on-surface-variant))', lineHeight: 1.6, paddingLeft: 26 }}>
                        <div style={{ display: 'flex', alignItems: 'baseline', gap: 8, marginBottom: 4 }}>
                          <span style={{ color: 'rgb(var(--mdui-color-primary))', fontWeight: 600 }}>1</span>
                          <span>打开 Android 应用 → 系统 → 设置</span>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'baseline', gap: 8, marginBottom: 4 }}>
                          <span style={{ color: 'rgb(var(--mdui-color-primary))', fontWeight: 600 }}>2</span>
                          <span>开启"Web 访问"开关</span>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
                          <span style={{ color: 'rgb(var(--mdui-color-primary))', fontWeight: 600 }}>3</span>
                          <span>复制显示的 6 位令牌</span>
                        </div>
                      </div>
                    </div>
                  </mdui-card>
                </div>

                <mdui-button
                  slot="action"
                  variant="filled"
                  onClick={handleTokenSubmit}
                  disabled={tokenInput.trim().length !== 6}
                >
                  确认
                </mdui-button>
              </mdui-dialog>
            )}
          </>
        )}
      </div>
    </div>
  );
}

function ChatDetailRoute({ data, currentMember, onBack, developerModeArmed, onDeveloperModeArm, onNavigateDeveloper }: {
  data: AppData;
  currentMember: Member;
  onBack: () => void;
  developerModeArmed: boolean;
  onDeveloperModeArm: () => void;
  onNavigateDeveloper: () => void;
}) {
  const { groupId } = useParams();
  const group = data.groups.find((g) => g.id === groupId) ?? data.groups[0];
  if (!group) {
    return (
      <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <span>未找到群聊</span>
      </div>
    );
  }

  const handleSendMessage = (content: string) => {
    const isDevCommand = content.trim().toLowerCase() === 'selves';
    if (developerModeArmed && isDevCommand) {
      onDeveloperModeArm();
      onNavigateDeveloper();
      return;
    }
    postJson(`/api/groups/${group.id}/messages`, {
      senderId: currentMember.id,
      content,
      type: 'TEXT',
    }).catch(() => {
      // 发送失败：消息不追加到本地，等待 WebSocket 或 reload 同步
    });
  };

  return (
    <ChatDetailPage
      currentMember={currentMember}
      group={group}
      messages={data.groupMessages[group.id] ?? []}
      members={data.members}
      onBack={onBack}
      onSendMessage={handleSendMessage}
    />
  );
}

function DynamicDetailRoute({
  data,
  currentMember,
  onBack,
  reload,
}: {
  data: AppData;
  currentMember: Member;
  onBack: () => void;
  reload: () => Promise<void>;
}) {
  const { dynamicId } = useParams();
  const [comments, setComments] = useState<DynamicComment[]>([]);

  useEffect(() => {
    if (!dynamicId) return;
    setComments([]);
    getDynamicComments(dynamicId)
      .then((list) => setComments(list))
      .catch(() => {
        // 网络不通时用本地缓存兜底
        if (data.dynamicComments) {
          setComments(data.dynamicComments.filter((c) => c.dynamicId === dynamicId));
        }
      });
  }, [dynamicId, data.dynamicComments]);

  const dynamic = data.dynamics.find((d) => d.id === dynamicId);
  if (!dynamic) {
    return (
      <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <span>未找到动态</span>
      </div>
    );
  }

  const handleSendComment = (content: string, parentCommentId?: string | null) => {
    createDynamicComment(dynamic.id, {
      content,
      authorId: currentMember.id,
      authorName: currentMember.name,
      authorAvatar: currentMember.avatarUrl,
      parentCommentId: parentCommentId ?? null,
    })
      .then(() => getDynamicComments(dynamic.id))
      .then((list) => setComments(list))
      .then(() => void reload())
      .catch(() => {});
  };

  const handleDeleteComment = (commentId: string) => {
    deleteDynamicComment(dynamic.id, commentId)
      .then(() => getDynamicComments(dynamic.id))
      .then((list) => setComments(list))
      .then(() => void reload())
      .catch(() => {});
  };

  const handleLike = () => {
    postJson(`/api/dynamics/${dynamic.id}/like`, { userId: currentMember.id })
      .then(() => void reload())
      .catch(() => {});
  };

  const handleDelete = () => {
    deleteApi(`/api/dynamics/${dynamic.id}`)
      .then(() => {
        void reload();
        onBack();
      })
      .catch(() => {});
  };

  return (
    <DynamicDetailPage
      dynamic={dynamic}
      currentMember={currentMember}
      comments={comments}
      onBack={onBack}
      onLikeClick={handleLike}
      onDeleteClick={handleDelete}
      onSendComment={handleSendComment}
      onDeleteComment={handleDeleteComment}
    />
  );
}

function VoteDetailRoute({
  data,
  currentMember,
  onBack,
  reload,
}: {
  data: AppData;
  currentMember: Member;
  onBack: () => void;
  reload: () => Promise<void>;
}) {
  const { voteId } = useParams();
  const vote = data.votes.find((v) => v.id === voteId) ?? data.votes[0];
  const [voteRecords, setVoteRecords] = useState<VoteRecord[]>([]);

  useEffect(() => {
    if (!voteId) return;
    setVoteRecords([]);
    getVoteRecords(voteId)
      .then((list) => setVoteRecords(list))
      .catch(() => {
        setVoteRecords((data.voteRecords ?? []).filter((r) => r.voteId === voteId));
      });
  }, [voteId, data.voteRecords]);

  if (!vote) {
    return (
      <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <span>未找到投票</span>
      </div>
    );
  }

  const voteRecords_fromData = (data.voteRecords ?? []).filter((r) => r.voteId === vote.id);

  const handleVote = (optionIds: string[]) => {
    castVote(vote.id, {
      userId: currentMember.id,
      userName: currentMember.name,
      userAvatar: currentMember.avatarUrl,
      optionIds,
    })
      .then(() => getVoteRecords(vote.id))
      .then((list) => setVoteRecords(list))
      .then(() => void reload())
      .catch(() => {});
  };

  const handleEndVote = () => {
    putJson(`/api/votes/${vote.id}/end`, {})
      .then(() => void reload())
      .catch(() => {});
  };

  const handleDeleteVote = () => {
    deleteApi(`/api/votes/${vote.id}`)
      .then(() => {
        void reload();
        onBack();
      })
      .catch(() => {});
  };

  return (
    <VoteDetailPage
      vote={vote}
      currentMember={currentMember}
      members={data.members}
      onBack={onBack}
      onVote={handleVote}
      onEndVote={handleEndVote}
      onDeleteVote={handleDeleteVote}
      voteRecords={voteRecords.length > 0 ? voteRecords : voteRecords_fromData}
    />
  );
}
