import type { Member } from '../types/models';
import { MemberAvatar } from './MemberAvatar';

interface MemberHeaderProps {
  member: Member;
  onMemberSwitch?: () => void;
}

export function MemberHeader({ member, onMemberSwitch }: MemberHeaderProps) {
  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: 16,
        backgroundColor: 'rgb(var(--mdui-color-surface))',
      }}
    >
      <div
        style={{ display: 'flex', alignItems: 'center', gap: 16, cursor: 'pointer', flex: 1, minWidth: 0 }}
        onClick={onMemberSwitch}
      >
        <MemberAvatar name={member.name} avatarUrl={member.avatarUrl} size={40} />
        <div style={{ fontSize: 16, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface))', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {member.name}
        </div>
      </div>
      <mdui-button-icon icon="swap_horiz" onClick={onMemberSwitch} style={{ color: 'rgb(var(--mdui-color-primary))' }}></mdui-button-icon>
    </div>
  );
}
