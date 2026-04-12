# 本地化工作待办清单

## 检查日期
2026-04-12

## 检查结果概述

### ✅ 已完成的本地化工作
- **strings.xml**: 中文版本（`values/strings.xml`）和英文版本（`values-en/strings.xml`）的所有字符串资源已完全同步，共计约400+个字符串资源。

### ❌ 未完成的本地化工作

#### 1. Kotlin代码中的硬编码文本

以下文件包含硬编码的中文文本，需要提取到 `strings.xml` 并添加英文翻译：

##### 1.1 OnlineStatsScreen.kt
**文件路径**: `app/src/main/java/com/selves/xnn/ui/screens/OnlineStatsScreen.kt`
- 第327行: `Text("全部")` - 筛选器标签
- 第336行: `Text("今日")` - 筛选器标签

##### 1.2 MemberManagementScreen.kt
**文件路径**: `app/src/main/java/com/selves/xnn/ui/screens/MemberManagementScreen.kt`
- 第218行: `Text("删除成员")` - 对话框标题
- 第221行: `Text("确定要删除成员「${showDeleteConfirmation!!.name}」吗？此操作不可撤销。")` - 确认消息
- 第233行: `Text("删除")` - 按钮文本
- 第240行: `Text("取消")` - 按钮文本
- 第321行: `Text("搜索成员...")` - 搜索框占位符
- 第441行: `Text("编辑")` - 菜单项
- 第454行: `Text("删除")` - 菜单项

##### 1.3 LocationTrackingScreen.kt
**文件路径**: `app/src/main/java/com/selves/xnn/ui/screens/LocationTrackingScreen.kt`
- 第237行: `Text("停止")` - 按钮文本
- 第247行: `Text("开始")` - 按钮文本

##### 1.4 DynamicDetailScreen.kt
**文件路径**: `app/src/main/java/com/selves/xnn/ui/screens/DynamicDetailScreen.kt`
- 第93行: `Text("动态不存在或已被删除")` - 错误消息

##### 1.5 CreateDynamicScreen.kt
**文件路径**: `app/src/main/java/com/selves/xnn/ui/screens/CreateDynamicScreen.kt`
- 第106行: `Text("编辑动态")` - 标题
- 第149行: `Text("分享你的想法...")` - 占位符文本

##### 1.6 CreateGroupDialog.kt
**文件路径**: `app/src/main/java/com/selves/xnn/ui/components/CreateGroupDialog.kt`
- 第138行: `Text("群聊名称")` - 标签
- 第141行: `Text("群聊名称不能为空")` - 错误消息
- 第153行: `Text("取消")` - 按钮文本
- 第165行: `Text("下一步")` - 按钮文本

##### 1.7 CreateMemberDialog.kt
**文件路径**: `app/src/main/java/com/selves/xnn/ui/components/CreateMemberDialog.kt`
- 第144行: `Text("成员名")` - 标签
- 第180行: `Text("取消")` - 按钮文本
- 第184行: `Text("确定")` - 按钮文本

##### 1.8 CreateSystemDialog.kt
**文件路径**: `app/src/main/java/com/selves/xnn/ui/components/CreateSystemDialog.kt`
- 第94行: `Text("创建")` - 按钮文本
- 第148行: `Text("点击选择头像")` - 提示文本
- 第182行: `Text("系统名称")` - 标签
- 第183行: `Text("请输入系统名称")` - 占位符

##### 1.9 CreateTodoDialog.kt
**文件路径**: `app/src/main/java/com/selves/xnn/ui/components/CreateTodoDialog.kt`
- 第53行: `Text("标题")` - 标签
- 第56行: `Text("标题不能为空")` - 错误消息
- 第67行: `Text("描述（可选）")` - 标签
- 第84行: `Text("优先级")` - 标签

##### 1.10 EditMemberDialog.kt
**文件路径**: `app/src/main/java/com/selves/xnn/ui/components/EditMemberDialog.kt`
- 第72行: `errorMessage = "成员名不能为空"` - 错误消息
- 第76行: `errorMessage = "成员名已存在"` - 错误消息
- 第109行: `Text("编辑成员")` - 标题
- 第162行: `Text("成员名")` - 标签
- 第198行: `Text("取消")` - 按钮文本

##### 1.11 DatePickerDialog.kt
**文件路径**: `app/src/main/java/com/selves/xnn/ui/components/DatePickerDialog.kt`
- 第38行: `Text("确认")` - 按钮文本
- 第43行: `Text("取消")` - 按钮文本
- 第51行: `Text("选择日期")` - 标题

