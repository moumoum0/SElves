package com.selves.xnn.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.selves.xnn.R
import com.selves.xnn.util.AdminPinCrypto

/**
 * 管理员数字密码对话框模式。
 * 恢复入口后续可加独立模式或按钮，不必改校验逻辑。
 */
enum class AdminPinDialogMode {
    /** 首次设置：输入一次 */
    Setup,
    /** 首次设置：再输一次确认 */
    SetupConfirm,
    /** 敏感操作前校验 */
    Verify,
    /** 修改：先验证旧 PIN */
    ChangeVerify,
    /** 修改：输入新 PIN */
    ChangeNew,
    /** 修改：确认新 PIN */
    ChangeConfirm
}

/** UI 层管理员 PIN 弹窗状态（ViewModel 共用） */
data class AdminPinUiState(
    val mode: AdminPinDialogMode,
    val canDismiss: Boolean,
    val errorMessage: String? = null
)

/**
 * 常规 6 位数字密码弹窗。
 *
 * @param canDismiss 强制设置时为 false
 * @param errorMessage 外部校验失败文案（如密码错误）
 */
@Composable
fun AdminPinDialog(
    mode: AdminPinDialogMode,
    canDismiss: Boolean = true,
    errorMessage: String? = null,
    onDismiss: () -> Unit = {},
    onSubmit: (String) -> Unit
) {
    var pin by remember(mode) { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val titleRes = when (mode) {
        AdminPinDialogMode.Setup, AdminPinDialogMode.ChangeNew -> R.string.admin_pin_setup_title
        AdminPinDialogMode.SetupConfirm, AdminPinDialogMode.ChangeConfirm -> R.string.admin_pin_confirm_title
        AdminPinDialogMode.Verify, AdminPinDialogMode.ChangeVerify -> R.string.admin_pin_verify_title
    }
    val messageRes = when (mode) {
        AdminPinDialogMode.Setup -> R.string.admin_pin_setup_message
        AdminPinDialogMode.SetupConfirm -> R.string.admin_pin_confirm_message
        AdminPinDialogMode.Verify -> R.string.admin_pin_verify_message
        AdminPinDialogMode.ChangeVerify -> R.string.admin_pin_change_verify_message
        AdminPinDialogMode.ChangeNew -> R.string.admin_pin_change_new_message
        AdminPinDialogMode.ChangeConfirm -> R.string.admin_pin_confirm_message
    }

    val formatError = stringResource(R.string.admin_pin_format_error)
    var localError by remember(mode, errorMessage) { mutableStateOf(errorMessage) }

    LaunchedEffect(mode) {
        focusRequester.requestFocus()
    }
    LaunchedEffect(errorMessage) {
        localError = errorMessage
    }

    fun trySubmit() {
        if (!AdminPinCrypto.isValidPinFormat(pin)) {
            localError = formatError
            return
        }
        localError = null
        onSubmit(pin)
    }

    AlertDialog(
        onDismissRequest = {
            if (canDismiss) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = canDismiss,
            dismissOnClickOutside = canDismiss
        ),
        title = { Text(stringResource(titleRes)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(messageRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }.take(AdminPinCrypto.PIN_LENGTH)
                        pin = digits
                        localError = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    label = { Text(stringResource(R.string.admin_pin_label)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { trySubmit() }),
                    isError = localError != null,
                    supportingText = {
                        if (localError != null) {
                            Text(localError!!)
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { trySubmit() },
                enabled = pin.length == AdminPinCrypto.PIN_LENGTH
            ) {
                Text(stringResource(R.string.admin_pin_confirm))
            }
        },
        dismissButton = {
            if (canDismiss) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        }
    )
}