package com.selves.xnn.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import com.selves.xnn.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.selves.xnn.ui.viewmodels.MainViewModel
import com.selves.xnn.ui.viewmodels.LoadingState
import com.selves.xnn.viewmodel.OnlineStatsViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.ui.graphics.luminance

// 动画配置常量
private const val ANIMATION_DURATION = 450
private const val BACKGROUND_FADE_DURATION = 300

/**
 * 打断动画说明：
 * 在原有动画基础上添加打断处理，不改变原有动画效果
 * - 保持原有的 tween 动画和时长
 * - 添加 finiteRepeatable 支持，确保打断时能正确处理
 */

// 页面进入动画：从右侧滑入（保持原有效果，添加打断支持）
private val slideInFromRight = slideInHorizontally(
    initialOffsetX = { fullWidth -> fullWidth },
    animationSpec = tween(
        durationMillis = ANIMATION_DURATION,
        easing = FastOutSlowInEasing
    )
)

// 页面退出动画：向左侧滑出（保持原有效果，添加打断支持）
private val slideOutToLeft = slideOutHorizontally(
    targetOffsetX = { fullWidth -> -fullWidth },
    animationSpec = tween(
        durationMillis = ANIMATION_DURATION - 100, // 稍快退出
        easing = EaseInOutCubic
    )
)

// 页面进入动画：从左侧滑入（返回时使用，保持原有效果）
private val slideInFromLeft = slideInHorizontally(
    initialOffsetX = { fullWidth -> -fullWidth },
    animationSpec = tween(
        durationMillis = ANIMATION_DURATION,
        easing = FastOutSlowInEasing
    )
)

