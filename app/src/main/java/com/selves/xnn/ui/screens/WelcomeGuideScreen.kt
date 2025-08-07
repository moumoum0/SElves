package com.selves.xnn.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // 进度指示器
        GuideProgressIndicator(
            currentStep = currentStep,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 主内容区域
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "guide_content"
            ) { step ->
                when (step) {
                    GuideStep.WELCOME -> {
                        WelcomeStepContent()
                    }
                    GuideStep.IMPORT_OR_CREATE -> {
                        ImportOrCreateStepContent(
                            onSelectImport = {
                                backupFileLauncher.launch("*/*")
                            },
                            onSelectCreate = {
                                currentStep = GuideStep.CREATE_SYSTEM
                            }
                        )
                    }
                    GuideStep.CREATE_SYSTEM -> {
                        CreateSystemStepContent(
                            name = systemName,
                            avatarUrl = systemAvatarUrl,
                            onNameChange = { systemName = it },
                            onAvatarUrlChange = { systemAvatarUrl = it }
                        )
                    }
                    GuideStep.CREATE_MEMBER -> {
                        CreateMemberStepContent(
                            name = memberName,
                            avatarUrl = memberAvatarUrl,
                            onNameChange = { memberName = it },
                            onAvatarUrlChange = { memberAvatarUrl = it }
                        )
                    }
                    GuideStep.COMPLETE -> {
                        CompleteStepContent()
                    }
                }
            }
        }
        
        // 底部导航按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 上一步按钮
            if (currentStep != GuideStep.WELCOME && currentStep != GuideStep.IMPORT_OR_CREATE) {
                OutlinedButton(
                    onClick = {
                        currentStep = when (currentStep) {
                            GuideStep.CREATE_SYSTEM -> GuideStep.IMPORT_OR_CREATE
                            GuideStep.CREATE_MEMBER -> GuideStep.CREATE_SYSTEM
                            GuideStep.COMPLETE -> if (backupImportSuccess) GuideStep.IMPORT_OR_CREATE else GuideStep.CREATE_MEMBER
                            else -> currentStep
                        }
                    },
                    modifier = Modifier.size(width = 120.dp, height = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("上一步")
                }
            } else {
                Spacer(modifier = Modifier.width(120.dp))
            }
            
            // 下一步/完成按钮
            if (currentStep != GuideStep.IMPORT_OR_CREATE) {
                Button(
                    onClick = {
                        when (currentStep) {
                            GuideStep.WELCOME -> {
                                currentStep = GuideStep.IMPORT_OR_CREATE
                            }
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
                            GuideStep.COMPLETE -> {
                                onCompleteGuide()
                            }
                            else -> {}
                        }
                    },
                    enabled = when (currentStep) {
                        GuideStep.WELCOME -> true
                        GuideStep.CREATE_SYSTEM -> systemName.isNotBlank()
                        GuideStep.CREATE_MEMBER -> memberName.isNotBlank()
                        GuideStep.COMPLETE -> true
                        else -> false
                    },
                    modifier = Modifier.size(width = 120.dp, height = 48.dp)
                ) {
                    Text(
                        text = when (currentStep) {
                            GuideStep.COMPLETE -> "完成"
                            else -> "下一步"
                        }
                    )
                    if (currentStep != GuideStep.COMPLETE) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(120.dp))
            }
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

@Composable
private fun GuideProgressIndicator(
    currentStep: GuideStep,
    modifier: Modifier = Modifier
) {
    val steps = listOf("欢迎", "选择方式", "创建系统", "添加成员", "完成")
    val currentIndex = when (currentStep) {
        GuideStep.WELCOME -> 0
        GuideStep.IMPORT_OR_CREATE -> 1
        GuideStep.CREATE_SYSTEM -> 2
        GuideStep.CREATE_MEMBER -> 3
        GuideStep.COMPLETE -> 4
    }
    
    Column(modifier = modifier) {
        // 步骤标题
        Text(
            text = steps[currentIndex],
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 进度条
        LinearProgressIndicator(
            progress = (currentIndex + 1) / steps.size.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary,
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 步骤指示器
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, stepName ->
                Text(
                    text = stepName,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (index <= currentIndex) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun WelcomeStepContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        
        Text(
            text = "欢迎使用 Selves",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "看起来这是你第一次使用本应用。\n让我们一起进行简单的设置，创建一些基本信息。",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "接下来我们将：",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                listOf(
                    "选择导入备份或创建新系统",
                    "设置你的聊天环境",
                    "开始使用应用"
                ).forEach { item ->
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportOrCreateStepContent(
    onSelectImport: () -> Unit,
    onSelectCreate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "选择设置方式",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "你可以导入之前的备份文件，或者创建全新的系统",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // 导入备份选项
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "导入备份",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "如果你之前使用过本应用并有备份文件，可以直接导入恢复所有数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onSelectImport,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("选择备份文件")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 创建新系统选项
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "创建新系统",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "如果这是你第一次使用，我们将引导你创建系统和添加成员",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = onSelectCreate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始创建")
                }
            }
        }
    }
}

@Composable
private fun CreateSystemStepContent(
    name: String,
    avatarUrl: String,
    onNameChange: (String) -> Unit,
    onAvatarUrlChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "创建系统",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "后续可以修改",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        CreateSystemForm(
            name = name,
            avatarUrl = avatarUrl,
            onNameChange = onNameChange,
            onAvatarUrlChange = onAvatarUrlChange
        )
    }
}

@Composable
private fun CreateMemberStepContent(
    name: String,
    avatarUrl: String,
    onNameChange: (String) -> Unit,
    onAvatarUrlChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "添加第一个成员",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "后续可以添加剩余成员",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        CreateMemberForm(
            name = name,
            avatarUrl = avatarUrl,
            onNameChange = onNameChange,
            onAvatarUrlChange = onAvatarUrlChange
        )
    }
}

@Composable
private fun CompleteStepContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        
        Text(
            text = "设置完成！",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "你的聊天系统已经准备就绪。\n现在可以开始使用应用了！",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "你可以随时在设置中：",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                listOf(
                    "• 添加更多成员",
                    "• 修改系统信息",
                    "• 创建聊天群组"
                ).forEach { item ->
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
} 