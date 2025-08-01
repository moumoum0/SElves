package com.selves.xnn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.selves.xnn.model.Member

/**
 * 可重复使用的用户信息头部组件
 */
@Composable
fun UserInfoHeader(
    currentMember: Member,
    onMemberSwitch: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    showSwitchButton: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onMemberSwitch() }
        ) {
            AvatarImage(
                avatarUrl = currentMember.avatarUrl,
                contentDescription = "成员头像",
                size = 40.dp
            )
            Text(
                text = currentMember.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        
        if (showSwitchButton) {
            IconButton(onClick = onMemberSwitch) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "切换成员",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}