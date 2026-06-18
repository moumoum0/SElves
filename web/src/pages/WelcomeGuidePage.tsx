import { useState, useEffect } from 'react';

/**
 * 引导步骤枚举 - 与安卓 GuideStep 对齐
 */
type GuideStep = 'WELCOME' | 'IMPORT_OR_CREATE' | 'CREATE_SYSTEM' | 'CREATE_MEMBER' | 'COMPLETE';

interface WelcomeGuidePageProps {
  onCreateSystem: (name: string, avatarUrl: string) => void;
  onCreateMember: (name: string, avatarUrl: string) => void;
  onImportBackup: () => void;
  onCompleteGuide: () => void;
}

export function WelcomeGuidePage({
  onCreateSystem,
  onCreateMember,
  onImportBackup,
  onCompleteGuide,
}: WelcomeGuidePageProps) {
  const [currentStep, setCurrentStep] = useState<GuideStep>('WELCOME');
  const [systemName, setSystemName] = useState('');
  const [systemAvatarUrl, setSystemAvatarUrl] = useState('');
  const [memberName, setMemberName] = useState('');
  const [memberAvatarUrl, setMemberAvatarUrl] = useState('');

  // 步骤索引 - 用于圆点指示器
  const steps: GuideStep[] = ['WELCOME', 'IMPORT_OR_CREATE', 'CREATE_SYSTEM', 'CREATE_MEMBER', 'COMPLETE'];
  const currentIndex = steps.indexOf(currentStep);

  const showBackButton = currentStep !== 'WELCOME' && currentStep !== 'IMPORT_OR_CREATE';
  const showNextButton = currentStep !== 'IMPORT_OR_CREATE';

  const handleNext = () => {
    switch (currentStep) {
      case 'WELCOME':
        setCurrentStep('IMPORT_OR_CREATE');
        break;
      case 'CREATE_SYSTEM':
        if (systemName.trim()) {
          onCreateSystem(systemName, systemAvatarUrl);
          setCurrentStep('CREATE_MEMBER');
        }
        break;
      case 'CREATE_MEMBER':
        if (memberName.trim()) {
          onCreateMember(memberName, memberAvatarUrl);
          setCurrentStep('COMPLETE');
        }
        break;
      case 'COMPLETE':
        onCompleteGuide();
        break;
      default:
        break;
    }
  };

  const handleBack = () => {
    switch (currentStep) {
      case 'CREATE_SYSTEM':
        setCurrentStep('IMPORT_OR_CREATE');
        break;
      case 'CREATE_MEMBER':
        setCurrentStep('CREATE_SYSTEM');
        break;
      case 'COMPLETE':
        setCurrentStep('CREATE_MEMBER');
        break;
      default:
        break;
    }
  };

  const getButtonText = () => {
    switch (currentStep) {
      case 'WELCOME':
        return 'Start';
      case 'COMPLETE':
        return 'Enter App';
      default:
        return 'Next';
    }
  };

  const isNextEnabled = () => {
    switch (currentStep) {
      case 'WELCOME':
        return true;
      case 'CREATE_SYSTEM':
        return systemName.trim().length > 0;
      case 'CREATE_MEMBER':
        return memberName.trim().length > 0;
      case 'COMPLETE':
        return true;
      default:
        return false;
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        display: 'flex',
        flexDirection: 'column',
        backgroundColor: 'rgb(var(--mdui-color-background))',
        zIndex: 1000,
      }}
    >
      {/* 主内容区域 */}
      <div style={{ flex: 1, overflow: 'hidden', position: 'relative' }}>
        <div
          className="guide-step-container"
          style={{
            position: 'absolute',
            inset: 0,
            display: 'flex',
            flexDirection: 'column',
          }}
        >
          {currentStep === 'WELCOME' && <WelcomeStepContent />}
          {currentStep === 'IMPORT_OR_CREATE' && (
            <ImportOrCreateStepContent
              onSelectImport={onImportBackup}
              onSelectCreate={() => setCurrentStep('CREATE_SYSTEM')}
            />
          )}
          {currentStep === 'CREATE_SYSTEM' && (
            <CreateSystemStepContent
              name={systemName}
              avatarUrl={systemAvatarUrl}
              onNameChange={setSystemName}
              onAvatarUrlChange={setSystemAvatarUrl}
            />
          )}
          {currentStep === 'CREATE_MEMBER' && (
            <CreateMemberStepContent
              name={memberName}
              avatarUrl={memberAvatarUrl}
              onNameChange={setMemberName}
              onAvatarUrlChange={setMemberAvatarUrl}
            />
          )}
          {currentStep === 'COMPLETE' && <CompleteStepContent />}
        </div>
      </div>

      {/* 底部操作栏 - 圆点指示器 + 按钮 */}
      {showNextButton && (
        <div
          style={{
            padding: '0 32px 24px',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
          }}
        >
          {/* 圆点指示器 */}
          <div
            style={{
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              marginBottom: 32,
              gap: 8,
            }}
          >
            {steps.map((_, index) => (
              <div
                key={index}
                style={{
                  width: index === currentIndex ? 8 : 6,
                  height: index === currentIndex ? 8 : 6,
                  borderRadius: '50%',
                  backgroundColor:
                    index === currentIndex
                      ? 'rgb(var(--mdui-color-primary))'
                      : 'rgb(var(--mdui-color-on-surface-variant))',
                  opacity: index <= currentIndex ? 1 : 0.3,
                  transition: 'all 0.3s ease',
                }}
              />
            ))}
          </div>

          {/* 按钮区域 */}
          <div
            style={{
              display: 'flex',
              width: '100%',
              justifyContent: showBackButton ? 'space-between' : 'center',
              alignItems: 'center',
            }}
          >
            {/* 返回按钮 */}
            {showBackButton && (
              <button
                onClick={handleBack}
                style={{
                  background: 'none',
                  border: 'none',
                  padding: '12px 16px',
                  cursor: 'pointer',
                  fontSize: 16,
                  color: 'rgb(var(--mdui-color-on-surface-variant))',
                }}
              >
                Back
              </button>
            )}

            {/* 主操作按钮 */}
            <button
              onClick={handleNext}
              disabled={!isNextEnabled()}
              style={{
                width: showBackButton ? 'auto' : '100%',
                height: 56,
                borderRadius: 28,
                border: 'none',
                backgroundColor: isNextEnabled()
                  ? 'rgb(var(--mdui-color-primary))'
                  : 'rgba(var(--mdui-color-on-surface), 0.12)',
                color: isNextEnabled()
                  ? 'rgb(var(--mdui-color-on-primary))'
                  : 'rgba(var(--mdui-color-on-surface), 0.38)',
                fontSize: 16,
                fontWeight: 600,
                cursor: isNextEnabled() ? 'pointer' : 'default',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 8,
                padding: '0 32px',
                transition: 'all 0.2s ease',
              }}
            >
              {getButtonText()}
              {currentStep !== 'COMPLETE' && (
                <span className="material-icons" style={{ fontSize: 20 }}>
                  arrow_forward
                </span>
              )}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

/* ============================================================ */
/* 欢迎页 - 澎湃OS风格：大字标题 + 品牌感 + 极简 */
/* ============================================================ */
function WelcomeStepContent() {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    setVisible(true);
  }, []);

  return (
    <div
      style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        padding: '0 32px',
        opacity: visible ? 1 : 0,
        transition: 'opacity 0.8s ease 0.2s',
      }}
    >
      <div style={{ flex: 0.3 }} />

      {/* 大标题 - 左对齐，澎湃OS风格 */}
      <div
        style={{
          fontSize: 48,
          lineHeight: '56px',
          fontWeight: 300,
          color: 'rgb(var(--mdui-color-on-background))',
          transform: visible ? 'translateY(0)' : 'translateY(40px)',
          opacity: visible ? 1 : 0,
          transition: 'all 0.8s cubic-bezier(0.0, 0.0, 0.2, 1) 0.2s',
        }}
      >
        Hello
      </div>

      <div style={{ height: 8 }} />

      <div
        style={{
          fontSize: 36,
          lineHeight: '44px',
          fontWeight: 700,
          color: 'rgb(var(--mdui-color-primary))',
          transform: visible ? 'translateY(0)' : 'translateY(40px)',
          opacity: visible ? 1 : 0,
          transition: 'all 0.8s cubic-bezier(0.0, 0.0, 0.2, 1) 0.2s',
        }}
      >
        Welcome to selves
      </div>

      <div style={{ height: 24 }} />

      {/* 副标题描述 */}
      <div
        style={{
          fontSize: 18,
          lineHeight: '28px',
          color: 'rgb(var(--mdui-color-on-surface-variant))',
          transform: visible ? 'translateY(0)' : 'translateY(30px)',
          opacity: visible ? 1 : 0,
          transition: 'all 0.8s cubic-bezier(0.0, 0.0, 0.2, 1) 0.5s',
        }}
      >
        For plural systems
      </div>

      <div style={{ flex: 0.5 }} />

      {/* 底部装饰线 */}
      <div
        style={{
          width: 40,
          height: 3,
          borderRadius: 2,
          background: `linear-gradient(to right, rgb(var(--mdui-color-primary)), rgba(var(--mdui-color-primary), 0.3))`,
          opacity: visible ? 1 : 0,
          transform: visible ? 'translateY(0)' : 'translateY(20px)',
          transition: 'all 0.8s cubic-bezier(0.0, 0.0, 0.2, 1) 0.5s',
        }}
      />

      <div style={{ flex: 0.2 }} />
    </div>
  );
}

