package com.selves.xnn.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.selves.xnn.R
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.selves.xnn.model.System
import com.selves.xnn.util.ImageUtils
import com.selves.xnn.viewmodel.SystemViewModel
import com.canhub.cropper.CropImageContract

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SystemEditDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    systemViewModel: SystemViewModel = hiltViewModel()
) {
    val currentSystem by systemViewModel.currentSystem.collectAsState()
    val isLoading by systemViewModel.isLoading.collectAsState()
    val error by systemViewModel.error.collectAsState()
    
    var systemName by remember { mutableStateOf("") }
    var systemDescription by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var currentAvatarUrl by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    // 初始化数据
    LaunchedEffect(currentSystem) {
        currentSystem?.let { system ->
            systemName = system.name
            systemDescription = system.description
            currentAvatarUrl = system.avatarUrl
        }
    }

    // 图片裁剪启动器
    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let {
                avatarUri = it
                currentAvatarUrl = null // 清除当前头像URL，因为选择了新头像
            }
        }
    }

    // 编辑系统的函数
    val editSystem = {
        if (!isSubmitting) {
            when {
                systemName.isBlank() -> {
                    showError = true
                    errorMessage = context.getString(R.string.error_empty_field)
                }
                else -> {
                    isSubmitting = true
                    
                    currentSystem?.let { system ->
                        // 如果有新头像，保存到内部存储
                        val savedAvatarPath = if (avatarUri != null) {
                            ImageUtils.saveAvatarToInternalStorage(context, avatarUri)
                        } else {
                            currentAvatarUrl // 保持原来的头像
                        }
                        
                        // 更新系统
                        val updatedSystem = system.copy(
                            name = systemName.trim(),
                            avatarUrl = savedAvatarPath,
                            description = systemDescription.trim()
                        )
                        systemViewModel.updateSystem(updatedSystem)
                        onConfirm()
                    }
                }
            }
        }
    }

    if (currentSystem != null) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.system_edit),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 头像选择区域
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp)
                            .clickable { 
                                // 启动图片裁剪器，可以选择从图库或相机
                                cropImageLauncher.launch(ImageUtils.createAvatarCropOptions(context))
                            }
                    ) {
                        if (avatarUri != null) {
                            // 显示新选择的头像
                            AsyncImage(
                                model = avatarUri,
                                contentDescription = stringResource(R.string.cd_system_avatar),
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else if (currentAvatarUrl != null) {
                            // 显示当前头像
                            AsyncImage(
                                model = currentAvatarUrl,
                                contentDescription = stringResource(R.string.cd_system_avatar),
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // 显示默认头像图标
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = stringResource(R.string.cd_select_avatar_action),
                                    modifier = Modifier.padding(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 系统名称输入 - 使用单行输入框
                    OutlinedTextField(
                        value = systemName,
                        onValueChange = { newValue -> 
                            // 过滤掉回车和换行符
                            systemName = newValue.replace("\n", "")
                            showError = false 
                        },
                        label = { Text(stringResource(R.string.system_name)) },
                        isError = showError,
                        supportingText = if (showError) {
                            { Text(errorMessage) }
                        } else null,
                        singleLine = true, // 确保是单行输入
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Text
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                editSystem()
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.key == Key.Enter) {
                                    keyboardController?.hide()
                                    editSystem()
                                    true
                                } else {
                                    false
                                }
                            }
                    )

                    OutlinedTextField(
                        value = systemDescription,
                        onValueChange = { systemDescription = it },
                        label = { Text(stringResource(R.string.label_system_description)) },
                        placeholder = { Text(stringResource(R.string.placeholder_system_description)) },
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    // 按钮区域
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.btn_cancel))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = editSystem,
                            enabled = !isSubmitting
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(stringResource(R.string.btn_confirm))
                            }
                        }
                    }
                }
            }
        }
    }
} 