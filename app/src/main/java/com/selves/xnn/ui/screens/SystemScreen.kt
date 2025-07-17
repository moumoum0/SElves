package com.selves.xnn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selves.xnn.model.Member

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemScreen(
    currentMember: Member,
    onMemberSwitch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 空的系统页面
        Text(
            text = "系统",
            style = MaterialTheme.typography.headlineMedium
        )
    }
} 