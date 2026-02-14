package com.selves.xnn.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.selves.xnn.ui.components.CreateMemberForm
import com.selves.xnn.ui.components.CreateSystemForm
import com.selves.xnn.ui.components.BackupProgressDialog
import com.selves.xnn.ui.components.ImportBackupWarningDialog

/**
 * 引导界面的步骤枚举
 */
enum class GuideStep {
    WELCOME,
    IMPORT_OR_CREATE,
    CREATE_SYSTEM,
    CREATE_MEMBER,
    COMPLETE
}

@Composable
fun WelcomeGuideScreen(
    onCreateSystem: (name: String, avatarUrl: String) -> Unit,
    onCreateMember: (name: String, avatarUrl: String) -> Unit,
    onImportBackup: (android.net.Uri) -> Unit,
    onCompleteGuide: () -> Unit,
    isBackupInProgress: Boolean = false,
    backupProgress: Float? = null,
    backupProgressMessage: String = "",
    showImportWarningDialog: Boolean = false,
    onConfirmImport: () -> Unit = {},
    onCancelImport: () -> Unit = {},
    backupImportSuccess: Boolean = false
) {
    var currentStep by remember { mutableStateOf(GuideStep.WELCOME) }
    
    // 系统信息状态
    var systemName by remember { mutableStateOf("") }
    var systemAvatarUrl by remember { mutableStateOf("") }
    
    // 成员信息状态
    var memberName by remember { mutableStateOf("") }
    var memberAvatarUrl by remember { mutableStateOf("") }
    
    // 文件选择器
    val backupFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onImportBackup(it) }
    }
    
    // 监听备份导入成功，直接跳转到完成步骤
    LaunchedEffect(backupImportSuccess) {
        if (backupImportSuccess) {
            currentStep = GuideStep.COMPLETE
        }
    }

    // 全屏沉浸式布局
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 主内容区域（占据大部分空间）
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        (slideInHorizontally { width -> width } + fadeIn(
                            animationSpec = tween(400)
                        )) togetherWith (slideOutHorizontally { width -> -width } + fadeOut(
                            animationSpec = tween(400)
                        ))
                    },
                    label = "guide_content"
                ) { step ->
                    when (step) {
                        GuideStep.WELCOME -> WelcomeStepContent()
                        GuideStep.IMPORT_OR_CREATE -> ImportOrCreateStepContent(
                            onSelectImport = { backupFileLauncher.launch("*/*") },
                            onSelectCreate = { currentStep = GuideStep.CREATE_SYSTEM }
                        )
                        GuideStep.CREATE_SYSTEM -> CreateSystemStepContent(
                            name = systemName,
                            avatarUrl = systemAvatarUrl,
                            onNameChange = { systemName = it },
                            onAvatarUrlChange = { systemAvatarUrl = it }
                        )
                        GuideStep.CREATE_MEMBER -> CreateMemberStepContent(
                            name = memberName,
                            avatarUrl = memberAvatarUrl,
                            onNameChange = { memberName = it },
                            onAvatarUrlChange = { memberAvatarUrl = it }
                        )
                        GuideStep.COMPLETE -> CompleteStepContent()
                    }
                }
            }
            
            // 底部操作区域
            GuideBottomBar(
                currentStep = currentStep,
                systemName = systemName,
                memberName = memberName,
                backupImportSuccess = backupImportSuccess,
                onNext = {
                    when (currentStep) {
                        GuideStep.WELCOME -> currentStep = GuideStep.IMPORT_OR_CREATE
                        GuideStep.CREATE_SYSTEM -> {
                            if (systemName.isNotBlank()) {
                                onCreateSystem(systemName, systemAvatarUrl)
                                currentStep = GuideStep.CREATE_MEMBER
                            }
                        }
                        GuideStep.CREATE_MEMBER -> {
                            if (memberName.isNotBlank()) {
                                onCreateMember(memberName, memberAvatarUrl)
                                currentStep = GuideStep.COMPLETE
                            }
                        }
                        GuideStep.COMPLETE -> onCompleteGuide()
                        else -> {}
                    }
                },
                onBack = {
                    currentStep = when (currentStep) {
                        GuideStep.CREATE_SYSTEM -> GuideStep.IMPORT_OR_CREATE
                        GuideStep.CREATE_MEMBER -> GuideStep.CREATE_SYSTEM
                        GuideStep.COMPLETE -> if (backupImportSuccess) GuideStep.IMPORT_OR_CREATE else GuideStep.CREATE_MEMBER
                        else -> currentStep
                    }
                }
            )
        }
    }
    
    // 备份进度对话框
    BackupProgressDialog(
        isVisible = isBackupInProgress,
        title = "正在导入备份",
        message = backupProgressMessage,
        progress = backupProgress
    )
    
    // 导入警告对话框
    ImportBackupWarningDialog(
        isVisible = showImportWarningDialog,
        onConfirm = onConfirmImport,
        onDismiss = onCancelImport
    )
}

