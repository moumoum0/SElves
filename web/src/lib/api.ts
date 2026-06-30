import { sampleData } from '../data/sampleData';
import type { AppData, ChatGroup, Dynamic, Member, MemberDiary, Message, SystemInfo, Todo, Vote, VoteRecord } from '../types/models';

const API_BASE_URL_KEY = 'selves-api-base-url';
const API_TOKEN_KEY = 'selves-api-token';

export function getApiBaseUrl(): string {
  const fromEnv = import.meta.env.VITE_API_BASE_URL as string | undefined;
  const fromStorage = window.localStorage.getItem(API_BASE_URL_KEY);
  const baseUrl = fromEnv || fromStorage || window.location.origin;
  return baseUrl.replace(/\/$/, '');
}

export function setApiBaseUrl(value: string): void {
  window.localStorage.setItem(API_BASE_URL_KEY, value.replace(/\/$/, ''));
}

export function getApiToken(): string {
  return window.localStorage.getItem(API_TOKEN_KEY) ?? '';
}

export function setApiToken(value: string): void {
  window.localStorage.setItem(API_TOKEN_KEY, value);
}

// ===== 写入请求 =====

async function mutate<T>(method: string, path: string, body?: unknown): Promise<T> {
  const token = getApiToken();
  const res = await fetch(`${getApiBaseUrl()}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

export { fetchJson };
export const postJson = <T>(path: string, body: unknown) => mutate<T>('POST', path, body);
export const putJson = <T>(path: string, body: unknown) => mutate<T>('PUT', path, body);
export const deleteApi = (path: string) => mutate<void>('DELETE', path);

async function fetchJson<T>(path: string): Promise<T> {
  const response = await fetch(`${getApiBaseUrl()}${path}`);
  if (!response.ok) {
    throw new Error(`${response.status} ${response.statusText}`);
  }
  return response.json() as Promise<T>;
}

async function fetchWithFallback<T>(path: string, fallback: T, timeoutMs = 8000): Promise<{ data: T; live: boolean }> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);
  try {
    const response = await fetch(`${getApiBaseUrl()}${path}`, { signal: controller.signal });
    clearTimeout(timer);
    if (!response.ok) throw new Error(`${response.status}`);
    const data = (await response.json()) as T;
    return { data, live: true };
  } catch {
    clearTimeout(timer);
    return { data: fallback, live: false };
  }
}

function normalizeMessageMap(groups: ChatGroup[], messages: Message[][]): Record<string, Message[]> {
  return groups.reduce<Record<string, Message[]>>((accumulator, group, index) => {
    accumulator[group.id] = messages[index] ?? [];
    return accumulator;
  }, {});
}

function buildUnreadCounts(groups: ChatGroup[], messageMap: Record<string, Message[]>): Record<string, number> {
  return groups.reduce<Record<string, number>>((accumulator, group) => {
    const liveCount = Math.min((messageMap[group.id] ?? []).length, 3);
    accumulator[group.id] = liveCount || sampleData.unreadCounts[group.id] || 0;
    return accumulator;
  }, {});
}

export interface AppDataResponse {
  data: AppData;
  isFallback: boolean;
  baseUrl: string;
}

export async function loadAppData(currentMemberId?: string): Promise<AppDataResponse> {
  // groups 一返回就立即开始拉消息，与其他接口完全并行
  const groupsAndMessages = fetchWithFallback<ChatGroup[]>('/api/groups', sampleData.groups).then(
    async (groupsResult) => {
      const messageResults = await Promise.all(
        groupsResult.data.map((group) =>
          fetchWithFallback<Message[]>(`/api/groups/${group.id}/messages`, sampleData.groupMessages[group.id] ?? []),
        ),
      );
      return { groupsResult, messageResults };
    },
  );

  const [systemResult, membersResult, todosResult, dynamicsResult, diariesResult, votesResult] = await Promise.all([
    fetchWithFallback<SystemInfo>('/api/system', sampleData.system),
    fetchWithFallback<Member[]>('/api/members', sampleData.members),
    fetchWithFallback<Todo[]>('/api/todos', sampleData.todos),
    fetchWithFallback<Dynamic[]>('/api/dynamics', sampleData.dynamics),
    fetchWithFallback<MemberDiary[]>('/api/diaries', sampleData.diaries),
    fetchWithFallback<Vote[]>(
      currentMemberId ? `/api/votes?userId=${encodeURIComponent(currentMemberId)}` : '/api/votes',
      sampleData.votes,
    ),
  ]);

  const { groupsResult, messageResults } = await groupsAndMessages;
  const groups = groupsResult.data;

  const groupMessages = normalizeMessageMap(
    groups,
    messageResults.map((result) => result.data),
  );

  const isFallback = [
    systemResult,
    membersResult,
    groupsResult,
    todosResult,
    dynamicsResult,
    diariesResult,
    votesResult,
    ...messageResults,
  ].some((result) => !result.live);

  return {
    baseUrl: getApiBaseUrl(),
    isFallback,
    data: {
      system: systemResult.data,
      members: membersResult.data,
      groups,
      groupMessages,
      unreadCounts: buildUnreadCounts(groups, groupMessages),
      todos: todosResult.data,
      dynamics: dynamicsResult.data,
      dynamicComments: [],
      votes: votesResult.data,
      voteRecords: [],
      diaries: diariesResult.data,
      tracking: sampleData.tracking,
    },
  };
}
