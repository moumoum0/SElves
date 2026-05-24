import { getInitial } from '../lib/utils';

interface MemberAvatarProps {
  name: string;
  avatarUrl?: string | null;
  size?: number;
}

export function MemberAvatar({ name, avatarUrl, size = 40 }: MemberAvatarProps) {
  const initial = getInitial(name);

  return (
    <div
      style={{
        width: size,
        height: size,
        borderRadius: '50%',
        overflow: 'hidden',
        flexShrink: 0,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: avatarUrl ? 'transparent' : 'rgb(var(--mdui-color-primary))',
        color: 'rgb(var(--mdui-color-on-primary))',
        fontWeight: 700,
        fontSize: size * 0.4,
      }}
    >
      {avatarUrl ? (
        <img
          src={avatarUrl}
          alt={name}
          style={{ width: '100%', height: '100%', objectFit: 'cover' }}
        />
      ) : (
        initial || <mdui-icon name="person" style={{ fontSize: size * 0.55 }}></mdui-icon>
      )}
    </div>
  );
}
