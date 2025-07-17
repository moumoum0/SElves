package com.selves.xnn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, List<String>, LocalDateTime?, Boolean, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var options by remember { mutableStateOf(mutableListOf("", "")) }
    var endTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var allowMultipleChoice by remember { mutableStateOf(false) }
    var isAnonymous by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDateTime?>(null) }
    
    val context = LocalContext.current
    val systemUiController = rememberSystemUiController()
    
    // 设置系统栏颜色为白色
    LaunchedEffect(Unit) {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
        systemUiController.setNavigationBarColor(
            color = Color.White,
            darkIcons = true
        )
    }
    
    // 使用Box替代Dialog来实现全屏效果
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部工具栏
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                TopAppBar(
                    title = { Text("创建投票") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                if (title.isNotBlank() && description.isNotBlank() && 
                                    options.filter { it.isNotBlank() }.size >= 2) {
                                    onConfirm(
                                        title,
                                        description,
                                        options.filter { it.isNotBlank() },
                                        endTime,
                                        allowMultipleChoice,
                                        isAnonymous
                                    )
                                }
                            },
                            enabled = title.isNotBlank() && description.isNotBlank() && 
                                     options.filter { it.isNotBlank() }.size >= 2
                        ) {
                            Text(
                                text = "发布",
                                color = if (title.isNotBlank() && description.isNotBlank() && 
                                           options.filter { it.isNotBlank() }.size >= 2) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
            
            // 内容区域
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 投票标题
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("投票标题") },
                        placeholder = { Text("请输入投票标题") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }
                
                // 投票描述
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("投票描述") },
                        placeholder = { Text("请输入投票描述") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }
                
                // 投票选项
                item {
                    Text(
                        text = "投票选项",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                itemsIndexed(options) { index, option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = option,
                            onValueChange = { newValue ->
                                options = options.toMutableList().apply {
                                    this[index] = newValue
                                }
                            },
                            label = { Text("选项 ${index + 1}") },
                            placeholder = { Text("请输入选项内容") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                        
                        // 删除选项按钮（至少保留2个选项）
                        if (options.size > 2) {
                            IconButton(
                                onClick = {
                                    options = options.toMutableList().apply {
                                        removeAt(index)
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "删除选项",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(48.dp))
                        }
                    }
                }
                
                // 添加选项按钮
                item {
                    if (options.size < 10) {
                        OutlinedButton(
                            onClick = {
                                options = options.toMutableList().apply {
                                    add("")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "添加选项")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("添加选项")
                        }
                    }
                }
                
                // 投票设置
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "投票设置",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // 允许多选
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "允许多选",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = allowMultipleChoice,
                                    onCheckedChange = { allowMultipleChoice = it }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // 匿名投票
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "匿名投票",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Switch(
                                    checked = isAnonymous,
                                    onCheckedChange = { isAnonymous = it }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // 结束时间
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePicker = true },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "结束时间",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = endTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) 
                                               ?: "不限制",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Row {
                                    if (endTime != null) {
                                        IconButton(
                                            onClick = { endTime = null }
                                        ) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = "清除时间",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                    
                                    IconButton(
                                        onClick = { showDatePicker = true }
                                    ) {
                                        Icon(
                                            Icons.Default.DateRange,
                                            contentDescription = "选择时间",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 日期选择器（这里简化处理，实际项目中可以使用DatePicker）
    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("选择结束时间") },
            text = {
                Column {
                    Text("当前时间: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("您可以选择一个未来的时间作为投票结束时间")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 设置为24小时后结束（示例）
                        endTime = LocalDateTime.now().plusHours(24)
                        showDatePicker = false
                    }
                ) {
                    Text("24小时后")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
} 