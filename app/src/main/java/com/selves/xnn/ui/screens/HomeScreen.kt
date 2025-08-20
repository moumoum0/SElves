package com.selves.xnn.ui.screens

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
import androidx.compose.material.icons.filled.LocationOn
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
import com.selves.xnn.ui.components.AvatarImage
import com.selves.xnn.ui.components.UserInfoHeader
import com.selves.xnn.ui.components.LocationTrackingPreview
import com.selves.xnn.ui.screens.TodoScreen
import com.selves.xnn.ui.screens.DynamicScreen
import com.selves.xnn.viewmodel.TodoViewModel
import com.selves.xnn.viewmodel.DynamicViewModel
import com.selves.xnn.viewmodel.VoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentMember: com.selves.xnn.model.Member?,
    onMemberSwitch: () -> Unit,
    onNavigateToTodo: () -> Unit,
    onNavigateToDynamic: () -> Unit,
    onNavigateToVote: () -> Unit,
    onNavigateToLocation: () -> Unit = {}
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeMainScreen(
                currentMember = currentMember,
                onMemberSwitch = onMemberSwitch,
                onNavigateToTodo = onNavigateToTodo,
                onNavigateToDynamic = onNavigateToDynamic,
                onNavigateToVote = onNavigateToVote,
                onNavigateToLocation = onNavigateToLocation
            )
        }
    }
}

