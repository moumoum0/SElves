import { useState, useMemo } from 'react';
import { MemberAvatar } from '../components/MemberAvatar';
import { formatDetailDateTime } from '../lib/utils';
import type { Member, Vote, VoteOption, VoteRecord } from '../types/models';

interface VoteDetailPageProps {
  vote: Vote;
  currentMember: Member;
  members: Member[];
  onBack: () => void;
  onVote: (optionIds: string[]) => void;
  onEndVote: () => void;
  onDeleteVote: () => void;
  voteRecords?: VoteRecord[];
}

export function VoteDetailPage({
  vote,
  currentMember,
  members,
  onBack,
  onVote,
  onEndVote,
  onDeleteVote,
  voteRecords = [],
}: VoteDetailPageProps) {
  const [selectedOptions, setSelectedOptions] = useState<Set<string>>(
    new Set(vote.options.filter((o) => o.isSelected).map((o) => o.id))
  );
  const [showVoteRecords, setShowVoteRecords] = useState(false);
  const [hasVoted, setHasVoted] = useState(vote.hasVoted);

  const isAuthor = currentMember.id === vote.authorId;
  const isActive = vote.isActive && !hasVoted;
  const showResults = !vote.isActive || hasVoted;

  const handleOptionSelect = (optionId: string) => {
    if (!isActive) return;
    setSelectedOptions((prev) => {
      const next = new Set(prev);
      if (vote.allowMultipleChoice) {
        if (next.has(optionId)) next.delete(optionId);
        else next.add(optionId);
      } else {
        return new Set([optionId]);
      }
      return next;
    });
  };

  const handleVoteAction = () => {
    if (selectedOptions.size === 0) return;
    onVote(Array.from(selectedOptions));
    setHasVoted(true);
  };

  const formatEndTime = (iso: string): string => {
    const d = new Date(iso);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const hh = String(d.getHours()).padStart(2, '0');
    const mm = String(d.getMinutes()).padStart(2, '0');
    return `截止 ${y}-${m}-${day} ${hh}:${mm}`;
  };

  const voteCreatedAt =
    typeof vote.createdAt === 'number'
      ? formatDetailDateTime(vote.createdAt)
      : formatDetailDateTime(new Date(vote.createdAt).getTime());

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        minHeight: '100%',
        backgroundColor: 'rgb(var(--mdui-color-surface))',
      }}
    >
      {/* ── TopAppBar ── */}
      <div
        style={{
          position: 'sticky',
          top: 0,
          zIndex: 10,
          backgroundColor: 'rgb(var(--mdui-color-surface))',
          borderBottom: '1px solid rgba(var(--mdui-color-outline-variant), 0.35)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', height: 64, padding: '0 4px' }}>
          <mdui-button-icon icon="arrow_back" onClick={onBack}></mdui-button-icon>
          <div style={{ flex: 1, minWidth: 0, padding: '0 8px', overflow: 'hidden' }}>
            <div
              style={{
                fontSize: 20,
                fontWeight: 400,
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                color: 'rgb(var(--mdui-color-on-surface))',
              }}
            >
              投票详情
            </div>
          </div>

          {/* Vote action — only when active and not yet voted */}
          {vote.isActive && !hasVoted && (
            <span
              onClick={selectedOptions.size > 0 ? handleVoteAction : undefined}
              style={{
                cursor: selectedOptions.size > 0 ? 'pointer' : 'default',
                fontSize: 14,
                fontWeight: 500,
                color:
                  selectedOptions.size > 0
                    ? 'rgb(var(--mdui-color-primary))'
                    : 'rgba(var(--mdui-color-on-surface), 0.38)',
                padding: '0 12px',
                lineHeight: '36px',
              }}
            >
              投票
            </span>
          )}
        </div>
      </div>

      {/* ── Content ── */}
      <div style={{ flex: 1, overflowY: 'auto', padding: 16 }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {/* ── VoteDetailCard ── */}
          <div
            style={{
              borderRadius: 12,
              backgroundColor: 'rgb(var(--mdui-color-surface-container))',
              padding: 16,
            }}
          >
            {/* Author row */}
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                width: '100%',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <MemberAvatar
                  name={vote.authorName}
                  avatarUrl={vote.authorAvatar}
                  size={48}
                />
                <div style={{ width: 12 }} />
                <div>
                  <div
                    style={{
                      fontSize: 16,
                      fontWeight: 500,
                      color: 'rgb(var(--mdui-color-on-surface))',
                      lineHeight: 1.4,
                    }}
                  >
                    {vote.authorName}
                  </div>
                  <div
                    style={{
                      fontSize: 12,
                      color: 'rgb(var(--mdui-color-on-surface-variant))',
                      lineHeight: 1.4,
                    }}
                  >
                    {voteCreatedAt}
                  </div>
                </div>
              </div>

              {/* Action buttons — author only */}
              {isAuthor && (
                <div style={{ display: 'flex', alignItems: 'center' }}>
                  {vote.isActive && (
                    <mdui-button-icon
                      icon="stop"
                      style={{ color: 'rgb(var(--mdui-color-primary))' }}
                      onClick={onEndVote}
                    ></mdui-button-icon>
                  )}
                  <mdui-button-icon
                    icon="delete"
                    style={{ color: 'rgb(var(--mdui-color-error))' }}
                    onClick={onDeleteVote}
                  ></mdui-button-icon>
                </div>
              )}
            </div>

            <div style={{ height: 16 }} />

            {/* Title */}
            <div
              style={{
                fontSize: 20,
                fontWeight: 600,
                color: 'rgb(var(--mdui-color-on-surface))',
                lineHeight: 1.35,
              }}
            >
              {vote.title}
            </div>

            <div style={{ height: 12 }} />

            {/* Description */}
            <div
              style={{
                fontSize: 16,
                color: 'rgb(var(--mdui-color-on-surface))',
                lineHeight: '24px',
              }}
            >
              {vote.description}
            </div>

            <div style={{ height: 16 }} />

            {/* Status + Stats */}
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                width: '100%',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                {/* Status chip */}
                <span
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    padding: '4px 8px',
                    borderRadius: 4,
                    fontSize: 12,
                    fontWeight: 500,
                    backgroundColor: vote.isActive
                      ? 'rgb(var(--mdui-color-primary-container))'
                      : 'rgb(var(--mdui-color-surface-variant))',
                    color: vote.isActive
                      ? 'rgb(var(--mdui-color-on-primary-container))'
                      : 'rgb(var(--mdui-color-on-surface-variant))',
                  }}
                >
                  {vote.isActive ? '进行中' : '已结束'}
                </span>
                {/* Votes count */}
                <span
                  style={{
                    fontSize: 16,
                    color: 'rgb(var(--mdui-color-on-surface-variant))',
                  }}
                >
                  {vote.totalVotes} 票
                </span>
              </div>

              {/* Deadline */}
              {vote.endTime && (
                <span
                  style={{
                    fontSize: 12,
                    color: 'rgb(var(--mdui-color-on-surface-variant))',
                  }}
                >
                  {formatEndTime(vote.endTime)}
                </span>
              )}
            </div>

            {/* Tags */}
            {(vote.allowMultipleChoice || vote.isAnonymous) && (
              <>
                <div style={{ height: 12 }} />
                <div style={{ display: 'flex', gap: 8 }}>
                  {vote.allowMultipleChoice && (
                    <span
                      style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: 4,
                        padding: '4px 8px',
                        borderRadius: 6,
                        fontSize: 12,
                        backgroundColor: 'rgba(var(--mdui-color-surface-variant), 0.6)',
                        color: 'rgb(var(--mdui-color-on-surface-variant))',
                      }}
                    >
                      <mdui-icon name="list" style={{ fontSize: 14 }}></mdui-icon>
                      多选
                    </span>
                  )}
                  {vote.isAnonymous && (
                    <span
                      style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: 4,
                        padding: '4px 8px',
                        borderRadius: 6,
                        fontSize: 12,
                        backgroundColor: 'rgba(var(--mdui-color-surface-variant), 0.6)',
                        color: 'rgb(var(--mdui-color-on-surface-variant))',
                      }}
                    >
                      <mdui-icon name="visibility_off" style={{ fontSize: 14 }}></mdui-icon>
                      匿名
                    </span>
                  )}
                </div>
              </>
            )}
          </div>

          {/* ── VoteOptionsSection ── */}
          <div
            style={{
              borderRadius: 12,
              backgroundColor: 'rgb(var(--mdui-color-surface-container-low))',
              padding: 16,
            }}
          >
            <div
              style={{
                fontSize: 18,
                fontWeight: 500,
                color: 'rgb(var(--mdui-color-on-surface))',
              }}
            >
              投票选项
            </div>

            <div style={{ height: 16 }} />

            {vote.options.map((option) => (
              <div key={option.id}>
                <VoteOptionItem
                  option={option}
                  isSelected={selectedOptions.has(option.id)}
                  isActive={isActive}
                  allowMultipleChoice={vote.allowMultipleChoice}
                  showResults={showResults}
                  onSelect={() => handleOptionSelect(option.id)}
                />
                <div style={{ height: 8 }} />
              </div>
            ))}
          </div>

          {/* ── VoteRecordsSection (only when not anonymous) ── */}
          {!vote.isAnonymous && (
            <div
              style={{
                borderRadius: 12,
                backgroundColor: 'rgb(var(--mdui-color-surface-container-low))',
                padding: 16,
              }}
            >
              <div
                onClick={() => setShowVoteRecords(!showVoteRecords)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  width: '100%',
                  cursor: 'pointer',
                }}
              >
                <div
                  style={{
                    fontSize: 16,
                    fontWeight: 500,
                    color: 'rgb(var(--mdui-color-on-surface))',
                  }}
                >
                  投票记录 ({voteRecords.length})
                </div>
                <mdui-icon
                  name={showVoteRecords ? 'keyboard_arrow_up' : 'keyboard_arrow_down'}
                  style={{
                    fontSize: 24,
                    color: 'rgb(var(--mdui-color-on-surface-variant))',
                  }}
                ></mdui-icon>
              </div>

              {showVoteRecords && (
                <>
                  <div style={{ height: 16 }} />
                  {voteRecords.length === 0 ? (
                    <div
                      style={{
                        fontSize: 14,
                        color: 'rgb(var(--mdui-color-on-surface-variant))',
                        textAlign: 'center',
                        padding: '16px 0',
                      }}
                    >
                      暂无投票记录
                    </div>
                  ) : (
                    voteRecords.map((record) => (
                      <VoteRecordItem key={record.id} record={record} />
                    ))
                  )}
                </>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// VoteOptionItem — mirrors VoteOptionItem in VoteDetailScreen.kt
// ────────────────────────────────────────────────────────────

function VoteOptionItem({
  option,
  isSelected,
  isActive,
  allowMultipleChoice,
  showResults,
  onSelect,
}: {
  option: VoteOption;
  isSelected: boolean;
  isActive: boolean;
  allowMultipleChoice: boolean;
  showResults: boolean;
  onSelect: () => void;
}) {
  const backgroundColor = isSelected
    ? 'rgb(var(--mdui-color-primary-container))'
    : showResults
      ? 'rgb(var(--mdui-color-surface-variant))'
      : 'rgb(var(--mdui-color-surface))';

  return (
    <div
      onClick={isActive ? onSelect : undefined}
      style={{
        borderRadius: 12,
        backgroundColor,
        padding: 16,
        cursor: isActive ? 'pointer' : 'default',
      }}
    >
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          width: '100%',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', flex: 1, minWidth: 0 }}>
          {/* Selection indicator */}
          {isActive && (
            <>
              {allowMultipleChoice ? (
                <div
                  onClick={(e) => {
                    e.stopPropagation();
                    onSelect();
                  }}
                  style={{
                    width: 18,
                    height: 18,
                    borderRadius: 2,
                    border: `2px solid ${
                      isSelected
                        ? 'rgb(var(--mdui-color-primary))'
                        : 'rgba(var(--mdui-color-on-surface), 0.6)'
                    }`,
                    backgroundColor: isSelected
                      ? 'rgb(var(--mdui-color-primary))'
                      : 'transparent',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    flexShrink: 0,
                    marginRight: 8,
                  }}
                >
                  {isSelected && (
                    <mdui-icon
                      name="check"
                      style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-primary))' }}
                    ></mdui-icon>
                  )}
                </div>
              ) : (
                <div
                  onClick={(e) => {
                    e.stopPropagation();
                    onSelect();
                  }}
                  style={{
                    width: 18,
                    height: 18,
                    borderRadius: '50%',
                    border: `2px solid ${
                      isSelected
                        ? 'rgb(var(--mdui-color-primary))'
                        : 'rgba(var(--mdui-color-on-surface), 0.6)'
                    }`,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    flexShrink: 0,
                    marginRight: 8,
                  }}
                >
                  {isSelected && (
                    <div
                      style={{
                        width: 10,
                        height: 10,
                        borderRadius: '50%',
                        backgroundColor: 'rgb(var(--mdui-color-primary))',
                      }}
                    />
                  )}
                </div>
              )}
            </>
          )}

          {/* Option content */}
          <div
            style={{
              fontSize: 16,
              color: 'rgb(var(--mdui-color-on-surface))',
              flex: 1,
              minWidth: 0,
              overflow: 'hidden',
              textOverflow: 'ellipsis',
            }}
          >
            {option.content}
          </div>
        </div>

        {/* Results */}
        {showResults && (
          <div style={{ textAlign: 'right', flexShrink: 0, marginLeft: 12 }}>
            <div
              style={{
                fontSize: 14,
                color: 'rgb(var(--mdui-color-on-surface-variant))',
              }}
            >
              {option.voteCount} 票
            </div>
            <div
              style={{
                fontSize: 12,
                color: 'rgb(var(--mdui-color-primary))',
                fontWeight: 500,
                textAlign: 'right',
              }}
            >
              {Math.round(option.percentage)}%
            </div>
          </div>
        )}
      </div>

      {/* Progress bar */}
      {showResults && (
        <>
          <div style={{ height: 8 }} />
          <div
            style={{
              width: '100%',
              height: 6,
              borderRadius: 3,
              backgroundColor: 'rgb(var(--mdui-color-surface-variant))',
              overflow: 'hidden',
            }}
          >
            <div
              style={{
                height: '100%',
                borderRadius: 3,
                width: `${option.percentage}%`,
                backgroundColor: 'rgb(var(--mdui-color-primary))',
              }}
            />
          </div>
        </>
      )}
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// VoteRecordItem — mirrors VoteRecordItem in VoteDetailScreen.kt
// ────────────────────────────────────────────────────────────

function VoteRecordItem({ record }: { record: VoteRecord }) {
  const votedAt =
    typeof record.votedAt === 'number'
      ? formatDetailDateTime(record.votedAt)
      : formatDetailDateTime(new Date(record.votedAt).getTime());

  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        width: '100%',
        padding: '8px 0',
      }}
    >
      <MemberAvatar
        name={record.userName}
        avatarUrl={record.userAvatar}
        size={32}
      />
      <div style={{ width: 12 }} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div
          style={{
            fontSize: 14,
            fontWeight: 500,
            color: 'rgb(var(--mdui-color-on-surface))',
            lineHeight: 1.4,
          }}
        >
          {record.userName}
        </div>
        <div
          style={{
            fontSize: 12,
            color: 'rgb(var(--mdui-color-on-surface-variant))',
            lineHeight: 1.4,
          }}
        >
          {votedAt}
        </div>
      </div>
    </div>
  );
}