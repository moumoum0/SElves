package com.example.myapplication.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.example.myapplication.model.Member
import com.example.myapplication.util.ImageUtils
import com.example.myapplication.ui.components.AvatarImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MemberSwitchDialog(
    members: List<Member>,
    currentMemberId: String,
    onDismiss: () -> Unit,
    onMemberSelected: (Member) -> Unit,
    onCreateNewMember: () -> Unit,
    onDeleteMember: (Member) -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf<Member?>(null) }
    var menuExpandedForMember by remember { mutableStateOf<Member?>(null) }
    var deleteCountdown by remember { mutableStateOf(3) }
    var isCountdownRunning by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // 菜单位置相关变量
    var menuOffset by remember { mutableStateOf(DpOffset.Zero) }
    var tapPosition by remember { mutableStateOf(Offset.Zero) }
    var rowWidth by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(members) { member ->
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onGloballyPositioned { coordinates ->
                                        rowWidth = coordinates.size.width.toFloat()
                                    }
                                    .combinedClickable(
                                        onClick = { onMemberSelected(member) },
                                        onLongClick = { 
                                            if (member.id != currentMemberId) {
                                                menuExpandedForMember = member
                                                
                                                // 基于点击位置计算菜单偏移量
                                                // 如果点击位置在行的左半部分，菜单左上角与点击位置对齐
                                                // 如果点击位置在行的右半部分，菜单右上角与点击位置对齐
                                                val isLeftHalf = tapPosition.x < rowWidth / 2
                                                
                                                if (isLeftHalf) {
                                                    // 菜单左上角与点击位置对齐，并向上偏移
                                                    menuOffset = with(density) {
                                                        DpOffset(
                                                            x = tapPosition.x.toDp(), 
                                                            y = (-24).dp
                                                        )
                                                    }
                                                } else {
                                                    // 菜单右上角与点击位置对齐，并向上偏移
                                                    // 注意：这里我们估算菜单宽度为150dp，如果实际宽度不同可能需要调整
                                                    val estimatedMenuWidth = 150.dp
                                                    menuOffset = with(density) {
                                                        DpOffset(
                                                            x = (tapPosition.x.toDp() - estimatedMenuWidth), 
                                                            y = (-24).dp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    )
                                    .pointerInput(Unit) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                val event = awaitPointerEvent()
                                                // 记录点击位置
                                                tapPosition = event.changes.first().position
                                            }
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 成员头像
                                AvatarImage(
                                    avatarUrl = member.avatarUrl,
                                    contentDescription = "成员头像",
                                    size = 40.dp
                                )

                                // 成员名
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 16.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // 当前选中成员的标记
                                if (member.id == currentMemberId) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "当前成员",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            // 成员操作菜单
                            DropdownMenu(
                                expanded = menuExpandedForMember?.id == member.id,
                                onDismissRequest = { menuExpandedForMember = null },
                                offset = menuOffset
                            ) {
                                if (member.id != currentMemberId) {
                                    DropdownMenuItem(
                                        text = { Text("删除成员") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "删除成员"
                                            )
                                        },
                                        onClick = {
                                            menuExpandedForMember = null
                                            showDeleteConfirmation = member
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // 创建新成员按钮
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCreateNewMember() }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "创建新成员",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(8.dp)
                            )
                            Text(
                                text = "创建新成员",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteConfirmation != null) {
        LaunchedEffect(showDeleteConfirmation) {
            deleteCountdown = 3
            isCountdownRunning = true
            while (deleteCountdown > 0 && isCountdownRunning) {
                delay(1000)
                deleteCountdown--
            }
            isCountdownRunning = false
        }
        
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirmation = null
                isCountdownRunning = false
            },
            title = { 
                Text(text = "删除成员")
            },
            text = { 
                Text(text = "确定要删除成员 ${showDeleteConfirmation!!.name} 吗？此操作不可撤销。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (deleteCountdown <= 0) {
                            showDeleteConfirmation?.let { onDeleteMember(it) }
                            showDeleteConfirmation = null
                        }
                    },
                    enabled = deleteCountdown <= 0
                ) {
                    if (deleteCountdown > 0) {
                        Text(text = "删除 (${deleteCountdown})")
                    } else {
                        Text(text = "删除")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteConfirmation = null
                    isCountdownRunning = false
                }) {
                    Text(text = "取消")
                }
            }
        )
    }
} 