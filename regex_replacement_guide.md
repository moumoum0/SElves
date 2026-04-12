# 正则表达式批量替换硬编码文本指南

## 主要替换模式

### 1. 简单文本替换（Text组件）

#### 模式 1: `Text("中文文本")`
**查找正则**:
```regex
Text\("([^"]+)"\)
```

**替换策略**:
需要根据具体文本内容替换为对应的字符串资源。由于每个文本对应不同的资源ID，无法使用单一正则完成，需要逐个替换。

**示例**:
- 查找: `Text("取消")`
- 替换: `Text(stringResource(R.string.btn_cancel))`

---

### 2. 带标签的文本（label参数）

#### 模式 2: `label = { Text("中文文本") }`
**查找正则**:
```regex
label = \{ Text\("([^"]+)"\) \}
```

**替换为**:
```kotlin
label = { Text(stringResource(R.string.xxx)) }
```

---

### 3. 占位符文本（placeholder参数）

#### 模式 3: `placeholder = { Text("中文文本") }`
**查找正则**:
```regex
placeholder = \{ Text\("([^"]+)"\) \}
```

**替换为**:
```kotlin
placeholder = { Text(stringResource(R.string.xxx)) }
```

---

### 4. 支持文本（supportingText参数）

#### 模式 4: `supportingText = { Text("中文文本") }`
**查找正则**:
```regex
supportingText.*?\{ Text\("([^"]+)"\) \}
```

---

### 5. 对话框标题和文本

#### 模式 5: `title = { Text("中文文本") }`
**查找正则**:
```regex
title = \{\s*Text\("([^"]+)"\)\s*\}
```

#### 模式 6: `text = { Text("中文文本") }`
**查找正则**:
```regex
text = \{\s*Text\("([^"]+)"\)\s*\}
```

---

### 6. 错误消息赋值

#### 模式 7: `errorMessage = "中文文本"`
**查找正则**:
```regex
errorMessage = "([^"]+)"
```

**替换为**:
```kotlin
errorMessage = context.getString(R.string.xxx)
```

**注意**: 需要确保有 `context` 变量可用，或使用 `LocalContext.current`

---

### 7. ContentDescription

#### 模式 8: `contentDescription = "中文文本"`
**查找正则**:
```regex
contentDescription = "([^"]+)"
```

**替换为**:
```kotlin
contentDescription = stringResource(R.string.xxx)
```

---

## 批量替换步骤

### 方法 1: 使用 IDE 的查找替换功能（推荐）

1. **在 Android Studio 中**:
   - 按 `Ctrl+Shift+R` (Windows) 或 `Cmd+Shift+R` (Mac)
   - 勾选 "Regex" 选项
   - 设置搜索范围为 `app/src/main/java/com/selves/xnn/ui`

2. **逐个模式替换**:
   - 使用上述正则表达式查找
   - 手动确认每个匹配项
   - 替换为对应的 `stringResource(R.string.xxx)`

### 方法 2: 使用脚本辅助（半自动）

创建一个映射表，然后使用脚本批量替换：

```kotlin
// 文本到资源ID的映射
val textToResourceMap = mapOf(
    "取消" to "btn_cancel",
    "确定" to "btn_confirm",
    "删除" to "btn_delete",
    "编辑" to "btn_edit",
    "全部" to "filter_all",
    "今日" to "filter_today",
    // ... 更多映射
)
```

---

## 具体文件替换示例

### OnlineStatsScreen.kt

**查找**:
```regex
label = \{ Text\("全部"\) \}
```
**替换为**:
```kotlin
label = { Text(stringResource(R.string.filter_all)) }
```

**查找**:
```regex
label = \{ Text\("今日"\) \}
```
**替换为**:
```kotlin
label = { Text(stringResource(R.string.filter_today)) }
```

---

### MemberManagementScreen.kt

**查找**:
```regex
Text\("删除成员"\)
```
**替换为**:
```kotlin
Text(stringResource(R.string.dialog_delete_member))
```

**查找**:
```regex
Text\("确定要删除成员「\$\{showDeleteConfirmation!!\.name\}」吗？此操作不可撤销。"\)
```
**替换为**:
```kotlin
Text(stringResource(R.string.dialog_delete_member_confirm, showDeleteConfirmation!!.name))
```

---

### 通用按钮文本替换

**查找**:
```regex
Text\("取消"\)
```
**替换为**:
```kotlin
Text(stringResource(R.string.btn_cancel))
```

**查找**:
```regex
Text\("确定"\)
```
**替换为**:
```kotlin
Text(stringResource(R.string.btn_confirm))
```

**查找**:
```regex
Text\("删除"\)
```
**替换为**:
```kotlin
Text(stringResource(R.string.btn_delete))
```

**查找**:
```regex
Text\("编辑"\)
```
**替换为**:
```kotlin
Text(stringResource(R.string.btn_edit))
```

---

## 需要添加的新字符串资源

以下是需要添加到 `strings.xml` 的新资源（当前不存在的）：

