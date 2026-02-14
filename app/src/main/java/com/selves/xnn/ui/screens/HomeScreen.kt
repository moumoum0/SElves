package com.selves.xnn.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import com.selves.xnn.ui.components.AvatarImage
import com.selves.xnn.ui.components.UserInfoHeader
import com.selves.xnn.ui.components.LocationTrackingPreview
import com.selves.xnn.ui.screens.TodoScreen
import com.selves.xnn.ui.screens.DynamicScreen
import com.selves.xnn.ui.viewmodels.MainViewModel
import com.selves.xnn.viewmodel.TodoViewModel
import com.selves.xnn.viewmodel.DynamicViewModel
import com.selves.xnn.viewmodel.VoteViewModel
import com.selves.xnn.viewmodel.LocationTrackingViewModel
import com.selves.xnn.viewmodel.LocationTrackingUiState
import com.selves.xnn.model.TrackingStatus
import com.selves.xnn.model.TrackingStats
import com.selves.xnn.model.HomeLayoutConfig
import com.selves.xnn.model.HomeModuleType
import com.selves.xnn.model.FunctionModuleConfig
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentMember: com.selves.xnn.model.Member?,
    onMemberSwitch: () -> Unit,
    onNavigateToTodo: () -> Unit,
    onNavigateToDynamic: () -> Unit,
    onNavigateToVote: () -> Unit,
    onNavigateToLocation: () -> Unit = {},
    viewModel: MainViewModel
) {
    HomeMainScreen(
        currentMember = currentMember,
        onMemberSwitch = onMemberSwitch,
        onNavigateToTodo = onNavigateToTodo,
        onNavigateToDynamic = onNavigateToDynamic,
        onNavigateToVote = onNavigateToVote,
        onNavigateToLocation = onNavigateToLocation,
        viewModel = viewModel
    )
}