/* ============================================================ */
/* 选择方式页 - 简洁卡片选择 */
/* ============================================================ */
function ImportOrCreateStepContent({
  onSelectImport,
  onSelectCreate,
}: {
  onSelectImport: () => void;
  onSelectCreate: () => void;
}) {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    setVisible(true);
  }, []);

  return (
    <div
      style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        padding: '64px 32px 32px',
        opacity: visible ? 1 : 0,
        transition: 'opacity 0.6s ease 0.1s',
        overflowY: 'auto',
      }}
    >
      {/* 标题区域 - 左对齐 */}
      <div
        style={{
          fontSize: 32,
          fontWeight: 700,
          color: 'rgb(var(--mdui-color-on-background))',
        }}
      >
        Choose Your Path
      </div>

      <div style={{ height: 8 }} />

      <div
        style={{
          fontSize: 16,
          lineHeight: '24px',
          color: 'rgb(var(--mdui-color-on-surface-variant))',
        }}
      >
        Create your system and first member
      </div>

      <div style={{ height: 48 }} />

      {/* 创建新系统 - 主要选项 */}
      <GuideOptionCard
        icon="add"
        title="Start Fresh"
        subtitle="Create your system and first member"
        isPrimary={true}
        onClick={onSelectCreate}
      />

      <div style={{ height: 16 }} />

      {/* 导入备份 - 次要选项 */}
      <GuideOptionCard
        icon="cloud_download"
        title="Import Backup"
        subtitle="Restore all data from backup file"
        isPrimary={false}
        onClick={onSelectImport}
      />

      <div style={{ height: 32 }} />
    </div>
  );
}

