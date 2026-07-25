export interface Member {
  id: string;
  name: string;
  avatarUrl: string | null;
  isDeleted?: boolean;
  bio?: string;
  pronouns?: string;
  groups?: string[];
  /** 系统管理员（Web 暂不做权限门控，仅透传展示） */
  isAdmin?: boolean;
}

export interface ChatGroup {
  id: string;
  name: string;
  avatarUrl: string | null;
  members: Member[];
  ownerId: string;
  createdAt: number;
}

export type MessageType = 'TEXT' | 'IMAGE';

export interface Message {
  id: string;
  senderId: string;
  content: string;
  timestamp: number;
  type: MessageType;
  imagePath?: string | null;
}

export type TodoPriority = 'LOW' | 'NORMAL' | 'HIGH';

export interface Todo {
  id: string;
  title: string;
  description: string;
  isCompleted: boolean;
  createdAt: number;
  completedAt?: number | null;
  priority: TodoPriority;
  createdBy: string;
}

export interface Dynamic {
  id: string;
  title: string;
  content: string;
  authorId: string;
  authorName: string;
  authorAvatar?: string | null;
  createdAt: unknown;
  updatedAt: unknown;
  type: string;
  images?: string[];
  likeCount?: number;
  commentCount?: number;
  isLiked?: boolean;
  tags?: string[];
}

export interface VoteOption {
  id: string;
  voteId: string;
  content: string;
  voteCount: number;
  percentage: number;
  isSelected: boolean;
}

export interface Vote {
  id: string;
  title: string;
  description: string;
  authorId: string;
  authorName: string;
  authorAvatar: string | null;
  createdAt: number | string;
  endTime: string;
  isActive: boolean;
  allowMultipleChoice: boolean;
  isAnonymous: boolean;
  options: VoteOption[];
  totalVotes: number;
  hasVoted: boolean;
}

export interface DynamicComment {
  id: string;
  dynamicId: string;
  content: string;
  authorId: string;
  authorName: string;
  authorAvatar: string | null;
  createdAt: number;
  parentCommentId?: string | null;
}

export interface MemberDiary {
  id: string;
  memberId: string;
  title: string;
  content: string;
  createdAt: number;
  updatedAt: number;
}

export interface SystemInfo {
  id: string;
  name: string;
  avatarUrl: string | null;
  description: string;
  createdAt: number;
  updatedAt: number;
}

export interface TrackingSummary {
  status: 'RECORDING' | 'STOPPED';
  todayRecords: number;
  totalRecords: number;
  lastRecordTime: string | null;
}

export type LocationSummary = TrackingSummary;

export interface LocationRecord {
  id: string;
  latitude: number;
  longitude: number;
  altitude?: number | null;
  accuracy?: number | null;
  address?: string | null;
  timestamp: string;
  memberId: string;
  note?: string | null;
}

export interface LocationRecordQuery {
  memberId?: string;
  limit?: number;
  from?: string | number;
  to?: string | number;
}

export interface OnlineMemberStatus {
  member: Member;
  isOnline: boolean;
  todayOnlineMinutes: number;
  lastActiveTime: number;
}

export interface OnlineStatus {
  onlineCount: number;
  memberStats: OnlineMemberStatus[];
}

export interface OnlineLog {
  id: number;
  memberId: string;
  memberName: string;
  memberAvatar: string | null;
  isOnline: boolean;
  loginTime: number;
  logoutTime?: number | null;
  duration?: number;
}

export interface OnlineLogQuery {
  memberId?: string;
  from?: number;
  to?: number;
  limit?: number;
}

export interface OnlineSummary {
  totalLogins: number;
  todayLogins: number;
  currentOnlineCount: number;
  averageOnlineTime: number;
}

export interface ApiStatus {
  status: string;
  version: string;
  port: number;
  connectedClients: number;
}

export interface VoteRecord {
  id: string;
  voteId: string;
  optionId: string;
  userId: string;
  userName: string;
  userAvatar: string | null;
  votedAt: number;
}

export interface AppData {
  system: SystemInfo;
  members: Member[];
  groups: ChatGroup[];
  groupMessages: Record<string, Message[]>;
  unreadCounts: Record<string, number>;
  todos: Todo[];
  dynamics: Dynamic[];
  dynamicComments: DynamicComment[];
  votes: Vote[];
  voteRecords: VoteRecord[];
  diaries: MemberDiary[];
  tracking: TrackingSummary;
}
