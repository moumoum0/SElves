package com.selves.xnn.ui.components

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selves.xnn.model.ColorScheme
import com.selves.xnn.model.getDisplayName
import com.selves.xnn.model.getDescription

@Composable
fun ColorSchemeDialog(
    isOpen: Boolean,
    selectedColorScheme: ColorScheme,
    onColorSchemeSelected: (ColorScheme) -> Unit,
    onDismiss: () -> Unit
) {
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "颜色与个性化",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )
            },
            text = {
                Column(
                    modifier = Modifier.selectableGroup()
                ) {
                    ColorScheme.values().forEach { colorScheme ->
                        val isEnabled = if (colorScheme == ColorScheme.WALLPAPER) {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        } else {
                            true
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (colorScheme == selectedColorScheme),
                                    onClick = {
                                        if (isEnabled) {
                                            onColorSchemeSelected(colorScheme)
                                        }
                                    },
                                    enabled = isEnabled,
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (colorScheme == selectedColorScheme),
                                onClick = null,
                                enabled = isEnabled
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column {
                                Text(
                                    text = colorScheme.getDisplayName(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isEnabled) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    }
                                )
                                
                                Text(
                                    text = colorScheme.getDescription(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isEnabled) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        )
    }
}
