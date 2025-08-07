package com.selves.xnn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.selves.xnn.ui.viewmodels.MainViewModel
import com.selves.xnn.ui.viewmodels.LoadingState
import com.selves.xnn.viewmodel.OnlineStatsViewModel

@Composable
fun AppNavigationScreen(
    viewModel: MainViewModel
) {
    val navController = rememberNavController()
    val currentMember by viewModel.currentMember.collectAsState()
    val members by viewModel.members.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasSystem by viewModel.hasSystem.collectAsState()
    val needsGuide by viewModel.needsGuide.collectAsState()
    
    // 如果正在加载，显示加载界面
    if (isLoading) {
        val loadingState by viewModel.loadingState.collectAsState()
        LoadingScreen(loadingState = loadingState)
    } else if (needsGuide == true) {
        // 如果需要引导，显示引导界面
        val isBackupInProgress by viewModel.isBackupInProgress.collectAsState()
        val backupProgress by viewModel.backupProgress.collectAsState()
        val backupProgressMessage by viewModel.backupProgressMessage.collectAsState()
        val showImportWarningDialog by viewModel.showImportWarningDialog.collectAsState()
        val backupImportSuccess by viewModel.backupImportSuccess.collectAsState()
        
        WelcomeGuideScreen(
            onCreateSystem = { name, avatarUrl ->
                viewModel.createSystem(name, avatarUrl)
            },
            onCreateMember = { name, avatarUrl ->
                viewModel.createMember(name, avatarUrl)
            },
            onImportBackup = { uri ->
                viewModel.showImportWarning(uri)
            },
            onCompleteGuide = {
                viewModel.completeGuide()
            },
            isBackupInProgress = isBackupInProgress,
            backupProgress = backupProgress,
            backupProgressMessage = backupProgressMessage,
            showImportWarningDialog = showImportWarningDialog,
            onConfirmImport = {
                viewModel.confirmImportBackup()
            },
            onCancelImport = {
                viewModel.cancelImportBackup()
            },
            backupImportSuccess = backupImportSuccess
        )
    } else {
        // 主导航结构
        NavHost(
            navController = navController,
            startDestination = "main"
        ) {
            // 主页（包含底部导航栏）
            composable("main") {
                MainTabScreen(
                    viewModel = viewModel,
                    onNavigateToTodo = {
                        navController.navigate("todo")
                    },
                    onNavigateToDynamic = {
                        navController.navigate("dynamic")
                    },
                    onNavigateToVote = {
                        navController.navigate("vote")
                    },
                    onNavigateToMemberManagement = {
                        navController.navigate("member_management")
                    },
                    onNavigateToOnlineStats = {
                        navController.navigate("online_stats")
                    },

                    onNavigateToSettings = {
                        navController.navigate("settings")
                    },
                    onNavigateToChat = { groupId ->
                        navController.navigate("chat/$groupId")
                    }
                )
            }
            
            // 待办事项界面（作为独立全屏页面）
            composable("todo") {
                TodoScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    currentMemberId = currentMember?.id ?: ""
                )
            }
            
            // 动态界面（作为独立全屏页面）
            composable("dynamic") {
                DynamicScreen(
                    currentMember = currentMember,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onDynamicClick = { dynamicId ->
                        navController.navigate("dynamic_detail/$dynamicId")
                    }
                )
            }
            
            // 动态详情界面
            composable(
                route = "dynamic_detail/{dynamicId}",
                arguments = listOf(navArgument("dynamicId") { type = NavType.StringType })
            ) { backStackEntry ->
                val dynamicId = backStackEntry.arguments?.getString("dynamicId") ?: return@composable
                
                DynamicDetailScreen(
                    dynamicId = dynamicId,
                    currentMember = currentMember,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            // 投票界面（作为独立全屏页面）
            composable("vote") {
                VoteScreen(
                    currentMember = currentMember,
                    members = members,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onVoteClick = { voteId ->
                        navController.navigate("vote_detail/$voteId")
                    },
                    onMemberSelected = { member ->
                        viewModel.setCurrentMember(member)
                    }
                )
            }
            
            // 投票详情界面
            composable(
                route = "vote_detail/{voteId}",
                arguments = listOf(navArgument("voteId") { type = NavType.StringType })
            ) { backStackEntry ->
                val voteId = backStackEntry.arguments?.getString("voteId") ?: return@composable
                
                VoteDetailScreen(
                    voteId = voteId,
                    currentMember = currentMember,
                    members = members,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onMemberSelected = { member ->
                        viewModel.setCurrentMember(member)
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
                    ChatScreen(
                        currentMember = currentMember!!,
                        group = group,
                        messages = messages[groupId] ?: emptyList(),
                        members = members,
                        onSendMessage = { content ->
                            viewModel.sendMessage(groupId, content)
                        },
                        onSendImageMessage = { imageUri ->
                            viewModel.sendImageMessage(groupId, imageUri)
                        },
                        onDeleteMessage = { messageId ->
                            viewModel.deleteMessage(groupId, messageId)
                        },
                        onAddMembers = { membersToAdd ->
                            viewModel.addMembersToGroup(groupId, membersToAdd)
                        },
                        onRemoveMembers = { membersToRemove ->
                            viewModel.removeMembersFromGroup(groupId, membersToRemove)
                        },
                        onUpdateGroupInfo = { newName, newAvatarUrl ->
                            viewModel.updateGroupInfo(groupId, newName, newAvatarUrl)
                        },
                        onDeleteGroup = {
                            viewModel.deleteGroup(groupId)
                            navController.popBackStack()
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onMemberSelected = { member ->
                            viewModel.setCurrentMember(member)
                        }
                    )
                }
            }
            
            // 成员管理界面（作为独立页面）
            composable("member_management") {
                MemberManagementScreen(
                    members = members,
                    currentMember = currentMember!!,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    mainViewModel = viewModel // 使用同一个 MainViewModel 实例，确保状态一致
                )
            }
            
            // 在线统计界面（作为独立页面）
            composable("online_stats") {
                val onlineStatsViewModel: OnlineStatsViewModel = hiltViewModel()
                val onlineStats by onlineStatsViewModel.onlineStats.collectAsState()
                val isLoading by onlineStatsViewModel.isLoading.collectAsState()
                val loginLogs by onlineStatsViewModel.loginLogs.collectAsState()
                val loginLogSummary by onlineStatsViewModel.loginLogSummary.collectAsState()
                val isLoadingLogs by onlineStatsViewModel.isLoadingLogs.collectAsState()
                
                LaunchedEffect(currentMember) {
                    currentMember?.let { member ->
                        onlineStatsViewModel.loadOnlineStats(member)
                    }
                }
                
                OnlineStatsScreen(
                    members = members,
                    currentMember = currentMember!!,
                    onlineStats = onlineStats,
                    isLoading = isLoading,
                    loginLogs = loginLogs,
                    loginLogSummary = loginLogSummary,
                    isLoadingLogs = isLoadingLogs,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onLoadLoginLogs = { filter ->
                        onlineStatsViewModel.loadLoginLogs(filter)
                    },
                    onLoadLoginLogSummary = {
                        onlineStatsViewModel.loadLoginLogSummary()
                    }
                )
            }
            

            // 设置界面（作为独立页面）
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToAbout = {
                        navController.navigate("about")
                    }
                )
            }
            
            // 关于界面（作为独立页面）
            composable("about") {
                AboutScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

/**
 * 统一的加载界面组件
 */
@Composable
fun LoadingScreen(
    loadingState: LoadingState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(48.dp)
        ) {
            // 进度条
            LinearProgressIndicator(
                progress = loadingState.progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 进度百分比
            Text(
                text = "${(loadingState.progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )
        }
    }
}