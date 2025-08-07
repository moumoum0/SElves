package com.selves.xnn.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selves.xnn.model.Member
import com.selves.xnn.ui.components.AlphabetIndexBar
import com.selves.xnn.ui.components.AvatarImage
import com.selves.xnn.ui.components.CreateMemberDialog
import com.selves.xnn.ui.components.EditMemberDialog
import com.selves.xnn.ui.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberManagementScreen(
    members: List<Member>,
    currentMember: Member,
    onNavigateBack: () -> Unit,
    mainViewModel: MainViewModel // 移除默认的hiltViewModel()，强制使用传递的实例
) {
    var showDeleteConfirmation by remember { mutableStateOf<Member?>(null) }
    var showCreateMemberDialog by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<Member?>(null) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedLetter by remember { mutableStateOf<String?>(null) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // 根据搜索条件过滤成员
    val filteredMembers = remember(members, searchQuery) {
        if (searchQuery.isBlank()) {
            members
        } else {
            members.filter { member ->
                member.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    // 按首字母分组成员
    val groupedMembers = remember(filteredMembers) {
        filteredMembers
            .sortedBy { it.name }
            .groupBy { member ->
                val firstChar = member.name.first().uppercaseChar()
                when {
                    firstChar in 'A'..'Z' -> firstChar.toString()
                    firstChar.isDigit() -> "#"
                    else -> "#"
                }
            }
            .toSortedMap()
    }
    
    // 获取所有可用的字母
    val availableLetters = remember(groupedMembers) {
        groupedMembers.keys.filter { it != "#" }.sorted()
    }
    
    Scaffold(
        topBar = {
            MemberManagementTopBar(
                showSearchBar = showSearchBar,
                searchQuery = searchQuery,
                onBackClick = onNavigateBack,
                onSearchClick = { showSearchBar = !showSearchBar },
                onSearchChange = { searchQuery = it },
                onSearchClose = { 
                    showSearchBar = false
                    searchQuery = ""
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateMemberDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加成员")
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 主列表区域
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // 如果没有搜索，显示分组列表
                if (searchQuery.isBlank()) {
                    groupedMembers.forEach { (letter, membersInGroup) ->
                        // 分组标题
                        item(key = "header_$letter") {
                            GroupHeader(letter = letter)
                        }
                        
                        // 分组中的成员
                        items(
                            items = membersInGroup,
                            key = { member -> member.id }
                        ) { member ->
                            MemberItem(
                                member = member,
                                isCurrentMember = member.id == currentMember.id,
                                onDeleteMember = { showDeleteConfirmation = member },
                                onEditMember = { memberToEdit = member }
                            )
                        }
                    }
                } else {
                    // 搜索模式，显示过滤后的扁平列表
                    items(
                        items = filteredMembers,
                        key = { member -> member.id }
                    ) { member ->
                        MemberItem(
                            member = member,
                            isCurrentMember = member.id == currentMember.id,
                            onDeleteMember = { showDeleteConfirmation = member },
                            onEditMember = { memberToEdit = member }
                        )
                    }
                    
                    // 如果搜索无结果，显示提示
                    if (filteredMembers.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "没有找到匹配的成员",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // 字母表索引栏（仅在非搜索模式下显示）
            if (searchQuery.isBlank() && availableLetters.isNotEmpty()) {
                AlphabetIndexBar(
                    availableLetters = availableLetters,
                    selectedLetter = selectedLetter,
                    onLetterSelected = { letter ->
                        selectedLetter = letter
                        // 滚动到对应字母的位置
                        coroutineScope.launch {
                            val targetIndex = groupedMembers.keys.take(
                                groupedMembers.keys.indexOf(letter) + 1
                            ).sumOf { key ->
                                1 + (groupedMembers[key]?.size ?: 0) // 1个标题 + 成员数量
                            } - (groupedMembers[letter]?.size ?: 0) // 减去当前组的成员数量，定位到标题
                            
                            listState.animateScrollToItem(maxOf(0, targetIndex - 1))
                        }
                    },
                    modifier = Modifier
                        .padding(end = 8.dp, top = 16.dp, bottom = 16.dp)
                )
            }
        }
    }
    
    // 删除确认对话框
    if (showDeleteConfirmation != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = {
                Text("删除成员")
            },
            text = {
                Text("确定要删除成员「${showDeleteConfirmation!!.name}」吗？此操作不可撤销。")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation?.let { mainViewModel.deleteMember(it) }
                        showDeleteConfirmation = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = null }
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    // 创建成员对话框
    if (showCreateMemberDialog) {
        CreateMemberDialog(
            existingMemberNames = members.map { it.name },
            onDismiss = { showCreateMemberDialog = false },
            onConfirm = { name, avatarUrl ->
                mainViewModel.createMember(name, avatarUrl, shouldSetAsCurrent = false)
                showCreateMemberDialog = false
            }
        )
    }
    
    // 编辑成员对话框
    memberToEdit?.let { member ->
        EditMemberDialog(
            member = member,
            existingMemberNames = members.map { it.name },
            onDismiss = { memberToEdit = null },
            onConfirm = { name, avatarUrl ->
                mainViewModel.updateMember(member.id, name, avatarUrl)
                memberToEdit = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberManagementTopBar(
    showSearchBar: Boolean,
    searchQuery: String,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSearchChange: (String) -> Unit,
    onSearchClose: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 标题（搜索栏未展开时显示）
                AnimatedVisibility(
                    visible = !showSearchBar,
                    enter = slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                ) {
                    Text(
                        text = "成员管理",
                        fontWeight = FontWeight.Normal
                    )
                }
                
                // 搜索栏展开动画（从右侧向左侧展开）
                AnimatedVisibility(
                    visible = showSearchBar,
                    enter = slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("搜索成员...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        },
                        trailingIcon = {
                            IconButton(onClick = onSearchClose) {
                                Icon(Icons.Default.Close, contentDescription = "关闭搜索")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回"
                )
            }
        },
        actions = {
            // 只在搜索栏未展开时显示搜索按钮
            AnimatedVisibility(
                visible = !showSearchBar,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                }
            }
        }
    )
}

@Composable
fun MemberItem(
    member: Member,
    isCurrentMember: Boolean,
    onDeleteMember: () -> Unit,
    onEditMember: (Member) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* 可以添加点击事件 */ }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像
        AvatarImage(
            avatarUrl = member.avatarUrl,
            contentDescription = "成员头像",
            size = 40.dp
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (isCurrentMember) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "当前",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Text(
                text = "ID: ${member.id.take(8)}...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 操作菜单 - 所有成员都显示菜单，但当前成员只能编辑，不能删除
        Box {
            IconButton(
                onClick = { showMenu = true }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "更多操作",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                // 编辑选项 - 所有成员都可以编辑
                DropdownMenuItem(
                    text = { Text("编辑") },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    },
                    onClick = {
                        showMenu = false
                        onEditMember(member)
                    }
                )
                
                // 删除选项 - 只有非当前成员才能删除
                if (!isCurrentMember) {
                    DropdownMenuItem(
                        text = { Text("删除") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDeleteMember()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(letter: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = letter,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
} 