/**
 * 底部操作栏：圆点指示器 + 操作按钮
 */
@Composable
private fun GuideBottomBar(
    currentStep: GuideStep,
    systemName: String,
    memberName: String,
    backupImportSuccess: Boolean,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val steps = GuideStep.entries
    val currentIndex = steps.indexOf(currentStep)
    val showBackButton = currentStep != GuideStep.WELCOME && currentStep != GuideStep.IMPORT_OR_CREATE
    val showNextButton = currentStep != GuideStep.IMPORT_OR_CREATE
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 圆点指示器
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            steps.forEachIndexed { index, _ ->
                val isActive = index == currentIndex
                val dotSize by animateDpAsState(
                    targetValue = if (isActive) 8.dp else 6.dp,
                    animationSpec = spring(dampingRatio = 0.6f),
                    label = "dot_size"
                )
                val dotAlpha by animateFloatAsState(
                    targetValue = if (index <= currentIndex) 1f else 0.3f,
                    animationSpec = tween(300),
                    label = "dot_alpha"
                )
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(dotSize)
                        .alpha(dotAlpha)
                        .clip(CircleShape)
                        .background(
                            if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                )
            }
        }
        
        // 按钮区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (showBackButton) Arrangement.SpaceBetween else Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 返回文字按钮
            if (showBackButton) {
                TextButton(
                    onClick = onBack,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "返回",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 主操作按钮
            if (showNextButton) {
                val buttonText = when (currentStep) {
                    GuideStep.WELCOME -> "开始"
                    GuideStep.COMPLETE -> "进入应用"
                    GuideStep.CREATE_SYSTEM -> "下一步"
                    GuideStep.CREATE_MEMBER -> "下一步"
                    else -> "下一步"
                }
                val isEnabled = when (currentStep) {
                    GuideStep.WELCOME -> true
                    GuideStep.CREATE_SYSTEM -> systemName.isNotBlank()
                    GuideStep.CREATE_MEMBER -> memberName.isNotBlank()
                    GuideStep.COMPLETE -> true
                    else -> false
                }
                
                Button(
                    onClick = onNext,
                    enabled = isEnabled,
                    modifier = Modifier
                        .then(
                            if (!showBackButton) Modifier.fillMaxWidth() else Modifier
                        )
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    ),
                    contentPadding = PaddingValues(horizontal = 32.dp)
                ) {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (currentStep != GuideStep.COMPLETE) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// 欢迎页 - 澎湃OS风格：大字标题 + 品牌感 + 极简
// ============================================================
@Composable
private fun WelcomeStepContent() {
    // 入场动画
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    
    val titleAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800, delayMillis = 200),
        label = "title_alpha"
    )
    val titleOffset by animateDpAsState(
        targetValue = if (visible) 0.dp else 40.dp,
        animationSpec = tween(800, delayMillis = 200, easing = EaseOutCubic),
        label = "title_offset"
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800, delayMillis = 500),
        label = "subtitle_alpha"
    )
    val subtitleOffset by animateDpAsState(
        targetValue = if (visible) 0.dp else 30.dp,
        animationSpec = tween(800, delayMillis = 500, easing = EaseOutCubic),
        label = "subtitle_offset"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.3f))
        
        // 大标题 - 左对齐，澎湃OS风格
        Text(
            text = "你好",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 48.sp,
                lineHeight = 56.sp
            ),
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .alpha(titleAlpha)
                .offset(y = titleOffset)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "欢迎使用 Selves",
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 36.sp,
                lineHeight = 44.sp
            ),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .alpha(titleAlpha)
                .offset(y = titleOffset)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 副标题描述
        Text(
            text = "让我们花一点时间\n完成一些简单的设置",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                lineHeight = 28.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .alpha(subtitleAlpha)
                .offset(y = subtitleOffset)
        )
        
        Spacer(modifier = Modifier.weight(0.5f))
        
        // 底部装饰线
        Box(
            modifier = Modifier
                .alpha(subtitleAlpha)
                .width(40.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    )
                )
        )
        
        Spacer(modifier = Modifier.weight(0.2f))
    }
}

