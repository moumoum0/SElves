package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.myapplication.model.ChatGroup
import com.example.myapplication.model.Member
import com.example.myapplication.navigation.BottomNavItem
import com.example.myapplication.ui.components.BottomNavBar
import com.example.myapplication.ui.components.MemberSwitchDialog
import com.example.myapplication.ui.components.CreateMemberDialog
import com.example.myapplication.ui.components.CreateGroupDialog
import com.example.myapplication.ui.screens.SettingsScreen
import com.example.myapplication.ui.viewmodels.MainViewModel
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import com.example.myapplication.util.ImageUtils
import com.example.myapplication.ui.components.AvatarImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val currentMember by viewModel.currentMember.collectAsState()
    val members by viewModel.members.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showCreateMemberDialog by remember { mutableStateOf(false) }
    var showMemberSwitchDialog by remember { mutableStateOf(false) }
    val navController = rememberNavController()
    val context = LocalContext.current

    // 预加载所有成员的头像到内存缓存
    LaunchedEffect(members) {
        if (members.isNotEmpty()) {
            ImageUtils.preloadAvatarsToMemory(
                context = context,
                avatarPaths = members.mapNotNull { it.avatarUrl },
                coroutineScope = this
            )
        }
    }
    
    // 如果没有成员，根据情况显示创建成员或切换成员对话框
    LaunchedEffect(currentMember, isLoading) {
        if (currentMember == null && !isLoading) {
            if (members.isEmpty()) {
                // 没有任何成员，显示创建成员对话框
                showCreateMemberDialog = true
            } else {
                // 有已存在的成员，显示成员切换对话框
                showMemberSwitchDialog = true
            }
        }
    }

    if (showCreateMemberDialog) {
        CreateMemberDialog(
            existingMemberNames = members.map { it.name },
            onDismiss = { showCreateMemberDialog = false },
            onConfirm = { name, avatarUrl ->
                // 立即关闭对话框，防止多次点击
                showCreateMemberDialog = false
                // 创建成员
                viewModel.createMember(name, avatarUrl)
            }
        )
    }

    if (showMemberSwitchDialog) {
        MemberSwitchDialog(
            members = members,
            currentMemberId = currentMember?.id ?: "",
            onMemberSelected = { member ->
                viewModel.setCurrentMember(member)
                showMemberSwitchDialog = false
            },
            onCreateNewMember = {
                showMemberSwitchDialog = false
                showCreateMemberDialog = true
            },
            onDeleteMember = { member ->
                // 成员删除功能可在后续实现
                showMemberSwitchDialog = false
            },
            onDismiss = { showMemberSwitchDialog = false }
        )
    }

    // 主导航结构
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        // 主页（包含底部导航栏）
        composable("main") {
            MainContent(
                viewModel = viewModel,
                currentMember = currentMember,
                groups = groups,
                isLoading = isLoading,
                onMemberSwitch = { showMemberSwitchDialog = true },
                onGroupClick = { group ->
                    // 导航到聊天界面（作为独立页面）
                    navController.navigate("chat/${group.id}")
                },
                onCreateGroup = { groupName ->
                    val newGroup = viewModel.createGroup(groupName, currentMember!!)
                    // 不再自动进入群聊
                }
            )
        }
        
        // 聊天界面（作为独立页面）
        composable(
            route = "chat/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            val group = groups.find { it.id == groupId }
            val messages by viewModel.messages.collectAsState()
            
            // 当进入聊天界面时，标记所有消息为已读
            LaunchedEffect(groupId) {
                viewModel.markGroupMessagesAsRead(groupId)
            }
            
            if (group != null) {
                // 顶部带返回按钮的聊天界面
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(group.name) },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "返回"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        ChatScreen(
                            currentMember = currentMember!!,
                            group = group,
                            messages = messages[groupId] ?: emptyList(),
                            members = members,
                            onSendMessage = { content ->
                                viewModel.sendMessage(groupId, content)
                            },
                            onDeleteMessage = { messageId ->
                                viewModel.deleteMessage(groupId, messageId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    viewModel: MainViewModel,
    currentMember: Member?,
    groups: List<ChatGroup>,
    isLoading: Boolean,
    onMemberSwitch: () -> Unit,
    onGroupClick: (ChatGroup) -> Unit,
    onCreateGroup: (String) -> Unit
) {
    val mainNavController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            BottomNavBar(navController = mainNavController)
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (currentMember != null) {
            NavHost(
                navController = mainNavController,
                startDestination = BottomNavItem.Chat.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(BottomNavItem.Chat.route) {
                    ChatTab(
                        viewModel = viewModel,
                        currentMember = currentMember,
                        groups = groups,
                        onMemberSwitch = onMemberSwitch,
                        onGroupClick = onGroupClick,
                        onCreateGroup = onCreateGroup
                    )
                }
                
                composable(BottomNavItem.Settings.route) {
                    SettingsScreen(
                        currentMember = currentMember,
                        onMemberSwitch = onMemberSwitch
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTab(
    viewModel: MainViewModel,
    currentMember: Member,
    groups: List<ChatGroup>,
    onMemberSwitch: () -> Unit,
    onGroupClick: (ChatGroup) -> Unit,
    onCreateGroup: (String) -> Unit
) {
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 创建头像默认图标
    val defaultIcon = @Composable {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "成员头像默认图标",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .padding(8.dp)
        )
    }

    if (showCreateGroupDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateGroupDialog = false },
            onConfirm = { groupName ->
                showCreateGroupDialog = false
                onCreateGroup(groupName)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            // 顶部成员信息栏
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
                TextButton(onClick = onMemberSwitch) {
                    Text("切换成员")
                }
            }

            // 群聊列表
            if (groups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无群聊，请点击右下角创建")
                }
            } else {
                // 获取消息状态用于排序
                val messages by viewModel.messages.collectAsState()
                
                // 按最新消息时间排序群聊列表
                val sortedGroups = groups.sortedByDescending { group ->
                    val groupMessages = messages[group.id] ?: emptyList()
                    val latestMessage = groupMessages.lastOrNull()
                    latestMessage?.timestamp ?: group.createdAt
                }
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(sortedGroups.size * 2 - 1) { index ->
                        if (index % 2 == 0) {
                            // 群聊项目
                            val groupIndex = index / 2
                            val group = sortedGroups[groupIndex]
                            GroupItem(
                                viewModel = viewModel,
                                group = group,
                                currentMember = currentMember,
                                onClick = { onGroupClick(group) }
                            )
                        } else {
                            // 分隔线
                            Divider(
                                modifier = Modifier
                                    .padding(start = 72.dp)
                                    .fillMaxWidth(),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }

        // 右下角的添加按钮
        FloatingActionButton(
            onClick = { showCreateGroupDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "创建群聊"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupItem(
    viewModel: MainViewModel,
    group: ChatGroup,
    currentMember: Member,
    onClick: () -> Unit
) {
    // 获取消息状态
    val messages by viewModel.messages.collectAsState()
    val groupMessages = messages[group.id] ?: emptyList()
    
    // 获取最新消息和未读数量
    val latestMessage = groupMessages.lastOrNull()
    val unreadCount = viewModel.getUnreadCount(group.id)
    val allMembers by viewModel.allMembers.collectAsState()
    
    // 获取发送者信息
    val sender = latestMessage?.let { message ->
        allMembers.find { it.id == message.senderId }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧头像
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (group.name.isNotEmpty()) {
                        // 根据群聊名称生成颜色
                        val colors = listOf(
                            Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFF9800),
                            Color(0xFF9C27B0), Color(0xFFF44336), Color(0xFF00BCD4)
                        )
                        colors[group.name.hashCode().rem(colors.size).let { if (it < 0) -it else it }]
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (group.name.isNotEmpty()) group.name.first().toString().uppercase() else "G",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 中间内容区域
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // 群聊名称
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // 最新消息
            if (latestMessage != null) {
                val messageText = if (sender?.id == currentMember.id) {
                    "你: ${latestMessage.content}"
                } else {
                    "${sender?.name ?: "未知用户"}: ${latestMessage.content}"
                }
                
                Text(
                    text = messageText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "暂无消息",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 右侧时间和未读数量
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 时间
            if (latestMessage != null) {
                Text(
                    text = viewModel.formatMessageTime(latestMessage.timestamp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = viewModel.formatMessageTime(group.createdAt),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            // 未读消息数量
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .defaultMinSize(minWidth = 20.dp, minHeight = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
} 