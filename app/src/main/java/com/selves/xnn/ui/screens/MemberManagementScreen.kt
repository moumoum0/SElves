package com.selves.xnn.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.selves.xnn.R
import com.selves.xnn.model.Member
import com.selves.xnn.model.MemberGroup
import com.selves.xnn.ui.components.AlphabetIndexBar
import com.selves.xnn.ui.components.AvatarImage
import com.selves.xnn.ui.components.CreateMemberDialog
import com.selves.xnn.ui.components.EditMemberDialog
import com.selves.xnn.ui.components.GroupEditDialog
import com.selves.xnn.ui.viewmodels.MainViewModel
import com.selves.xnn.util.PinyinUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    var groupToEdit by remember { mutableStateOf<MemberGroup?>(null) }
    var groupToDelete by remember { mutableStateOf<MemberGroup?>(null) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedLetter by remember { mutableStateOf<String?>(null) }
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    var viewMode by remember { mutableStateOf(MemberViewMode.BY_LETTER) }
    var showViewModeMenu by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()
    val memberGroups by mainViewModel.memberGroups.collectAsState()
    val expandedGroups = remember { mutableStateMapOf<String, Boolean>() }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    val memberSorter = remember {
        Comparator<Member> { a, b ->
            val pinyinA = PinyinUtils.getPinyin(a.name)
            val pinyinB = PinyinUtils.getPinyin(b.name)
            pinyinA.compareTo(pinyinB)
        }
    }

    val allGroups = remember(members, memberGroups) {
        (members.flatMap { it.groups } + memberGroups.map { it.name })
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sortedBy { PinyinUtils.getPinyin(it) }
    }

    val memberGroupsByName = remember(memberGroups) {
        memberGroups.associateBy { it.name }
    }

    val filteredMembers = remember(members, searchQuery) {
        var result = members

        if (searchQuery.isNotBlank()) {
            result = result.filter { member ->
                PinyinUtils.matchesKeyword(member.name, searchQuery)
            }
        }

        result
    }
    
    val groupedMembers = remember(filteredMembers, memberSorter) {
        filteredMembers
            .sortedWith(memberSorter)
            .groupBy { member ->
                PinyinUtils.getFirstLetter(member.name)
            }
            .toSortedMap { a, b ->
                when {
                    a == "#" && b != "#" -> 1
                    a != "#" && b == "#" -> -1
                    else -> a.compareTo(b)
                }
            }
    }
    
    val availableLetters = remember(groupedMembers) {
        groupedMembers.keys.toList()
    }

    val groupedSections = remember(allGroups, filteredMembers, memberGroupsByName, searchQuery, memberSorter) {
        allGroups.mapNotNull { groupName ->
            val membersInGroup = filteredMembers
                .filter { groupName in it.groups }
                .sortedWith(memberSorter)
            val group = memberGroupsByName[groupName] ?: MemberGroup(name = groupName)
            if (searchQuery.isBlank() || membersInGroup.isNotEmpty() || PinyinUtils.matchesKeyword(groupName, searchQuery) || group.description.contains(searchQuery, ignoreCase = true)) {
                MemberGroupSection(
                    group = group,
                    members = membersInGroup,
                    isUngrouped = false
                )
            } else {
                null
            }
        }
    }

    val ungroupedSection = remember(filteredMembers, searchQuery, memberSorter) {
        val ungroupedMembers = filteredMembers
            .filter { it.groups.isEmpty() }
            .sortedWith(memberSorter)
        if (ungroupedMembers.isNotEmpty() || (searchQuery.isBlank() && members.any { it.groups.isEmpty() })) {
            MemberGroupSection(
                group = MemberGroup(name = "__NO_GROUP__"),
                members = ungroupedMembers,
                isUngrouped = true
            )
        } else {
            null
        }
    }

    val membersByGroup = remember(filteredMembers, memberSorter) {
        val map = mutableMapOf<String, MutableList<Member>>()
        filteredMembers.forEach { member ->
            member.groups.forEach { groupName ->
                map.getOrPut(groupName) { mutableListOf() }.add(member)
            }
        }
        map.mapValues { (_, list) -> list.sortedWith(memberSorter) }
    }

    val rootTreeNodes = remember(memberGroups, membersByGroup) {
        fun buildNodes(parentName: String?): List<GroupTreeNode> {
            return memberGroups
                .filter { it.parentName == parentName }
                .sortedBy { PinyinUtils.getPinyin(it.name) }
                .map { group ->
                    GroupTreeNode(
                        group = group,
                        members = membersByGroup[group.name] ?: emptyList(),
                        children = buildNodes(group.name)
                    )
                }
        }
        buildNodes(null)
    }
    
    Scaffold(
        topBar = {
            Column {
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
                // 视图模式选择器（位于搜索栏下方）
                Surface(
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                AssistChip(
                                    onClick = { showViewModeMenu = !showViewModeMenu },
                                    label = {
                                        Text(
                                            text = when (viewMode) {
                                                MemberViewMode.BY_LETTER -> stringResource(R.string.member_view_by_letter)
                                                MemberViewMode.BY_GROUP -> stringResource(R.string.member_view_by_group)
                                            }
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = stringResource(R.string.member_view_mode),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                                DropdownMenu(
                                    expanded = showViewModeMenu,
                                    onDismissRequest = { showViewModeMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.member_view_by_letter)) },
                                        onClick = {
                                            viewMode = MemberViewMode.BY_LETTER
                                            showViewModeMenu = false
                                        },
                                        leadingIcon = {
                                            if (viewMode == MemberViewMode.BY_LETTER) {
                                                Icon(Icons.Default.Check, contentDescription = null)
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.member_view_by_group)) },
                                        onClick = {
                                            viewMode = MemberViewMode.BY_GROUP
                                            showViewModeMenu = false
                                        },
                                        enabled = allGroups.isNotEmpty() || members.any { it.groups.isEmpty() },
                                        leadingIcon = {
                                            if (viewMode == MemberViewMode.BY_GROUP) {
                                                Icon(Icons.Default.Check, contentDescription = null)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateMemberDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_member))
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
                // 按字母分组模式且没有搜索
                if (viewMode == MemberViewMode.BY_LETTER && searchQuery.isBlank()) {
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
                                onEditMember = { memberToEdit = member },
                                onMemberClick = { selectedMember = member }
                            )
                        }
                    }
                } else if (viewMode == MemberViewMode.BY_GROUP && searchQuery.isBlank()) {
                    if (rootTreeNodes.isNotEmpty() || ungroupedSection != null) {
                        items(
                            items = rootTreeNodes,
                            key = { node -> node.key }
                        ) { node ->
                            GroupTreeNodeSection(
                                node = node,
                                depth = 0,
                                expandedGroups = expandedGroups,
                                currentMemberId = currentMember.id,
                                onEditGroup = { groupToEdit = it },
                                onDeleteGroup = { groupToDelete = it },
                                onDeleteMember = { showDeleteConfirmation = it },
                                onEditMember = { memberToEdit = it },
                                onMemberClick = { selectedMember = it }
                            )
                        }
                        ungroupedSection?.let { section ->
                            item(key = "ungrouped") {
                                GroupFolderSection(
                                    section = section,
                                    isExpanded = expandedGroups[section.key] ?: false,
                                    onToggleExpanded = {
                                        expandedGroups[section.key] = !(expandedGroups[section.key] ?: false)
                                    },
                                    onEditDescription = {},
                                    onDeleteGroup = {},
                                    currentMemberId = currentMember.id,
                                    onDeleteMember = { showDeleteConfirmation = it },
                                    onEditMember = { memberToEdit = it },
                                    onMemberClick = { selectedMember = it }
                                )
                            }
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.member_no_match),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    val flatSections = buildList {
                        addAll(groupedSections)
                        ungroupedSection?.let { add(it) }
                    }
                    if (flatSections.isNotEmpty()) {
                        items(
                            items = flatSections,
                            key = { section -> section.key }
                        ) { section ->
                            GroupFolderSection(
                                section = section,
                                isExpanded = expandedGroups[section.key] ?: false,
                                onToggleExpanded = {
                                    expandedGroups[section.key] = !(expandedGroups[section.key] ?: false)
                                },
                                onEditDescription = {
                                    if (!section.isUngrouped) groupToEdit = section.group
                                },
                                onDeleteGroup = {
                                    if (!section.isUngrouped) groupToDelete = section.group
                                },
                                currentMemberId = currentMember.id,
                                onDeleteMember = { showDeleteConfirmation = it },
                                onEditMember = { memberToEdit = it },
                                onMemberClick = { selectedMember = it }
                            )
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.member_no_match),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // 字母表索引栏（仅在按字母模式且非搜索模式下显示）
            if (viewMode == MemberViewMode.BY_LETTER && searchQuery.isBlank() && availableLetters.isNotEmpty()) {
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
    
    // 成员资料底部面板
    selectedMember?.let { member ->
        ModalBottomSheet(
            onDismissRequest = { selectedMember = null },
            sheetState = bottomSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AvatarImage(
                    avatarUrl = member.avatarUrl,
                    contentDescription = null,
                    size = 80.dp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (member.bio.isBlank()) "暂无简介" else member.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (member.bio.isBlank())
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                if (member.pronouns.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = member.pronouns,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                
                // 分组标签
                if (member.groups.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        member.groups.forEach { group ->
                            AssistChip(
                                onClick = { },
                                label = { Text(group) }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = {
                        memberToEdit = member
                        selectedMember = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("编辑资料")
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteConfirmation != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = {
                Text(stringResource(R.string.dialog_delete_member))
            },
            text = {
                Text(stringResource(R.string.error_delete_member_confirm, showDeleteConfirmation!!.name))
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
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = null }
                ) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
    
    // 创建成员对话框
    if (showCreateMemberDialog) {
        CreateMemberDialog(
            existingMemberNames = members.map { it.name },
            existingGroups = allGroups,
            onDismiss = { showCreateMemberDialog = false },
            onConfirm = { name, avatarUrl, bio, pronouns, groups ->
                mainViewModel.createMember(name, avatarUrl, bio, pronouns, groups, shouldSetAsCurrent = false)
                showCreateMemberDialog = false
            },
            onGroupDescriptionsCreated = { descriptions ->
                descriptions.forEach { (groupName, desc) ->
                    mainViewModel.updateMemberGroupDescription(groupName, desc)
                }
            }
        )
    }
    
    // 编辑成员对话框
    memberToEdit?.let { member ->
        EditMemberDialog(
            member = member,
            existingMemberNames = members.map { it.name },
            existingGroups = allGroups,
            onDismiss = { memberToEdit = null },
            onConfirm = { name, avatarUrl, bio, pronouns, groups ->
                mainViewModel.updateMember(member.id, name, avatarUrl, bio, pronouns, groups)
                memberToEdit = null
            }
        )
    }

    groupToEdit?.let { group ->
        GroupEditDialog(
            group = group,
            existingGroupNames = allGroups.filter { it != group.name },
            allMemberGroups = memberGroups,
            onDismiss = { groupToEdit = null },
            onConfirm = { newName, description, parentName ->
                mainViewModel.updateMemberGroup(group.name, newName, description, parentName)
                groupToEdit = null
            }
        )
    }

    groupToDelete?.let { group ->
        AlertDialog(
            onDismissRequest = { groupToDelete = null },
            title = { Text(stringResource(R.string.group_delete)) },
            text = { Text(stringResource(R.string.group_delete_confirm, group.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        mainViewModel.deleteMemberGroup(group.name)
                        groupToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { groupToDelete = null }) {
                    Text(stringResource(R.string.btn_cancel))
                }
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
                        text = stringResource(R.string.member_management_title),
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
                        placeholder = { Text(stringResource(R.string.placeholder_search_members)) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.cd_search_action))
                        },
                        trailingIcon = {
                            IconButton(onClick = onSearchClose) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close_search))
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back)
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
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.cd_search))
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemberItem(
    member: Member,
    isCurrentMember: Boolean,
    onDeleteMember: () -> Unit,
    onEditMember: (Member) -> Unit,
    onMemberClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMemberClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像
        AvatarImage(
            avatarUrl = member.avatarUrl,
            contentDescription = stringResource(R.string.cd_member_avatar),
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
                            text = stringResource(R.string.member_current),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Text(
                text = if (member.bio.isBlank()) "暂无简介" else member.bio,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // 操作菜单 - 所有成员都显示菜单，但当前成员只能编辑，不能删除
        Box {
            IconButton(
                onClick = { showMenu = true }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.cd_more_actions),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                // 编辑选项 - 所有成员都可以编辑
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_edit)) },
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
                        text = { Text(stringResource(R.string.menu_delete)) },
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
            color = Color.Black,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

private data class MemberGroupSection(
    val group: MemberGroup,
    val members: List<Member>,
    val isUngrouped: Boolean
) {
    val key: String = if (isUngrouped) "ungrouped" else group.name
}

private data class GroupTreeNode(
    val group: MemberGroup,
    val members: List<Member>,
    val children: List<GroupTreeNode>
) {
    val key: String = group.name
}

@Composable
private fun GroupFolderSection(
    section: MemberGroupSection,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onEditDescription: () -> Unit,
    onDeleteGroup: () -> Unit,
    currentMemberId: String,
    onDeleteMember: (Member) -> Unit,
    onEditMember: (Member) -> Unit,
    onMemberClick: (Member) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        GroupFolderHeader(
            section = section,
            isExpanded = isExpanded,
            onClick = onToggleExpanded,
            onEditDescription = onEditDescription,
            onDeleteGroup = onDeleteGroup
        )

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 4.dp)
            ) {
                if (section.members.isEmpty()) {
                    Text(
                        text = stringResource(R.string.member_group_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                    )
                } else {
                    section.members.forEach { member ->
                        MemberItem(
                            member = member,
                            isCurrentMember = member.id == currentMemberId,
                            onDeleteMember = { onDeleteMember(member) },
                            onEditMember = onEditMember,
                            onMemberClick = { onMemberClick(member) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupFolderHeader(
    section: MemberGroupSection,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onEditDescription: () -> Unit,
    onDeleteGroup: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    val description = when {
        section.isUngrouped -> stringResource(R.string.member_no_group_description)
        section.group.description.isBlank() -> ""
        else -> section.group.description
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = { if (!section.isUngrouped) showMenu = true }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (section.isUngrouped) stringResource(R.string.member_no_group) else section.group.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (description.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black.copy(alpha = 0.55f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (!section.isUngrouped) {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.group_edit_title)) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onEditDescription()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(R.string.group_delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        showMenu = false
                        onDeleteGroup()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupTreeNodeSection(
    node: GroupTreeNode,
    depth: Int,
    expandedGroups: MutableMap<String, Boolean>,
    currentMemberId: String,
    onEditGroup: (MemberGroup) -> Unit,
    onDeleteGroup: (MemberGroup) -> Unit,
    onDeleteMember: (Member) -> Unit,
    onEditMember: (Member) -> Unit,
    onMemberClick: (Member) -> Unit
) {
    val isExpanded = expandedGroups[node.key] ?: false
    val startPad = (depth * 20).dp
    var showMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { expandedGroups[node.key] = !isExpanded },
                            onLongClick = { showMenu = true }
                        )
                        .padding(start = startPad)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = node.group.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (node.group.description.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = node.group.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black.copy(alpha = 0.55f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.group_edit_title)) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onEditGroup(node.group)
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(R.string.group_delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        showMenu = false
                        onDeleteGroup(node.group)
                    }
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.fillMaxWidth()) {
                node.children.forEach { child ->
                    GroupTreeNodeSection(
                        node = child,
                        depth = depth + 1,
                        expandedGroups = expandedGroups,
                        currentMemberId = currentMemberId,
                        onEditGroup = onEditGroup,
                        onDeleteGroup = onDeleteGroup,
                        onDeleteMember = onDeleteMember,
                        onEditMember = onEditMember,
                        onMemberClick = onMemberClick
                    )
                }
                if (node.children.isEmpty() && node.members.isEmpty()) {
                    Text(
                        text = stringResource(R.string.member_group_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = startPad + 28.dp, top = 8.dp, bottom = 8.dp)
                    )
                } else {
                    node.members.forEach { member ->
                        Box(modifier = Modifier.padding(start = startPad + 20.dp)) {
                            MemberItem(
                                member = member,
                                isCurrentMember = member.id == currentMemberId,
                                onDeleteMember = { onDeleteMember(member) },
                                onEditMember = onEditMember,
                                onMemberClick = { onMemberClick(member) }
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class MemberViewMode {
    BY_LETTER,  // 按字母分组
    BY_GROUP    // 按分组筛选
}