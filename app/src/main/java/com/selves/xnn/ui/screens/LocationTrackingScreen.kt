package com.selves.xnn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.activity.compose.BackHandler
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import com.selves.xnn.model.*
import com.selves.xnn.ui.components.*
import com.selves.xnn.viewmodel.LocationTrackingViewModel
import com.selves.xnn.util.MapNavigationUtils
import com.selves.xnn.util.LocationPermissionHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationTrackingScreen(
    currentMemberId: String,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: LocationTrackingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val trackingConfig by viewModel.trackingConfig.collectAsState()
    
    val todayRecords by viewModel.getTodayRecords().collectAsState(initial = emptyList())
    
    LaunchedEffect(currentMemberId) {
        viewModel.setCurrentMemberId(currentMemberId)
    }
    
    LaunchedEffect(uiState.navigationRequest) {
        uiState.navigationRequest?.let { request ->
            MapNavigationUtils.navigateToLocation(
                context = context,
                latitude = request.locationRecord.latitude,
                longitude = request.locationRecord.longitude,
                mapApp = request.mapApp,
                name = request.locationRecord.address
            )
            viewModel.clearNavigationRequest()
        }
    }
    
    var showHistoryScreen by remember { mutableStateOf(false) }
    
    BackHandler(enabled = showHistoryScreen) {
        showHistoryScreen = false
    }
    
    androidx.compose.animation.AnimatedContent(
        targetState = showHistoryScreen,
        transitionSpec = {
            if (targetState) {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                )
            } else {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                )
            }
        },
        label = "history_screen_transition"
    ) { isHistoryScreen ->
        if (isHistoryScreen) {
            LocationHistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { showHistoryScreen = false }
            )
        } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "轨迹记录",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.ShowConfigDialog) }
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "设置",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SimpleTrackingControl(
                    isTracking = uiState.trackingStatus == TrackingStatus.RECORDING,
                    recordingInterval = trackingConfig.recordingInterval,
                    onStartTracking = {
                        if (!LocationPermissionHelper.hasLocationPermission(context)) {
                            (context as? Activity)?.let { activity ->
                                LocationPermissionHelper.requestLocationPermission(activity)
                            }
                            return@SimpleTrackingControl
                        }
                        viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.StartTracking)
                    },
                    onStopTracking = {
                        viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.StopTracking)
                    }
                )
                
                TodayLocationList(
                    locationRecords = todayRecords,
                    onNavigate = { record, mapApp ->
                        viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.NavigateToLocation(record, mapApp))
                    },
                    onViewHistory = { showHistoryScreen = true }
                )
            }
        }
    }
    }
    
    if (uiState.showConfigDialog) {
        LocationTrackingConfigDialog(
            config = trackingConfig,
            onConfigUpdate = { config ->
                viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.UpdateConfig(config))
            },
            onDismiss = { viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.HideConfigDialog) }
        )
    }
}

@Composable
private fun SimpleTrackingControl(
    isTracking: Boolean,
    recordingInterval: Int,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isTracking) "正在记录" else "未开始记录",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (isTracking) {
                    Text(
                        text = "每${recordingInterval}秒记录一次",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            if (isTracking) {
                Button(
                    onClick = onStopTracking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("停止")
                }
            } else {
                Button(onClick = onStartTracking) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("开始")
                }
            }
        }
    }
}

@Composable
private fun TodayLocationList(
    locationRecords: List<LocationRecord>,
    onNavigate: (LocationRecord, MapApp) -> Unit,
    onViewHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "今日记录",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                
                IconButton(
                    onClick = onViewHistory,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "查看历史",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            if (locationRecords.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.LocationOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无记录",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击上方卡片开始记录轨迹",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            } else {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val screenHeight = maxHeight
                    val itemHeight = 52.dp
                    val headerHeight = 60.dp
                    val buttonHeight = 56.dp
                    val padding = 16.dp

                    val availableHeight = screenHeight - headerHeight - padding
                    val maxItemsNoButton = (availableHeight / itemHeight).toInt().coerceAtLeast(1)
                    val needsViewMore = locationRecords.size > maxItemsNoButton
                    val maxItemsWithButton = ((availableHeight - buttonHeight) / itemHeight).toInt().coerceAtLeast(1)
                    val maxItemsToShow = if (needsViewMore) maxItemsWithButton else maxItemsNoButton
                    val displayRecords = locationRecords.take(maxItemsToShow)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        displayRecords.forEachIndexed { index, record ->
                            LocationTimelineItem(
                                locationRecord = record,
                                isFirst = index == 0,
                                isLast = index == displayRecords.lastIndex && !needsViewMore,
                                onNavigate = { mapApp -> onNavigate(record, mapApp) }
                            )
                        }

                        if (needsViewMore) {
                            TextButton(
                                onClick = onViewHistory,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "查看更多",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationHistoryScreen(
    viewModel: LocationTrackingViewModel,
    onNavigateBack: () -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val locationRecords by viewModel.locationRecords.collectAsState()
    val scrollState = rememberLazyListState()
    val density = LocalDensity.current
    val calendarMaxHeight = 360.dp
    val calendarMaxHeightPx = with(density) { calendarMaxHeight.toPx() }
    var calendarOffsetPx by remember { mutableStateOf(0f) }

    val calendarNestedScroll = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val deltaY = available.y
                if (abs(deltaY) < 0.5f) return Offset.Zero

                val newOffset = (calendarOffsetPx - deltaY)
                    .coerceAtLeast(0f)
                    .coerceAtMost(calendarMaxHeightPx)

                val consumed = calendarOffsetPx != newOffset
                calendarOffsetPx = newOffset

                return if (consumed) Offset(0f, deltaY) else Offset.Zero
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "历史记录",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            }
        )
        
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(calendarNestedScroll)
        ) {
            item {
                val calendarHeight = with(density) {
                    (calendarMaxHeightPx - calendarOffsetPx).coerceAtLeast(0f).toDp()
                }
                val alpha = (1f - (calendarOffsetPx / calendarMaxHeightPx)).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(calendarHeight)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .alpha(alpha)
                ) {
                    if (calendarHeight > 0.dp) {
                        MonthCalendar(
                            selectedDate = selectedDate,
                            onDateSelected = { date ->
                                viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.SelectDate(date))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            if (locationRecords.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.LocationOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedDate.isEqual(LocalDate.now())) "暂无记录" else "该日期无记录",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击上方卡片开始记录轨迹",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(locationRecords.size) { index ->
                    LocationTimelineItem(
                        locationRecord = locationRecords[index],
                        isFirst = index == 0,
                        isLast = index == locationRecords.lastIndex,
                        onNavigate = { mapApp ->
                            viewModel.handleEvent(com.selves.xnn.viewmodel.LocationTrackingEvent.NavigateToLocation(locationRecords[index], mapApp))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
