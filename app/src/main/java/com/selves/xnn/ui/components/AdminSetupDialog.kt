package com.selves.xnn.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.selves.xnn.R
import com.selves.xnn.model.Member

/**
 * 升级后尚无管理员时的强制引导：至少勾选一名成员设为管理员，不可取消。
 */
@Composable
fun AdminSetupDialog(
    members: List<Member>,
    onConfirm: (Set<String>) -> Unit
) {
    var selectedIds by remember(members) {
        // 默认勾选当前列表第一位，降低「点确认却未选」的概率
        mutableStateOf(
            members.firstOrNull()?.id?.let { setOf(it) } ?: emptySet()
        )
    }

    AlertDialog(
        onDismissRequest = { /* 强制完成，不可关闭 */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        title = { Text(stringResource(R.string.admin_setup_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.admin_setup_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(members, key = { it.id }) { member ->
                        val checked = member.id in selectedIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedIds = if (checked) {
                                        selectedIds - member.id
                                    } else {
                                        selectedIds + member.id
                                    }
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarImage(
                                avatarUrl = member.avatarUrl,
                                contentDescription = stringResource(R.string.cd_member_avatar),
                                size = 40.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    selectedIds = if (isChecked) {
                                        selectedIds + member.id
                                    } else {
                                        selectedIds - member.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedIds) },
                enabled = selectedIds.isNotEmpty()
            ) {
                Text(stringResource(R.string.admin_setup_confirm))
            }
        }
    )
}