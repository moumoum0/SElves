import { useState } from 'react';
import type { Member } from '../types/models';
import { MemberAvatar } from './MemberAvatar';
import { CreateMemberDialog } from './CreateMemberDialog';
import { MemberSwitchDialog } from './MemberSwitchDialog';

interface MemberHeaderProps {
  member: Member;
  members?: Member[];
  onMemberSwitch?: () => void;
  onMemberSelected?: (member: Member) => void;
  onCreateMember?: (name: string, bio: string, pronouns: string, groups: string[]) => void;
  onDeleteMember?: (member: Member) => void;
}

export function MemberHeader({ member, members, onMemberSwitch, onMemberSelected, onCreateMember, onDeleteMember }: MemberHeaderProps) {
  const [showSwitch, setShowSwitch] = useState(false);
  const [showCreateMember, setShowCreateMember] = useState(false);

  const handleSwitchClick = () => {
    if (members && members.length > 0) {
      setShowSwitch(true);
    } else {
      onMemberSwitch?.();
    }
  };

  return (
    <>
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
          onClick={handleSwitchClick}
        >
          <MemberAvatar name={member.name} avatarUrl={member.avatarUrl} size={40} />
          <div style={{ fontSize: 16, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface))', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {member.name}
          </div>
        </div>
        <mdui-button-icon icon="swap_horiz" onClick={handleSwitchClick} style={{ color: 'rgb(var(--mdui-color-primary))' }}></mdui-button-icon>
      </div>

      {showSwitch && members && (
        <MemberSwitchDialog
          members={members}
          currentMemberId={member.id}
          onDismiss={() => setShowSwitch(false)}
          onMemberSelected={(m) => {
            setShowSwitch(false);
            onMemberSelected?.(m);
          }}
          onCreateNewMember={() => {
            setShowSwitch(false);
            setShowCreateMember(true);
          }}
          onDeleteMember={onDeleteMember}
        />
      )}

      {showCreateMember && members && (
        <CreateMemberDialog
          existingMembers={members}
          existingGroups={Array.from(new Set(members.flatMap((m) => m.groups ?? [])))}
          onDismiss={() => setShowCreateMember(false)}
          onConfirm={(name, bio, pronouns, groups) => {
            setShowCreateMember(false);
            onCreateMember?.(name, bio, pronouns, groups);
          }}
        />
      )}
    </>
  );
}
