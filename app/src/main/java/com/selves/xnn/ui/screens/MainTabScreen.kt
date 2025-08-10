package com.selves.xnn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.selves.xnn.model.ChatGroup
import com.selves.xnn.model.Member
import com.selves.xnn.navigation.BottomNavItem
import com.selves.xnn.ui.components.BottomNavBar
import com.selves.xnn.ui.components.MemberSwitchDialog
import com.selves.xnn.ui.components.CreateMemberDialog
import com.selves.xnn.ui.components.CreateSystemDialog
import com.selves.xnn.ui.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabScreen(
    viewModel: MainViewModel,
    onNavigateToTodo: () -> Unit,
    onNavigateToDynamic: () -> Unit,
    onNavigateToVote: () -> Unit,
    onNavigateToMemberManagement: () -> Unit,
    onNavigateToOnlineStats: () -> Unit,

    onNavigateToSettings: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val currentMember by viewModel.currentMember.collectAsState()
    val members by viewModel.members.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val hasSystem by viewModel.hasSystem.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val memberLoginRecords by viewModel.memberLoginRecords.collectAsState()
    
    var showCreateMemberDialog by remember { mutableStateOf(false) }
    var showMemberSwitchDialog by remember { mutableStateOf(false) }
    var showCreateSystemDialog by remember { mutableStateOf(false) }
    val mainNavController = rememberNavController()
    val context = LocalContext.current
    
    // 检查系统是否存在，如果不存在则显示创建系统对话框
    LaunchedEffect(hasSystem, isLoading) {
        // 只有在加载完成且系统不存在时才显示创建系统对话框
        if (!isLoading && hasSystem == false) {
            showCreateSystemDialog = true
        }
    }
    
    // 如果没有成员，根据情况显示创建成员或切换成员对话框
    LaunchedEffect(currentMember, isLoading, hasSystem) {
        if (currentMember == null && !isLoading && hasSystem == true) {
            if (members.isEmpty()) {
                // 没有任何成员，显示创建成员对话框
                showCreateMemberDialog = true
            } else {
                // 有已存在的成员，显示成员切换对话框
                showMemberSwitchDialog = true
            }
        }
    }

    if (showCreateSystemDialog) {
        CreateSystemDialog(
            onDismiss = { 
                // 系统创建是必需的，不允许取消
            },
            onConfirm = { name, avatarUrl ->
                showCreateSystemDialog = false
                // 创建系统
                viewModel.createSystem(name, avatarUrl)
            },
            canDismiss = false
        )
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
            onDismiss = { showMemberSwitchDialog = false },
            loginRecordsMap = memberLoginRecords
        )
    }

    if (currentMember != null) {
        Scaffold(
            bottomBar = {
                BottomNavBar(navController = mainNavController)
            }
        ) { paddingValues ->
            NavHost(
                navController = mainNavController,
                startDestination = BottomNavItem.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(BottomNavItem.Home.route) {
                    HomeScreen(
                        currentMember = currentMember,
                        onMemberSwitch = { showMemberSwitchDialog = true },
                        onNavigateToTodo = onNavigateToTodo,
                        onNavigateToDynamic = onNavigateToDynamic,
                        onNavigateToVote = onNavigateToVote
                    )
                }
                
                composable(BottomNavItem.Chat.route) {
                    GroupChatScreen(
                        viewModel = viewModel,
                        currentMember = currentMember!!,
                        groups = groups,
                        members = members,
                        onMemberSwitch = { showMemberSwitchDialog = true },
                        onGroupClick = { group ->
                            // 导航到聊天界面（作为独立页面）
                            onNavigateToChat(group.id)
                        },
                        onCreateGroup = { groupName, avatarUrl, selectedMembers ->
                            val newGroup = viewModel.createGroup(groupName, avatarUrl, selectedMembers, currentMember!!)
                            // 不再自动进入群聊
                        }
                    )
                }
                
                composable(BottomNavItem.Settings.route) {
                    SystemScreen(
                        currentMember = currentMember!!,
                        allMembers = members,
                        onNavigateToMemberManagement = onNavigateToMemberManagement,
                        onNavigateToOnlineStats = onNavigateToOnlineStats,

                        onNavigateToSettings = onNavigateToSettings
                    )
                }
            }
        }
    } else {
        // 当没有当前成员时，显示空白页面，让对话框处理成员创建
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 不显示加载指示器，让对话框处理成员创建流程
        }
    }
}