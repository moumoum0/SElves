import { useMemo, useState, useRef, useCallback } from 'react';
import { MemberAvatar } from '../components/MemberAvatar';
import { AlphabetIndexBar } from '../components/AlphabetIndexBar';
import { CreateMemberDialog } from '../components/CreateMemberDialog';
import { EditMemberDialog } from '../components/EditMemberDialog';
import type { Member } from '../types/models';
import { SubPageScaffold } from './SubPageScaffold';

interface MemberManagementPageProps {
  members: Member[];
  currentMember: Member;
  onBack: () => void;
  onCreateMember?: (name: string, bio: string, pronouns: string, groups: string[]) => void;
  onEditMember?: (id: string, name: string, bio: string, pronouns: string, groups: string[]) => void;
  onDeleteMember?: (id: string) => void;
}

export function MemberManagementPage({ members, currentMember, onBack, onCreateMember, onEditMember, onDeleteMember }: MemberManagementPageProps) {
  const [showSearchBar, setShowSearchBar] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [viewMode, setViewMode] = useState<'group' | 'letter'>('group');
  const [expandedGroups, setExpandedGroups] = useState<Record<string, boolean>>({});
  const [selectedLetter, setSelectedLetter] = useState<string | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Member | null>(null);
  const [showCreate, setShowCreate] = useState(false);
  const [editTarget, setEditTarget] = useState<Member | null>(null);
  const letterSectionRefs = useRef<Record<string, HTMLDivElement | null>>({});

  const filteredMembers = useMemo(() => {
    const keyword = searchQuery.trim().toLocaleLowerCase();
    if (!keyword) {
      return members;
    }
    return members.filter((member) => {
      const groupText = (member.groups ?? []).join(' ');
      return [member.name, member.bio ?? '', member.pronouns ?? '', groupText]
        .join(' ')
        .toLocaleLowerCase()
        .includes(keyword);
    });
  }, [members, searchQuery]);

  const groups = useMemo(() => buildGroups(filteredMembers), [filteredMembers]);
  const letters = useMemo(() => buildLetters(filteredMembers), [filteredMembers]);
  const availableLetters = useMemo(() => letters.map((s) => s.letter), [letters]);

  const handleLetterSelected = useCallback((letter: string) => {
    setSelectedLetter(letter);
    letterSectionRefs.current[letter]?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }, []);

  return (
    <>
      <SubPageScaffold
        title="成员管理"
        onBack={onBack}
        fab={<mdui-fab icon="add" style={{ position: 'absolute', right: 16, bottom: 16 }} onClick={() => setShowCreate(true)}></mdui-fab>}
        actions={
          <div style={{ display: 'flex', alignItems: 'center' }}>
            {!showSearchBar ? (
              <mdui-button-icon icon="search" onClick={() => setShowSearchBar(true)}></mdui-button-icon>
            ) : null}
          </div>
        }
      >
        {showSearchBar ? (
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              marginBottom: 12,
            }}
          >
            <mdui-text-field
              value={searchQuery}
              placeholder="搜索成员"
              icon="search"
              clearable
              style={{ flex: 1 }}
              onInput={(event) => setSearchQuery((event.target as HTMLInputElement).value)}
            ></mdui-text-field>
            <mdui-button-icon
              icon="close"
              onClick={() => {
                setShowSearchBar(false);
                setSearchQuery('');
              }}
            ></mdui-button-icon>
          </div>
        ) : null}

        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: 12,
            marginBottom: 12,
          }}
        >
          <div style={{ fontSize: 13, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
            共 {filteredMembers.length} 位成员 · {groups.length} 个分组
          </div>
          <button
            type="button"
            onClick={() => setViewMode(viewMode === 'group' ? 'letter' : 'group')}
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: 6,
              height: 32,
              padding: '0 12px',
              borderRadius: 16,
              border: '1px solid rgb(var(--mdui-color-outline-variant))',
              backgroundColor: 'rgb(var(--mdui-color-surface-container-low))',
              color: 'rgb(var(--mdui-color-on-surface))',
              cursor: 'pointer',
              flexShrink: 0,
            }}
          >
            <mdui-icon name="more_vert" style={{ fontSize: 18 }}></mdui-icon>
            <span style={{ fontSize: 13 }}>{viewMode === 'letter' ? '按首字母' : '按分组'}</span>
          </button>
        </div>

        <div style={{ display: 'flex', gap: 0 }}>
          <div style={{ flex: 1, minWidth: 0 }}>
            {viewMode === 'group' ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                {groups.map((group) => (
                  <GroupSection
                    key={group.name}
                    group={group}
                    expanded={expandedGroups[group.name] ?? false}
                    onToggle={() =>
                      setExpandedGroups((prev) => ({
                        ...prev,
                        [group.name]: !(prev[group.name] ?? false),
                      }))
                    }
                    currentMemberId={currentMember.id}
                    onDeleteMember={setDeleteTarget}
                    onEditMember={setEditTarget}
                  />
                ))}

                {groups.length === 0 ? (
                  <div style={{ padding: '32px 0', textAlign: 'center', color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
                    没有匹配的成员
                  </div>
                ) : null}
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                {letters.map((section) => (
                  <div
                    key={section.letter}
                    ref={(el) => {
                      letterSectionRefs.current[section.letter] = el;
                    }}
                  >
                    <div
                      style={{
                        width: '100%',
                        padding: '8px 4px',
                        fontSize: 16,
                        fontWeight: 700,
                        color: 'rgb(var(--mdui-color-on-surface))',
                      }}
                    >
                      {section.letter}
                    </div>
                    <div>
                      {section.members.map((m) => (
                        <MemberRow
                          key={m.id}
                          member={m}
                          active={m.id === currentMember.id}
                          onDelete={() => setDeleteTarget(m)}
                          onEdit={() => setEditTarget(m)}
                        />
                      ))}
                    </div>
                  </div>
                ))}

                {letters.length === 0 ? (
                  <div style={{ padding: '32px 0', textAlign: 'center', color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
                    没有匹配的成员
                  </div>
                ) : null}
              </div>
            )}
          </div>

          {/* 右侧字母索引栏 — 仅在按字母模式且非搜索时显示 */}
          {viewMode === 'letter' && searchQuery === '' && availableLetters.length > 0 && (
            <AlphabetIndexBar
              availableLetters={availableLetters}
              selectedLetter={selectedLetter}
              onLetterSelected={handleLetterSelected}
            />
          )}
        </div>
      </SubPageScaffold>

      {/* 删除确认对话框 */}
      {deleteTarget && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            zIndex: 1000,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            backgroundColor: 'rgba(0,0,0,0.5)',
          }}
          onClick={() => setDeleteTarget(null)}
        >
          <mdui-dialog
            open
            headline="删除成员"
            on-close={() => setDeleteTarget(null)}
            style={{ maxWidth: 360 }}
            onClick={(e: React.MouseEvent) => e.stopPropagation()}
          >
            <div style={{ padding: '16px 0', fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
              确定要删除成员「{deleteTarget.name}」吗？此操作不可撤销。
            </div>
            <div slot="actions" style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
              <mdui-button onClick={() => setDeleteTarget(null)}>取消</mdui-button>
              <mdui-button
                variant="tonal"
                style={{ backgroundColor: 'rgb(var(--mdui-color-error))', color: 'rgb(var(--mdui-color-on-error))' }}
                onClick={() => { onDeleteMember?.(deleteTarget.id); setDeleteTarget(null); }}
              >
                删除
              </mdui-button>
            </div>
          </mdui-dialog>
        </div>
      )}

      {/* 新建成员弹窗 */}
      {showCreate && (
        <CreateMemberDialog
          existingMembers={members}
          existingGroups={Array.from(new Set(members.flatMap((m) => m.groups ?? [])))}
          onDismiss={() => setShowCreate(false)}
          onConfirm={(name, bio, pronouns, groups) => {
            setShowCreate(false);
            onCreateMember?.(name, bio, pronouns, groups);
          }}
        />
      )}

      {/* 编辑成员弹窗 */}
      {editTarget && (
        <EditMemberDialog
          member={editTarget}
          existingMembers={members}
          existingGroups={Array.from(new Set(members.flatMap((m) => m.groups ?? [])))}
          onDismiss={() => setEditTarget(null)}
          onConfirm={(name, bio, pronouns, groups) => {
            onEditMember?.(editTarget.id, name, bio, pronouns, groups);
            setEditTarget(null);
          }}
        />
      )}
    </>
  );
}

