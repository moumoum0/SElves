import { useState } from 'react';
import { MemberAvatar } from '../components/MemberAvatar';
import { formatDetailDateTime } from '../lib/utils';
import type { Dynamic, DynamicComment, Member } from '../types/models';
import { SubPageScaffold } from './SubPageScaffold';
import { IconButton } from '../ui/components/IconButton';
import { TextField } from '../ui/components/TextField';
import { Icon } from '../ui/components/Icon';
import { Chip } from '../ui/components/Chip';

interface DynamicDetailPageProps {
  dynamic: Dynamic;
  currentMember: Member;
  comments: DynamicComment[];
  onBack: () => void;
  onLikeClick: () => void;
  onDeleteClick: () => void;
  onSendComment: (content: string, parentCommentId?: string | null) => void;
  onDeleteComment: (commentId: string) => void;
}

export function DynamicDetailPage({
  dynamic,
  currentMember,
  comments,
  onBack,
  onLikeClick,
  onDeleteClick,
  onSendComment,
  onDeleteComment,
}: DynamicDetailPageProps) {
  const [commentText, setCommentText] = useState('');
  const [replyToComment, setReplyToComment] = useState<DynamicComment | null>(null);
  const [previewImage, setPreviewImage] = useState<string | null>(null);

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
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            height: 64,
            padding: '0 4px',
          }}
        >
          <IconButton onClick={onBack}><md-icon>arrow_back</md-icon></IconButton>
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
              动态详情
            </div>
          </div>
        </div>
      </div>

      {/* ── Content area ── */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <div style={{ flex: 1, overflowY: 'auto', padding: 16 }}>
          {/* Dynamic Detail Card */}
          <DynamicDetailCard
            dynamic={dynamic}
            currentUserId={currentMember.id}
            onLikeClick={onLikeClick}
            onImageClick={(path) => setPreviewImage(path)}
            onDeleteClick={onDeleteClick}
          />

          {/* Comments divider */}
          <div style={{ padding: '8px 0' }}>
            <div
              style={{
                height: 1,
                backgroundColor: 'rgba(var(--mdui-color-outline-variant), 0.35)',
              }}
            />
          </div>

          {/* Comments header */}
          <div
            style={{
              fontSize: 16,
              fontWeight: 500,
              color: 'rgb(var(--mdui-color-on-surface))',
              marginBottom: 16,
            }}
          >
            评论 ({comments.length})
          </div>

          {/* Comments list */}
          {comments.length === 0 ? (
            <div
              style={{
                display: 'flex',
                justifyContent: 'center',
                padding: '32px',
                color: 'rgb(var(--mdui-color-on-surface-variant))',
                fontSize: 14,
              }}
            >
              暂无评论
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {comments.map((comment) => (
                <CommentItem
                  key={comment.id}
                  comment={comment}
                  currentUserId={currentMember.id}
                  onReplyClick={() => setReplyToComment(comment)}
                  onDeleteClick={() => onDeleteComment(comment.id)}
                />
              ))}
            </div>
          )}

          {/* Bottom padding for input area */}
          <div style={{ height: 80 }} />
        </div>
      </div>

      {/* ── Comment input section ── */}
      <div
        style={{
          position: 'sticky',
          bottom: 0,
          backgroundColor: 'rgb(var(--mdui-color-surface))',
          borderTop: '1px solid rgba(var(--mdui-color-outline-variant), 0.35)',
        }}
      >
        <div style={{ padding: 16 }}>
          {/* Reply indicator */}
          {replyToComment && (
            <>
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 8,
                  backgroundColor: 'rgb(var(--mdui-color-primary-container))',
                  borderRadius: 8,
                  padding: '8px 12px',
                  marginBottom: 8,
                }}
              >
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div
                    style={{
                      fontSize: 12,
                      fontWeight: 500,
                      color: 'rgb(var(--mdui-color-on-primary-container))',
                    }}
                  >
                    回复 {replyToComment.authorName}
                  </div>
                  <div
                    style={{
                      fontSize: 12,
                      color: 'rgb(var(--mdui-color-on-primary-container))',
                      opacity: 0.8,
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    {replyToComment.content}
                  </div>
                </div>
                <IconButton
                  style={{ fontSize: 16, color: 'rgb(var(--mdui-color-on-primary-container))' }}
                  onClick={() => setReplyToComment(null)}
                ><md-icon>close</md-icon></IconButton>
              </div>
            </>
          )}

          {/* Input row */}
          <div style={{ display: 'flex', alignItems: 'flex-end', gap: 8 }}>
            <TextField
              variant="outlined"
              placeholder={replyToComment ? '回复评论...' : '写评论...'}
              value={commentText}
              style={{ flex: 1 }}
              maxlength={1000}
              onChange={(val) => setCommentText(val)}
            />
            <button
              onClick={() => {
                if (commentText.trim()) {
                  onSendComment(commentText.trim(), replyToComment?.id);
                  setCommentText('');
                  setReplyToComment(null);
                }
              }}
              disabled={!commentText.trim()}
              style={{
                width: 48,
                height: 48,
                borderRadius: '50%',
                border: 'none',
                cursor: commentText.trim() ? 'pointer' : 'default',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                backgroundColor: commentText.trim()
                  ? 'rgb(var(--mdui-color-primary))'
                  : 'rgb(var(--mdui-color-surface-variant))',
                color: commentText.trim()
                  ? 'rgb(var(--mdui-color-on-primary))'
                  : 'rgb(var(--mdui-color-on-surface-variant))',
                flexShrink: 0,
              }}
            >
              <Icon style={{ fontSize: 20 }}>send</Icon>
            </button>
          </div>
        </div>
      </div>

      {/* ── Image viewer overlay ── */}
      {previewImage && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(0,0,0,0.9)',
            zIndex: 100,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
          onClick={() => setPreviewImage(null)}
        >
          <IconButton
            style={{
              position: 'absolute',
              top: 16,
              right: 16,
              color: 'white',
              zIndex: 101,
            }}
            onClick={() => setPreviewImage(null)}
          ><md-icon>close</md-icon></IconButton>
          <img
            src={previewImage}
            alt=""
            style={{
              maxWidth: '90%',
              maxHeight: '90%',
              objectFit: 'contain',
              borderRadius: 8,
            }}
            onClick={(e) => e.stopPropagation()}
          />
        </div>
      )}
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// DynamicDetailCard — mirrors DynamicDetailCard composable
// ────────────────────────────────────────────────────────────

