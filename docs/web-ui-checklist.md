# Selves Web 端 UI 1:1 还原进度清单
必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui
禁止幻想uiui遇到不确定的地方，再读取一遍文件。不要参考当前的网页ui，一定要参考安卓app的ui。

你是1:1还原，不能想你自己想要的东西,所有代码必须要在安卓侧能找到依据，禁止自己添加
> 目标：以 `app/src/main/java/com/selves/xnn/ui/screens/*` 为唯一参考，对 `web/src/pages/*` 进行 1:1 视觉与交互还原。除非遇到 Web 平台不可抗力（如系统级权限、原生服务），不得自行臆想 UI，必需严格按照安卓app的文件内容来进行1:1复刻ui
> 状态图例：⬜ 未开始 / 🟡 进行中 / ✅ 已完成 / ⚠️ 受限（已记录原因）

## 一、底层基建
| 模块 | 安卓源 | Web 目标 | 状态 |
| --- | --- | --- | --- |
| 主题 / 颜色方案 | `ui/theme/*` | `styles.css` + mdui 主题 | 🟡 |
| 应用脚手架（容器卡片）| `MainActivity.kt` + `AppNavigationScreen.kt` | `App.tsx` | 🟡 |
| 二级页面过渡动画（左右滑入/滑出）| `AppNavigationScreen.kt` | `App.tsx` Routes | ⬜ |
| 顶部 AppBar 通用组件 | 各 Screen `TopAppBar` | `pages/SubPageScaffold.tsx` | 🟡 已补 sticky surface 背景、底部分隔线、标题字重 normal，更接近安卓 TopAppBar |
| 头像组件 | `components/AvatarImage.kt` | `components/Avatar.tsx` | ⬜ |
| 底部导航栏 | `components/BottomNavBar.kt` | `App.tsx` `mdui-navigation-bar` | ✅ |

## 二、引导与加载
| 页面 | 安卓源 | Web 目标 | 状态 |
| --- | --- | --- | --- |
| 启动加载页 | `AppNavigationScreen.LoadingScreen` | `App.tsx` 加载分支 | 🟡 |
| 欢迎引导（创建系统/成员、导入备份） | `WelcomeGuideScreen.kt` | （待新增）`pages/WelcomeGuidePage.tsx` | ⬜ |

## 三、主导航三 Tab
| 页面 | 安卓源 | Web 目标 | 状态 |
| --- | --- | --- | --- |
| 主页 Home | `HomeScreen.kt` | `pages/HomePage.tsx` | ✅ 1:1 还原（编辑模式横幅 primaryContainer+"编辑模式"/"完成"按钮；模块始终显示含空状态；轨迹记录补全状态圆点8dp+三统计项Evenly；待办 TodoItem 补描述14sp+优先级Chip padding2x6 12sp+Checkbox；动态补formatDynamicTime时间+作者12sp primary；投票补描述2行省略+formatVoteRemaining截止+票数；日记补标题15sp 1行省略+内容2行省略+分隔线+超过2条"查看更多"；底部FAB空白间距80px） |
| 群聊列表 Chat | `GroupChatScreen.kt` | `pages/GroupChatPage.tsx` | ✅ 1:1 还原（顶部 UserInfoHeader：padding16/头像40/字号16/切换图标 primary 色；空态文案"暂无群聊，请点击右下角创建"；列表项 padding 16x12、头像52、群名 titleMedium 16semibold、消息 bodyMedium 14、时间 bodySmall 12 alpha0.7、空消息 alpha0.6+"暂无消息"、图片消息显示 [图片]、未读徽章 minW20 minH16 padding 2x6 圆角10 字色 surface；分隔线 marginLeft72 outline α0.3） |
| 系统 System | `SystemScreen.kt` | `pages/SystemPage.tsx` | ✅ 1:1 还原（SystemInfoCard 圆角16+padding20+surfaceContainer 填充；点击系统名缩放0.9 并展开"还没有系统简介"占位；分组标题"系统管理/其它"+裸点击行 4dp 横向 padding；列表项无右箭头无分隔线；hover 0.04/active 0.08 ripple 模拟） |

