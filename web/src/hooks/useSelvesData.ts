import { useCallback, useEffect, useMemo, useState } from 'react';
import { loadAppData } from '../lib/api';
import type { AppData, Member } from '../types/models';

const CURRENT_MEMBER_KEY = 'selves-current-member-id';

export function useSelvesData() {
  const [data, setData] = useState<AppData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isFallback, setIsFallback] = useState(false);
  const [baseUrl, setBaseUrl] = useState('');
  const [currentMemberId, setCurrentMemberId] = useState<string>(() => window.localStorage.getItem(CURRENT_MEMBER_KEY) ?? '');

  const reload = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await loadAppData();
      setData(response.data);
      setBaseUrl(response.baseUrl);
      setIsFallback(response.isFallback);
      if (!currentMemberId && response.data.members[0]) {
        setCurrentMemberId(response.data.members[0].id);
      }
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : '数据加载失败');
    } finally {
      setLoading(false);
    }
  }, [currentMemberId]);

  useEffect(() => {
    void reload();
  }, [reload]);

  useEffect(() => {
    if (currentMemberId) {
      window.localStorage.setItem(CURRENT_MEMBER_KEY, currentMemberId);
    }
  }, [currentMemberId]);

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
