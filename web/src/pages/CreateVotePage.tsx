import { useState, useRef } from 'react';
import type { Member } from '../types/models';

interface CreateVotePageProps {
  currentMember: Member;
  onBack: () => void;
  onSubmit?: (params: {
    title: string;
    description: string;
    options: string[];
    endTime: string | null;
    allowMultipleChoice: boolean;
    isAnonymous: boolean;
    authorName: string;
    authorAvatar: string | null;
  }) => void;
}

export function CreateVotePage({ currentMember, onBack, onSubmit }: CreateVotePageProps) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [options, setOptions] = useState<string[]>(['', '']);
  const [endTime, setEndTime] = useState<string | null>(null);
  const [allowMultipleChoice, setAllowMultipleChoice] = useState(false);
  const [isAnonymous, setIsAnonymous] = useState(false);
  const [showDatePicker, setShowDatePicker] = useState(false);
  const dateInputRef = useRef<HTMLInputElement>(null);

  const validOptions = options.filter((o) => o.trim().length > 0);
  const canPublish =
    title.trim().length > 0 && description.trim().length > 0 && validOptions.length >= 2;

  const handlePublish = () => {
    if (!canPublish || !currentMember) return;
    onSubmit?.({
      title: title.trim(),
      description: description.trim(),
      options: validOptions,
      endTime,
      allowMultipleChoice,
      isAnonymous,
      authorName: currentMember.name,
      authorAvatar: currentMember.avatarUrl,
    });
    onBack();
  };

  const handleOptionChange = (index: number, value: string) => {
    const next = [...options];
    next[index] = value;
    setOptions(next);
  };

  const handleRemoveOption = (index: number) => {
    if (options.length <= 2) return;
    setOptions(options.filter((_, i) => i !== index));
  };

  const handleAddOption = () => {
    if (options.length >= 10) return;
    setOptions([...options, '']);
  };

  const handleDateConfirm = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    if (val) {
      const d = new Date(val);
      d.setHours(23, 59, 59, 999);
      setEndTime(d.toISOString());
    }
    setShowDatePicker(false);
  };

  const formatEndTime = (iso: string | null): string => {
    if (!iso) return '不限制';
    const d = new Date(iso);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const hh = String(d.getHours()).padStart(2, '0');
    const mm = String(d.getMinutes()).padStart(2, '0');
    return `截止: ${y}-${m}-${day} ${hh}:${mm}`;
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
              创建投票
            </div>
          </div>

          {/* Publish button */}
          <span
            onClick={canPublish ? handlePublish : undefined}
            style={{
              cursor: canPublish ? 'pointer' : 'default',
              fontSize: 14,
              fontWeight: 500,
              color: canPublish
                ? 'rgb(var(--mdui-color-primary))'
                : 'rgba(var(--mdui-color-on-surface), 0.38)',
              padding: '0 12px',
              lineHeight: '36px',
            }}
          >
            发布
          </span>
        </div>
      </div>

      {/* ── Body ── */}
      <div style={{ flex: 1, overflowY: 'auto', padding: 16 }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {/* ── VoteInfoCard ── */}
          <div
            style={{
              borderRadius: 12,
              backgroundColor: 'rgb(var(--mdui-color-surface-container-low))',
              padding: 16,
            }}
          >
            <SectionHeader
              icon="title"
              title="投票信息"
              subtitle="填写投票的基本信息"
            />

            <div style={{ height: 16 }} />

            <div
              style={{
                position: 'relative',
                border: '1px solid rgba(var(--mdui-color-outline), 0.5)',
                borderRadius: 4,
                padding: '16px 12px 4px',
              }}
            >
              <span
                style={{
                  position: 'absolute',
                  top: -8,
                  left: 12,
                  fontSize: 12,
                  color: 'rgb(var(--mdui-color-on-surface-variant))',
                  backgroundColor: 'rgb(var(--mdui-color-surface-container-low))',
                  padding: '0 4px',
                }}
              >
                投票标题
              </span>
              <input
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="请输入投票标题"
                style={{
                  width: '100%',
                  border: 'none',
                  outline: 'none',
                  fontSize: 16,
                  lineHeight: '24px',
                  fontFamily: 'inherit',
                  color: 'rgb(var(--mdui-color-on-surface))',
                  backgroundColor: 'transparent',
                }}
              />
            </div>

            <div style={{ height: 12 }} />

            <div
              style={{
                position: 'relative',
                border: '1px solid rgba(var(--mdui-color-outline), 0.5)',
                borderRadius: 4,
                padding: '16px 12px 4px',
              }}
            >
              <span
                style={{
                  position: 'absolute',
                  top: -8,
                  left: 12,
                  fontSize: 12,
                  color: 'rgb(var(--mdui-color-on-surface-variant))',
                  backgroundColor: 'rgb(var(--mdui-color-surface-container-low))',
                  padding: '0 4px',
                }}
              >
                投票描述
              </span>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="请输入投票描述"
                style={{
                  width: '100%',
                  minHeight: 72,
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
            </div>
          </div>

          {/* ── VoteOptionsCard ── */}
          <div
            style={{
              borderRadius: 12,
              backgroundColor: 'rgb(var(--mdui-color-surface-container-low))',
              padding: 16,
            }}
          >
            <SectionHeader
              icon="list"
              title="投票选项"
              subtitle="至少添加2个选项"
            />

            <div style={{ height: 16 }} />

            {options.map((option, index) => (
              <div key={index}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 0 }}>
                  <div style={{ flex: 1, position: 'relative' }}>
                    <div
                      style={{
                        border: '1px solid rgba(var(--mdui-color-outline), 0.5)',
                        borderRadius: 4,
                        padding: '16px 12px 4px',
                      }}
                    >
                      <span
                        style={{
                          position: 'absolute',
                          top: -8,
                          left: 12,
                          fontSize: 12,
                          color: 'rgb(var(--mdui-color-on-surface-variant))',
                          backgroundColor: 'rgb(var(--mdui-color-surface-container-low))',
                          padding: '0 4px',
                        }}
                      >
                        选项 {index + 1}
                      </span>
                      <input
                        value={option}
                        onChange={(e) => handleOptionChange(index, e.target.value)}
                        placeholder={`选项 ${index + 1}`}
                        style={{
                          width: '100%',
                          border: 'none',
                          outline: 'none',
                          fontSize: 16,
                          lineHeight: '24px',
                          fontFamily: 'inherit',
                          color: 'rgb(var(--mdui-color-on-surface))',
                          backgroundColor: 'transparent',
                        }}
                      />
                    </div>
                  </div>

                  {options.length > 2 ? (
                    <mdui-button-icon
                      icon="delete"
                      style={{
                        color: 'rgb(var(--mdui-color-error))',
                        flexShrink: 0,
                      }}
                      onClick={() => handleRemoveOption(index)}
                    ></mdui-button-icon>
                  ) : (
                    <div style={{ width: 48, flexShrink: 0 }} />
                  )}
                </div>

                {index < options.length - 1 && <div style={{ height: 8 }} />}
              </div>
            ))}

            <div style={{ height: 12 }} />

            {options.length < 10 && (
              <div
                onClick={handleAddOption}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: 8,
                  padding: '0 24px',
                  height: 40,
                  borderRadius: 20,
                  border: '1px solid rgb(var(--mdui-color-outline))',
                  cursor: 'pointer',
                  color: 'rgb(var(--mdui-color-primary))',
                  fontSize: 14,
                  fontWeight: 500,
                }}
              >
                <mdui-icon name="add" style={{ fontSize: 18 }}></mdui-icon>
                <span>添加选项</span>
              </div>
            )}
          </div>

          {/* ── VoteSettingsCard ── */}
          <div
            style={{
              borderRadius: 12,
              backgroundColor: 'rgb(var(--mdui-color-surface-container-low))',
              padding: 16,
            }}
          >
            <SectionHeader
              icon="settings"
              title="投票设置"
              subtitle="配置投票的附加选项"
            />

            <div style={{ height: 16 }} />

            {/* Multiple choice */}
            <SettingsRow
              icon="list"
              title="允许多选"
              subtitle="允许选择多个选项"
              trailing={
                <mdui-switch
                  checked={allowMultipleChoice}
                  onClick={() => setAllowMultipleChoice(!allowMultipleChoice)}
                ></mdui-switch>
              }
            />

            <div style={{ height: 12 }} />

            {/* Anonymous */}
            <SettingsRow
              icon="visibility_off"
              title="匿名投票"
              subtitle="投票结果不显示投票者"
              trailing={
                <mdui-switch
                  checked={isAnonymous}
                  onClick={() => setIsAnonymous(!isAnonymous)}
                ></mdui-switch>
              }
            />

            <div style={{ height: 12 }} />

            {/* End time */}
            <SettingsRow
              icon="schedule"
              title="投票截止时间"
              subtitle={formatEndTime(endTime)}
              onClick={() => setShowDatePicker(true)}
              trailing={
                endTime ? (
                  <mdui-button-icon
                    icon="close"
                    onClick={(e) => {
                      e.stopPropagation();
                      setEndTime(null);
                    }}
                  ></mdui-button-icon>
                ) : null
              }
            />
          </div>
        </div>
      </div>

      {/* ── DatePicker overlay ── */}
      {showDatePicker && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            zIndex: 100,
            backgroundColor: 'rgba(0,0,0,0.3)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
          onClick={() => setShowDatePicker(false)}
        >
          <div
            onClick={(e) => e.stopPropagation()}
            style={{
              borderRadius: 28,
              backgroundColor: 'rgb(var(--mdui-color-surface))',
              padding: 24,
              minWidth: 300,
            }}
          >
            <div
              style={{
                fontSize: 16,
                fontWeight: 500,
                color: 'rgb(var(--mdui-color-on-surface))',
                marginBottom: 16,
              }}
            >
              选择截止日期
            </div>
            <input
              ref={dateInputRef}
              type="date"
              onChange={handleDateConfirm}
              style={{
                width: '100%',
                padding: '12px',
                fontSize: 16,
                border: '1px solid rgba(var(--mdui-color-outline), 0.5)',
                borderRadius: 8,
                fontFamily: 'inherit',
                color: 'rgb(var(--mdui-color-on-surface))',
                backgroundColor: 'rgb(var(--mdui-color-surface-container-high))',
                boxSizing: 'border-box',
              }}
            />
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 16 }}>
              <span
                onClick={() => setShowDatePicker(false)}
                style={{
                  padding: '8px 16px',
                  fontSize: 14,
                  fontWeight: 500,
                  color: 'rgb(var(--mdui-color-primary))',
                  cursor: 'pointer',
                  borderRadius: 20,
                }}
              >
                取消
              </span>
              <span
                onClick={() => {
                  const input = dateInputRef.current;
                  if (input?.value) {
                    const d = new Date(input.value);
                    d.setHours(23, 59, 59, 999);
                    setEndTime(d.toISOString());
                  }
                  setShowDatePicker(false);
                }}
                style={{
                  padding: '8px 16px',
                  fontSize: 14,
                  fontWeight: 500,
                  color: 'rgb(var(--mdui-color-primary))',
                  cursor: 'pointer',
                  borderRadius: 20,
                }}
              >
                确定
              </span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// SectionHeader — mirrors SectionHeader in CreateVoteScreen.kt
