import { useMemo, useState } from 'react';
import { MemberAvatar } from '../components/MemberAvatar';
import { formatDetailDateTime } from '../lib/utils';
import type { Dynamic, Member } from '../types/models';
import { IconButton } from '../ui/components/IconButton';
import { TextField } from '../ui/components/TextField';
import { Icon } from '../ui/components/Icon';
import { CircularProgress } from '../ui/components/Progress';
import { FAB } from '../ui/components/FAB';
import { Chip } from '../ui/components/Chip';

type FilterType = 'IMAGE' | 'TEXT' | null;

interface DynamicPageProps {
  dynamics: Dynamic[];
  currentMember: Member;
  onBack: () => void;
  onDynamicClick?: (id: string) => void;
  onNavigateToCreateDynamic?: () => void;
}

export function DynamicPage({
  dynamics,
  currentMember,
  onBack,
  onDynamicClick,
  onNavigateToCreateDynamic,
}: DynamicPageProps) {
  const [showSearchBar, setShowSearchBar] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState<FilterType>(null);
  const isLoading = false;

  const filteredDynamics = useMemo(() => {
    let result = dynamics;
    const q = searchQuery.trim().toLowerCase();
    if (q) {
      result = result.filter(
        (d) =>
          d.title.toLowerCase().includes(q) ||
          d.content.toLowerCase().includes(q) ||
          d.authorName.toLowerCase().includes(q),
      );
    }
    if (filterType === 'IMAGE') {
      result = result.filter((d) => d.images && d.images.length > 0);
    } else if (filterType === 'TEXT') {
      result = result.filter((d) => !d.images || d.images.length === 0);
    }
    return result;
  }, [dynamics, searchQuery, filterType]);

  const cycleFilterType = () => {
    setFilterType((cur) => {
      if (cur === null) return 'IMAGE';
      if (cur === 'IMAGE') return 'TEXT';
      return null;
    });
  };

  const filterIcon =
    filterType === 'IMAGE' ? 'image' : filterType === 'TEXT' ? 'text_format' : 'filter_list';

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
        overflow: 'hidden',
        position: 'relative',
        backgroundColor: 'rgb(var(--mdui-color-surface))',
      }}
    >
      {/* ── TopBar ── */}
      <div
        style={{
          flexShrink: 0,
          zIndex: 10,
          backgroundColor: 'rgb(var(--mdui-color-surface))',
          borderBottom: '1px solid rgba(var(--mdui-color-outline-variant), 0.35)',
        }}
      >
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            height: 64,
            padding: '0 4px',
          }}
        >
          {/* Back */}
          <IconButton onClick={onBack}><md-icon>arrow_back</md-icon></IconButton>

          {/* Title / SearchBar toggle */}
          <div style={{ flex: 1, minWidth: 0, padding: '0 8px', overflow: 'hidden' }}>
            {showSearchBar ? (
              <TextField
                value={searchQuery}
                placeholder="搜索动态..."
                variant="outlined"
                style={{ width: '100%' }}
                onChange={(val) => setSearchQuery(val)}
              >
                <Icon slot="icon">search</Icon>
                <div slot="end-icon" style={{ display: 'flex', alignItems: 'center' }}>
                  <IconButton
                    style={{
                      color:
                        filterType !== null
                          ? 'rgb(var(--mdui-color-primary))'
                          : 'rgb(var(--mdui-color-on-surface-variant))',
                    }}
                    onClick={cycleFilterType}
                  ><md-icon>{filterIcon}</md-icon></IconButton>
                  <IconButton
                    onClick={() => {
                      setShowSearchBar(false);
                      setSearchQuery('');
                    }}
                  ><md-icon>close</md-icon></IconButton>
                </div>
              </TextField>
            ) : (
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
                动态
              </div>
            )}
          </div>

          {/* Search icon — only when search is not open */}
          {!showSearchBar && (
            <IconButton onClick={() => setShowSearchBar(true)}><md-icon>search</md-icon></IconButton>
          )}
        </div>
      </div>

      {/* ── Content ── */}
      <div
        style={{
          flex: 1,
          minHeight: 0,
          overflowY: 'auto',
          overflowX: 'hidden',
          padding: 16,
          display: 'flex',
          flexDirection: 'column',
          gap: 12,
        }}
      >
        {isLoading ? (
          <div
            style={{
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              minHeight: 200,
            }}
          >
            <CircularProgress />
          </div>
        ) : filteredDynamics.length === 0 ? (
          /* ── Empty state ── */
          <div
            style={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              padding: 32,
            }}
          >
            <Icon
              style={{
                fontSize: 64,
                opacity: 0.5,
                color: 'rgb(var(--mdui-color-on-surface-variant))',
                display: 'block',
              }}
            >timeline</Icon>
            <div style={{ height: 16 }} />
            <span
              style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-surface-variant))' }}
            >
              暂无动态
            </span>
            <div style={{ height: 8 }} />
            <span
              style={{
                fontSize: 14,
                opacity: 0.6,
                color: 'rgb(var(--mdui-color-on-surface-variant))',
                textAlign: 'center',
              }}
            >
              点击右下角按钮发布第一条动态
            </span>
          </div>
        ) : (
          filteredDynamics.map((dynamic) => (
            <DynamicCard
              key={dynamic.id}
              dynamic={dynamic}
              currentUserId={currentMember.id}
              onLikeClick={() => {}}
              onCommentClick={() => onDynamicClick?.(dynamic.id)}
              onDeleteClick={() => {}}
              onCardClick={() => onDynamicClick?.(dynamic.id)}
            />
          ))
        )}
      </div>

      {/* ── FAB ── */}
      <FAB
        icon="add"
        style={{ position: 'absolute', right: 16, bottom: 16 }}
        onClick={onNavigateToCreateDynamic}
      />
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// DynamicCard — mirrors DynamicCard composable in DynamicScreen.kt
// ────────────────────────────────────────────────────────────
interface DynamicCardProps {
  dynamic: Dynamic;
  currentUserId: string;
  onLikeClick: () => void;
  onCommentClick: () => void;
  onDeleteClick: () => void;
  onCardClick?: () => void;
}