interface DynamicDetailCardProps {
  dynamic: Dynamic;
  currentUserId: string;
  onLikeClick: () => void;
  onImageClick: (path: string) => void;
  onDeleteClick: () => void;
}

function DynamicDetailCard({
  dynamic,
  currentUserId,
  onLikeClick,
  onImageClick,
  onDeleteClick,
}: DynamicDetailCardProps) {
  return (
    <div
      style={{
        backgroundColor: 'rgb(var(--mdui-color-surface-container))',
        borderRadius: 12,
        padding: 16,
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
            size={48}
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
              {formatDetailDateTime(
                typeof dynamic.createdAt === 'number'
                  ? dynamic.createdAt
                  : new Date(dynamic.createdAt as string).getTime()
              )}
            </div>
          </div>
        </div>

        {/* Delete — only visible to author */}
        {currentUserId === dynamic.authorId && (
          <IconButton
            style={{ color: 'rgb(var(--mdui-color-error))' }}
            onClick={onDeleteClick}
          ><md-icon>delete</md-icon></IconButton>
        )}
      </div>

      <div style={{ height: 16 }} />

      {/* ── Title ── */}
      {dynamic.title && (
        <>
          <div
            style={{
              fontWeight: 600,
              fontSize: 20,
              color: 'rgb(var(--mdui-color-on-surface))',
            }}
          >
            {dynamic.title}
          </div>
          <div style={{ height: 12 }} />
        </>
      )}

      {/* ── Content ── */}
      <div
        style={{
          fontSize: 16,
          color: 'rgb(var(--mdui-color-on-surface))',
          lineHeight: '24px',
        }}
      >
        {dynamic.content}
      </div>

      {/* ── Images ── */}
      {dynamic.images && dynamic.images.length > 0 && (
        <>
          <div style={{ height: 16 }} />
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
                  cursor: 'pointer',
                }}
                onClick={() => onImageClick(img)}
              />
            ))}
          </div>
        </>
      )}

      {/* ── Tags ── */}
      {dynamic.tags && dynamic.tags.length > 0 && (
        <>
          <div style={{ height: 16 }} />
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            {dynamic.tags.map((tag) => (
              <Chip key={tag} variant="assist" style={{ fontSize: 12 }}>
                #{tag}
              </Chip>
            ))}
          </div>
        </>
      )}

      <div style={{ height: 16 }} />

      {/* ── Like button ── */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 8,
          cursor: 'pointer',
        }}
        onClick={onLikeClick}
      >
        <Icon
          style={{
            fontSize: 24,
            color: dynamic.isLiked
              ? 'rgb(var(--mdui-color-error))'
              : 'rgb(var(--mdui-color-on-surface-variant))',
          }}
        >{dynamic.isLiked ? 'favorite' : 'favorite_border'}</Icon>
        <span
          style={{
            fontSize: 16,
            color: 'rgb(var(--mdui-color-on-surface-variant))',
          }}
        >
          {dynamic.likeCount ?? 0} 次赞
        </span>
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// CommentItem — mirrors CommentItem composable
// ────────────────────────────────────────────────────────────

interface CommentItemProps {
  comment: DynamicComment;
  currentUserId: string;
  onReplyClick: () => void;
  onDeleteClick: () => void;
}

function CommentItem({
  comment,
  currentUserId,
  onReplyClick,
  onDeleteClick,
}: CommentItemProps) {
  return (
    <div
      style={{
        backgroundColor: 'transparent',
        padding: 12,
        borderRadius: 12,
      }}
    >
      {/* Header row */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <MemberAvatar
            name={comment.authorName}
            avatarUrl={comment.authorAvatar}
            size={32}
          />
          <div>
            <div
              style={{
                fontWeight: 500,
                fontSize: 14,
                color: 'rgb(var(--mdui-color-on-surface))',
              }}
            >
              {comment.authorName}
            </div>
            <div
              style={{
                fontSize: 12,
                color: 'rgb(var(--mdui-color-on-surface-variant))',
              }}
            >
              {formatDetailDateTime(comment.createdAt)}
            </div>
          </div>
        </div>

        {/* Actions */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
          <button
            onClick={onReplyClick}
            style={{
              background: 'none',
              border: 'none',
              cursor: 'pointer',
              fontSize: 12,
              color: 'rgb(var(--mdui-color-primary))',
              padding: '4px 8px',
            }}
          >
            回复
          </button>
          {currentUserId === comment.authorId && (
            <IconButton
              style={{
                fontSize: 16,
                color: 'rgb(var(--mdui-color-error))',
                width: 32,
                height: 32,
              }}
              onClick={onDeleteClick}
            ><md-icon>delete</md-icon></IconButton>
          )}
        </div>
      </div>

      <div style={{ height: 8 }} />

      {/* Comment content */}
      <div
        style={{
          fontSize: 14,
          color: 'rgb(var(--mdui-color-on-surface))',
          lineHeight: '20px',
        }}
      >
        {comment.content}
      </div>
    </div>
  );
}
