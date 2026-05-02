package com.selves.xnn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selves.xnn.R
import com.selves.xnn.model.Member
import com.selves.xnn.model.System
import com.selves.xnn.ui.components.SystemAvatarImage
import com.selves.xnn.viewmodel.SystemViewModel
import com.selves.xnn.ui.components.SystemEditDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemScreen(
    currentMember: Member,
    allMembers: List<Member>,
    onNavigateToMemberManagement: () -> Unit,
    onNavigateToOnlineStats: () -> Unit,

    onNavigateToSettings: () -> Unit,
    systemViewModel: SystemViewModel = hiltViewModel()
) {
    val currentSystem by systemViewModel.currentSystem.collectAsState()
    val isLoading by systemViewModel.isLoading.collectAsState()
    var showSystemEditDialog by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 系统信息卡片
        item {
            if (isLoading) {
                SystemCardSkeleton()
            } else {
                currentSystem?.let { system ->
                    SystemInfoCard(
                        system = system
                    )
                }
            }
        }
        
        // 系统管理功能列表
        item {
            Text(
                text = stringResource(R.string.system_management),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
        
        item {
            SystemManagementItem(
                icon = Icons.Default.Edit,
                title = stringResource(R.string.system_edit),
                subtitle = stringResource(R.string.system_edit_subtitle),
                onClick = { showSystemEditDialog = true }
            )
        }
        
        item {
            SystemManagementItem(
                icon = Icons.Default.Group,
                title = stringResource(R.string.system_member_management),
                subtitle = stringResource(R.string.system_member_management_subtitle),
                onClick = onNavigateToMemberManagement
            )
        }
        
        item {
            SystemManagementItem(
                icon = Icons.Default.Schedule,
                title = stringResource(R.string.system_online_stats),
                subtitle = stringResource(R.string.system_online_stats_subtitle),
                onClick = onNavigateToOnlineStats
            )
        }
        
        // 其它功能列表
        item {
            Text(
                text = stringResource(R.string.system_other),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
        
        item {
            SystemManagementItem(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.system_settings),
                subtitle = stringResource(R.string.system_settings_subtitle),
                onClick = onNavigateToSettings
            )
        }
    }
    
    // 系统编辑对话框
    if (showSystemEditDialog) {
        SystemEditDialog(
            onDismiss = { showSystemEditDialog = false },
            onConfirm = { showSystemEditDialog = false }
        )
    }
}

@Composable
fun SystemInfoCard(
    system: System
) {
    var isDescriptionVisible by remember { mutableStateOf(false) }
    val titleScale by animateFloatAsState(
        targetValue = if (isDescriptionVisible) 0.9f else 1f,
        label = "system_title_scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(20.dp),
            verticalAlignment = if (isDescriptionVisible) Alignment.Top else Alignment.CenterVertically
        ) {
            SystemAvatarImage(
                avatarUrl = system.avatarUrl,
                contentDescription = stringResource(R.string.cd_system_avatar),
                size = 60.dp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = system.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .scale(titleScale)
                        .clickable { isDescriptionVisible = !isDescriptionVisible }
                )

                AnimatedVisibility(
                    visible = isDescriptionVisible,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Text(
                        text = system.description.ifBlank { stringResource(R.string.system_description_empty) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}

@Composable
fun SystemManagementItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 徽章显示
        if (badge != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SystemCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像骨架
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 名称骨架
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
} 