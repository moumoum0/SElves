import { useRef, useState } from 'react';
import type { Dynamic, Member } from '../types/models';

interface CreateDynamicPageProps {
  currentMember: Member;
  onBack: () => void;
  onSubmit?: (params: {
    title: string;
    content: string;
    images: string[];
    authorName: string;
    authorAvatar: string | null;
    tags: string[];
  }) => void;
}

const MAX_IMAGES = 9;
const PLACEHOLDER_TEXT = '分享此刻的想法……';

export function CreateDynamicPage({
  currentMember,
  onBack,
  onSubmit,
}: CreateDynamicPageProps) {
  const [content, setContent] = useState('');
  const [imagePaths, setImagePaths] = useState<string[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const canPublish = content.trim().length > 0 || imagePaths.length > 0;

  const handleImageSelect = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files) return;
    const availableSlots = MAX_IMAGES - imagePaths.length;
    if (availableSlots <= 0) return;

    const newFiles = Array.from(files).slice(0, availableSlots);
    const newPaths: string[] = [];
    let loaded = 0;

    newFiles.forEach((file) => {
      const reader = new FileReader();
      reader.onload = (ev) => {
        if (ev.target?.result && typeof ev.target.result === 'string') {
          newPaths.push(ev.target.result);
        }
        loaded++;
        if (loaded === newFiles.length) {
          setImagePaths((prev) => [...prev, ...newPaths]);
        }
      };
      reader.readAsDataURL(file);
    });

    // Reset input so same file can be re-selected
    e.target.value = '';
  };

  const handleRemoveImage = (path: string) => {
    setImagePaths((prev) => prev.filter((p) => p !== path));
  };

  const handlePublish = () => {
    if (!canPublish) return;
    onSubmit?.({
      title: '',
      content,
      images: imagePaths,
      authorName: currentMember.name,
      authorAvatar: currentMember.avatarUrl,
      tags: [],
    });
    onBack();
  };

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
              编辑动态
            </div>
          </div>

          {/* Publish button */}
          <mdui-button-icon
            icon="check"
            style={{
              color: canPublish
                ? 'rgb(var(--mdui-color-primary))'
                : 'rgb(var(--mdui-color-on-surface), 0.38)',
              cursor: canPublish ? 'pointer' : 'default',
            }}
            onClick={canPublish ? handlePublish : undefined}
          ></mdui-button-icon>
        </div>
      </div>

      {/* ── Body ── */}
      <div style={{ flex: 1, padding: 16, overflowY: 'auto' }}>
        {/* Content textarea */}
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder={PLACEHOLDER_TEXT}
          maxLength={10000}
          style={{
            width: '100%',
            height: 200,
            border: 'none',
            outline: 'none',
            resize: 'none',
            fontSize: 16,
            lineHeight: '24px',
            fontFamily: 'inherit',
            color: 'rgb(var(--mdui-color-on-surface))',
            backgroundColor: 'transparent',
          }}
        />

        <div style={{ height: 16 }} />

        {/* Divider */}
        <div
          style={{
            height: 1,
            backgroundColor: 'rgba(var(--mdui-color-outline-variant), 0.3)',
            marginBottom: 16,
          }}
        />

        {/* Image grid */}
        <ImageGrid
          imagePaths={imagePaths}
          maxImages={MAX_IMAGES}
          onAddClick={handleImageSelect}
          onRemoveImage={handleRemoveImage}
        />
      </div>

      {/* Hidden file input */}
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        multiple
        style={{ display: 'none' }}
        onChange={handleFileChange}
      />
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// ImageGrid — mirrors the image grid in CreateDynamicScreen.kt
// ────────────────────────────────────────────────────────────

interface ImageGridProps {
  imagePaths: string[];
  maxImages: number;
  onAddClick: () => void;
  onRemoveImage: (path: string) => void;
}

function ImageGrid({ imagePaths, maxImages, onAddClick, onRemoveImage }: ImageGridProps) {
  const showAddButton = imagePaths.length < maxImages;
  const allItems: (string | 'ADD_BUTTON')[] = [...imagePaths];
  if (showAddButton) {
    allItems.push('ADD_BUTTON');
  }

  // Chunk into rows of 3
  const rows: (typeof allItems)[] = [];
  for (let i = 0; i < allItems.length; i += 3) {
    rows.push(allItems.slice(i, i + 3));
  }

  return (
    <div>
      {rows.map((row, rowIdx) => (
        <div key={rowIdx} style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
          {row.map((item, colIdx) => {
            if (item === 'ADD_BUTTON') {
              return (
                <div
                  key="add"
                  style={{
                    flex: 1,
                    aspectRatio: '1',
                    borderRadius: 8,
                    border: '2px dashed rgba(var(--mdui-color-outline), 0.5)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    cursor: 'pointer',
                  }}
                  onClick={onAddClick}
                >
                  <mdui-icon
                    name="add"
                    style={{ fontSize: 32, color: 'rgb(var(--mdui-color-on-surface-variant))' }}
                  ></mdui-icon>
                </div>
              );
            }
            return (
              <div
                key={`img-${colIdx}`}
                style={{
                  flex: 1,
                  aspectRatio: '1',
                  borderRadius: 8,
                  overflow: 'hidden',
                  position: 'relative',
                  backgroundColor: 'rgb(var(--mdui-color-surface-variant))',
                }}
              >
                <img
                  src={item}
                  alt=""
                  style={{
                    width: '100%',
                    height: '100%',
                    objectFit: 'cover',
                  }}
                />
                {/* Close button — mirrors error-colored Surface(12dp) + Close icon */}
                <div
                  onClick={() => onRemoveImage(item)}
                  style={{
                    position: 'absolute',
                    top: 2,
                    right: 2,
                    width: 20,
                    height: 20,
                    borderRadius: '50%',
                    backgroundColor: 'rgb(var(--mdui-color-error))',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    cursor: 'pointer',
                  }}
                >
                  <mdui-icon
                    name="close"
                    style={{
                      fontSize: 14,
                      color: 'rgb(var(--mdui-color-on-error))',
                    }}
                  ></mdui-icon>
                </div>
              </div>
            );
          })}
          {/* Fill remaining slots in the row */}
          {Array.from({ length: 3 - row.length }).map((_, i) => (
            <div key={`spacer-${i}`} style={{ flex: 1 }} />
          ))}
        </div>
      ))}
    </div>
  );
}