// ────────────────────────────────────────────────────────────

function SectionHeader({
  icon,
  title,
  subtitle,
}: {
  icon: string;
  title: string;
  subtitle: string;
}) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', width: '100%' }}>
      <div
        style={{
          width: 36,
          height: 36,
          borderRadius: 10,
          backgroundColor: 'rgb(var(--mdui-color-primary-container))',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          flexShrink: 0,
        }}
      >
        <mdui-icon
          name={icon}
          style={{
            fontSize: 20,
            color: 'rgb(var(--mdui-color-on-primary-container))',
          }}
        ></mdui-icon>
      </div>
      <div style={{ width: 12 }} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div
          style={{
            fontSize: 14,
            fontWeight: 600,
            color: 'rgb(var(--mdui-color-on-surface))',
            lineHeight: 1.3,
          }}
        >
          {title}
        </div>
        <div
          style={{
            fontSize: 12,
            color: 'rgb(var(--mdui-color-on-surface-variant))',
            lineHeight: 1.3,
          }}
        >
          {subtitle}
        </div>
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// SettingsRow — mirrors SettingsRow in CreateVoteScreen.kt
// ────────────────────────────────────────────────────────────

function SettingsRow({
  icon,
  title,
  subtitle,
  trailing,
  onClick,
}: {
  icon: string;
  title: string;
  subtitle: string;
  trailing?: React.ReactNode;
  onClick?: () => void;
}) {
  return (
    <div
      onClick={onClick}
      style={{
        display: 'flex',
        alignItems: 'center',
        width: '100%',
        padding: '8px 0',
        cursor: onClick ? 'pointer' : 'default',
      }}
    >
      <mdui-icon
        name={icon}
        style={{
          fontSize: 24,
          color: 'rgb(var(--mdui-color-on-surface-variant))',
          flexShrink: 0,
        }}
      ></mdui-icon>
      <div style={{ width: 12 }} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div
          style={{
            fontSize: 14,
            color: 'rgb(var(--mdui-color-on-surface))',
            lineHeight: 1.4,
          }}
        >
          {title}
        </div>
        <div
          style={{
            fontSize: 12,
            color: 'rgb(var(--mdui-color-on-surface-variant))',
            lineHeight: 1.4,
          }}
        >
          {subtitle}
        </div>
      </div>
      {trailing}
    </div>
  );
}