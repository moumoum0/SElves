import { useMemo, useState } from 'react';
import { MemberAvatar } from '../components/MemberAvatar';
import { formatVoteRemaining, formatTimestamp } from '../lib/utils';
import type { Member, Vote, VoteOption } from '../types/models';
import { Icon } from '../ui/components/Icon';
import { IconButton } from '../ui/components/IconButton';
import { Card } from '../ui/components/Card';
import { FAB } from '../ui/components/FAB';
import { Chip } from '../ui/components/Chip';
import { TextField } from '../ui/components/TextField';

interface VotePageProps {
  votes: Vote[];
  currentMember: Member;
  onBack: () => void;
  onVoteClick?: (id: string) => void;
  onNavigateToCreateVote?: () => void;
}

export function VotePage({ votes, currentMember, onBack, onVoteClick, onNavigateToCreateVote }: VotePageProps) {
  const [showSearchBar, setShowSearchBar] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterActive, setFilterActive] = useState(true);

  const filteredVotes = useMemo(() => {
    let result = votes.filter((v) => v.isActive === filterActive);
    const q = searchQuery.trim().toLowerCase();
    if (q) {
      result = result.filter(
        (v) =>
          v.title.toLowerCase().includes(q) ||
          v.description.toLowerCase().includes(q)
      );
    }
    return result;
  }, [votes, filterActive, searchQuery]);

  return (
    <div style={{ position: 'relative', height: '100%', overflow: 'hidden', backgroundColor: 'rgb(var(--mdui-color-surface))', display: 'flex', flexDirection: 'column' }}>
      {/* TopBar */}
      <div style={{ flexShrink: 0, zIndex: 10, backgroundColor: 'rgb(var(--mdui-color-surface))' }}>
        {/* AppBar */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            height: 64,
            padding: '0 4px',
            borderBottom: '1px solid rgba(var(--mdui-color-outline-variant), 0.35)',
          }}
        >
          <IconButton onClick={onBack}><md-icon>arrow_back</md-icon></IconButton>
          <div style={{ flex: 1, minWidth: 0, padding: '0 8px' }}>
            {showSearchBar ? (
              <TextField
                value={searchQuery}
                placeholder="搜索投票..."
                variant="outlined"
                style={{ width: '100%' }}
                onChange={(val) => setSearchQuery(val)}
              >
                {searchQuery ? (
                  <IconButton
                    slot="end-icon"
                    onClick={() => setSearchQuery('')}
                  ><md-icon>close</md-icon></IconButton>
                ) : (
                  <IconButton
                    slot="end-icon"
                    onClick={() => {
                      setShowSearchBar(false);
                      setSearchQuery('');
                    }}
                  ><md-icon>close</md-icon></IconButton>
                )}
              </TextField>
            ) : (
              <div
                style={{
                  fontSize: 16,
                  fontWeight: 400,
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace: 'nowrap',
                  color: 'rgb(var(--mdui-color-on-surface))',
                }}
              >
                投票
              </div>
            )}
          </div>
          {!showSearchBar ? (
            <IconButton onClick={() => setShowSearchBar(true)}><md-icon>search</md-icon></IconButton>
          ) : null}
        </div>

        {/* FilterChips */}
        <div style={{ display: 'flex', gap: 8, padding: '8px 16px' }}>
          <Chip selectable selected={filterActive} onClick={() => setFilterActive(true)}>
            {filterActive ? <Icon slot="icon" style={{ fontSize: 18 }}>check</Icon> : null}
            进行中
          </Chip>
          <Chip selectable selected={!filterActive} onClick={() => setFilterActive(false)}>
            {!filterActive ? <Icon slot="icon" style={{ fontSize: 18 }}>check</Icon> : null}
            已结束
          </Chip>
        </div>
      </div>

      {/* Content */}
      <div style={{ flex: 1, minHeight: 0, overflowY: 'auto', overflowX: 'hidden', padding: 16, display: 'flex', flexDirection: 'column', gap: 16 }}>
        {filteredVotes.length === 0 ? (
          <div
            style={{
              textAlign: 'center',
              padding: '32px 16px',
              color: 'rgb(var(--mdui-color-on-surface-variant))',
            }}
          >
            <Icon
              style={{ fontSize: 64, opacity: 0.5, display: 'block', margin: '0 auto 16px' }}
            >poll</Icon>
            <div style={{ fontSize: 16 }}>
              {filterActive ? '暂无进行中的投票' : '暂无已结束的投票'}
            </div>
            <div style={{ fontSize: 14, opacity: 0.6, marginTop: 8 }}>
              点击右下角按钮创建第一个投票
            </div>
          </div>
        ) : (
          filteredVotes.map((vote) => (
            <VoteCard
              key={vote.id}
              vote={vote}
              currentUserId={currentMember.id}
              onVoteClick={() => onVoteClick?.(vote.id)}
            />
          ))
        )}
      </div>

      <FAB
        icon="add"
        style={{ position: 'absolute', right: 16, bottom: 16 }}
        onClick={onNavigateToCreateVote}
      />
    </div>
  );
}