@Composable
fun HomeMainScreen(
    currentMember: com.selves.xnn.model.Member?,
    onMemberSwitch: () -> Unit,
    onNavigateToTodo: () -> Unit,
    onNavigateToDynamic: () -> Unit,
    onNavigateToVote: () -> Unit,
    onNavigateToLocation: () -> Unit = {},
    viewModel: MainViewModel,
    todoViewModel: TodoViewModel = hiltViewModel(),
    dynamicViewModel: DynamicViewModel = hiltViewModel(),
    voteViewModel: VoteViewModel = hiltViewModel(),
    locationTrackingViewModel: LocationTrackingViewModel = hiltViewModel()
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
    
    // 观察轨迹记录数据
    val trackingUiState by locationTrackingViewModel.uiState.collectAsState()
    val trackingStats by locationTrackingViewModel.trackingStats.collectAsState()
    
    // 设置轨迹记录的当前成员
    LaunchedEffect(currentMember?.id) {
        currentMember?.id?.let { memberId ->
            locationTrackingViewModel.setCurrentMemberId(memberId)
        }
    }

    // 首页布局配置
    val homeLayoutConfig by viewModel.homeLayoutConfig.collectAsState()

    // 编辑模式状态
    val isEditMode by viewModel.isHomeEditMode.collectAsState()

    // 拖拽排序状态
    var draggedModuleType by remember { mutableStateOf<HomeModuleType?>(null) }
    // 拖动偏移量（像素值，用于graphicsLayer跟手）
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    // 记录每个可见模块的高度（像素），用于计算拖动目标
    val moduleHeights = remember { mutableStateMapOf<HomeModuleType, Int>() }

    // 获取当前可见且按顺序排列的模块列表
    val visibleModuleOrder = remember(homeLayoutConfig) {
        homeLayoutConfig.moduleOrder.filter { homeLayoutConfig.moduleVisibility[it] == true }
    }

    // 显示编辑对话框的状态
    var showEditDialog by remember { mutableStateOf(false) }
    var showFunctionModuleEditDialog by remember { mutableStateOf(false) }

    // 获取轨迹记录状态（通过服务直接获取，避免创建 ViewModel）
    val trackingStatus = remember {
        com.selves.xnn.service.LocationTrackingService.isTrackingActive()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // 双指向中心滑动手势检测
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    // 双指缩放手势检测（捏合手势）
                    if (zoom < 0.95f && !isEditMode) {
                        viewModel.toggleHomeEditMode()
                    }
                }
            }
    ) {
        // 编辑模式提示 - 类似安卓桌面编辑风格
        if (isEditMode) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "桌面编辑模式",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    TextButton(onClick = { viewModel.exitHomeEditMode() }) {
                        Text("完成")
                    }
                }
            }
        }

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
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = !isEditMode  // 编辑模式下禁用滚动，以便拖动卡片
        ) {

        // 按 moduleOrder 顺序遍历渲染可见模块
        items(
            count = visibleModuleOrder.size,
            key = { visibleModuleOrder[it].name }
        ) { index ->
            val moduleType = visibleModuleOrder[index]
            val isCurrentDragging = draggedModuleType == moduleType

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        moduleHeights[moduleType] = coordinates.size.height
                    }
                    .graphicsLayer {
                        // 被拖动的卡片：跟手偏移 + 提升到最上层 + 轻微缩放
                        if (isCurrentDragging) {
                            translationY = dragOffsetPx
                            scaleX = 1.03f
                            scaleY = 1.03f
                        }
                    }
                    .zIndex(if (isCurrentDragging) 10f else 0f)
                    .then(
                        if (isEditMode) {
                            Modifier.pointerInput(moduleType) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedModuleType = moduleType
                                        dragOffsetPx = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetPx += dragAmount.y
                                    },
                                    onDragEnd = {
                                        // 根据累计偏移量计算目标位置
                                        val currentIndex = visibleModuleOrder.indexOf(moduleType)
                                        if (currentIndex != -1) {
                                            var accumulatedHeight = 0f
                                            val spacing = 16f * density // 16.dp 转像素
                                            if (dragOffsetPx > 0) {
                                                // 向下拖动：累加后续模块高度
                                                var targetIndex = currentIndex
                                                for (i in (currentIndex + 1) until visibleModuleOrder.size) {
                                                    val h = (moduleHeights[visibleModuleOrder[i]] ?: 0).toFloat() + spacing
                                                    accumulatedHeight += h
                                                    if (dragOffsetPx > accumulatedHeight - h / 2) {
                                                        targetIndex = i
                                                    } else {
                                                        break
                                                    }
                                                }
                                                if (targetIndex != currentIndex) {
                                                    val newOrder = homeLayoutConfig.moduleOrder.toMutableList()
                                                    newOrder.remove(moduleType)
                                                    val insertAt = newOrder.indexOf(visibleModuleOrder[targetIndex]) + 1
                                                    newOrder.add(insertAt.coerceAtMost(newOrder.size), moduleType)
                                                    viewModel.updateModuleOrder(newOrder)
                                                }
                                            } else if (dragOffsetPx < 0) {
                                                // 向上拖动：累加前面模块高度
                                                var targetIndex = currentIndex
                                                for (i in (currentIndex - 1) downTo 0) {
                                                    val h = (moduleHeights[visibleModuleOrder[i]] ?: 0).toFloat() + spacing
                                                    accumulatedHeight += h
                                                    if (-dragOffsetPx > accumulatedHeight - h / 2) {
                                                        targetIndex = i
                                                    } else {
                                                        break
                                                    }
                                                }
                                                if (targetIndex != currentIndex) {
                                                    val newOrder = homeLayoutConfig.moduleOrder.toMutableList()
                                                    newOrder.remove(moduleType)
                                                    val insertAt = newOrder.indexOf(visibleModuleOrder[targetIndex])
                                                    newOrder.add(insertAt.coerceAtLeast(0), moduleType)
                                                    viewModel.updateModuleOrder(newOrder)
                                                }
                                            }
                                        }
                                        draggedModuleType = null
                                        dragOffsetPx = 0f
                                    },
                                    onDragCancel = {
                                        draggedModuleType = null
                                        dragOffsetPx = 0f
                                    }
                                )
                            }
                        } else Modifier
                    )
            ) {
                when (moduleType) {
                    HomeModuleType.FUNCTION_MODULES -> FunctionModulesSection(
                        onNavigateToTodo = onNavigateToTodo,
                        onNavigateToDynamic = onNavigateToDynamic,
                        onNavigateToVote = onNavigateToVote,
                        onNavigateToLocation = onNavigateToLocation,
                        functionModules = homeLayoutConfig.functionModules,
                        isEditMode = isEditMode,
                        onEditClick = { showFunctionModuleEditDialog = true },
                        onModuleToggle = { moduleId ->
                            viewModel.toggleFunctionModuleEnabled(moduleId)
                        }
                    )
                    HomeModuleType.LOCATION_TRACKING -> {
                        if (currentMember != null) {
                            LocationTrackingSection(
                                trackingUiState = trackingUiState,
                                trackingStats = trackingStats,
                                onNavigateToLocation = onNavigateToLocation,
                                isEditMode = isEditMode,
                                onEditClick = {
                                    viewModel.toggleModuleVisibility(HomeModuleType.LOCATION_TRACKING)
                                }
                            )
                        }
                    }
                    HomeModuleType.TODO -> TodoSection(
                        onNavigateToTodo = onNavigateToTodo,
                        pendingTodos = pendingTodos,
                        todoStats = todoStats,
                        onTodoStatusChange = { todoId, isCompleted ->
                            todoViewModel.updateTodoStatus(todoId, isCompleted)
                        },
                        isEditMode = isEditMode,
                        onEditClick = {
                            viewModel.toggleModuleVisibility(HomeModuleType.TODO)
                        }
                    )
                    HomeModuleType.DYNAMIC -> DynamicSection(
                        dynamics = dynamics.take(3),
                        onNavigateToDynamic = onNavigateToDynamic,
                        isEditMode = isEditMode,
                        onEditClick = {
                            viewModel.toggleModuleVisibility(HomeModuleType.DYNAMIC)
                        }
                    )
                    HomeModuleType.VOTE -> VoteSection(
                        votes = activeVotes.take(2),
                        onNavigateToVote = onNavigateToVote,
                        isEditMode = isEditMode,
                        onEditClick = {
                            viewModel.toggleModuleVisibility(HomeModuleType.VOTE)
                        }
                    )
                }
            }
        }

        // 添加空白区域，为 FAB 留出空间
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
        }
    }

    // 编辑模式下的 FAB
    if (isEditMode) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { showEditDialog = true },
                modifier = Modifier
                    .padding(end = 16.dp, bottom = 16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "编辑布局"
                )
            }
        }
    }

    // 首页布局编辑对话框
    if (showEditDialog) {
        HomeLayoutEditDialog(
            currentConfig = homeLayoutConfig,
            onDismiss = { showEditDialog = false },
            onSave = { newConfig ->
                viewModel.saveHomeLayoutConfig(newConfig)
                showEditDialog = false
            }
        )
    }

    // 功能模块编辑对话框
    if (showFunctionModuleEditDialog) {
        FunctionModuleEditDialog(
            currentModules = homeLayoutConfig.functionModules,
            onDismiss = { showFunctionModuleEditDialog = false },
            onSave = { newModules ->
                viewModel.updateFunctionModules(newModules)
                showFunctionModuleEditDialog = false
            }
        )
    }
}



