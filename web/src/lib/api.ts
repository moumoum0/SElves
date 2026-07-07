import { sampleData } from '../data/sampleData';
import type { AppData, ChatGroup, Dynamic, DynamicComment, LocationRecord, LocationRecordQuery, LocationSummary, Member, MemberDiary, Message, OnlineLog, OnlineLogQuery, OnlineStatus, OnlineSummary, SystemInfo, Todo, Vote, VoteRecord } from '../types/models';

const API_BASE_URL_KEY = 'selves-api-base-url';
const API_TOKEN_KEY = 'selves-api-token';

// 认证错误类，用于标识token失效
export class AuthError extends Error {
  constructor(message = 'Authentication failed') {
    super(message);
    this.name = 'AuthError';
  }
}

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

const TODO_PRIORITY_VALUE: Record<Todo['priority'], number> = {
  LOW: 0,
  NORMAL: 1,
  HIGH: 2,
};

export function createTodo(params: Pick<Todo, 'title' | 'description' | 'priority' | 'createdBy'>): Promise<Todo> {
  return postJson<Todo>('/api/todos', {
    title: params.title,
    description: params.description,
    createdBy: params.createdBy,
    priority: TODO_PRIORITY_VALUE[params.priority],
  });
}

export function updateTodoStatus(todoId: string, isCompleted: boolean): Promise<Todo> {
  return putJson<Todo>(`/api/todos/${todoId}`, { isCompleted });
}

export function deleteTodo(todoId: string): Promise<void> {
  return deleteApi(`/api/todos/${todoId}`);
}

export function getDynamicComments(dynamicId: string): Promise<DynamicComment[]> {
  return fetchJson<DynamicComment[]>(`/api/dynamics/${dynamicId}/comments`);
}

export function createDynamicComment(
  dynamicId: string,
  body: Pick<DynamicComment, 'content' | 'authorId' | 'authorName' | 'authorAvatar'> & { parentCommentId?: string | null },
): Promise<DynamicComment> {
  return postJson<DynamicComment>(`/api/dynamics/${dynamicId}/comments`, body);
}

export function deleteDynamicComment(dynamicId: string, commentId: string): Promise<void> {
  return deleteApi(`/api/dynamics/${dynamicId}/comments/${commentId}`);
}

export function getVoteRecords(voteId: string): Promise<VoteRecord[]> {
  return fetchJson<VoteRecord[]>(`/api/votes/${voteId}/records`);
}

function toQueryString(params: object): string {
  const query = Object.entries(params as Record<string, string | number | undefined>)
    .filter(([, value]) => value !== undefined && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join('&');
  return query ? `?${query}` : '';
}

export function getLocationSummary(memberId?: string): Promise<LocationSummary> {
  return fetchJson<LocationSummary>(`/api/location/summary${toQueryString({ memberId })}`);
}

export function getLocationRecords(params: LocationRecordQuery = {}): Promise<LocationRecord[]> {
  return fetchJson<LocationRecord[]>(`/api/location/records${toQueryString(params)}`);
}

export function getOnlineStatus(): Promise<OnlineStatus> {
  return fetchJson<OnlineStatus>('/api/online/status');
}

export function getOnlineLogs(params: OnlineLogQuery = {}): Promise<OnlineLog[]> {
  return fetchJson<OnlineLog[]>(`/api/online/logs${toQueryString(params)}`);
}

export function getOnlineSummary(params: Pick<OnlineLogQuery, 'from' | 'to'> = {}): Promise<OnlineSummary> {
  return fetchJson<OnlineSummary>(`/api/online/summary${toQueryString(params)}`);
}

export async function exportBackup(): Promise<Blob> {
  const token = getApiToken();
  const response = await fetch(`${getApiBaseUrl()}/api/backup/export`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  if (!response.ok) throw new Error(`${response.status} ${response.statusText}`);
  return response.blob();
}

export function importBackup(file: File): Promise<{ status: string }> {
  const token = getApiToken();
  return fetch(`${getApiBaseUrl()}/api/backup/import`, {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    body: file,
  }).then(async (response) => {
    if (!response.ok) {
      const error = await response.json().catch(() => null) as { error?: string } | null;
      throw new Error(error?.error || `${response.status} ${response.statusText}`);
    }
    return response.json() as Promise<{ status: string }>;
  });
}

export function castVote(
  voteId: string,
  body: Pick<VoteRecord, 'userId' | 'userName' | 'userAvatar'> & { optionIds: string[] },
): Promise<Vote> {
  return postJson<Vote>(`/api/votes/${voteId}/vote`, body);
}

async function fetchJson<T>(path: string): Promise<T> {
  const token = getApiToken();
  const response = await fetch(`${getApiBaseUrl()}${path}`, {
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });
  if (!response.ok) {
    throw new Error(`${response.status} ${response.statusText}`);
  }
  return response.json() as Promise<T>;
}

async function fetchWithFallback<T>(path: string, fallback: T, timeoutMs = 8000): Promise<{ data: T; live: boolean }> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);
  // 判断是否为开发环境：localhost 或 DEV 模式
  const isDev = import.meta.env.DEV || window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
  
  try {
    const token = getApiToken();
    const response = await fetch(`${getApiBaseUrl()}${path}`, {
      signal: controller.signal,
      headers: {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    });
    clearTimeout(timer);
    if (!response.ok) {
      // 401认证失败：直接抛出AuthError，不要fallback
      if (response.status === 401) {
        throw new AuthError('Token无效或已过期');
      }
      throw new Error(`${response.status}`);
    }
    const data = (await response.json()) as T;
    return { data, live: true };
  } catch (error) {
    clearTimeout(timer);
    // 认证错误：不fallback，直接抛出
    if (error instanceof AuthError) {
      throw error;
    }
    // 其他错误（网络问题、超时等）：
    // - 开发环境：fallback到示例数据
    // - 生产环境：也抛出错误
    if (isDev) {
      return { data: fallback, live: false };
    } else {
      throw error;
    }
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
  authError?: boolean;
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