## 四、二级功能页面
| 页面 | 安卓源 | Web 目标 | 状态 |
| --- | --- | --- | --- |
| 待办事项 | `TodoScreen.kt` | `pages/TodoPage.tsx` | 🟡 已补齐统计卡片、空状态、待完成/已完成折叠分组、列表项时间与优先级样式；创建弹窗/长按底部弹窗仍待补 |
| 动态列表 | `DynamicScreen.kt` | `pages/DynamicPage.tsx` | ✅ 1:1 还原（自定义 TopBar：标题 fontWeight Normal/搜索栏动画切换；搜索栏 leading Search 图标+trailing 过滤图标循环 null→IMAGE→TEXT→null primary 色激活/Close 关闭；DynamicCard surfaceContainer 圆角12 elevation0 padding16；作者头像40+12gap+名字16sp Medium+时间12sp onSurfaceVariant；删除按钮仅 authorId==currentUserId 可见 error 色；标题18sp SemiBold 2行省略+8dp间距；内容14sp onSurfaceVariant；图片3列网格圆角4；标签AssistChip 4dp gap 12sp #tag；互动行：点赞Favorite/FavoriteBorder 20dp liked=error色+数量14sp、评论Comment 20dp+数量14sp；空状态 Timeline图标64dp opacity0.5+16dp间距+"暂无动态"16sp+8dp间距+"点击右下角按钮发布第一条动态"14sp opacity0.6；FAB fixed right16 bottom16） |
| 创建动态 | `CreateDynamicScreen.kt` | （待新增） | ⬜ |
| 动态详情 | `DynamicDetailScreen.kt` | （待新增） | ⬜ |
| 投票列表 | `VoteScreen.kt` | `pages/VotePage.tsx` | ✅ 1:1 还原（自定义 sticky TopBar：标题/搜索栏切换+FilterChip 进行中/已结束；VoteCard surfaceContainer 圆角16+padding16；作者头像44+名字15sp semibold+时间12sp onSurfaceVariant；操作按钮 stop/delete 仅作者可见；标题18sp bold 2行省略；描述14sp 3行省略；选项预览序号24dp secondaryContainer 圆角6+内容14sp+票数13sp Medium+百分比13sp bold primary；进度条6dp 圆角3 进行中primary/已结束secondary+右侧40dp百分比条；底部状态Chip圆角20+8dp状态圆点+剩余时间；people图标+参与人数；多选/匿名标签；已参与提示 primaryContainer 0.5 背景+check_circle；空状态 Poll图标64+进行/结束双文案+创建提示；FAB fixed right16 bottom16） |
| 创建投票 | `CreateVoteScreen.kt` | （待新增） | ⬜ |
| 投票详情 | `VoteDetailScreen.kt` | （待新增） | ⬜ |
| 成员日记 | `DiaryScreen.kt` | `pages/DiaryPage.tsx` | ✅ 1:1 还原（TopAppBar 标题 `${member.name} 的日记` fontWeight Normal；空状态 Edit 图标 64dp alpha0.5+"还没有日记"16sp+"点击右下角按钮新建日记"14sp alpha0.6；列表横向 padding16+纵向 contentPadding 16+item 间距 8；Card surfaceContainer 圆角16 elevation0；长按底部弹窗：标题+formatDetailDateTime 时间+分隔线+编辑/删除 TextButton 红色；删除确认 AlertDialog error 色确认按钮；创建/编辑弹窗 OutlinedTextField 标题单行过滤换行+内容 textarea rows5+内容空错误提示；FAB fixed right16 bottom16） |
| 轨迹记录 | `LocationTrackingScreen.kt` | `pages/LocationPage.tsx` | ⚠️ Web 无后台定位服务，仅做 UI 还原 |
| 成员管理 | `MemberManagementScreen.kt` | `pages/MemberManagementPage.tsx` | 🟡 已补搜索栏显隐、成员计数、视图模式切换入口、分组折叠结构、字母分组标题、成员行当前标记与列表间距；右侧字母索引与长按菜单仍待补 |
| 在线统计 | `OnlineStatsScreen.kt` | `pages/OnlineStatsPage.tsx` | ✅ 1:1 还原（TabRow 三标签：在线/时长/日志；OnlineStatItem 头像40+12spacer+name bodyLarge+当前成员6dp primary圆点+今日在线 bodySmall+状态 tertiary/onSurfaceVariant+右侧8dp圆点；时长页 Card surfaceContainer padding16 按分钟降序+右侧 titleMedium bold primary；日志页 LoginLogSummaryCard surfaceContainer 标题 bold+三列 SummaryItem headlineSmall bold+FilterChips+LoginLogItem surfaceContainer 头像40+登录/登出 bodySmall+空状态 Card padding32） |
| 设置 | `SettingsScreen.kt` | `pages/SettingsPage.tsx` | ✅ 1:1 还原（分组标题通用/数据与备份/Web访问/其他，颜色 primary titleMedium padding8x4；SettingsItem 24px图标+16spacer+title bodyLarge 16+subtitle bodyMedium 14 onSurfaceVariant；SettingsSwitchItem 同+Switch；SettingsItemWithProgress 同+loading时20dp CircularProgress；WebAccessInfoCard surfaceVariant 圆角12 padding16+URL labelMedium 12+bodyMedium 14 Medium+复制按钮+QR占位180dp+提示文案；语言/主题/配色弹窗使用 mdui-dialog） |
| 关于 | `AboutScreen.kt` | （待新增） | ⬜ |
| 开发者模式 | `DeveloperModeScreen.kt` | （待新增） | ⬜ |
| 群聊详情 | `ChatScreen.kt` | `pages/ChatDetailPage.tsx` | ⬜ |

## 五、关键弹窗 / 子组件（按主页面被调用时同步还原）
- `MemberSwitchDialog`、`QuickMemberSwitch`、`MemberSelectionDialog`
- `CreateMemberDialog` / `EditMemberDialog` / `CreateMemberForm`
- `CreateSystemDialog` / `SystemEditDialog` / `SystemSettingsDialog`
- `CreateGroupDialog` / `GroupManagementDialog` / `GroupDescriptionEditDialog`
- `CreateTodoDialog`
- `CreateVoteDialog` / `EditDynamicDialog`
- `BackupDialogs`、`MemberManagementDialog`、`OnlineStatsDialog`
- `LocationTrackingConfigDialog` / `LocationTrackingPreview` / `LocationTrackingSection` / `LocationTimelineItem`
- `DiaryEditDialog`
- `ColorSchemeDialog` / `ThemeModeDialog` / `LanguageDialog`
- `AlphabetIndexBar`、`MonthCalendar`、`ImageViewer`、`UserInfoHeader`、`DatePickerDialog`、`AvatarImage`、`SystemAvatarImage`

## 六、当前迭代计划
按用户优先级一次还原一个页面，完成一个就在本表打勾并附"还原要点"备注。
