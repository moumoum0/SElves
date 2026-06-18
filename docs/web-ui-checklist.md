# Selves Web 端 UI 1:1 还原进度清单
必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui必需严格按照安卓app的文件内容来进行1:1复刻ui
禁止幻想uiui遇到不确定的地方，再读取一遍文件。不要参考当前的网页ui，一定要参考安卓app的ui。

你是1:1还原，不能想你自己想要的东西,所有代码必须要在安卓侧能找到依据，禁止自己添加
> 目标：以 `app/src/main/java/com/selves/xnn/ui/screens/*` 为唯一参考，对 `web/src/pages/*` 进行 1:1 视觉与交互还原。除非遇到 Web 平台不可抗力（如系统级权限、原生服务），不得自行臆想 UI，必需严格按照安卓app的文件内容来进行1:1复刻ui
> 状态图例：⬜ 未开始 / 🟡 进行中 / ✅ 已完成 / ⚠️ 受限（已记录原因）

## 一、底层基建
| 模块 | 安卓源 | Web 目标 | 状态 |
| --- | --- | --- | --- |
| 主题 / 颜色方案 | `ui/theme/*` | `styles.css` + mdui 主题 | ✅ 从 Color.kt/Theme.kt 提取完整 APP_DEFAULT（蓝/紫）+ 云野 CLOUD_FIELD（绿）两套配色，light/dark 各 35 token 完整覆盖；SettingsPage 切换即时生效并持久化 localStorage；main.tsx 启动时恢复用户选择 |
| 应用脚手架（容器卡片）| `MainActivity.kt` + `AppNavigationScreen.kt` | `App.tsx` | ✅ 容器卡片居中+圆角阴影（桌面）；开发者模式 arm/unlock 机制（关于页连点7次图标 arm，群聊输入"selves"触发跳转 /developer-mode）；加载/引导/主导航三段逻辑；过渡动画+scrim |
| 二级页面过渡动画（左右滑入/滑出）| `AppNavigationScreen.kt` | `App.tsx` Routes | ✅ 进入子页面从右滑入450ms FastOutSlowIn；返回从左滑入450ms；退出350ms EaseInOutCubic；前进时旧页面上方叠加 scrim α0.4 暗化层300ms；主Tab间切换无动画 |
| 顶部 AppBar 通用组件 | 各 Screen `TopAppBar` | `pages/SubPageScaffold.tsx` | ✅ sticky surface 背景+底部分隔线+标题字重 normal+副标题12sp opacity0.7+actions插槽；noPadding 选项 |
| 头像组件 | `components/AvatarImage.kt` | `components/MemberAvatar.tsx` | ✅ 成员/系统头像统一为圆形裁剪 + `primary` 10% 背景；无图或加载失败时回退 `person` 图标，并按 40/60/80dp 对齐不同内边距 |
| 底部导航栏 | `components/BottomNavBar.kt` | `App.tsx` `mdui-navigation-bar` | ✅ |

## 二、引导与加载
| 页面 | 安卓源 | Web 目标 | 状态 |
| --- | --- | --- | --- |
| 启动加载页 | `AppNavigationScreen.LoadingScreen` | `App.tsx` 加载分支 | ✅ 改为安卓同款纯图标加载页：背景 `background`，居中 120dp 应用图标，12dp 圆角；移除 Web 自增 spinner 和文案 |
| 欢迎引导（创建系统/成员、导入备份） | `WelcomeGuideScreen.kt` | `pages/WelcomeGuidePage.tsx` | ✅ 1:1 还原（5步引导流程：WELCOME大标题48sp Light+副标题36sp Bold primary+描述18sp+装饰线渐变；IMPORT_OR_CREATE标题32sp Bold+选项卡片圆角20 primaryContainer 0.5/surfaceContainerHigh+图标容器52dp圆角16+标题16sp SemiBold+副标题14sp+箭头；CREATE_SYSTEM/CREATE_MEMBER头像120dp圆形+相机叠加32dp primary+输入框；COMPLETE成功图标80dp弹性动画+标题36sp Bold+功能提示卡片圆角20；底部圆点指示器8dp primary/6dp+按钮56dp圆角28；步骤间水平滑入/滑出400ms动画） |