function DynamicCard({
  dynamic,
  currentUserId,
  onLikeClick,
  onCommentClick,
  onDeleteClick,
  onCardClick,
}: DynamicCardProps) {
  return (
    <div
      onClick={onCardClick}
      style={{
        backgroundColor: 'rgb(var(--mdui-color-surface-container))',
        borderRadius: 12,
        padding: 16,
        cursor: 'pointer',
      }}
    >
      {/* ── Author row ── */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <MemberAvatar
            name={dynamic.authorName}
            avatarUrl={dynamic.authorAvatar}
            size={40}
          />
          <div>
            <div
              style={{
                fontWeight: 500,
                fontSize: 16,
                color: 'rgb(var(--mdui-color-on-surface))',
              }}
            >
              {dynamic.authorName}
            </div>
            <div
              style={{
                fontSize: 12,
                color: 'rgb(var(--mdui-color-on-surface-variant))',
              }}
            >
              {formatDetailDateTime(dynamic.createdAt as number)}
            </div>
          </div>
        </div>

        {/* Delete — only visible to author */}
        {currentUserId === dynamic.authorId && (
          <IconButton
            style={{ color: 'rgb(var(--mdui-color-error))' }}
            onClick={(e) => {
              (e as unknown as MouseEvent).stopPropagation();
              onDeleteClick();
            }}
          ><md-icon>delete</md-icon></IconButton>
        )}
      </div>

      {/* 12dp spacer */}
      <div style={{ height: 12 }} />

      {/* ── Title (if present) ── */}
      {dynamic.title && (
        <>
          <div
            style={{
              fontWeight: 600,
              fontSize: 18,
              color: 'rgb(var(--mdui-color-on-surface))',
              display: '-webkit-box',
              WebkitLineClamp: 2,
              WebkitBoxOrient: 'vertical',
              overflow: 'hidden',
            }}
          >
            {dynamic.title}
          </div>
          <div style={{ height: 8 }} />
        </>
      )}

      {/* ── Content ── */}
      <div
        style={{
          fontSize: 14,
          color: 'rgb(var(--mdui-color-on-surface-variant))',
        }}
      >
        {dynamic.content}
      </div>

      {/* ── Images (if present) ── */}
      {dynamic.images && dynamic.images.length > 0 && (
        <>
          <div style={{ height: 8 }} />
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(3, 1fr)',
              gap: 4,
            }}
          >
            {dynamic.images.map((img, i) => (
              <img
                key={i}
                src={img}
                alt=""
                style={{
                  width: '100%',
                  aspectRatio: '1',
                  objectFit: 'cover',
                  borderRadius: 4,
                }}
                onClick={(e) => e.stopPropagation()}
              />
            ))}
          </div>
        </>
      )}

      {/* ── Tags (if present) ── */}
      {dynamic.tags && dynamic.tags.length > 0 && (
        <>
          <div style={{ height: 8 }} />
          <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
            {dynamic.tags.map((tag) => (
              <Chip key={tag} variant="assist" style={{ fontSize: 12 }}>
                #{tag}
              </Chip>
            ))}
          </div>
        </>
      )}

      {/* 12dp spacer */}
      <div style={{ height: 12 }} />

      {/* ── Interaction row ── */}
      <div style={{ display: 'flex', gap: 16 }}>
        {/* Like */}
        <div
          style={{ display: 'flex', alignItems: 'center', gap: 4, cursor: 'pointer' }}
          onClick={(e) => {
            e.stopPropagation();
            onLikeClick();
          }}
        >
          <Icon
            style={{
              fontSize: 20,
              color: dynamic.isLiked
                ? 'rgb(var(--mdui-color-error))'
                : 'rgb(var(--mdui-color-on-surface-variant))',
            }}
          >{dynamic.isLiked ? 'favorite' : 'favorite_border'}</Icon>
          <span
            style={{
              fontSize: 14,
              color: 'rgb(var(--mdui-color-on-surface-variant))',
            }}
          >
            {dynamic.likeCount ?? 0}
          </span>
        </div>

        {/* Comment */}
        <div
          style={{ display: 'flex', alignItems: 'center', gap: 4, cursor: 'pointer' }}
          onClick={(e) => {
            e.stopPropagation();
            onCommentClick();
          }}
        >
          <Icon
            style={{
              fontSize: 20,
              color: 'rgb(var(--mdui-color-on-surface-variant))',
            }}
          >comment</Icon>
          <span
            style={{
              fontSize: 14,
              color: 'rgb(var(--mdui-color-on-surface-variant))',
            }}
          >
            {dynamic.commentCount ?? 0}
          </span>
        </div>
      </div>
    </div>
  );
}
