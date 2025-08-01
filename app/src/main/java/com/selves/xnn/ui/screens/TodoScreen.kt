package com.selves.xnn.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.selves.xnn.model.Todo
import com.selves.xnn.model.TodoPriority
import com.selves.xnn.model.Member
import com.selves.xnn.ui.components.CreateTodoDialog
import com.selves.xnn.ui.components.AvatarImage
import com.selves.xnn.viewmodel.TodoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TodoScreen(
    onNavigateBack: () -> Unit,
    currentMemberId: String,
    todoViewModel: TodoViewModel = hiltViewModel()
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showPendingTodos by remember { mutableStateOf(true) }
    var showCompletedTodos by remember { mutableStateOf(false) }
    var selectedTodo by remember { mutableStateOf<Todo?>(null) }
    val bottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // 观察ViewModel状态
    val pendingTodos by todoViewModel.pendingTodos.collectAsState()
    val completedTodos by todoViewModel.completedTodos.collectAsState()
    val todoStats by todoViewModel.todoStats.collectAsState()
    val isLoading by todoViewModel.isLoading.collectAsState()
    val error by todoViewModel.error.collectAsState()
    
    // 设置当前成员（用于创建待办事项时记录创建者）
    LaunchedEffect(currentMemberId) {
        todoViewModel.setCurrentMember(currentMemberId)
    }
    
    // 错误处理
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // 这里可以显示错误信息，比如使用SnackBar
            todoViewModel.clearError()
        }
    }
    
    // 创建待办事项对话框
    if (showCreateDialog) {
        CreateTodoDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, description, priority ->
                showCreateDialog = false
                todoViewModel.createTodo(title, description, priority)
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "待办事项",
                        fontWeight = FontWeight.Normal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "创建待办事项"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            
            // 统计信息卡片
            if (todoStats.total > 0) {
                TodoStatsCard(
                    stats = todoStats,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            // 主要内容
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (pendingTodos.isEmpty() && completedTodos.isEmpty()) {
                // 空状态
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "暂无待办事项",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击右下角按钮创建第一个待办事项",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // 待办事项列表
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 待办事项标题
                    if (pendingTodos.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { showPendingTodos = !showPendingTodos },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "待办事项 (${pendingTodos.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                
                                val rotation by animateFloatAsState(
                                    targetValue = if (showPendingTodos) 180f else 0f,
                                    label = "arrow_rotation"
                                )
                                
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (showPendingTodos) "收起" else "展开",
                                    modifier = Modifier.rotate(rotation),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // 待办事项列表
                        items(pendingTodos) { todo ->
                            AnimatedVisibility(
                                visible = showPendingTodos,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                TodoItem(
                                    todo = todo,
                                    onStatusChange = { isCompleted ->
                                        todoViewModel.updateTodoStatus(todo.id, isCompleted)
                                    },
                                    onDelete = { todoViewModel.deleteTodo(todo.id) },
                                    onLongPress = {
                                        selectedTodo = todo
                                        showBottomSheet = true
                                    }
                                )
                            }
                        }
                    }
                    
                    // 已完成事项
                    if (completedTodos.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { showCompletedTodos = !showCompletedTodos },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "已完成 (${completedTodos.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                
                                val rotation by animateFloatAsState(
                                    targetValue = if (showCompletedTodos) 180f else 0f,
                                    label = "completed_arrow_rotation"
                                )
                                
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (showCompletedTodos) "收起" else "展开",
                                    modifier = Modifier.rotate(rotation),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // 已完成事项列表
                        items(completedTodos) { todo ->
                            AnimatedVisibility(
                                visible = showCompletedTodos,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                TodoItem(
                                    todo = todo,
                                    onStatusChange = { isCompleted ->
                                        todoViewModel.updateTodoStatus(todo.id, isCompleted)
                                    },
                                    onDelete = { todoViewModel.deleteTodo(todo.id) },
                                    onLongPress = {
                                        selectedTodo = todo
                                        showBottomSheet = true
                                    }
                                )
                            }
                        }
                    }
                    
                    // 底部间距，避免被FAB遮挡
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
    
    // 待办事项详情底部弹窗
    if (showBottomSheet && selectedTodo != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                selectedTodo = null
            },
            sheetState = bottomSheetState
        ) {
            TodoDetailBottomSheet(
                todo = selectedTodo!!,
                todoViewModel = todoViewModel,
                onDismiss = {
                    scope.launch {
                        bottomSheetState.hide()
                        showBottomSheet = false
                        selectedTodo = null
                    }
                }
            )
        }
    }
}

@Composable
fun TodoStatsCard(
    stats: com.selves.xnn.viewmodel.TodoStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                title = "总计",
                value = stats.total.toString(),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatItem(
                title = "待办",
                value = stats.pending.toString(),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            StatItem(
                title = "已完成",
                value = stats.completed.toString(),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatItem(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoItem(
    todo: Todo,
    onStatusChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onLongPress: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { },
                    onLongClick = onLongPress
                )
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 左侧内容
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 标题和优先级
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null,
                        color = if (todo.isCompleted) 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) 
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // 优先级标识
                    if (todo.priority != TodoPriority.NORMAL && !todo.isCompleted) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = when (todo.priority) {
                                TodoPriority.HIGH -> MaterialTheme.colorScheme.error
                                TodoPriority.LOW -> MaterialTheme.colorScheme.outline
                                else -> Color.Transparent
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = when (todo.priority) {
                                    TodoPriority.HIGH -> "紧急"
                                    TodoPriority.LOW -> "不急"
                                    else -> ""
                                },
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                // 描述
                if (todo.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = todo.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (todo.isCompleted) 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) 
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // 时间信息
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (todo.isCompleted) {
                        "完成于 ${formatTime(todo.completedAt ?: todo.createdAt)}"
                    } else {
                        "创建于 ${formatTime(todo.createdAt)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 右侧勾选框
            Spacer(modifier = Modifier.width(16.dp))
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = onStatusChange
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    return com.selves.xnn.util.TimeFormatter.formatTimestamp(timestamp)
}

@Composable
fun TodoDetailBottomSheet(
    todo: Todo,
    todoViewModel: TodoViewModel,
    onDismiss: () -> Unit
) {
    var creator by remember { mutableStateOf<Member?>(null) }
    
    // 获取创建者信息
    LaunchedEffect(todo.createdBy) {
        creator = todoViewModel.getMemberById(todo.createdBy)
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 标题
        Text(
            text = "待办事项详情",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 待办事项标题
        Text(
            text = todo.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // 描述
        if (todo.description.isNotEmpty()) {
            Text(
                text = todo.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // 优先级
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "优先级：",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                color = when (todo.priority) {
                    TodoPriority.HIGH -> MaterialTheme.colorScheme.error
                    TodoPriority.LOW -> MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.secondary
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = when (todo.priority) {
                        TodoPriority.HIGH -> "紧急"
                        TodoPriority.LOW -> "不急"
                        else -> "一般"
                    },
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        
        // 状态
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "状态：",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (todo.isCompleted) "已完成" else "待办",
                style = MaterialTheme.typography.bodyMedium,
                color = if (todo.isCompleted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
        
        // 创建人信息
        if (creator != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "创建人：",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                AvatarImage(
                    avatarUrl = creator!!.avatarUrl,
                    contentDescription = "创建者头像",
                    size = 24.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = creator!!.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // 创建时间
        Text(
            text = "创建时间：${formatTime(todo.createdAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // 完成时间
        if (todo.isCompleted && todo.completedAt != null) {
            Text(
                text = "完成时间：${formatTime(todo.completedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 关闭按钮
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("关闭")
        }
    }
} 