function MemberRow({
  member,
  active,
  onDelete,
  onEdit,
}: {
  member: Member;
  active: boolean;
  onDelete: () => void;
  onEdit: () => void;
}) {
  const [showMenu, setShowMenu] = useState(false);

  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 12,
        padding: '12px 4px',
        position: 'relative',
        userSelect: 'none',
      }}
      onContextMenu={(e) => {
        e.preventDefault();
        setShowMenu(true);
      }}
    >
      <MemberAvatar name={member.name} avatarUrl={member.avatarUrl} size={40} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <span
            style={{
              fontWeight: 400,
              fontSize: 16,
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
              color: 'rgb(var(--mdui-color-on-surface))',
            }}
          >
            {member.name}
          </span>
          {active ? (
            <span
              style={{
                padding: '2px 6px',
                borderRadius: 4,
                fontSize: 11,
                lineHeight: 1.2,
                backgroundColor: 'rgb(var(--mdui-color-primary))',
                color: 'rgb(var(--mdui-color-on-primary))',
              }}
            >
              当前
            </span>
          ) : null}
        </div>
        <div
          style={{
            fontSize: 12,
            color: 'rgb(var(--mdui-color-on-surface-variant))',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
          }}
        >
          {member.bio || '暂无简介'}
        </div>
      </div>

      {/* 操作菜单触发按钮 */}
      <div style={{ position: 'relative' }}>
        <mdui-button-icon icon="more_vert" onClick={() => setShowMenu(true)}></mdui-button-icon>

        {showMenu && (
          <>
            <div
              style={{ position: 'fixed', inset: 0, zIndex: 99 }}
              onClick={() => setShowMenu(false)}
            />
            <div
              style={{
                position: 'absolute',
                right: 0,
                top: '100%',
                zIndex: 100,
                minWidth: 140,
                padding: '4px 0',
                borderRadius: 8,
                backgroundColor: 'rgb(var(--mdui-color-surface-container))',
                boxShadow: '0 2px 12px rgba(0,0,0,0.2)',
              }}
            >
              <button
                type="button"
                onClick={() => { setShowMenu(false); onEdit(); }}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 8,
                  width: '100%',
                  padding: '10px 16px',
                  border: 'none',
                  background: 'transparent',
                  cursor: 'pointer',
                  fontSize: 14,
                  color: 'rgb(var(--mdui-color-on-surface))',
                  textAlign: 'left',
                }}
              >
                <mdui-icon name="edit" style={{ fontSize: 18 }}></mdui-icon>
                编辑
              </button>
              {!active && (
                <button
                  type="button"
                  onClick={() => {
                    setShowMenu(false);
                    onDelete();
                  }}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 8,
                    width: '100%',
                    padding: '10px 16px',
                    border: 'none',
                    background: 'transparent',
                    cursor: 'pointer',
                    fontSize: 14,
                    color: 'rgb(var(--mdui-color-error))',
                    textAlign: 'left',
                  }}
                >
                  <mdui-icon name="delete" style={{ fontSize: 18 }}></mdui-icon>
                  删除
                </button>
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
}