@Composable
fun FunctionModulesSection(
    onNavigateToTodo: () -> Unit = {},
    onNavigateToDynamic: () -> Unit = {},
    onNavigateToVote: () -> Unit = {},
    onNavigateToLocation: () -> Unit = {},
    functionModules: List<FunctionModuleConfig> = FunctionModuleConfig.defaultList(),
    isEditMode: Boolean = false,
    onEditClick: () -> Unit = {},
    onModuleToggle: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEditMode) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
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
                    text = "功能模块",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                if (isEditMode) {
                    TextButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "编辑",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("管理")
                    }
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(functionModules.filter { it.enabled }) { module ->
                    val icon = when (module.iconName) {
                        "Assignment" -> Icons.Default.Assignment
                        "Timeline" -> Icons.Default.Timeline
                        "Poll" -> Icons.Default.Poll
                        "LocationOn" -> Icons.Default.LocationOn
                        else -> Icons.Default.Assignment
                    }
                    FunctionModuleItem(
                        title = module.title,
                        icon = icon,
                        onClick = {
                            when (module.id) {
                                "todo" -> onNavigateToTodo()
                                "dynamic" -> onNavigateToDynamic()
                                "vote" -> onNavigateToVote()
                                "location" -> onNavigateToLocation()
                            }
                        },
                        isEditMode = isEditMode,
                        onToggle = { onModuleToggle(module.id) }
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
    onClick: () -> Unit = {},
    isEditMode: Boolean = false,
    onToggle: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(100.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = CardDefaults.outlinedCardBorder()
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
    onTodoStatusChange: (String, Boolean) -> Unit = { _, _ -> },
    isEditMode: Boolean = false,
    onEditClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEditMode) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
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
                if (isEditMode) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "隐藏",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    IconButton(onClick = onNavigateToTodo) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "查看更多",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (!isEditMode) {
            // 待办事项列表预览（显示前2个）
            if (pendingTodos.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "暂无待办事项",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "点击右上角进入待办页面创建",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
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
            }  // 关闭 if (!isEditMode)
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
fun LocationTrackingSection(
    trackingUiState: LocationTrackingUiState,
    trackingStats: TrackingStats,
    onNavigateToLocation: () -> Unit,
    isEditMode: Boolean = false,
    onEditClick: () -> Unit = {}
) {
    // 颜色直接在 Composable 作用域中计算
    val statusColor = when (trackingUiState.trackingStatus) {
        TrackingStatus.RECORDING -> MaterialTheme.colorScheme.primary
        TrackingStatus.STOPPED -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.outline
    }
    val statusText = remember(trackingUiState.trackingStatus) {
        when (trackingUiState.trackingStatus) {
            TrackingStatus.RECORDING -> "记录中"
            TrackingStatus.STOPPED -> "未记录"
            else -> "未知"
        }
    }

    // 缓存最后记录时间格式化
    val lastRecordTimeText = remember(trackingStats.lastRecordTime) {
        trackingStats.lastRecordTime?.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) ?: "--:--"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEditMode) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "轨迹记录",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isEditMode) {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "隐藏",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        // 状态指示器
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = statusColor,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = statusText,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(onClick = onNavigateToLocation) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "查看更多",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (!isEditMode) {
            Spacer(modifier = Modifier.height(12.dp))

            // 统计信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LocationStatItem(
                    label = "今日记录",
                    value = trackingStats.todayRecords.toString(),
                    icon = Icons.Default.Today
                )

                LocationStatItem(
                    label = "总记录数",
                    value = trackingStats.totalRecords.toString(),
                    icon = Icons.Default.Timeline
                )

                LocationStatItem(
                    label = "最后记录",
                    value = lastRecordTimeText,
                    icon = Icons.Default.Schedule
                )
            }
            }  // 关闭 if (!isEditMode)
        }
    }
}

