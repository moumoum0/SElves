import { useRef, useEffect, useMemo } from 'react';
import { Navigate, Route, Routes, useLocation, useNavigate, useParams } from 'react-router-dom';
import { useSelvesData } from './hooks/useSelvesData';
import { ChatDetailPage } from './pages/ChatDetailPage';
import { DiaryPage } from './pages/DiaryPage';
import { DynamicPage } from './pages/DynamicPage';
import { GroupChatPage } from './pages/GroupChatPage';
import { HomePage } from './pages/HomePage';
import { LocationPage } from './pages/LocationPage';
import { MemberManagementPage } from './pages/MemberManagementPage';
import { OnlineStatsPage } from './pages/OnlineStatsPage';
import { SettingsPage } from './pages/SettingsPage';
import { SystemPage } from './pages/SystemPage';
import { TodoPage } from './pages/TodoPage';
import { VotePage } from './pages/VotePage';
import type { AppData, Member } from './types/models';

const MAIN_TABS = ['/', '/chat', '/system'];

export default function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const navBarRef = useRef<HTMLElement>(null);
  const { data, loading, error, isFallback, baseUrl, currentMember, setCurrentMember, reload } = useSelvesData();

  const isMainTab = MAIN_TABS.includes(location.pathname);

  const currentTab = useMemo(() => {
    if (location.pathname.startsWith('/chat')) return '/chat';
    if (location.pathname === '/system') return '/system';
    return '/';
  }, [location.pathname]);

  const handleMemberSwitch = () => {
    if (!data || !currentMember) return;
    const idx = data.members.findIndex((m: Member) => m.id === currentMember.id);
    const next = data.members[(idx + 1) % data.members.length];
    setCurrentMember(next.id);
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
  }, [navigate, loading, isMainTab]);

  return (
    <div className="app-shell">
      <div className="app-card">
        {loading ? (
          <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 16 }}>
            <mdui-circular-progress></mdui-circular-progress>
            <span style={{ color: 'rgb(var(--mdui-color-on-surface-variant))' }}>正在加载 Selves Web...</span>
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

            <div className="page-content">
              <Routes>
                <Route
                  path="/"
                  element={
                    currentMember && data ? (
                      <HomePage
                        data={data}
                        currentMember={currentMember}
                        onMemberSwitch={handleMemberSwitch}
                        onNavigate={navigate}
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
                        groups={data.groups}
                        groupMessages={data.groupMessages}
                        unreadCounts={data.unreadCounts}
                        onMemberSwitch={handleMemberSwitch}
                        onOpenGroup={(id: string) => navigate(`/chat/${id}`)}
                      />
                    ) : <div />
                  }
                />
                <Route
                  path="/chat/:groupId"
                  element={
                    currentMember && data ? (
                      <ChatDetailRoute data={data} currentMember={currentMember} onBack={() => navigate('/chat')} />
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
                <Route path="/todo" element={data && currentMember ? <TodoPage todos={data.todos} members={data.members} currentMember={currentMember} onBack={() => navigate('/')} /> : <div />} />
                <Route path="/dynamic" element={data && currentMember ? <DynamicPage dynamics={data.dynamics} currentMember={currentMember} onBack={() => navigate('/')} /> : <div />} />
                <Route path="/vote" element={data && currentMember ? <VotePage votes={data.votes} currentMember={currentMember} onBack={() => navigate('/')} /> : <div />} />
                <Route path="/diary" element={data && currentMember ? <DiaryPage diaries={data.diaries} currentMember={currentMember} onBack={() => navigate('/')} /> : <div />} />
                <Route path="/location" element={data && currentMember ? <LocationPage tracking={data.tracking} currentMember={currentMember} onBack={() => navigate('/')} /> : <div />} />
                <Route path="/member-management" element={data && currentMember ? <MemberManagementPage members={data.members} currentMember={currentMember} onBack={() => navigate('/system')} /> : <div />} />
                <Route path="/online-stats" element={data && currentMember ? <OnlineStatsPage members={data.members} currentMember={currentMember} onBack={() => navigate('/system')} /> : <div />} />
                <Route path="/settings" element={<SettingsPage baseUrl={baseUrl} onBack={() => navigate('/system')} />} />
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </div>

            {isMainTab && (
              <mdui-navigation-bar ref={navBarRef} value={currentTab} label-visibility="selected">
                <mdui-navigation-bar-item icon="home" value="/">首页</mdui-navigation-bar-item>
                <mdui-navigation-bar-item icon="chat" value="/chat">群聊</mdui-navigation-bar-item>
                <mdui-navigation-bar-item icon="manage_accounts" value="/system">系统</mdui-navigation-bar-item>
              </mdui-navigation-bar>
            )}
          </>
        )}
      </div>
    </div>
  );
}

function ChatDetailRoute({ data, currentMember, onBack }: { data: AppData; currentMember: Member; onBack: () => void }) {
  const { groupId } = useParams();
  const group = data.groups.find((g) => g.id === groupId) ?? data.groups[0];
  if (!group) {
    return (
      <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <span>未找到群聊</span>
      </div>
    );
  }
  return <ChatDetailPage currentMember={currentMember} group={group} messages={data.groupMessages[group.id] ?? []} onBack={onBack} />;
}
