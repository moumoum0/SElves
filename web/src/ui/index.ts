/**
 * src/ui 组件库统一导出入口。
 *
 * 使用方式：
 *   import { Button, TextField, Dialog } from '@/ui';
 *   // 或具体路径：import { Button } from '@/ui/components/Button';
 */

// 主题
export { getThemeMode, setThemeMode, initTheme } from './theme/themeManager';
export type { ThemeMode } from './theme/themeManager';

// 组件
export { Button } from './components/Button';
export type { ButtonProps, ButtonVariant } from './components/Button';

export { IconButton } from './components/IconButton';
export type { IconButtonProps, IconButtonVariant } from './components/IconButton';

export { TextField } from './components/TextField';
export type { TextFieldProps, TextFieldVariant } from './components/TextField';

export { Switch } from './components/Switch';
export type { SwitchProps } from './components/Switch';

export { Checkbox } from './components/Checkbox';
export type { CheckboxProps } from './components/Checkbox';

export { Dialog } from './components/Dialog';
export type { DialogProps } from './components/Dialog';

export { List, ListItem } from './components/List';
export type { ListProps, ListItemProps } from './components/List';

export { CircularProgress, LinearProgress } from './components/Progress';
export type { CircularProgressProps, LinearProgressProps } from './components/Progress';

export { Icon } from './components/Icon';
export type { IconProps } from './components/Icon';

export { Card } from './components/Card';
export type { CardProps, CardVariant } from './components/Card';

export { FAB } from './components/FAB';
export type { FABProps, FABSize, FABVariant } from './components/FAB';

export { Chip } from './components/Chip';
export type { ChipProps, ChipVariant } from './components/Chip';

export { Radio } from './components/Radio';
export type { RadioProps } from './components/Radio';

export { Tabs, Tab } from './components/Tabs';
export type { TabsProps, TabProps } from './components/Tabs';