```xml
<!-- 筛选器 -->
<string name="filter_all">全部</string>
<string name="filter_today">今日</string>

<!-- 表单标签 -->
<string name="label_member_name">成员名</string>
<string name="label_system_name">系统名称</string>
<string name="label_group_name">群聊名称</string>
<string name="label_title">标题</string>
<string name="label_description_optional">描述（可选）</string>
<string name="label_priority">优先级</string>

<!-- 占位符 -->
<string name="placeholder_member_name">请输入成员名</string>
<string name="placeholder_system_name">请输入系统名称</string>
<string name="placeholder_share_thoughts">分享你的想法...</string>
<string name="placeholder_search_members">搜索成员...</string>
<string name="placeholder_select_avatar">点击选择头像</string>
<string name="placeholder_select_date">选择日期</string>

<!-- 错误消息 -->
<string name="error_member_name_empty">成员名不能为空</string>
<string name="error_member_name_exists">成员名已存在</string>
<string name="error_group_name_empty">群聊名称不能为空</string>
<string name="error_title_empty">标题不能为空</string>
<string name="error_operation_irreversible">此操作不可撤销</string>

<!-- 对话框 -->
<string name="dialog_edit_member">编辑成员</string>
<string name="dialog_edit_dynamic">编辑动态</string>
<string name="dialog_select_map">选择地图应用</string>
<string name="dialog_no_map_installed">未检测到已安装的地图应用</string>
<string name="dialog_confirm_import">确定导入</string>

<!-- 按钮 -->
<string name="btn_next_step">下一步</string>
<string name="btn_create">创建</string>

<!-- 其他 -->
<string name="dynamic_not_found">动态不存在或已被删除</string>
<string name="cd_navigation">导航</string>
```

对应的英文版本（`values-en/strings.xml`）：

```xml
<!-- Filters -->
<string name="filter_all">All</string>
<string name="filter_today">Today</string>

<!-- Form Labels -->
<string name="label_member_name">Name</string>
<string name="label_system_name">System Name</string>
<string name="label_group_name">Group Name</string>
<string name="label_title">Title</string>
<string name="label_description_optional">Description (Optional)</string>
<string name="label_priority">Priority</string>

<!-- Placeholders -->
<string name="placeholder_member_name">Enter name</string>
<string name="placeholder_system_name">Enter system name</string>
<string name="placeholder_share_thoughts">Share your thoughts...</string>
<string name="placeholder_search_members">Search members...</string>
<string name="placeholder_select_avatar">Tap to select avatar</string>
<string name="placeholder_select_date">Select Date</string>

<!-- Error Messages -->
<string name="error_member_name_empty">Name cannot be empty</string>
<string name="error_member_name_exists">Name already exists</string>
<string name="error_group_name_empty">Group name cannot be empty</string>
<string name="error_title_empty">Title cannot be empty</string>
<string name="error_operation_irreversible">This operation cannot be undone</string>

<!-- Dialogs -->
<string name="dialog_edit_member">Edit Headmate</string>
<string name="dialog_edit_dynamic">Edit Post</string>
<string name="dialog_select_map">Select Map App</string>
<string name="dialog_no_map_installed">No map apps detected</string>
<string name="dialog_confirm_import">Confirm Import</string>

<!-- Buttons -->
<string name="btn_next_step">Next</string>
<string name="btn_create">Create</string>

<!-- Others -->
<string name="dynamic_not_found">Post not found or deleted</string>
<string name="cd_navigation">Navigation</string>
```

---

## 注意事项

1. **导入语句**: 替换后需要确保文件顶部有以下导入：
   ```kotlin
   import androidx.compose.ui.res.stringResource
   import com.selves.xnn.R
   ```

2. **带参数的字符串**: 对于包含变量的字符串（如 `"确定要删除成员「${name}」吗？"`），需要：
   - 在 `strings.xml` 中使用占位符: `<string name="xxx">确定要删除成员「%1$s」吗？</string>`
   - 在代码中传递参数: `stringResource(R.string.xxx, name)`

3. **Context 依赖**: 在某些情况下（如 ViewModel 中），可能需要使用 `context.getString()` 而不是 `stringResource()`

4. **逐步验证**: 建议每替换一个文件就编译测试一次，确保没有引入错误

5. **备份**: 在批量替换前建议先提交当前代码到 Git，以便出错时可以回滚

---

## 推荐工作流程

1. **先添加字符串资源**: 将所有新的字符串资源添加到 `strings.xml` 和 `strings-en.xml`
2. **逐文件替换**: 按照优先级逐个文件进行替换
3. **编译测试**: 每完成一个文件就编译测试
4. **运行时测试**: 在中英文环境下测试界面显示
5. **提交代码**: 确认无误后提交

---

## 自动化脚本建议

如果需要更自动化的方案，可以考虑编写 Python 脚本：

```python
import re
import os

# 文本映射表
text_map = {
    "取消": "btn_cancel",
    "确定": "btn_confirm",
    "删除": "btn_delete",
    # ... 更多映射
}

def replace_hardcoded_text(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    for chinese_text, resource_id in text_map.items():
        # 替换 Text("中文")
        pattern = f'Text\\("{chinese_text}"\\)'
        replacement = f'Text(stringResource(R.string.{resource_id}))'
        content = re.sub(pattern, replacement, content)
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

# 使用示例
# replace_hardcoded_text('path/to/file.kt')
```

但这种方法需要非常小心，建议只用于辅助，最终还是要人工检查每个替换。