function VoteCard({
  vote,
  currentUserId,
  onVoteClick,
}: {
  vote: Vote;
  currentUserId: string;
  onVoteClick: () => void;
}) {
  const isAuthor = currentUserId === vote.authorId;
  const showPercentage = !vote.isActive || vote.hasVoted;

  return (
    <Card
      variant="filled"
      style={{
        display: 'block',
        borderRadius: 16,
        backgroundColor: 'rgb(var(--mdui-color-surface-container))',
        overflow: 'hidden',
      }}
      onClick={onVoteClick}
    >
      <div style={{ padding: 16 }}>
        {/* 顶部栏：作者信息 + 操作按钮 */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <MemberAvatar name={vote.authorName} avatarUrl={vote.authorAvatar} size={44} />
            <div>
              <div
                style={{
                  fontSize: 15,
                  fontWeight: 600,
                  color: 'rgb(var(--mdui-color-on-surface))',
                  lineHeight: 1.4,
                }}
              >
                {vote.authorName}
              </div>
              <div style={{ fontSize: 12, color: 'rgb(var(--mdui-color-on-surface-variant))', lineHeight: 1.4 }}>
                {typeof vote.createdAt === 'number' ? formatTimestamp(vote.createdAt) : String(vote.createdAt)}
              </div>
            </div>
          </div>

          {isAuthor && (
            <div style={{ display: 'flex', alignItems: 'center' }}>
              {vote.isActive && (
                <IconButton
                  style={{ color: 'rgb(var(--mdui-color-primary))' }}
                  onClick={(e) => {
                    e.stopPropagation();
                  }}
                ><md-icon>stop</md-icon></IconButton>
              )}
              <IconButton
                style={{ color: 'rgba(var(--mdui-color-error), 0.7)' }}
                onClick={(e) => {
                  e.stopPropagation();
                }}
              ><md-icon>delete</md-icon></IconButton>
            </div>
          )}
        </div>

        <div style={{ height: 16 }} />

        {/* 投票标题 */}
        <div
          style={{
            fontSize: 18,
            fontWeight: 700,
            color: 'rgb(var(--mdui-color-on-surface))',
            lineHeight: 1.35,
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
            overflow: 'hidden',
          }}
        >
          {vote.title}
        </div>

        <div style={{ height: 8 }} />

        {/* 投票描述 */}
        {vote.description && (
          <>
            <div
              style={{
                fontSize: 14,
                color: 'rgb(var(--mdui-color-on-surface-variant))',
                lineHeight: 1.5,
                display: '-webkit-box',
                WebkitLineClamp: 3,
                WebkitBoxOrient: 'vertical',
                overflow: 'hidden',
              }}
            >
              {vote.description}
            </div>
            <div style={{ height: 12 }} />
          </>
        )}

        {/* 投票选项预览 */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          {vote.options.slice(0, 4).map((option, index) => (
            <VoteOptionPreview
              key={option.id}
              option={option}
              index={index}
              isActive={vote.isActive}
              showPercentage={showPercentage}
              totalVotes={vote.totalVotes}
            />
          ))}
          {vote.options.length > 4 && (
            <div
              style={{
                fontSize: 13,
                color: 'rgb(var(--mdui-color-primary))',
                fontWeight: 500,
                paddingTop: 4,
              }}
            >
              还有 {vote.options.length - 4} 个选项
            </div>
          )}
        </div>

        <div style={{ height: 16 }} />

        {/* 底部栏：状态 + 统计 */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <VoteStatusChip isActive={vote.isActive} endTime={vote.endTime} />
          <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
            <Icon
              style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-surface-variant))' }}
            >people</Icon>
            <span style={{ fontSize: 13, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
              {vote.totalVotes} 人参与
            </span>
          </div>
        </div>

        {/* 投票设置标签 */}
        {(vote.allowMultipleChoice || vote.isAnonymous) && (
          <>
            <div style={{ height: 12 }} />
            <div style={{ display: 'flex', gap: 8 }}>
              {vote.allowMultipleChoice && (
                <VoteTag icon="list" text="多选" />
              )}
              {vote.isAnonymous && (
                <VoteTag icon="visibility_off" text="匿名" />
              )}
            </div>
          </>
        )}

        {/* 参与状态提示 */}
        {vote.hasVoted && vote.isActive && (
          <>
            <div style={{ height: 12 }} />
            <div
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: 6,
                padding: '8px 12px',
                borderRadius: 8,
                backgroundColor: 'rgba(var(--mdui-color-primary-container), 0.5)',
              }}
            >
              <Icon
                style={{ fontSize: 16, color: 'rgb(var(--mdui-color-primary))' }}
              >check_circle</Icon>
              <span style={{ fontSize: 13, fontWeight: 500, color: 'rgb(var(--mdui-color-primary))' }}>
                您已参与投票
              </span>
            </div>
          </>
        )}
      </div>
    </Card>
  );
}

function VoteOptionPreview({
  option,
  index,
  isActive,
  showPercentage,
  totalVotes,
}: {
  option: VoteOption;
  index: number;
  isActive: boolean;
  showPercentage: boolean;
  totalVotes: number;
}) {
  const percentage = totalVotes > 0 ? option.percentage : 0;

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
        {/* 选项序号 */}
        <div
          style={{
            width: 24,
            height: 24,
            borderRadius: 6,
            backgroundColor: 'rgb(var(--mdui-color-secondary-container))',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0,
          }}
        >
          <span
            style={{
              fontSize: 12,
              fontWeight: 700,
              color: 'rgb(var(--mdui-color-on-secondary-container))',
            }}
          >
            {index + 1}
          </span>
        </div>

        {/* 选项内容 */}
        <div
          style={{
            flex: 1,
            fontSize: 14,
            color: 'rgb(var(--mdui-color-on-surface))',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
          }}
        >
          {option.content}
        </div>

        {/* 投票数和百分比 */}
        {showPercentage && (
          <>
            <span style={{ fontSize: 13, fontWeight: 500, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>
              {option.voteCount} 票
            </span>
            <span style={{ fontSize: 13, fontWeight: 700, color: 'rgb(var(--mdui-color-primary))', minWidth: 36, textAlign: 'right' }}>
              {Math.round(percentage)}%
            </span>
          </>
        )}
      </div>

      <div style={{ height: 4 }} />

      {/* 进度条 */}
      <div style={{ display: 'flex', alignItems: 'center', paddingLeft: 34 }}>
        <div
          style={{
            flex: 1,
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
              width: `${percentage}%`,
              backgroundColor: isActive
                ? 'rgb(var(--mdui-color-primary))'
                : 'rgb(var(--mdui-color-secondary))',
            }}
          />
        </div>

        {/* 右侧百分比条 */}
        {showPercentage && percentage > 0 && (
          <>
            <div style={{ width: 8 }} />
            <div
              style={{
                width: 40,
                height: 6,
                borderRadius: 3,
                backgroundColor: 'rgb(var(--mdui-color-primary-container))',
                overflow: 'hidden',
                position: 'relative',
              }}
            >
              <div
                style={{
                  position: 'absolute',
                  top: 0,
                  left: 0,
                  height: '100%',
                  borderRadius: 3,
                  width: `${percentage}%`,
                  backgroundColor: 'rgb(var(--mdui-color-primary))',
                }}
              />
            </div>
          </>
        )}
      </div>
    </div>
  );
}

function VoteStatusChip({ isActive, endTime }: { isActive: boolean; endTime: string }) {
  const remaining = isActive && endTime ? formatVoteRemaining(endTime) : null;

  return (
    <div
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: 8,
        padding: '6px 12px',
        borderRadius: 20,
        backgroundColor: isActive
          ? 'rgb(var(--mdui-color-primary-container))'
          : 'rgb(var(--mdui-color-surface-variant))',
      }}
    >
      {/* 状态指示灯 */}
      <div
        style={{
          width: 8,
          height: 8,
          borderRadius: 4,
          backgroundColor: isActive
            ? 'rgb(var(--mdui-color-primary))'
            : 'rgb(var(--mdui-color-outline))',
          flexShrink: 0,
        }}
      />
      <span
        style={{
          fontSize: 13,
          fontWeight: 500,
          color: isActive
            ? 'rgb(var(--mdui-color-on-primary-container))'
            : 'rgb(var(--mdui-color-on-surface-variant))',
        }}
      >
        {isActive ? '进行中' : '已结束'}
      </span>
      {isActive && remaining && (
        <span
          style={{
            fontSize: 12,
            color: 'rgba(var(--mdui-color-on-primary-container), 0.7)',
          }}
        >
          · {remaining}
        </span>
      )}
    </div>
  );
}

function VoteTag({ icon, text }: { icon: string; text: string }) {
  return (
    <div
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: 4,
        padding: '4px 8px',
        borderRadius: 6,
        backgroundColor: 'rgba(var(--mdui-color-surface-variant), 0.6)',
      }}
    >
      <Icon
        style={{ fontSize: 14, color: 'rgb(var(--mdui-color-on-surface-variant))' }}
      >{icon}</Icon>
      <span style={{ fontSize: 11, color: 'rgb(var(--mdui-color-on-surface-variant))' }}>{text}</span>
    </div>
  );
}