@Composable
fun LocationStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DynamicSection(
    dynamics: List<com.selves.xnn.model.Dynamic> = emptyList(),
    onNavigateToDynamic: () -> Unit = {},
    isEditMode: Boolean = false,
    onEditClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEditMode) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
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
                if (isEditMode) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "隐藏",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    IconButton(onClick = onNavigateToDynamic) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "查看更多",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (!isEditMode) {
            // 动态列表预览
            if (dynamics.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "暂无动态",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "点击右上角进入动态页面发布",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            } else {
                dynamics.take(3).forEach { dynamic ->
                    DynamicItem(dynamic = dynamic)
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
            }  // 关闭 if (!isEditMode)
        }
    }
}

@Composable
fun DynamicItem(dynamic: com.selves.xnn.model.Dynamic, modifier: Modifier = Modifier) {
    // 使用 remember 缓存时间格式化结果
    val formattedTime = remember(dynamic.createdAt) {
        com.selves.xnn.util.TimeFormatter.formatDetailDateTime(dynamic.createdAt)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = dynamic.title.ifEmpty { "无标题" },
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dynamic.content,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formattedTime,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            if (dynamic.authorName.isNotEmpty()) {
                Text(
                    text = "by ${dynamic.authorName}",
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
    onNavigateToVote: () -> Unit = {},
    isEditMode: Boolean = false,
    onEditClick: () -> Unit = {}
) {
    // 使用 remember 缓存当前时间，避免每次重组都计算
    val now = remember { java.time.LocalDateTime.now() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEditMode) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
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
                if (isEditMode) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "隐藏",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    IconButton(onClick = onNavigateToVote) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "查看更多",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (!isEditMode) {
            // 投票列表预览
            if (votes.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Poll,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "暂无投票活动",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "点击右上角进入投票页面创建",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            } else {
                votes.forEach { vote ->
                    VoteItem(
                        vote = vote,
                        now = now
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
            }  // 关闭 if (!isEditMode)
        }
    }
}

@Composable
fun VoteItem(
    vote: com.selves.xnn.model.Vote,
    now: java.time.LocalDateTime = remember { java.time.LocalDateTime.now() }
) {
    // 使用 remember 缓存时间计算结果
    val endTimeText = remember(vote.endTime, now) {
        vote.endTime?.let { endTime ->
            val duration = java.time.Duration.between(now, endTime)
            when {
                duration.toDays() > 0 -> "还有${duration.toDays()}天结束"
                duration.toHours() > 0 -> "还有${duration.toHours()}小时结束"
                duration.toMinutes() > 0 -> "还有${duration.toMinutes()}分钟结束"
                else -> "即将结束"
            }
        } ?: "无时间限制"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = vote.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = vote.description,
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
                text = endTimeText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${vote.totalVotes} 票",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 首页布局编辑对话框
 */
@Composable
fun HomeLayoutEditDialog(
    currentConfig: HomeLayoutConfig,
    onDismiss: () -> Unit,
    onSave: (HomeLayoutConfig) -> Unit
) {
    var moduleVisibility by remember { mutableStateOf(currentConfig.moduleVisibility.toMutableMap()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "编辑首页布局")
        },
        text = {
            Column {
                Text(
                    text = "点击切换模块显示/隐藏",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 模块列表
                HomeModuleType.entries.forEach { moduleType ->
                    val isVisible = moduleVisibility[moduleType] ?: true
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isVisible) {
                                MaterialTheme.colorScheme.surfaceVariant
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (moduleType) {
                                    HomeModuleType.FUNCTION_MODULES -> "功能模块"
                                    HomeModuleType.LOCATION_TRACKING -> "轨迹记录"
                                    HomeModuleType.TODO -> "待办事项"
                                    HomeModuleType.DYNAMIC -> "最新动态"
                                    HomeModuleType.VOTE -> "投票活动"
                                }
                            )
                            Switch(
                                checked = isVisible,
                                onCheckedChange = { visible ->
                                    moduleVisibility = moduleVisibility.toMutableMap().apply {
                                        this[moduleType] = visible
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        HomeLayoutConfig(
                            moduleOrder = currentConfig.moduleOrder,
                            moduleVisibility = moduleVisibility,
                            functionModules = currentConfig.functionModules
                        )
                    )
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 功能模块编辑对话框
 */
@Composable
fun FunctionModuleEditDialog(
    currentModules: List<FunctionModuleConfig>,
    onDismiss: () -> Unit,
    onSave: (List<FunctionModuleConfig>) -> Unit
) {
    var modules by remember { mutableStateOf(currentModules) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("管理功能模块") },
        text = {
            Column {
                modules.forEach { module ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = module.title)
                        Switch(
                            checked = module.enabled,
                            onCheckedChange = { checked ->
                                modules = modules.map {
                                    if (it.id == module.id) it.copy(enabled = checked) else it
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(modules) }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}