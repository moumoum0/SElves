package com.selves.xnn.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpSize
import com.selves.xnn.model.Member

/**
 * 快捷成员切换组件
 * 简化版的成员信息显示，只有头像和切换图标
 */
@Composable
fun QuickMemberSwitch(
    currentMember: Member?,
    members: List<Member>,
    onMemberSelected: (Member) -> Unit,
    modifier: Modifier = Modifier,
    size: DpSize = DpSize(48.dp, 48.dp)
) {
    var showMemberSwitchDialog by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .size(size)
            .clickable { showMemberSwitchDialog = true },
        contentAlignment = Alignment.Center
    ) {
        // 成员头像
        AvatarImage(
            avatarUrl = currentMember?.avatarUrl,
            contentDescription = "成员头像",
            size = size.width
        )
        
        // 交换图标在右下角，不要圆形背景
        Icon(
            imageVector = Icons.Default.SwapHoriz,
            contentDescription = "切换成员",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(size.width * 0.3f, size.height * 0.3f)
                .align(Alignment.BottomEnd)
        )
    }
    
    // 成员切换对话框
    if (showMemberSwitchDialog) {
        MemberSwitchDialog(
            members = members,
            currentMemberId = currentMember?.id ?: "",
            onMemberSelected = { member ->
                onMemberSelected(member)
                showMemberSwitchDialog = false
            },
            onCreateNewMember = {
                // 这里可以添加创建新成员的逻辑
                showMemberSwitchDialog = false
            },
            onDeleteMember = { member ->
                // 这里可以添加删除成员的逻辑
                showMemberSwitchDialog = false
            },
            onDismiss = { showMemberSwitchDialog = false }
        )
    }
}

 