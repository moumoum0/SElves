/**
 * @material/web 组件按需注册入口。
 * 集中管理，避免注册散落到各页面。Phase 1 先注册最核心的一批；
 * 随着迁移推进在此处追加即可。
 */

// 通用基础
import '@material/web/icon/icon.js';
import '@material/web/elevation/elevation.js';
import '@material/web/ripple/ripple.js';
import '@material/web/divider/divider.js';
import '@material/web/focus/md-focus-ring.js';

// 按钮
import '@material/web/button/filled-button.js';
import '@material/web/button/outlined-button.js';
import '@material/web/button/text-button.js';
import '@material/web/button/filled-tonal-button.js';
import '@material/web/button/elevated-button.js';
import '@material/web/iconbutton/icon-button.js';
import '@material/web/iconbutton/filled-icon-button.js';
import '@material/web/iconbutton/filled-tonal-icon-button.js';
import '@material/web/iconbutton/outlined-icon-button.js';
import '@material/web/fab/fab.js';

// 表单控件
import '@material/web/textfield/filled-text-field.js';
import '@material/web/textfield/outlined-text-field.js';
import '@material/web/switch/switch.js';
import '@material/web/checkbox/checkbox.js';
import '@material/web/radio/radio.js';

// 对话框 / 列表 / 卡片
import '@material/web/dialog/dialog.js';
import '@material/web/list/list.js';
import '@material/web/list/list-item.js';
import '@material/web/labs/card/elevated-card.js';
import '@material/web/labs/card/filled-card.js';
import '@material/web/labs/card/outlined-card.js';

// 进度指示器
import '@material/web/progress/circular-progress.js';
import '@material/web/progress/linear-progress.js';

// 导航（labs）
import '@material/web/labs/navigationbar/navigation-bar.js';
import '@material/web/labs/navigationtab/navigation-tab.js';

// Chips
import '@material/web/chips/chip-set.js';
import '@material/web/chips/assist-chip.js';
import '@material/web/chips/filter-chip.js';
import '@material/web/chips/input-chip.js';
import '@material/web/chips/suggestion-chip.js';

// Tabs
import '@material/web/tabs/tabs.js';
import '@material/web/tabs/primary-tab.js';
import '@material/web/tabs/secondary-tab.js';