## 三、主导航三 Tab
| 页面 | 安卓源 | Web 目标 | 状态 |
| --- | --- | --- | --- |
| 主页 Home | `HomeScreen.kt` | `pages/HomePage.tsx` | ✅ 1:1 还原（编辑模式横幅 primaryContainer+"编辑模式"/"完成"按钮；模块始终显示含空状态；轨迹记录补全状态圆点8dp+三统计项Evenly；待办 TodoItem 补描述14sp+优先级Chip padding2x6 12sp+Checkbox；动态补formatDynamicTime时间+作者12sp primary；投票补描述2行省略+formatVoteRemaining截止+票数；日记补标题15sp 1行省略+内容2行省略+分隔线+超过2条"查看更多"；底部FAB空白间距80px） |
| 群聊列表 Chat | `GroupChatScreen.kt` | `pages/GroupChatPage.tsx` | ✅ 1:1 还原（顶部 UserInfoHeader：padding16/头像40/字号16/切换图标 primary 色；空态文案"暂无群聊，请点击右下角创建"；列表项 padding 16x12、头像52、群名 titleMedium 16semibold、消息 bodyMedium 14、时间 bodySmall 12 alpha0.7、空消息 alpha0.6+"暂无消息"、图片消息显示 [图片]、未读徽章 minW20 minH16 padding 2x6 圆角10 字色 surface；分隔线 marginLeft72 outline α0.3） |
| 系统 System | `SystemScreen.kt` | `pages/SystemPage.tsx` | ✅ 1:1 还原（SystemInfoCard 圆角16+padding20+surfaceContainer 填充；点击系统名缩放0.9 并展开"还没有系统简介"占位；分组标题"系统管理/其它"+裸点击行 4dp 横向 padding；列表项无右箭头无分隔线；hover 0.04/active 0.08 ripple 模拟） |

