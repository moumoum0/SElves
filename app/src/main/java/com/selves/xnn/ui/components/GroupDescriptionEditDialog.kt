package com.selves.xnn.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.selves.xnn.R
import com.selves.xnn.model.MemberGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupEditDialog(
    group: MemberGroup,
    existingGroupNames: List<String>,
    allMemberGroups: List<MemberGroup> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, parentName: String?) -> Unit
) {
    var name by remember(group) { mutableStateOf(group.name) }
    var description by remember(group) { mutableStateOf(group.description) }
    var selectedParentName by remember(group) { mutableStateOf(group.parentName) }
    var nameError by remember { mutableStateOf("") }
    var parentMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val ineligibleNames = remember(group.name, allMemberGroups) {
        buildSet {
            add(group.name)
            fun addDescendants(parentName: String) {
                allMemberGroups.filter { it.parentName == parentName }.forEach { child ->
                    add(child.name)
                    addDescendants(child.name)
                }
            }
            addDescendants(group.name)
        }
    }
    val eligibleParents = remember(allMemberGroups, ineligibleNames) {
        allMemberGroups.filter { it.name !in ineligibleNames }
            .sortedBy { it.name }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.group_edit_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.replace("\n", ""); nameError = "" },
                    label = { Text(stringResource(R.string.group_name_label)) },
                    placeholder = { Text(stringResource(R.string.group_name_hint)) },
                    isError = nameError.isNotEmpty(),
                    supportingText = if (nameError.isNotEmpty()) {
                        { Text(nameError) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.group_description_label)) },
                    placeholder = { Text(stringResource(R.string.group_description_hint)) },
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = parentMenuExpanded,
                    onExpandedChange = { parentMenuExpanded = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    OutlinedTextField(
                        value = selectedParentName ?: stringResource(R.string.group_parent_none),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.group_parent_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = parentMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = parentMenuExpanded,
                        onDismissRequest = { parentMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.group_parent_none)) },
                            onClick = {
                                selectedParentName = null
                                parentMenuExpanded = false
                            }
                        )
                        eligibleParents.forEach { parent ->
                            DropdownMenuItem(
                                text = { Text(parent.name) },
                                onClick = {
                                    selectedParentName = parent.name
                                    parentMenuExpanded = false
                                }
                            )
                        }
                    }
                }

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
                            val trimmedName = name.trim()
                            when {
                                trimmedName.isBlank() ->
                                    nameError = context.getString(R.string.error_member_group_name_empty)
                                trimmedName != group.name && trimmedName in existingGroupNames ->
                                    nameError = context.getString(R.string.error_member_group_name_exists)
                                else -> onConfirm(trimmedName, description.trim(), selectedParentName)
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

@Composable
fun GroupDescriptionEditDialog(
    group: MemberGroup,
    existingGroupNames: List<String> = emptyList(),
    allMemberGroups: List<MemberGroup> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, parentName: String?) -> Unit
) {
    GroupEditDialog(
        group = group,
        existingGroupNames = existingGroupNames,
        allMemberGroups = allMemberGroups,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}