##### 1.12 EditDynamicDialog.kt
**文件路径**: `app/src/main/java/com/selves/xnn/ui/components/EditDynamicDialog.kt`
- 第100行: `Text("编辑动态")` - 标题
- 第133行: `Text("分享你的想法...")` - 占位符

##### 1.13 LocationTimelineItem.kt
**文件路径**: `app/src/main/java/com/selves/xnn/ui/components/LocationTimelineItem.kt`
- 第112行: `contentDescription = "导航"` - 内容描述
- 第142行: `Text("选择地图应用")` - 对话框标题
- 第147行: `Text("未检测到已安装的地图应用")` - 提示消息

##### 1.14 BackupDialogs.kt
**文件路径**: `app/src/main/java/com/selves/xnn/ui/components/BackupDialogs.kt`
- 第182行: `Text("确定导入")` - 按钮文本
- 第189行: `Text("取消")` - 按钮文本

##### 1.15 CreateMemberForm.kt
**文件路径**: `app/src/main/java/com/selves/xnn/ui/components/CreateMemberForm.kt`
- 第93行: `Text("点击选择头像")` - 提示文本
- 第130行: `Text("成员名")` - 标签
- 第131行: `Text("请输入成员名")` - 占位符

##### 1.16 CreateSystemForm.kt
**文件路径**: `app/src/main/java/com/selves/xnn/ui/components/CreateSystemForm.kt`
- 第94行: `Text("点击选择头像")` - 提示文本
- 第128行: `Text("系统名称")` - 标签
- 第129行: `Text("请输入系统名称")` - 占位符

##### 1.17 MemberManagementDialog.kt
**文件路径**: `app/src/main/java/com/selves/xnn/ui/components/MemberManagementDialog.kt`
- 第70行: `Text("成员管理")` - 标题
- **注意**: 此文件部分使用了 `stringResource`，但仍有硬编码文本

#### 2. 其他资源文件

##### 2.1 values-v31/themes.xml
**文件路径**: `app/src/main/res/values-v31/themes.xml`
- 包含中文注释，但这些是开发者注释，不影响用户界面
- **建议**: 如果需要国际化团队协作，可以考虑将注释也翻译成英文

##### 2.2 values/themes.xml
**文件路径**: `app/src/main/res/values/themes.xml`
- 包含中文注释
- **建议**: 同上

## 修复建议

### 优先级 1（高）- 用户可见文本
需要立即修复以下包含用户可见硬编码文本的文件：
1. OnlineStatsScreen.kt
2. MemberManagementScreen.kt
3. LocationTrackingScreen.kt
4. DynamicDetailScreen.kt
5. CreateDynamicScreen.kt
6. 所有 Dialog 组件（CreateGroupDialog, CreateMemberDialog, CreateSystemDialog, CreateTodoDialog, EditMemberDialog, DatePickerDialog, EditDynamicDialog, BackupDialogs）
7. 所有 Form 组件（CreateMemberForm, CreateSystemForm）
8. LocationTimelineItem.kt
9. MemberManagementDialog.kt

### 优先级 2（中）- 开发者注释
可选修复：
1. themes.xml 文件中的中文注释

## 修复步骤

1. **创建新的字符串资源**
   - 在 `values/strings.xml` 中添加所有硬编码文本的中文版本
   - 在 `values-en/strings.xml` 中添加对应的英文翻译

2. **替换硬编码文本**
   - 将所有 Kotlin 文件中的硬编码文本替换为 `stringResource(R.string.xxx)`
   - 确保导入 `androidx.compose.ui.res.stringResource`

3. **测试验证**
   - 在中文环境下测试所有界面
   - 在英文环境下测试所有界面
   - 确保所有文本正确显示

## 统计信息

- **需要修复的文件数量**: 17个 Kotlin 文件
- **估计需要添加的字符串资源**: 约50-60个
- **当前 strings.xml 资源数量**: 约400+个
- **本地化完成度**: 约87%（strings.xml 完成，但代码中仍有硬编码）

## 备注

- `values-en` 目录中只有 `strings.xml`，缺少 `colors.xml` 和 `themes.xml`，但这两个文件不需要本地化（颜色值和主题样式是通用的）
- 所有硬编码文本都是中文，需要提取并添加英文翻译
- 部分文件（如 MemberManagementDialog.kt）已经开始使用 `stringResource`，说明团队已经意识到本地化的重要性