/* ============================================================ */
/* 引导选项卡片 - 澎湃OS风格 */
/* ============================================================ */
function GuideOptionCard({
  icon,
  title,
  subtitle,
  isPrimary,
  onClick,
}: {
  icon: string;
  title: string;
  subtitle: string;
  isPrimary: boolean;
  onClick: () => void;
}) {
  return (
    <div
      onClick={onClick}
      style={{
        width: '100%',
        borderRadius: 20,
        backgroundColor: isPrimary
          ? 'rgba(var(--mdui-color-primary-container), 0.5)'
          : 'rgb(var(--mdui-color-surface-container-high))',
        cursor: 'pointer',
        transition: 'transform 0.15s ease',
      }}
      onMouseDown={(e) => {
        (e.currentTarget as HTMLElement).style.transform = 'scale(0.98)';
      }}
      onMouseUp={(e) => {
        (e.currentTarget as HTMLElement).style.transform = 'scale(1)';
      }}
      onMouseLeave={(e) => {
        (e.currentTarget as HTMLElement).style.transform = 'scale(1)';
      }}
    >
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          padding: 24,
          gap: 20,
        }}
      >
        {/* 图标容器 */}
        <div
          style={{
            width: 52,
            height: 52,
            borderRadius: 16,
            backgroundColor: isPrimary
              ? 'rgba(var(--mdui-color-primary), 0.12)'
              : 'rgba(var(--mdui-color-on-surface), 0.06)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0,
          }}
        >
          <span
            className="material-icons"
            style={{
              fontSize: 26,
              color: isPrimary
                ? 'rgb(var(--mdui-color-primary))'
                : 'rgb(var(--mdui-color-on-surface-variant))',
            }}
          >
            {icon}
          </span>
        </div>

        {/* 文字区域 */}
        <div style={{ flex: 1, minWidth: 0 }}>
          <div
            style={{
              fontSize: 16,
              fontWeight: 600,
              color: 'rgb(var(--mdui-color-on-surface))',
            }}
          >
            {title}
          </div>
          <div
            style={{
              fontSize: 14,
              color: 'rgb(var(--mdui-color-on-surface-variant))',
              marginTop: 4,
            }}
          >
            {subtitle}
          </div>
        </div>

        {/* 箭头 */}
        <span
          className="material-icons"
          style={{
            fontSize: 20,
            color: 'rgba(var(--mdui-color-on-surface-variant), 0.5)',
            flexShrink: 0,
          }}
        >
          arrow_forward_ios
        </span>
      </div>
    </div>
  );
}

