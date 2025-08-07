package com.selves.xnn.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.canhub.cropper.CropImageContract
import com.selves.xnn.model.Member
import com.selves.xnn.util.ImageUtils

@Composable
fun CreateGroupDialog(
    availableMembers: List<Member>,
    currentMember: Member,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, List<Member>) -> Unit  // 新增avatarUrl参数
) {
    var groupName by remember { mutableStateOf("") }
    var groupAvatarUrl by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }
    var showMemberSelection by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // 头像裁剪启动器
    val avatarCropLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                val savedPath = ImageUtils.saveAvatarToInternalStorage(context, uri)
                groupAvatarUrl = savedPath
            }
        }
    }

    if (showMemberSelection) {
        MemberSelectionDialog(
            availableMembers = availableMembers,
            currentMember = currentMember,
            onDismiss = { showMemberSelection = false },
            onConfirm = { selectedMembers ->
                showMemberSelection = false
                if (groupName.isNotBlank()) {
                    onConfirm(groupName, groupAvatarUrl, selectedMembers)
                }
            }
        )
    } else {
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
                        text = "创建新群聊",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 群聊头像选择区域
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    if (groupAvatarUrl != null) {
                                        Color.Transparent
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    }
                                )
                                .clickable {
                                    val cropOptions = ImageUtils.createAvatarCropOptions()
                                    avatarCropLauncher.launch(cropOptions)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (groupAvatarUrl != null) {
                                AvatarImage(
                                    avatarUrl = groupAvatarUrl,
                                    contentDescription = "群聊头像",
                                    size = 80.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "选择头像",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "点击选择群聊头像（可选）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { 
                            groupName = it
                            showError = false
                        },
                        label = { Text("群聊名称") },
                        isError = showError,
                        supportingText = if (showError) {
                            { Text("群聊名称不能为空") }
                        } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("取消")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (groupName.isBlank()) {
                                    showError = true
                                } else {
                                    showMemberSelection = true
                                }
                            }
                        ) {
                            Text("下一步")
                        }
                    }
                }
            }
        }
    }
} 