## 四、二级功能页面
| 页面 | 安卓源 | Web 目标 | 状态 |
| --- | --- | --- | --- |
| 待办事项 | `TodoScreen.kt` | `pages/TodoPage.tsx` | ✅ 1:1 还原（统计卡片、空状态、待完成/已完成折叠分组、列表项时间与优先级样式、创建弹窗、长按详情底部弹窗） |
| 动态列表 | `DynamicScreen.kt` | `pages/DynamicPage.tsx` | ✅ 1:1 还原（自定义 TopBar：标题 fontWeight Normal/搜索栏动画切换；搜索栏 leading Search 图标+trailing 过滤图标循环 null→IMAGE→TEXT→null primary 色激活/Close 关闭；DynamicCard surfaceContainer 圆角12 elevation0 padding16；作者头像40+12gap+名字16sp Medium+时间12sp onSurfaceVariant；删除按钮仅 authorId==currentUserId 可见 error 色；标题18sp SemiBold 2行省略+8dp间距；内容14sp onSurfaceVariant；图片3列网格圆角4；标签AssistChip 4dp gap 12sp #tag；互动行：点赞Favorite/FavoriteBorder 20dp liked=error色+数量14sp、评论Comment 20dp+数量14sp；空状态 Timeline图标64dp opacity0.5+16dp间距+"暂无动态"16sp+8dp间距+"点击右下角按钮发布第一条动态"14sp opacity0.6；FAB fixed right16 bottom16） |
| 创建动态 | `CreateDynamicScreen.kt` | `pages/CreateDynamicPage.tsx` | ✅ 1:1 还原（TopAppBar "编辑动态" + 返回 + 勾选发布按钮；textarea 200px 无边线 placeholder "分享此刻的想法…"；分隔线；3列图片网格，添加按钮虚线边框 + add 图标，最大9张；图片项 error色 圆角 20dp 关闭按钮 |
| 动态详情 | `DynamicDetailScreen.kt` | `pages/DynamicDetailPage.tsx` | ✅ 1:1 还原（TopAppBar "动态详情" fontWeight Normal；DynamicDetailCard：作者头像48dp + 名字+时间、删除仅作者 error色、标题20sp SemiBold、内容16sp lineHeight24、图片3列网格、标签AssistChip、点赞Favorite/error色；评论区 分隔线+计数+"暂无评论"空态；CommentItem 头像32dp+名字14sp+时间+"回复"按钮+删除(error色 16dp)；CommentInputSection：回复指示 primaryContainer背景 圆角8 + 输入框 + 48dp圆发送按钮 primary色 |
| 投票列表 | `VoteScreen.kt` | `pages/VotePage.tsx` | ✅ 1:1 还原（自定义 sticky TopBar：标题/搜索栏切换+FilterChip 进行中/已结束；VoteCard surfaceContainer 圆角16+padding16；作者头像44+名字15sp semibold+时间12sp onSurfaceVariant；操作按钮 stop/delete 仅作者可见；标题18sp bold 2行省略；描述14sp 3行省略；选项预览序号24dp secondaryContainer 圆角6+内容14sp+票数13sp Medium+百分比13sp bold primary；进度条6dp 圆角3 进行中primary/已结束secondary+右侧40dp百分比条；底部状态Chip圆角20+8dp状态圆点+剩余时间；people图标+参与人数；多选/匿名标签；已参与提示 primaryContainer 0.5 背景+check_circle；空状态 Poll图标64+进行/结束双文案+创建提示；FAB fixed right16 bottom16） |
| 创建投票 | `CreateVoteScreen.kt` | `pages/CreateVotePage.tsx` | ✅ 1:1 还原（TopAppBar "创建投票" + 返回 + 发布按钮，disabled 状态 alpha0.38；VoteInfoCard SectionHeader 36dp primaryContainer 圆角10 + icon + 12gap + title 14sp SemiBold + subtitle 12sp；OutlinedTextField 标题单行+描述 textarea minLines3；VoteOptionsCard 选项输入+delete 按钮仅 >2 项时显示 error 色+添加选项按钮 max10；VoteSettingsCard 允许多选/匿名投票 Switch+截止时间 click 弹出 DatePicker 覆盖层+确定/取消按钮） |
| 投票详情 | `VoteDetailScreen.kt` | `pages/VoteDetailPage.tsx` | ✅ 1:1 还原（TopAppBar "投票详情" fontWeight Normal+投票按钮仅 active 且未投票时显示；VoteDetailCard 作者头像48+名字16sp+时间12sp+操作按钮 stop primary色/delete error色仅作者可见；标题20sp SemiBold；描述16sp lineHeight24；状态 Chip "进行中" primaryContainer/"已结束" surfaceVariant+票数16sp+截止时间12sp；多选/匿名标签；VoteOptionsSection 标题18sp Medium+选项 Card 选中 primaryContainer 背景+未选中投票态 surfaceVariant+单选用 RadioButton 18dp/多选用 Checkbox+票数百分比+进度条6dp primary；VoteRecordsSection 展开/折叠+头像32+名字14sp+时间12sp+空态"暂无投票记录"） |
| 成员日记 | `DiaryScreen.kt` | `pages/DiaryPage.tsx` | ✅ 1:1 还原（TopAppBar 标题 `${member.name} 的日记` fontWeight Normal；空状态 Edit 图标 64dp alpha0.5+"还没有日记"16sp+"点击右下角按钮新建日记"14sp alpha0.6；列表横向 padding16+纵向 contentPadding 16+item 间距 8；Card surfaceContainer 圆角16 elevation0；长按底部弹窗：标题+formatDetailDateTime 时间+分隔线+编辑/删除 TextButton 红色；删除确认 AlertDialog error 色确认按钮；创建/编辑弹窗 OutlinedTextField 标题单行过滤换行+内容 textarea rows5+内容空错误提示；FAB fixed right16 bottom16） |
| 轨迹记录 | `LocationTrackingScreen.kt` | `pages/LocationPage.tsx` | ⚠️ Web 无后台定位服务，仅做 UI 还原 |
| 成员管理 | `MemberManagementScreen.kt` | `pages/MemberManagementPage.tsx` | ✅ 1:1 还原（搜索栏显隐、成员计数、视图模式切换、分组折叠结构、字母分组标题、成员行当前标记与列表间距；右侧 AlphabetIndexBar 索引栏支持点击+拖动选字母+气泡指示+滚动定位；MemberRow 长按/点击 more_vert 弹出编辑/删除菜单，仅非当前成员显示删除；删除确认 dialog 含 error 色确认按钮） |
| 在线统计 | `OnlineStatsScreen.kt` | `pages/OnlineStatsPage.tsx` | ✅ 1:1 还原（TabRow 三标签：在线/时长/日志；OnlineStatItem 头像40+12spacer+name bodyLarge+当前成员6dp primary圆点+今日在线 bodySmall+状态 tertiary/onSurfaceVariant+右侧8dp圆点；时长页 Card surfaceContainer padding16 按分钟降序+右侧 titleMedium bold primary；日志页 LoginLogSummaryCard surfaceContainer 标题 bold+三列 SummaryItem headlineSmall bold+FilterChips+LoginLogItem surfaceContainer 头像40+登录/登出 bodySmall+空状态 Card padding32） |
| 设置 | `SettingsScreen.kt` | `pages/SettingsPage.tsx` | ✅ 1:1 还原（分组标题通用/数据与备份/Web访问/其他，颜色 primary titleMedium padding8x4；SettingsItem 24px图标+16spacer+title bodyLarge 16+subtitle bodyMedium 14 onSurfaceVariant；SettingsSwitchItem 同+Switch；SettingsItemWithProgress 同+loading时20dp CircularProgress；WebAccessInfoCard surfaceVariant 圆角12 padding16+URL labelMedium 12+bodyMedium 14 Medium+复制按钮+QR占位180dp+提示文案；语言/主题/配色弹窗使用 mdui-dialog） |
| 关于 | `AboutScreen.kt` | `pages/AboutPage.tsx` | ✅ |
| 开发者模式 | `DeveloperModeScreen.kt` | `pages/DeveloperModePage.tsx` | ✅ |
| 群聊详情 | `ChatScreen.kt` | `pages/ChatDetailPage.tsx` | ✅ |

