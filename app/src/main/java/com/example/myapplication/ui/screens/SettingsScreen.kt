package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Member

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
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
        // 空的设置页面
        Text(
            text = "设置页面",
            style = MaterialTheme.typography.headlineMedium
        )
    }
} 