package com.selves.xnn.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.selves.xnn.util.ImageUtils
import com.canhub.cropper.CropImageContract

@Composable
fun CreateMemberForm(
    name: String,
    avatarUrl: String,
    onNameChange: (String) -> Unit,
    onAvatarUrlChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    
    // 图片裁剪启动器
    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            selectedImageUri = result.uriContent
            // 保存头像到内部存储
            val savedAvatarPath = ImageUtils.saveAvatarToInternalStorage(context, result.uriContent)
            savedAvatarPath?.let { onAvatarUrlChange(it) }
        }
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 头像选择区域
        Box(
            modifier = Modifier
                .size(120.dp)
                .clickable { 
                    // 启动图片裁剪器，可以选择从图库或相机
                    cropImageLauncher.launch(ImageUtils.createAvatarCropOptions())
                }
        ) {
            // 主头像区域
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null || avatarUrl.isNotEmpty()) {
                    AsyncImage(
                        model = selectedImageUri ?: avatarUrl,
                        contentDescription = "成员头像",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "选择头像",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击选择头像",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // 相机图标叠加
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                 Icon(
                     Icons.Default.PhotoCamera,
                     contentDescription = "选择照片",
                     tint = MaterialTheme.colorScheme.surface,
                     modifier = Modifier.size(18.dp)
                 )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 成员名输入
        OutlinedTextField(
            value = name,
            onValueChange = { newValue -> 
                // 过滤掉回车和换行符
                onNameChange(newValue.replace("\n", ""))
            },
            label = { Text("成员名") },
            placeholder = { Text("请输入成员名") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
} 