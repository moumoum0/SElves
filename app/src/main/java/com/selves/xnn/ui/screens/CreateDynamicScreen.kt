package com.selves.xnn.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.res.stringResource
import com.selves.xnn.R
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.selves.xnn.model.DynamicType
import com.selves.xnn.model.Member
import com.selves.xnn.util.ImageUtils
import com.selves.xnn.viewmodel.DynamicViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDynamicScreen(
    currentMember: Member?,
    onNavigateBack: () -> Unit,
    dynamicViewModel: DynamicViewModel = hiltViewModel()
) {
    var content by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var savedImagePaths by remember { mutableStateOf<List<String>>(emptyList()) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentMember?.id) {
        currentMember?.id?.let { dynamicViewModel.setCurrentUser(it) }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val maxImages = 9
        val availableSlots = maxImages - savedImagePaths.size
        if (availableSlots > 0) {
            val newUris = uris.take(availableSlots)
            selectedImageUris = selectedImageUris + newUris
            scope.launch {
                val newPaths = mutableListOf<String>()
                newUris.forEach { uri ->
                    ImageUtils.saveMessageImageToInternalStorage(context, uri)?.let { newPaths.add(it) }
                }
                savedImagePaths = savedImagePaths + newPaths
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dialog_edit_dynamic)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if ((content.isNotBlank() || savedImagePaths.isNotEmpty()) && currentMember != null) {
                                dynamicViewModel.createDynamic(
                                    title = "",
                                    content = content,
                                    authorName = currentMember.name,
                                    authorAvatar = currentMember.avatarUrl,
                                    type = if (savedImagePaths.isNotEmpty()) DynamicType.IMAGE else DynamicType.TEXT,
                                    images = savedImagePaths,
                                    tags = emptyList()
                                )
                                onNavigateBack()
                            }
                        },
                        enabled = content.isNotBlank() || savedImagePaths.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.btn_publish))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text(stringResource(R.string.placeholder_share_thoughts)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                maxLines = 10
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val allItems = mutableListOf<String>().apply {
                addAll(savedImagePaths)
                if (savedImagePaths.size < 9) add("ADD_BUTTON")
            }

            allItems.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { item ->
                        if (item == "ADD_BUTTON") {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add), modifier = Modifier.size(32.dp))
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
                                    val request = remember(item) { ImageUtils.createMessageImageRequest(context, item) }
                                    if (request != null) {
                                        AsyncImage(
                                            model = request,
                                            contentDescription = stringResource(R.string.cd_image),
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(stringResource(R.string.chat_image_load_failed), fontSize = 12.sp)
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { savedImagePaths = savedImagePaths - item },
                                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.cd_delete),
                                            tint = MaterialTheme.colorScheme.onError,
                                            modifier = Modifier.padding(2.dp).size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
