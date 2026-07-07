import { useMemo, useRef, useState } from 'react';
import { Icon } from '../ui/components/Icon';

interface MemberAvatarProps {
  name: string;
  avatarUrl?: string | null;
  size?: number;
}

function getPlaceholderPadding(size: number): number {
  if (size >= 80) return 16;
  if (size >= 60) return 12;
  return 8;
}

export function MemberAvatar({ name, avatarUrl, size = 40 }: MemberAvatarProps) {
  const [hasError, setHasError] = useState(false);
  const triedImageRef = useRef(false);

  const shouldShowImage = useMemo(() => {
    const normalized = avatarUrl?.trim();
    return Boolean(normalized) && !hasError;
  }, [avatarUrl, hasError]);

  const placeholderPadding = getPlaceholderPadding(size);

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
        backgroundColor: 'rgba(var(--mdui-color-primary), 0.1)',
        position: 'relative',
      }}
    >
      {shouldShowImage && (
        <img
          src={avatarUrl ?? undefined}
          alt={name}
          onError={() => {
            if (!triedImageRef.current) {
              triedImageRef.current = true;
              setHasError(true);
            }
          }}
          style={{
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            display: 'block',
          }}
        />
      )}

      {!shouldShowImage && (
        <Icon
          style={{
            fontSize: size - placeholderPadding * 2,
            color: 'rgb(var(--mdui-color-on-surface-variant))',
          }}
        >person</Icon>
      )}
    </div>
  );
}
