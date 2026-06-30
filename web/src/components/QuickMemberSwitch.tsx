import { useState } from 'react';
import { MemberAvatar } from './MemberAvatar';
import { MemberSwitchDialog } from './MemberSwitchDialog';
import type { Member } from '../types/models';
import { Icon } from '../ui/components/Icon';

interface QuickMemberSwitchProps {
  currentMember: Member | null;
  members: Member[];
  onMemberSelected: (member: Member) => void;
  size?: number;
}

/**
 * 快捷成员切换组件
 * 简化版的成员信息显示，只有头像和切换图标
 * 与安卓 QuickMemberSwitch.kt 1:1 对应
 */
export function QuickMemberSwitch({
  currentMember,
  members,
  onMemberSelected,
  size = 48,
}: QuickMemberSwitchProps) {
  const [showDialog, setShowDialog] = useState(false);

  return (
    <>
      <div
        style={{
          width: size,
          height: size,
          position: 'relative',
          cursor: 'pointer',
          flexShrink: 0,
        }}
        onClick={() => setShowDialog(true)}
      >
        {/* 成员头像 */}
        <MemberAvatar
          name={currentMember?.name ?? '?'}
          avatarUrl={currentMember?.avatarUrl}
          size={size}
        />

        {/* 交换图标在右下角，不要圆形背景 */}
        <Icon
          style={{
            position: 'absolute',
            bottom: 0,
            right: 0,
            width: size * 0.3,
            height: size * 0.3,
            fontSize: size * 0.3,
            color: 'rgb(var(--mdui-color-primary))',
          }}
        >swap_horiz</Icon>
      </div>

      {/* 成员切换对话框 */}
      {showDialog && (
        <MemberSwitchDialog
          members={members}
          currentMemberId={currentMember?.id ?? ''}
          onDismiss={() => setShowDialog(false)}
          onMemberSelected={(member) => {
            onMemberSelected(member);
            setShowDialog(false);
          }}
          onCreateNewMember={() => setShowDialog(false)}
          onDeleteMember={() => setShowDialog(false)}
        />
      )}
    </>
  );
}