/* ============================================================ */
/* 创建系统页 */
/* ============================================================ */
function CreateSystemStepContent({
  name,
  avatarUrl,
  onNameChange,
  onAvatarUrlChange,
}: {
  name: string;
  avatarUrl: string;
  onNameChange: (name: string) => void;
  onAvatarUrlChange: (url: string) => void;
}) {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    setVisible(true);
  }, []);

  return (
    <div
      style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        padding: '64px 32px 32px',
        opacity: visible ? 1 : 0,
        transition: 'opacity 0.6s ease 0.1s',
        overflowY: 'auto',
      }}
    >
      {/* 标题 - 左对齐 */}
      <div
        style={{
          fontSize: 32,
          fontWeight: 700,
          color: 'rgb(var(--mdui-color-on-background))',
        }}
      >
        Create Your System
      </div>

      <div style={{ height: 8 }} />

      <div
        style={{
          fontSize: 16,
          lineHeight: '24px',
          color: 'rgb(var(--mdui-color-on-surface-variant))',
        }}
      >
        Give your system a name
      </div>

      <div style={{ height: 48 }} />

      {/* 表单区域 */}
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        {/* 头像选择区域 */}
        <div
          style={{
            width: 120,
            height: 120,
            borderRadius: '50%',
            backgroundColor: 'rgb(var(--mdui-color-surface-variant))',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer',
            position: 'relative',
            overflow: 'hidden',
          }}
          onClick={() => {
            // Web 端暂时不支持图片选择
          }}
        >
          {avatarUrl ? (
            <img
              src={avatarUrl}
              alt="System Avatar"
              style={{
                width: '100%',
                height: '100%',
                objectFit: 'cover',
                borderRadius: '50%',
              }}
            />
          ) : (
            <div style={{ textAlign: 'center' }}>
              <span
                className="material-icons"
                style={{
                  fontSize: 48,
                  color: 'rgb(var(--mdui-color-on-surface-variant))',
                }}
              >
                person
              </span>
              <div
                style={{
                  fontSize: 12,
                  color: 'rgb(var(--mdui-color-on-surface-variant))',
                  marginTop: 8,
                }}
              >
                Select Avatar
              </div>
            </div>
          )}

          {/* 相机图标叠加 */}
          <div
            style={{
              position: 'absolute',
              bottom: 4,
              right: 4,
              width: 32,
              height: 32,
              borderRadius: '50%',
              backgroundColor: 'rgb(var(--mdui-color-primary))',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <span
              className="material-icons"
              style={{
                fontSize: 18,
                color: 'rgb(var(--mdui-color-surface))',
              }}
            >
              photo_camera
            </span>
          </div>
        </div>

        <div style={{ height: 32 }} />

        {/* 系统名称输入 */}
        <div style={{ width: '100%' }}>
          <mdui-text-field
            label="System Name"
            placeholder="Enter system name"
            value={name}
            onInput={(e) => {
              const target = e.target as HTMLInputElement;
              onNameChange(target.value);
            }}
            style={{ width: '100%' }}
          />
        </div>
      </div>

      <div style={{ height: 16 }} />

      {/* 提示文字 */}
      <div
        style={{
          fontSize: 12,
          color: 'rgba(var(--mdui-color-on-surface-variant), 0.6)',
          textAlign: 'center',
        }}
      >
        You can modify these later
      </div>
    </div>
  );
}

