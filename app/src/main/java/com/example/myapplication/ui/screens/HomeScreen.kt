package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.components.AvatarImage
import com.example.myapplication.ui.screens.TodoScreen
import com.example.myapplication.viewmodel.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentMember: com.example.myapplication.model.Member?,
    onMemberSwitch: () -> Unit,
    onNavigateToTodo: () -> Unit
) {
    HomeMainScreen(
        currentMember = currentMember,
        onMemberSwitch = onMemberSwitch,
        onNavigateToTodo = onNavigateToTodo
    )
}

@Composable
fun HomeMainScreen(
    currentMember: com.example.myapplication.model.Member?,
    onMemberSwitch: () -> Unit,
    onNavigateToTodo: () -> Unit,
    todoViewModel: TodoViewModel = hiltViewModel()
) {
    // 设置当前成员（用于创建待办事项时记录创建者）
    LaunchedEffect(currentMember?.id) {
        currentMember?.id?.let { memberId ->
            todoViewModel.setCurrentMember(memberId)
        }
    }
    
    // 观察共享的待办事项数据
    val pendingTodos by todoViewModel.pendingTodos.collectAsState()
    val todoStats by todoViewModel.todoStats.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部用户信息栏
        if (currentMember != null) {
            UserInfoHeader(
                currentMember = currentMember,
                onMemberSwitch = onMemberSwitch
            )
        }
        
        // 主要内容区域
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        
        item {
            FunctionModulesSection(onNavigateToTodo = onNavigateToTodo)
        }
        
        item {
            TodoSection(
                onNavigateToTodo = onNavigateToTodo,
                pendingTodos = pendingTodos,
                todoStats = todoStats,
                onTodoStatusChange = { todoId, isCompleted ->
                    todoViewModel.updateTodoStatus(todoId, isCompleted)
                }
            )
        }
        
        item {
            DynamicSection()
        }
        
        item {
            VoteSection()
        }
    }
    }
}

@Composable
fun UserInfoHeader(
    currentMember: com.example.myapplication.model.Member,
    onMemberSwitch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onMemberSwitch() }
        ) {
            AvatarImage(
                avatarUrl = currentMember.avatarUrl,
                contentDescription = "成员头像",
                size = 40.dp
            )
            Text(
                text = currentMember.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        IconButton(onClick = onMemberSwitch) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "切换成员",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun FunctionModulesSection(onNavigateToTodo: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "功能模块",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    listOf(
                        "待办" to Icons.Default.Assignment,
                        "动态" to Icons.Default.Timeline,
                        "投票" to Icons.Default.Poll
                    )
                ) { (title, icon) ->
                    FunctionModuleItem(
                        title = title, 
                        icon = icon,
                        onClick = {
                            when (title) {
                                "待办" -> onNavigateToTodo()
                                // 其他功能模块的点击事件可以在这里添加
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FunctionModuleItem(
    title: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(100.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TodoSection(
    onNavigateToTodo: () -> Unit = {},
    pendingTodos: List<com.example.myapplication.model.Todo> = emptyList(),
    todoStats: com.example.myapplication.viewmodel.TodoStats = com.example.myapplication.viewmodel.TodoStats(),
    onTodoStatusChange: (String, Boolean) -> Unit = { _, _ -> }
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "待办事项",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onNavigateToTodo) {
                    Text("查看更多")
                }
            }
            

            
            // 待办事项列表预览（显示前2个）
            if (pendingTodos.isEmpty()) {
                Text(
                    text = "暂无待办事项",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                pendingTodos.take(2).forEach { todo ->
                    TodoItem(
                        todo = todo,
                        onStatusChange = { isCompleted ->
                            onTodoStatusChange(todo.id, isCompleted)
                        }
                    )
                }
                
                // 如果有更多待办事项，显示提示
                if (pendingTodos.size > 2) {
                    Text(
                        text = "还有 ${pendingTodos.size - 2} 个待办事项",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable { onNavigateToTodo() }
                    )
                }
            }
        }
    }
}

@Composable
fun TodoItem(
    todo: com.example.myapplication.model.Todo,
    onStatusChange: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = todo.title,
                fontSize = 16.sp,
                color = if (todo.isCompleted) 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) 
                else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null
            )
            if (todo.description.isNotEmpty()) {
                Text(
                    text = todo.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }
        
        // 优先级指示器
        if (todo.priority != com.example.myapplication.model.TodoPriority.NORMAL) {
            Card(
                modifier = Modifier.padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (todo.priority) {
                        com.example.myapplication.model.TodoPriority.HIGH -> MaterialTheme.colorScheme.error
                        com.example.myapplication.model.TodoPriority.LOW -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Text(
                    text = when (todo.priority) {
                        com.example.myapplication.model.TodoPriority.HIGH -> "紧急"
                        com.example.myapplication.model.TodoPriority.LOW -> "不急"
                        else -> "一般"
                    },
                    fontSize = 12.sp,
                    color = when (todo.priority) {
                        com.example.myapplication.model.TodoPriority.HIGH -> MaterialTheme.colorScheme.onError
                        com.example.myapplication.model.TodoPriority.LOW -> MaterialTheme.colorScheme.onTertiary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        
        Checkbox(
            checked = todo.isCompleted,
            onCheckedChange = { onStatusChange(it) }
        )
    }
}

@Composable
fun DynamicSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "最新动态",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { /* TODO: 查看更多动态 */ }) {
                    Text("查看更多")
                }
            }
            
            // 动态列表预览
            repeat(3) { index ->
                DynamicItem(
                    title = "动态标题 ${index + 1}",
                    content = "这是动态内容的简要描述...",
                    time = "${index + 1}小时前"
                )
            }
        }
    }
}

@Composable
fun DynamicItem(title: String, content: String, time: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = time,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun VoteSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "投票活动",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { /* TODO: 查看更多投票 */ }) {
                    Text("查看更多")
                }
            }
            
            // 投票列表预览
            repeat(2) { index ->
                VoteItem(
                    title = "投票主题 ${index + 1}",
                    description = "这是投票的详细描述...",
                    endTime = "还有${3 - index}天结束"
                )
            }
        }
    }
}

@Composable
fun VoteItem(title: String, description: String, endTime: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = endTime,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
} 