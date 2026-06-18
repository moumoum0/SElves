export function getInitial(text: string): string {
  return text.trim().slice(0, 1).toUpperCase() || 'S';
}

export function groupColorFromName(name: string): string {
  let hash = 0;
  for (let index = 0; index < name.length; index += 1) {
    hash = name.charCodeAt(index) + ((hash << 5) - hash);
  }
  const hue = Math.abs(hash) % 360;
  return `hsl(${hue}, 60%, 50%)`;
}

export function parseUnknownDate(value: unknown): Date | null {
  if (value instanceof Date) {
    return value;
  }

  if (typeof value === 'number') {
    return new Date(value);
  }

  if (typeof value === 'string') {
    const parsed = new Date(value);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  }

  if (value && typeof value === 'object') {
    const record = value as Record<string, unknown>;
    if (
      typeof record.year === 'number' &&
      typeof record.monthValue === 'number' &&
      typeof record.dayOfMonth === 'number'
    ) {
      return new Date(
        record.year,
        record.monthValue - 1,
        record.dayOfMonth,
        typeof record.hour === 'number' ? record.hour : 0,
        typeof record.minute === 'number' ? record.minute : 0,
        typeof record.second === 'number' ? record.second : 0,
      );
    }
  }

  return null;
}

function isToday(date: Date): boolean {
  const now = new Date();
  return (
    date.getFullYear() === now.getFullYear() &&
    date.getMonth() === now.getMonth() &&
    date.getDate() === now.getDate()
  );
}

function isThisYear(date: Date): boolean {
  return date.getFullYear() === new Date().getFullYear();
}

export function formatTimestamp(value: number): string {
  const date = new Date(value);
  const now = Date.now();
  const diff = now - value;

  if (diff < 60 * 1000) return '刚刚';
  if (diff < 60 * 60 * 1000) return `${Math.floor(diff / (60 * 1000))}分钟前`;
  if (isToday(date)) {
    return new Intl.DateTimeFormat('zh-CN', { hour: '2-digit', minute: '2-digit' }).format(date);
  }
  if (isThisYear(date)) {
    return new Intl.DateTimeFormat('zh-CN', { month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' }).format(date);
  }
  return new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' }).format(date);
}

export function formatDetailDateTime(value: number): string {
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

export function formatMessageTime(value: number): string {
  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

export function formatDynamicTime(value: unknown): string {
  const date = parseUnknownDate(value);
  if (!date) {
    return '--';
  }

  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
}

export function formatVoteRemaining(endTime: string): string {
  const target = new Date(endTime);
  const diff = target.getTime() - Date.now();
  if (Number.isNaN(target.getTime()) || diff <= 0) {
    return '即将结束';
  }

  const minutes = Math.floor(diff / 60000);
  if (minutes >= 1440) {
    return `${Math.floor(minutes / 1440)} 天后结束`;
  }
  if (minutes >= 60) {
    return `${Math.floor(minutes / 60)} 小时后结束`;
  }
  return `${minutes} 分钟后结束`;
}