## 五、关键弹窗 / 子组件（按主页面被调用时同步还原）
- `MemberSwitchDialog`、`QuickMemberSwitch`、`MemberSelectionDialog` ✅（MemberSwitchDialog：成员列表+当前标记+长按删除+倒计时确认+新建成员按钮；MemberHeader 集成）
- `CreateMemberDialog` / `EditMemberDialog` / `CreateMemberForm` ✅（头像选择+名称/简介/代词/分组 Chip+新建分组子弹窗；接入 MemberManagementPage FAB+编辑菜单）
- `CreateSystemDialog` / `SystemEditDialog` / `SystemSettingsDialog` ✅（SystemEditDialog：头像+名称+描述；接入 SystemPage 编辑系统入口）
- `CreateGroupDialog` / `GroupManagementDialog` / `GroupDescriptionEditDialog` ✅（CreateGroupDialog：两步流程名称+成员选择；GroupManagementDialog：添加/移除成员+编辑群信息+转让群主+解散群组；接入 GroupChatPage FAB 和 ChatDetailPage more_vert）
- `CreateTodoDialog` ✅（已内联于 TodoPage：标题单行+描述 minLines3+优先级下拉 LOW/NORMAL/HIGH 对应颜色；接入 FAB）
- `CreateVoteDialog` / `EditDynamicDialog` ✅（EditDynamicDialog：全屏 TopAppBar+无边框 textarea 200dp+分隔线+3列图片网格 dashed add按钮+error圆角删除；接入 CreateDynamicPage）
- `BackupDialogs` ✅（BackupProgressDialog：surfaceContainer卡片+线性/环形进度+百分比；ImportBackupWarningDialog：warning图标+error色 bullet 列表+不可撤销警告+确认导入按钮；接入 SettingsPage 导入备份按钮）
- `MemberManagementDialog` ✅（group图标标题+primary add按钮；成员行头像40+当前标记chip+简介1行省略+more_vert菜单编辑/删除；删除确认 error色按钮；内嵌 EditMemberDialog）
- `OnlineStatsDialog` ✅（Schedule图标标题；总成员/活跃/在线三列统计卡；成员行头像40+当前6dp primary圆点+今日在线/最后活跃时间+右侧8dp在线状态圆点）
- `LocationTrackingConfigDialog` ✅（surfaceContainer卡片+close按钮；记录间隔单选5档30s/1m/5m/10m/30m；自动开始Switch+延迟单选5档；接入 LocationPage 状态芯片）
- `DiaryEditDialog` ✅（已内联于 DiaryPage EditDialog：标题单行过滤换行+内容 minLines5+内容空错误提示；接入 FAB+长按编辑）
- `ColorSchemeDialog` / `ThemeModeDialog` / `LanguageDialog` ✅（已内联于 SettingsPage：mdui-dialog 列表单选+即时应用+持久化 localStorage）
- `AlphabetIndexBar` ✅（点击+拖动选字母+气泡指示+滚动定位）、`MonthCalendar` ✅（年月导航+星期行+日期格子 selected-primary/today-primaryContainer/future-alpha0.3；禁止未来月）、`ImageViewer` ✅（全屏黑色背景+返回按钮圆形半透明+发送者/时间信息+点击背景关闭+ESC关闭）、`UserInfoHeader` ✅（已集成于 MemberHeader）、`DatePickerDialog` ✅（已内联于 CreateVotePage）、`AvatarImage` ✅（MemberAvatar 组件）、`SystemAvatarImage` ✅（MemberAvatar 兼容系统头像）

## 六、当前迭代计划
按用户优先级一次还原一个页面，完成一个就在本表打勾并附"还原要点"备注。