// ============================================================
// 选择方式页 - 简洁卡片选择
// ============================================================
@Composable
private fun ImportOrCreateStepContent(
    onSelectImport: () -> Unit,
    onSelectCreate: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600, delayMillis = 100),
        label = "content_alpha"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .alpha(contentAlpha)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        
        // 标题区域 - 左对齐
        Text(
            text = "选择你的方式",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp
            ),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "导入已有数据，或从零开始",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // 创建新系统 - 主要选项
        GuideOptionCard(
            icon = Icons.Default.Add,
            title = "全新开始",
            subtitle = "创建你的系统和第一个成员",
            isPrimary = true,
            onClick = onSelectCreate
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 导入备份 - 次要选项
        GuideOptionCard(
            icon = Icons.Outlined.CloudDownload,
            title = "导入备份",
            subtitle = "从备份文件恢复所有数据",
            isPrimary = false,
            onClick = onSelectImport
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * 引导选项卡片 - 澎湃OS风格
 */
@Composable
private fun GuideOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isPrimary) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val iconTint = if (isPrimary) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        tonalElevation = if (isPrimary) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标容器
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isPrimary) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = iconTint
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // 文字区域
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 箭头
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

// ============================================================
// 创建系统页
// ============================================================
@Composable
private fun CreateSystemStepContent(
    name: String,
    avatarUrl: String,
    onNameChange: (String) -> Unit,
    onAvatarUrlChange: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600, delayMillis = 100),
        label = "content_alpha"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .alpha(contentAlpha)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        
        // 标题 - 左对齐
        Text(
            text = "创建你的系统",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp
            ),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "为你的系统起一个名字吧",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        CreateSystemForm(
            name = name,
            avatarUrl = avatarUrl,
            onNameChange = onNameChange,
            onAvatarUrlChange = onAvatarUrlChange
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 提示文字
        Text(
            text = "这些信息之后都可以修改",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

// ============================================================
// 添加成员页
// ============================================================
@Composable
private fun CreateMemberStepContent(
    name: String,
    avatarUrl: String,
    onNameChange: (String) -> Unit,
    onAvatarUrlChange: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600, delayMillis = 100),
        label = "content_alpha"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .alpha(contentAlpha)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        
        // 标题 - 左对齐
        Text(
            text = "添加第一个成员",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp
            ),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "你可以之后继续添加更多成员",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        CreateMemberForm(
            name = name,
            avatarUrl = avatarUrl,
            onNameChange = onNameChange,
            onAvatarUrlChange = onAvatarUrlChange
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "之后可以在设置中添加更多成员",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

// ============================================================
// 完成页 - 简洁庆祝
// ============================================================
@Composable
private fun CompleteStepContent() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    
    val iconScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon_scale"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800, delayMillis = 400),
        label = "text_alpha"
    )
    val textOffset by animateDpAsState(
        targetValue = if (visible) 0.dp else 20.dp,
        animationSpec = tween(800, delayMillis = 400, easing = EaseOutCubic),
        label = "text_offset"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.35f))
        
        // 成功图标 - 带弹性动画
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .scale(iconScale),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 完成标题
        Text(
            text = "一切就绪",
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 36.sp
            ),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .alpha(textAlpha)
                .offset(y = textOffset)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "所有信息均在本地存储",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                lineHeight = 28.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .alpha(textAlpha)
                .offset(y = textOffset)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // 功能提示卡片
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(textAlpha)
                .offset(y = textOffset),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "你随时可以",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                GuideFeatureItem(text = "添加更多成员")
                Spacer(modifier = Modifier.height(10.dp))
                GuideFeatureItem(text = "修改系统信息")
                Spacer(modifier = Modifier.height(10.dp))
                GuideFeatureItem(text = "创建聊天群组")
            }
        }
        
        Spacer(modifier = Modifier.weight(0.35f))
    }
}

@Composable
private fun GuideFeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}