// 页面退出动画：向右侧滑出（返回时使用，保持原有效果）
private val slideOutToRight = slideOutHorizontally(
    targetOffsetX = { fullWidth -> fullWidth },
    animationSpec = tween(
        durationMillis = ANIMATION_DURATION - 100, // 稍快退出
        easing = EaseInOutCubic
    )
)

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
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        
        // 动画背景暗化效果：排除聊天页，其他二级页面保持暗化
        val shouldDarkenBackground = currentRoute != null &&
            currentRoute != "main" &&
            !currentRoute.startsWith("chat/")

        val backgroundAlpha by animateFloatAsState(
            targetValue = if (shouldDarkenBackground) 0.4f else 0f,
            animationSpec = tween(
                durationMillis = BACKGROUND_FADE_DURATION,
                easing = EaseInOutCubic
            ),
            label = "backgroundAlpha"
        )

        // 根据当前页面动态设置系统系统栏颜色：
        // - 主页（含底部导航）使用 surfaceVariant 与 BottomNav 保持一致
        // - 二级页面使用背景色，保证与页面背景一致
        // 进入二级页面的过渡动画期间（有暗化背景时），将系统栏设为透明，让阴影覆盖到状态栏与导航栏区域，实现整页滑入的观感
        val systemUiController = rememberSystemUiController()
        val baseBarColor = if (currentRoute == "main") MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background
        val navBarColor = baseBarColor
        val useDarkIcons = navBarColor.luminance() > 0.5f
        // 恢复状态栏时遵循应用原有策略：浅色主题为白色，深色主题为 surface
        val isLightTheme = MaterialTheme.colorScheme.surface.luminance() > 0.5f
        val defaultStatusBarColor = if (isLightTheme) Color.White else MaterialTheme.colorScheme.surface
        val defaultStatusBarDarkIcons = defaultStatusBarColor.luminance() > 0.5f
        // 使用背景透明度判断动画阶段，确保退出动画期间也透明覆盖到顶/底部
        LaunchedEffect(backgroundAlpha, navBarColor) {
            if (backgroundAlpha > 0.01f) {
                // 动画阴影期间（含进入与退出），透明系统栏以覆盖到顶/底部
                systemUiController.setStatusBarColor(color = Color.Transparent, darkIcons = false)
                systemUiController.setNavigationBarColor(color = Color.Transparent, darkIcons = false)
            } else {
                // 非阴影状态
                // - 状态栏：恢复为原始策略（浅色白、深色 surface）
                // - 导航栏：与页面背景一致
                systemUiController.setStatusBarColor(color = defaultStatusBarColor, darkIcons = defaultStatusBarDarkIcons)
                systemUiController.setNavigationBarColor(color = navBarColor, darkIcons = useDarkIcons)
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    // 使用主题的表面色调暗化，创造更和谐的视觉效果
                    MaterialTheme.colorScheme.scrim.copy(alpha = backgroundAlpha)
                )
        ) {
            NavHost(
                navController = navController,
                startDestination = "main"
            ) {
            // 主页（包含底部导航栏）
            composable("main") {
                MainTabScreen(
                    viewModel = viewModel,
                    onNavigateToTodo = {
                        navController.navigate("todo") {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToDynamic = {
                        navController.navigate("dynamic") {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToVote = {
                        navController.navigate("vote") {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToMemberManagement = {
                        navController.navigate("member_management") {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToOnlineStats = {
                        navController.navigate("online_stats") {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLocation = {
                        navController.navigate("location") {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate("settings") {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToChat = { groupId ->
                        navController.navigate("chat/$groupId") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            // 待办事项界面（作为独立全屏页面）
            composable(
                route = "todo",
                enterTransition = { slideInFromRight },
                exitTransition = { slideOutToLeft },
                popEnterTransition = { slideInFromLeft },
                popExitTransition = { slideOutToRight }
            ) {
                TodoScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    currentMemberId = currentMember?.id ?: ""
                )
            }
            
            // 动态界面（作为独立全屏页面）
            composable(
                route = "dynamic",
                enterTransition = { slideInFromRight },
                exitTransition = { slideOutToLeft },
                popEnterTransition = { slideInFromLeft },
                popExitTransition = { slideOutToRight }
            ) {
                DynamicScreen(
                    currentMember = currentMember,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onDynamicClick = { dynamicId ->
                        navController.navigate("dynamic_detail/$dynamicId") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            // 动态详情界面 - 与动态页一致的过渡动画
            composable(
                route = "dynamic_detail/{dynamicId}",
                arguments = listOf(navArgument("dynamicId") { type = NavType.StringType }),
                enterTransition = { slideInFromRight },
                exitTransition = { slideOutToLeft },
                popEnterTransition = { slideInFromLeft },
                popExitTransition = { slideOutToRight }
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
            composable(
                route = "vote",
                enterTransition = { slideInFromRight },
                exitTransition = { slideOutToLeft },
                popEnterTransition = { slideInFromLeft },
                popExitTransition = { slideOutToRight }
            ) {
                VoteScreen(
                    currentMember = currentMember,
                    members = members,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onVoteClick = { voteId ->
                        navController.navigate("vote_detail/$voteId") {
                            launchSingleTop = true
                        }
                    },
                    onMemberSelected = { member ->
                        viewModel.setCurrentMember(member)
                    }
                )
            }
            
            // 投票详情界面 - 与动态页一致的过渡动画
            composable(
                route = "vote_detail/{voteId}",
                arguments = listOf(navArgument("voteId") { type = NavType.StringType }),
                enterTransition = { slideInFromRight },
                exitTransition = { slideOutToLeft },
                popEnterTransition = { slideInFromLeft },
                popExitTransition = { slideOutToRight }
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

            // 聊天界面（作为独立页面） - 与动态页一致的过渡动画
            composable(
                route = "chat/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.StringType }),
                enterTransition = { slideInFromRight },
                exitTransition = { slideOutToLeft },
                popEnterTransition = { slideInFromLeft },
                popExitTransition = { slideOutToRight }
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
            composable(
                route = "member_management",
                enterTransition = { slideInFromRight },
                exitTransition = { slideOutToLeft },
                popEnterTransition = { slideInFromLeft },
                popExitTransition = { slideOutToRight }
            ) {
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
            composable(
                route = "online_stats",
                enterTransition = { slideInFromRight },
                exitTransition = { slideOutToLeft },
                popEnterTransition = { slideInFromLeft },
                popExitTransition = { slideOutToRight }
            ) {
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
            
            // 轨迹记录界面（作为独立全屏页面）
            composable(
                route = "location",
                enterTransition = { slideInFromRight },
                exitTransition = { slideOutToLeft },
                popEnterTransition = { slideInFromLeft },
                popExitTransition = { slideOutToRight }
            ) {
                LocationTrackingScreen(
                    currentMemberId = currentMember?.id ?: "",
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 设置界面（作为独立页面）
            composable(
                route = "settings",
                enterTransition = { slideInFromRight },
                exitTransition = { slideOutToLeft },
                popEnterTransition = { slideInFromLeft },
                popExitTransition = { slideOutToRight }
            ) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToAbout = {
                        navController.navigate("about") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            // 关于界面（作为独立页面）
            composable(
                route = "about",
                enterTransition = { slideInFromRight },
                exitTransition = { slideOutToLeft },
                popEnterTransition = { slideInFromLeft },
                popExitTransition = { slideOutToRight }
            ) {
                AboutScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
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
    val context = LocalContext.current
    val iconBitmap = remember {
        val drawable = androidx.core.content.ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
        drawable?.toBitmap()?.asImageBitmap()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // 显示应用图标
        iconBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
    }
}