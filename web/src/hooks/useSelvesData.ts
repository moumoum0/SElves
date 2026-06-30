import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { loadAppData } from '../lib/api';
import { useWebSocket } from './useWebSocket';
import type { WebSocketEvent } from './useWebSocket';
import type { AppData, Member, Message } from '../types/models';

const CURRENT_MEMBER_KEY = 'selves-current-member-id';

export function useSelvesData() {
  const [data, setData] = useState<AppData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isFallback, setIsFallback] = useState(false);
  const [baseUrl, setBaseUrl] = useState('');
  const [currentMemberId, setCurrentMemberId] = useState<string>(() => window.localStorage.getItem(CURRENT_MEMBER_KEY) ?? '');

  // 用 ref 读取最新值，避免 reload 依赖 currentMemberId 导致初始化双重加载
  const currentMemberIdRef = useRef(currentMemberId);
  currentMemberIdRef.current = currentMemberId;

  const reload = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await loadAppData(currentMemberIdRef.current || undefined);
      setData(response.data);
      setBaseUrl(response.baseUrl);
      setIsFallback(response.isFallback);
      if (!currentMemberIdRef.current && response.data.members[0]) {
        setCurrentMemberId(response.data.members[0].id);
      }
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : '数据加载失败');
    } finally {
      setLoading(false);
    }
  }, []); // 稳定引用，不随 currentMemberId 变化重建

  useEffect(() => {
    void reload();
  }, [reload]);

  useEffect(() => {
    if (currentMemberId) {
      window.localStorage.setItem(CURRENT_MEMBER_KEY, currentMemberId);
    }
  }, [currentMemberId]);

  const handleWsEvent = useCallback((event: WebSocketEvent) => {
    const { type } = event;
    if (type === 'MESSAGE_CREATED') {
      const msg = event.data as Message & { groupId?: string };
      if (msg && msg.id) {
        setData((prev) => {
          if (!prev) return prev;
          const groupId = msg.groupId ?? Object.keys(prev.groupMessages)[0];
          if (!groupId) return prev;
          const existing = prev.groupMessages[groupId] ?? [];
          return {
            ...prev,
            groupMessages: { ...prev.groupMessages, [groupId]: [...existing, msg] },
          };
        });
        return;
      }
    }
    // All other events: reload
    void reload();
  }, [reload]);

  useWebSocket({ onEvent: handleWsEvent, enabled: !isFallback });

  const currentMember = useMemo<Member | null>(() => {
    if (!data) {
      return null;
    }
    return data.members.find((member: Member) => member.id === currentMemberId) ?? data.members[0] ?? null;
  }, [currentMemberId, data]);

  const setCurrentMember = useCallback((memberId: string) => {
    setCurrentMemberId(memberId);
  }, []);

  return {
    data,
    loading,
    error,
    isFallback,
    baseUrl,
    currentMember,
    setCurrentMember,
    reload,
  };
}