/* ============================================================ */
/* 添加成员页 */
/* ============================================================ */
function CreateMemberStepContent({
  name,
  avatarUrl,
  onNameChange,
  onAvatarUrlChange,
}: {
  name: string;
  avatarUrl: string;
  onNameChange: (name: string) => void;
  onAvatarUrlChange: (url: string) => void;
}) {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    setVisible(true);
  }, []);

  return (
    <div
      style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        padding: '64px 32px 32px',
        opacity: visible ? 1 : 0,
        transition: 'opacity 0.6s ease 0.1s',
        overflowY: 'auto',
      }}
    >
      {/* 标题 - 左对齐 */}
      <div
        style={{
          fontSize: 32,
          fontWeight: 700,
          color: 'rgb(var(--mdui-color-on-background))',
        }}
      >
        Add Your First Member
      </div>

      <div style={{ height: 8 }} />

      <div
        style={{
          fontSize: 16,
          lineHeight: '24px',
          color: 'rgb(var(--mdui-color-on-surface-variant))',
        }}
      >
        You can add more members later
      </div>

      <div style={{ height: 48 }} />

      {/* 表单区域 */}
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        {/* 头像选择区域 */}
        <div
          style={{
            width: 120,
            height: 120,
            borderRadius: '50%',
            backgroundColor: 'rgb(var(--mdui-color-surface-variant))',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer',
            position: 'relative',
            overflow: 'hidden',
          }}
          onClick={() => {
            // Web 端暂时不支持图片选择
          }}
        >
          {avatarUrl ? (
            <img
              src={avatarUrl}
              alt="Member Avatar"
              style={{
                width: '100%',
                height: '100%',
                objectFit: 'cover',
                borderRadius: '50%',
              }}
            />
          ) : (
            <div style={{ textAlign: 'center' }}>
              <span
                className="material-icons"
                style={{
                  fontSize: 48,
                  color: 'rgb(var(--mdui-color-on-surface-variant))',
                }}
              >
                person
              </span>
              <div
                style={{
                  fontSize: 12,
                  color: 'rgb(var(--mdui-color-on-surface-variant))',
                  marginTop: 8,
                }}
              >
                Select Avatar
              </div>
            </div>
          )}

          {/* 相机图标叠加 */}
          <div
            style={{
              position: 'absolute',
              bottom: 4,
              right: 4,
              width: 32,
              height: 32,
              borderRadius: '50%',
              backgroundColor: 'rgb(var(--mdui-color-primary))',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <span
              className="material-icons"
              style={{
                fontSize: 18,
                color: 'rgb(var(--mdui-color-surface))',
              }}
            >
              photo_camera
            </span>
          </div>
        </div>

        <div style={{ height: 32 }} />

        {/* 成员名称输入 */}
        <div style={{ width: '100%' }}>
          <mdui-text-field
            label="Member Name"
            placeholder="Enter member name"
            value={name}
            onInput={(e) => {
              const target = e.target as HTMLInputElement;
              // 过滤掉回车和换行符
              onNameChange(target.value.replace(/\n/g, ''));
            }}
            style={{ width: '100%' }}
          />
        </div>
      </div>

      <div style={{ height: 16 }} />

      {/* 提示文字 */}
      <div
        style={{
          fontSize: 12,
          color: 'rgba(var(--mdui-color-on-surface-variant), 0.6)',
          textAlign: 'center',
        }}
      >
        Add more members in settings later
      </div>
    </div>
  );
}

