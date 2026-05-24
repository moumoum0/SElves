import { sampleData } from '../data/sampleData';
import type { AppData, ChatGroup, Dynamic, Member, MemberDiary, Message, SystemInfo, Todo } from '../types/models';

const API_BASE_URL_KEY = 'selves-api-base-url';

export function getApiBaseUrl(): string {
  const fromEnv = import.meta.env.VITE_API_BASE_URL as string | undefined;
  const fromStorage = window.localStorage.getItem(API_BASE_URL_KEY);
  const baseUrl = fromEnv || fromStorage || window.location.origin;
  return baseUrl.replace(/\/$/, '');
}

export function setApiBaseUrl(value: string): void {
  window.localStorage.setItem(API_BASE_URL_KEY, value.replace(/\/$/, ''));
}

async function fetchJson<T>(path: string): Promise<T> {
  const response = await fetch(`${getApiBaseUrl()}${path}`);
  if (!response.ok) {
    throw new Error(`${response.status} ${response.statusText}`);
  }
  return response.json() as Promise<T>;
}

async function fetchWithFallback<T>(path: string, fallback: T): Promise<{ data: T; live: boolean }> {
  try {
    const data = await fetchJson<T>(path);
    return { data, live: true };
  } catch {
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

export async function loadAppData(): Promise<AppDataResponse> {
  const [systemResult, membersResult, groupsResult, todosResult, dynamicsResult, diariesResult] = await Promise.all([
    fetchWithFallback<SystemInfo>('/api/system', sampleData.system),
    fetchWithFallback<Member[]>('/api/members', sampleData.members),
    fetchWithFallback<ChatGroup[]>('/api/groups', sampleData.groups),
    fetchWithFallback<Todo[]>('/api/todos', sampleData.todos),
    fetchWithFallback<Dynamic[]>('/api/dynamics', sampleData.dynamics),
    fetchWithFallback<MemberDiary[]>('/api/diaries', sampleData.diaries),
  ]);

  const groups = groupsResult.data;

  const messageResults = await Promise.all(
    groups.map((group) => fetchWithFallback<Message[]>(`/api/groups/${group.id}/messages`, sampleData.groupMessages[group.id] ?? [])),
  );

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
      votes: sampleData.votes,
      diaries: diariesResult.data,
      tracking: sampleData.tracking,
    },
  };
}