@Composable
fun HomeMainScreen(
    currentMember: com.selves.xnn.model.Member?,
    onMemberSwitch: () -> Unit,
    onNavigateToTodo: () -> Unit,
    onNavigateToDynamic: () -> Unit,
    onNavigateToVote: () -> Unit,
    onNavigateToLocation: () -> Unit = {},
    todoViewModel: TodoViewModel = hiltViewModel(),
    dynamicViewModel: DynamicViewModel = hiltViewModel(),
    voteViewModel: VoteViewModel = hiltViewModel()
) {
    // 设置当前成员（用于创建待办事项时记录创建者）
    LaunchedEffect(currentMember?.id) {
        currentMember?.id?.let { memberId ->
            todoViewModel.setCurrentMember(memberId)
            dynamicViewModel.setCurrentUser(memberId)
            voteViewModel.setCurrentUser(memberId)
        }
    }
    
    // 观察共享的待办事项数据
    val pendingTodos by todoViewModel.pendingTodos.collectAsState()
    val todoStats by todoViewModel.todoStats.collectAsState()
    
    // 观察动态数据
    val dynamics by dynamicViewModel.dynamics.collectAsState()
    
    // 观察投票数据
    val activeVotes by voteViewModel.activeVotes.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部用户信息栏
        if (currentMember != null) {
            com.selves.xnn.ui.components.UserInfoHeader(
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
            FunctionModulesSection(
                onNavigateToTodo = onNavigateToTodo,
                onNavigateToDynamic = onNavigateToDynamic,
                onNavigateToVote = onNavigateToVote,
                onNavigateToLocation = onNavigateToLocation
            )
        }
        
        // 轨迹记录预览区域
        item {
            if (currentMember != null) {
                LocationTrackingPreview(
                    currentMemberId = currentMember.id,
                    onNavigateToLocationPage = onNavigateToLocation,
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
            DynamicSection(
                dynamics = dynamics.take(3),
                onNavigateToDynamic = onNavigateToDynamic
            )
        }
        
        item {
            VoteSection(
                votes = activeVotes.take(2),
                onNavigateToVote = onNavigateToVote
            )
        }
    }
    }
}



@Composable
fun FunctionModulesSection(
    onNavigateToTodo: () -> Unit = {},
    onNavigateToDynamic: () -> Unit = {},
    onNavigateToVote: () -> Unit = {},
    onNavigateToLocation: () -> Unit = {}
) {
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
                        "投票" to Icons.Default.Poll,
                        "轨迹" to Icons.Default.LocationOn
                    )
                ) { (title, icon) ->
                    FunctionModuleItem(
                        title = title, 
                        icon = icon,
                        onClick = {
                            when (title) {
                                "待办" -> onNavigateToTodo()
                                "动态" -> onNavigateToDynamic()
                                "投票" -> onNavigateToVote()
                                "轨迹" -> onNavigateToLocation()
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
    pendingTodos: List<com.selves.xnn.model.Todo> = emptyList(),
    todoStats: com.selves.xnn.viewmodel.TodoStats = com.selves.xnn.viewmodel.TodoStats(),
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
    todo: com.selves.xnn.model.Todo,
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
        if (todo.priority != com.selves.xnn.model.TodoPriority.NORMAL) {
            Card(
                modifier = Modifier.padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (todo.priority) {
                        com.selves.xnn.model.TodoPriority.HIGH -> MaterialTheme.colorScheme.error
                        com.selves.xnn.model.TodoPriority.LOW -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Text(
                    text = when (todo.priority) {
                        com.selves.xnn.model.TodoPriority.HIGH -> "紧急"
                        com.selves.xnn.model.TodoPriority.LOW -> "不急"
                        else -> "一般"
                    },
                    fontSize = 12.sp,
                    color = when (todo.priority) {
                        com.selves.xnn.model.TodoPriority.HIGH -> MaterialTheme.colorScheme.onError
                        com.selves.xnn.model.TodoPriority.LOW -> MaterialTheme.colorScheme.onTertiary
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
fun DynamicSection(
    dynamics: List<com.selves.xnn.model.Dynamic> = emptyList(),
    onNavigateToDynamic: () -> Unit = {}
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
                    text = "最新动态",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onNavigateToDynamic) {
                    Text("查看更多")
                }
            }
            
            // 动态列表预览
            if (dynamics.isEmpty()) {
                Text(
                    text = "暂无动态",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                dynamics.forEach { dynamic ->
                    DynamicItem(
                        title = dynamic.title.ifEmpty { "无标题" },
                        content = dynamic.content,
                        time = com.selves.xnn.util.TimeFormatter.formatDetailDateTime(dynamic.createdAt),
                        authorName = dynamic.authorName
                    )
                }
                
                if (dynamics.size >= 3) {
                    Text(
                        text = "查看更多动态",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable { onNavigateToDynamic() }
                    )
                }
            }
        }
    }
}

@Composable
fun DynamicItem(title: String, content: String, time: String, authorName: String = "") {
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = time,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            if (authorName.isNotEmpty()) {
                Text(
                    text = "by $authorName",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun VoteSection(
    votes: List<com.selves.xnn.model.Vote> = emptyList(),
    onNavigateToVote: () -> Unit = {}
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
                    text = "投票活动",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onNavigateToVote) {
                    Text("查看更多")
                }
            }
            
            // 投票列表预览
            if (votes.isEmpty()) {
                Text(
                    text = "暂无投票活动",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                votes.forEach { vote ->
                    VoteItem(
                        title = vote.title,
                        description = vote.description,
                        endTime = vote.endTime?.let { endTime ->
                            val now = java.time.LocalDateTime.now()
                            val duration = java.time.Duration.between(now, endTime)
                            when {
                                duration.toDays() > 0 -> "还有${duration.toDays()}天结束"
                                duration.toHours() > 0 -> "还有${duration.toHours()}小时结束"
                                duration.toMinutes() > 0 -> "还有${duration.toMinutes()}分钟结束"
                                else -> "即将结束"
                            }
                        } ?: "无时间限制",
                        totalVotes = vote.totalVotes
                    )
                }
                
                if (votes.size >= 2) {
                    Text(
                        text = "查看更多投票",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable { onNavigateToVote() }
                    )
                }
            }
        }
    }
}

@Composable
fun VoteItem(title: String, description: String, endTime: String, totalVotes: Int = 0) {
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
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = endTime,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${totalVotes} 票",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 