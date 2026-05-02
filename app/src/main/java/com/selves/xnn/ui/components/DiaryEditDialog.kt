package com.selves.xnn.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.selves.xnn.R
import com.selves.xnn.model.MemberDiary

@Composable
fun DiaryEditDialog(
    diary: MemberDiary? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String) -> Unit
) {
    val isEditing = diary != null
    var title by remember { mutableStateOf(diary?.title ?: "") }
    var content by remember { mutableStateOf(diary?.content ?: "") }
    var showContentError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (isEditing) stringResource(R.string.diary_edit)
                    else stringResource(R.string.diary_create),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.replace("\n", "") },
                    label = { Text(stringResource(R.string.diary_title_label)) },
                    placeholder = { Text(stringResource(R.string.diary_title_hint)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        showContentError = false
                    },
                    label = { Text(stringResource(R.string.diary_content_label)) },
                    placeholder = { Text(stringResource(R.string.diary_content_hint)) },
                    isError = showContentError,
                    supportingText = if (showContentError) {
                        { Text(stringResource(R.string.diary_content_empty)) }
                    } else null,
                    minLines = 5,
                    maxLines = 10,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (content.isBlank()) {
                                showContentError = true
                            } else {
                                onConfirm(title, content)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.btn_confirm))
                    }
                }
            }
        }
    }
}
