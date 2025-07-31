package com.selves.xnn.ui.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.selves.xnn.model.Member
import com.selves.xnn.util.ImageUtils
import com.selves.xnn.ui.components.AvatarImage

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditMemberDialog(
    member: Member,
    existingMemberNames: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var memberName by remember { mutableStateOf(member.name) }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var currentAvatarUrl by remember { mutableStateOf(member.avatarUrl) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    // 图片选择器启动器
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            avatarUri = it
            currentAvatarUrl = null // 清除当前头像URL，因为选择了新头像
        }
    }

    // 编辑成员的函数
    val editMember = {
        if (!isSubmitting) {
            when {
                memberName.isBlank() -> {
                    showError = true
                    errorMessage = "成员名不能为空"
                }
                memberName in existingMemberNames && memberName != member.name -> {
                    showError = true
                    errorMessage = "成员名已存在"
                }
                else -> {
                    isSubmitting = true
                    
                    // 如果有新头像，保存到内部存储
                    val savedAvatarPath = if (avatarUri != null) {
                        ImageUtils.saveAvatarToInternalStorage(context, avatarUri)
                    } else {
                        currentAvatarUrl // 保持原来的头像
                    }
                    
                    // 使用保存后的头像路径
                    onConfirm(memberName, savedAvatarPath)
                }
            }
        }
    }

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
                    text = "编辑成员",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 头像选择区域
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                        .clickable { launcher.launch("image/*") }
                ) {
                    if (avatarUri != null) {
                        // 显示新选择的头像
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = "成员头像",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                    } else if (currentAvatarUrl != null) {
                        // 显示当前头像
                        AvatarImage(
                            avatarUrl = currentAvatarUrl,
                            contentDescription = "成员头像",
                            size = 80.dp
                        )
                    } else {
                        // 显示默认头像图标
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "选择头像",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(16.dp)
                        )
                    }
                }

                // 成员名输入 - 使用单行输入框
                OutlinedTextField(
                    value = memberName,
                    onValueChange = { newValue -> 
                        // 过滤掉回车和换行符
                        memberName = newValue.replace("\n", "")
                        showError = false 
                    },
                    label = { Text("成员名") },
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
                            editMember()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.key == Key.Enter) {
                                keyboardController?.hide()
                                editMember()
                                true
                            } else {
                                false
                            }
                        }
                )

                // 按钮区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = editMember) {
                        Text("确定")
                    }
                }
            }
        }
    }
} 