/* ============================================================ */
/* 完成页 - 简洁庆祝 */
/* ============================================================ */
function CompleteStepContent() {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    setVisible(true);
  }, []);

  return (
    <div
      style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        padding: '0 32px',
      }}
    >
      <div style={{ flex: 0.35 }} />

      {/* 成功图标 - 带弹性动画 */}
      <div
        style={{
          fontSize: 80,
          color: 'rgb(var(--mdui-color-primary))',
          transform: visible ? 'scale(1)' : 'scale(0)',
          transition: 'transform 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) 0.2s',
        }}
      >
        <span className="material-icons" style={{ fontSize: 80 }}>
          check_circle
        </span>
      </div>

      <div style={{ height: 32 }} />

      {/* 完成标题 */}
      <div
        style={{
          fontSize: 36,
          fontWeight: 700,
          color: 'rgb(var(--mdui-color-on-background))',
          textAlign: 'center',
          opacity: visible ? 1 : 0,
          transform: visible ? 'translateY(0)' : 'translateY(20px)',
          transition: 'all 0.8s cubic-bezier(0.0, 0.0, 0.2, 1) 0.4s',
        }}
      >
        All Set
      </div>

      <div style={{ height: 16 }} />

      <div
        style={{
          fontSize: 18,
          lineHeight: '28px',
          color: 'rgb(var(--mdui-color-on-surface-variant))',
          textAlign: 'center',
          opacity: visible ? 1 : 0,
          transform: visible ? 'translateY(0)' : 'translateY(20px)',
          transition: 'all 0.8s cubic-bezier(0.0, 0.0, 0.2, 1) 0.4s',
        }}
      >
        All information is stored locally
      </div>

      <div style={{ height: 48 }} />

      {/* 功能提示卡片 */}
      <div
        style={{
          width: '100%',
          borderRadius: 20,
          backgroundColor: 'rgba(var(--mdui-color-surface-container-high), 0.7)',
          padding: 24,
          opacity: visible ? 1 : 0,
          transform: visible ? 'translateY(0)' : 'translateY(20px)',
          transition: 'all 0.8s cubic-bezier(0.0, 0.0, 0.2, 1) 0.4s',
        }}
      >
        <div
          style={{
            fontSize: 14,
            fontWeight: 600,
            color: 'rgb(var(--mdui-color-on-surface))',
          }}
        >
          You can always
        </div>

        <div style={{ height: 16 }} />

        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          <GuideFeatureItem text="Add more members" />
          <GuideFeatureItem text="Modify system information" />
          <GuideFeatureItem text="Backup and restore data" />
        </div>
      </div>

      <div style={{ flex: 0.35 }} />
    </div>
  );
}

/* ============================================================ */
/* 功能提示项 */
/* ============================================================ */
function GuideFeatureItem({ text }: { text: string }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center' }}>
      <div
        style={{
          width: 6,
          height: 6,
          borderRadius: '50%',
          backgroundColor: 'rgba(var(--mdui-color-primary), 0.6)',
          flexShrink: 0,
        }}
      />
      <div style={{ width: 12 }} />
      <div
        style={{
          fontSize: 14,
          color: 'rgb(var(--mdui-color-on-surface-variant))',
        }}
      >
        {text}
      </div>
    </div>
  );
}