function GroupSection({
  group,
  expanded,
  onToggle,
  currentMemberId,
  onDeleteMember,
  onEditMember,
}: {
  group: { name: string; members: Member[] };
  expanded: boolean;
  onToggle: () => void;
  currentMemberId: string;
  onDeleteMember: (member: Member) => void;
  onEditMember: (member: Member) => void;
}) {
  const description = group.name === '未分组' ? '未归档成员' : '';

  return (
    <div style={{ width: '100%', padding: '4px 0' }}>
      <button
        type="button"
        onClick={onToggle}
        style={{
          width: '100%',
          display: 'flex',
          alignItems: 'center',
          gap: 4,
          padding: '8px 0',
          border: 'none',
          background: 'transparent',
          cursor: 'pointer',
          textAlign: 'left',
        }}
      >
        <mdui-icon
          name={expanded ? 'keyboard_arrow_down' : 'keyboard_arrow_right'}
          style={{ fontSize: 20, color: 'rgb(var(--mdui-color-on-surface))', marginLeft: 4 }}
        ></mdui-icon>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, minWidth: 0, flex: 1 }}>
          <span style={{ fontSize: 16, fontWeight: 700, color: 'rgb(var(--mdui-color-on-surface))', whiteSpace: 'nowrap' }}>
            {group.name}
          </span>
          {description ? (
            <span
              style={{
                fontSize: 12,
                color: 'rgba(var(--mdui-color-on-surface), 0.55)',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
              }}
            >
              {description}
            </span>
          ) : null}
        </div>
      </button>

      {expanded ? (
        <div style={{ paddingLeft: 20, paddingTop: 4 }}>
          {group.members.length === 0 ? (
            <div style={{ padding: '12px 8px', fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
              该分组暂无成员
            </div>
          ) : (
            group.members.map((member) => (
              <MemberRow key={member.id} member={member} active={member.id === currentMemberId} onDelete={() => onDeleteMember(member)} onEdit={() => onEditMember(member)} />
            ))
          )}
        </div>
      ) : null}
    </div>
  );
}

function buildGroups(members: Member[]) {
  const map = new Map<string, Member[]>();
  members.forEach((member) => {
    const groups = member.groups && member.groups.length > 0 ? member.groups : ['未分组'];
    groups.forEach((group) => {
      map.set(group, [...(map.get(group) ?? []), member]);
    });
  });
  return [...map.entries()]
    .sort(([left], [right]) => left.localeCompare(right, 'zh-CN'))
    .map(([name, groupMembers]) => ({
      name,
      members: [...groupMembers].sort((a, b) => a.name.localeCompare(b.name, 'zh-CN')),
    }));
}

function buildLetters(members: Member[]) {
  const map = new Map<string, Member[]>();
  members.forEach((member) => {
    const letter = member.name.trim().slice(0, 1).toUpperCase() || '#';
    map.set(letter, [...(map.get(letter) ?? []), member]);
  });
  return [...map.entries()]
    .sort(([left], [right]) => left.localeCompare(right, 'zh-CN'))
    .map(([letter, letterMembers]) => ({
      letter,
      members: [...letterMembers].sort((a, b) => a.name.localeCompare(b.name, 'zh-CN')),
    }));
}