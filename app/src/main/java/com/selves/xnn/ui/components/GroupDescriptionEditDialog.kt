package com.selves.xnn.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.selves.xnn.R
import com.selves.xnn.model.MemberGroup

@Composable
fun GroupDescriptionEditDialog(
    group: MemberGroup,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var description by remember(group) { mutableStateOf(group.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.group_edit_description))
        },
        text = {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.group_description_label))
                },
                placeholder = {
                    Text(stringResource(R.string.group_description_hint))
                },
                supportingText = {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                },
                minLines = 3,
                maxLines = 5
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(description.trim()) }) {
                Text(stringResource(R.string.btn_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
