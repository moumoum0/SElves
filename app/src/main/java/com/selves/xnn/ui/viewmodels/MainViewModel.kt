package com.selves.xnn.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selves.xnn.data.MemberPreferences
import com.selves.xnn.data.repository.ChatGroupRepository
import com.selves.xnn.data.repository.MessageRepository
import com.selves.xnn.data.repository.MemberRepository
import com.selves.xnn.data.repository.MessageReadStatusRepository
import com.selves.xnn.data.repository.SystemRepository
import com.selves.xnn.data.repository.OnlineStatusRepository
import com.selves.xnn.data.entity.OnlineStatusEntity
import com.selves.xnn.data.BackupService
import com.selves.xnn.data.BackupResult
import com.selves.xnn.model.ChatGroup
import com.selves.xnn.model.Message
import com.selves.xnn.model.MessageType
import com.selves.xnn.model.Member
import com.selves.xnn.model.HomeLayoutConfig
import com.selves.xnn.model.HomeModuleType
import com.selves.xnn.model.FunctionModuleConfig
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.util.*
import javax.inject.Inject
import android.content.Context
import com.selves.xnn.util.ImageUtils
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 加载状态数据类
 */
data class LoadingState(
    val isLoading: Boolean = true
)

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val memberRepository: MemberRepository,
    private val chatGroupRepository: ChatGroupRepository,
    private val messageRepository: MessageRepository,
    private val messageReadStatusRepository: MessageReadStatusRepository,
    private val memberPreferences: MemberPreferences,
    private val systemRepository: SystemRepository,
    private val onlineStatusRepository: OnlineStatusRepository,
    private val backupService: com.selves.xnn.data.BackupService
) : ViewModel() {
    
    private val TAG = "MainViewModel"
    
    // 当前成员
    private val _currentMember = MutableStateFlow<Member?>(null)
    val currentMember: StateFlow<Member?> = _currentMember.asStateFlow()
    
    // 所有成员
    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members.asStateFlow()
    
    // 所有成员（包括已删除成员）用于消息显示
    private val _allMembers = MutableStateFlow<List<Member>>(emptyList())
    val allMembers: StateFlow<List<Member>> = _allMembers.asStateFlow()
    
    // 群组
    private val _groups = MutableStateFlow<List<ChatGroup>>(emptyList())
    val groups: StateFlow<List<ChatGroup>> = _groups.asStateFlow()
    
    // 系统是否存在
    private val _hasSystem = MutableStateFlow<Boolean?>(null)
    val hasSystem: StateFlow<Boolean?> = _hasSystem.asStateFlow()
    
    // 是否需要显示引导界面
    private val _needsGuide = MutableStateFlow<Boolean?>(null)
    val needsGuide: StateFlow<Boolean?> = _needsGuide.asStateFlow()
    
    // 备份导入相关状态
    private val _isBackupInProgress = MutableStateFlow(false)
    val isBackupInProgress: StateFlow<Boolean> = _isBackupInProgress.asStateFlow()
    
    private val _backupProgress = MutableStateFlow<Float?>(null)
    val backupProgress: StateFlow<Float?> = _backupProgress.asStateFlow()
    
    private val _backupProgressMessage = MutableStateFlow("")
    val backupProgressMessage: StateFlow<String> = _backupProgressMessage.asStateFlow()
    
    private val _showImportWarningDialog = MutableStateFlow(false)
    val showImportWarningDialog: StateFlow<Boolean> = _showImportWarningDialog.asStateFlow()
    
    private val _backupImportSuccess = MutableStateFlow(false)
    val backupImportSuccess: StateFlow<Boolean> = _backupImportSuccess.asStateFlow()
    
    // 存储待导入的URI，等用户确认后使用
    private var pendingImportUri: Uri? = null
    
    // 首页布局配置
    private val _homeLayoutConfig = MutableStateFlow(HomeLayoutConfig())
    val homeLayoutConfig: StateFlow<HomeLayoutConfig> = _homeLayoutConfig.asStateFlow()
    
    // 首页编辑模式
    private val _isHomeEditMode = MutableStateFlow(false)
    val isHomeEditMode: StateFlow<Boolean> = _isHomeEditMode.asStateFlow()
    
    // 统一的加载状态
    private val _loadingState = MutableStateFlow(LoadingState())
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()
    
    // 保持向后兼容的加载状态
    val isLoading: StateFlow<Boolean> = _loadingState.map { it.isLoading }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
    
    // 消息管理
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()
    
    // 未读消息数量管理
    private val _unreadCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadCounts: StateFlow<Map<String, Int>> = _unreadCounts.asStateFlow()
    
    // 成员登录记录管理
    private val _memberLoginRecords = MutableStateFlow<Map<String, List<OnlineStatusEntity>>>(emptyMap())
    val memberLoginRecords: StateFlow<Map<String, List<OnlineStatusEntity>>> = _memberLoginRecords.asStateFlow()
    
    // 已加载消息的群组ID列表
    private val loadedMessageGroups = mutableSetOf<String>()
    
    // 加载阶段跟踪
    private var membersLoaded = false
    private var currentMemberLoaded = false
    private var groupsLoaded = false
    private var messagesLoaded = false
    private var imagesPreloaded = false
    
    // 防止重复加载的标志
    private var isLoadingInProgress = false
    
    // 异常处理器
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable is kotlinx.coroutines.CancellationException) {
            Log.d(TAG, "协程被取消: ${throwable.message}")
        } else {
            Log.e(TAG, "协程异常: ${throwable.message}", throwable)
            // 即使出现异常也要完成加载
            completeLoading()
        }
    }
    
    init {
        // 不在init中加载数据，等待UI准备好后手动触发
    }
    
    /**
     * 启动数据加载（由UI层调用）
     */
    fun startLoading() {
        if (isLoadingInProgress) {
            Log.d(TAG, "加载已在进行中")
            return
        }
        viewModelScope.launch {
            startUnifiedLoading()
        }
    }
    
    /**
     * 开始统一的加载流程
     */
    private fun startUnifiedLoading(skipSystemCheck: Boolean = false) {
        // 防止重复加载
        if (isLoadingInProgress) {
            Log.d(TAG, "加载已在进行中，跳过重复加载")
            return
        }
        
        isLoadingInProgress = true
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                // 检查系统是否存在（备份导入后跳过）
                if (!skipSystemCheck) {
                    checkSystemExists()
                }
                
                // 并行加载基础数据（成员 + 当前成员同时加载，充分利用多核）
                val membersJob = async(Dispatchers.IO) { loadMembers() }
                val currentMemberJob = async(Dispatchers.IO) { loadSavedMember() }
                
                // 等待基础数据加载完成
                awaitAll(membersJob, currentMemberJob)
                
                // 加载群组数据
                loadGroups()
                
                // 并行：消息加载 + 头像预加载同时进行
                val messagesJob = async(Dispatchers.IO) { loadInitialMessages() }
                val avatarPreloadJob = async(Dispatchers.IO) { preloadAvatarsOnly() }
                
                // 等待消息和头像都就绪
                awaitAll(messagesJob, avatarPreloadJob)
                
                // 所有关键数据就绪，完成加载，Splash Screen消失
                completeLoading()
                
                // 后台异步加载非关键数据，不阻塞UI显示
                launch(Dispatchers.IO) {
                    try {
                        // 消息图片预加载（非关键，后台处理）
                        preloadMessageImagesOnly()
                        // 加载成员登录记录
                        loadMemberLoginRecords()
                        
                        Log.d(TAG, "后台数据加载完成")
                    } catch (e: Exception) {
                        Log.e(TAG, "后台数据加载失败: ${e.message}", e)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "统一加载流程失败: ${e.message}", e)
                completeLoading()
            } finally {
                isLoadingInProgress = false
            }
        }
    }
    

    
    /**
     * 完成加载
     */
    private fun completeLoading() {
        _loadingState.value = LoadingState(
            isLoading = false
        )
        isLoadingInProgress = false
        Log.d(TAG, "应用加载完成")
    }
    

    
    /**
     * 加载初始消息数据（等待所有群组消息首次加载完成后再返回）
     */
    private suspend fun loadInitialMessages() {
        val groups = _groups.value
        if (groups.isNotEmpty()) {
            // 并行加载所有群组消息，等待首次数据到达
            val deferreds = groups.map { group ->
                viewModelScope.async(exceptionHandler) {
                    loadGroupMessagesAndWait(group.id)
                    loadUnreadCount(group.id)
                }
            }
            deferreds.awaitAll()
        }
        messagesLoaded = true
    }
    
    /**
     * 预加载头像（启动阶段，与消息加载并行执行）
     */
    private suspend fun preloadAvatarsOnly() {
        val members = _members.value
        
        if (members.isNotEmpty()) {
            ImageUtils.preloadAvatarsToMemory(
                context = context,
                avatarPaths = members.mapNotNull { it.avatarUrl },
                coroutineScope = viewModelScope
            )
        }
    }
    
    /**
     * 预加载消息图片（后台非阻塞任务，不影响UI显示）
     */
    private suspend fun preloadMessageImagesOnly() {
        val allMessages = _messages.value.values.flatten()
        val imagePaths = allMessages.mapNotNull { it.imagePath }
        if (imagePaths.isNotEmpty()) {
            ImageUtils.preloadMessageImages(
                context = context,
                imagePaths = imagePaths,
                coroutineScope = viewModelScope
            )
        }
        imagesPreloaded = true
    }
    
    /**
     * 加载成员登录记录（用于排序）
     * 只查询一次数据库，按memberId分组，避免N+1串行查询
     */
    private suspend fun loadMemberLoginRecords() {
        try {
            val members = _members.value
            if (members.isEmpty()) {
                Log.d(TAG, "没有成员，跳过登录记录加载")
                return
            }
            
            // 为每个成员获取登录记录（最近90天）
            val currentTime = System.currentTimeMillis()
            val threeMonthsAgo = currentTime - (90L * 24 * 60 * 60 * 1000) // 90天前
            
            // 只查一次数据库，然后按memberId分组
            val allRecords = withContext(Dispatchers.IO) {
                onlineStatusRepository.getLoginLogsByDateRange(threeMonthsAgo, currentTime)
            }
            val recordsByMember = allRecords.groupBy { it.memberId }
            
            val loginRecordsMap = members.associate { member ->
                member.id to (recordsByMember[member.id] ?: emptyList())
            }
            
            _memberLoginRecords.value = loginRecordsMap
            
        } catch (e: Exception) {
            Log.e(TAG, "加载成员登录记录失败: ${e.message}", e)
            _memberLoginRecords.value = emptyMap()
        }
    }
    
    private suspend fun loadMembers() {
        // 创建一个CompletableDeferred来等待数据加载完成
        val membersDeferred = CompletableDeferred<Unit>()
        val allMembersDeferred = CompletableDeferred<Unit>()
        
        // 加载未删除的成员（用于UI显示）
        val membersJob = viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            memberRepository.getAllMembers()
                .collect { memberList ->
                    _members.value = memberList
                    if (!membersLoaded) {
                        membersDeferred.complete(Unit)
                    }
                }
        }
        
        // 加载所有成员包括已删除的（用于消息显示）
        val allMembersJob = viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            memberRepository.getAllMembersIncludingDeleted()
                .collect { memberList ->
                    _allMembers.value = memberList
                    if (!membersLoaded) {
                        allMembersDeferred.complete(Unit)
                    }
                }
        }
        
        // 等待两个任务完成
        membersDeferred.await()
        allMembersDeferred.await()
        membersLoaded = true
    }
    
    private suspend fun loadGroups() {
        try {
            val currentMember = _currentMember.value
            val filteredGroups = if (currentMember != null) {
                // 只获取当前成员所属的群聊
                withContext(Dispatchers.IO) {
                    chatGroupRepository.getGroupsByMemberId(currentMember.id).first()
                }
            } else {
                // 如果没有当前成员，返回空列表
                emptyList()
            }
            _groups.value = filteredGroups
            groupsLoaded = true
        } catch (e: Exception) {
            Log.e(TAG, "加载群聊失败: ${e.message}", e)
            _groups.value = emptyList()
            groupsLoaded = true
        }
    }
    
    private suspend fun loadSavedMember() {
        val memberDeferred = CompletableDeferred<Unit>()
        
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            memberPreferences.currentMemberId
                .take(1) // 只取第一个值，这里保持不变因为我们只需要初始值
                .collect { memberId ->
                    if (!memberId.isNullOrEmpty()) {
                        memberRepository.getMemberById(memberId)?.let { member ->
                            _currentMember.value = member
                            // 让保存的成员上线
                            onlineStatusRepository.loginMember(member.id)
                        }
                    }
                    currentMemberLoaded = true
                    memberDeferred.complete(Unit)
                }
        }
        
        memberDeferred.await()
    }
    
    // 创建成员
    fun createMember(name: String, avatarUrl: String?, shouldSetAsCurrent: Boolean = true) {
        val memberId = UUID.randomUUID().toString()
        
        val member = Member(
            id = memberId,
            name = name,
            avatarUrl = avatarUrl
        )
        
        // 使用单一协程进行所有操作，避免并发问题
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                memberRepository.saveMember(member)
                
                if (shouldSetAsCurrent) {
                    // 先让之前的成员下线
                    _currentMember.value?.let { previousMember ->
                        if (previousMember.id != member.id) {
                            onlineStatusRepository.logoutMember(previousMember.id)
                        }
                    }
                    
                    // 设置新的当前成员
                    _currentMember.value = member
                    memberPreferences.saveCurrentMemberId(member.id)
                    
                    // 让新创建的成员上线
                    onlineStatusRepository.loginMember(member.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "创建成员失败: ${e.message}", e)
            }
        }
    }
    
    // 更新成员信息
    fun updateMember(memberId: String, name: String, avatarUrl: String?) {
        
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                // 获取成员当前信息
                val existingMember = memberRepository.getMemberById(memberId)
                
                if (existingMember != null) {
                    // 创建更新后的成员对象
                    val updatedMember = existingMember.copy(
                        name = name,
                        avatarUrl = avatarUrl ?: existingMember.avatarUrl
                    )
                    
                    // 保存到数据库
                    memberRepository.saveMember(updatedMember)
                    
                    // 如果是当前成员，更新当前成员信息
                    if (_currentMember.value?.id == memberId) {
                        _currentMember.value = updatedMember
                    }
                } else {
                    Log.e(TAG, "无法更新成员，未找到ID为 $memberId 的成员")
                }
            } catch (e: Exception) {
                Log.e(TAG, "更新成员失败: ${e.message}", e)
            }
        }
    }
    
    // 设置当前成员
    fun setCurrentMember(member: Member) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            // 如果有之前的成员，先让其下线
            _currentMember.value?.let { previousMember ->
                if (previousMember.id != member.id) {
                    onlineStatusRepository.logoutMember(previousMember.id)
                }
            }
            
            // 设置新的当前成员
            _currentMember.value = member
            
            // 让新成员上线
            onlineStatusRepository.loginMember(member.id)
            
            // 重新加载群聊列表（基于新的当前成员）
            loadGroups()
            
            // 重新加载所有群组的未读数量
            _groups.value.forEach { group ->
                loadUnreadCount(group.id)
            }
        }
        
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            memberPreferences.saveCurrentMemberId(member.id)
        }
    }
    
    // 创建群聊
    fun createGroup(name: String, avatarUrl: String?, selectedMembers: List<Member>, creator: Member): ChatGroup {
        val groupId = UUID.randomUUID().toString()
        
        // 创建群聊对象，创建者为群主
        val newGroup = ChatGroup(
            id = groupId,
            name = name,
            avatarUrl = avatarUrl,
            members = selectedMembers,
            ownerId = creator.id,
            createdAt = System.currentTimeMillis()
        )
        
        // 更新群组列表
        val currentGroups = _groups.value.toMutableList()
        currentGroups.add(newGroup)
        _groups.value = currentGroups
        
        // 保存到数据库
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                chatGroupRepository.saveGroup(newGroup)
                
                // 为新群组初始化消息列表和未读数量
                _messages.update { it + (groupId to emptyList()) }
                loadedMessageGroups.add(groupId)
                loadUnreadCount(groupId)
            } catch (e: Exception) {
                Log.e(TAG, "保存群聊失败: ${e.message}", e)
            }
        }
        
        return newGroup
    }
    
    // 加载群组未读消息数量
    private fun loadUnreadCount(groupId: String) {
        val currentMember = _currentMember.value ?: return
        
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                messageReadStatusRepository.getUnreadMessageCountFlow(groupId, currentMember.id)
                    .collect { count ->
                        _unreadCounts.update { it + (groupId to count) }
                    }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.e(TAG, "加载群组 $groupId 未读数量失败: ${e.message}", e)
                }
            }
        }
    }

    fun getGroupMessages(groupId: String): List<Message> {
        // 如果尚未加载该群组的消息，则从数据库加载
        if (!loadedMessageGroups.contains(groupId)) {
            loadGroupMessages(groupId)
        }
        return _messages.value[groupId] ?: emptyList()
    }
    
    /**
     * 加载群组消息并等待首次数据到达（用于启动阶段，确保数据就绪后再进入主页）
     * 启动阶段只加载最近50条消息，快速显示UI，之后异步切换到全量监听
     */
    private suspend fun loadGroupMessagesAndWait(groupId: String) {
        if (loadedMessageGroups.contains(groupId)) {
            return
        }
        
        val firstDataDeferred = CompletableDeferred<Unit>()
        
        // 先初始化空列表，防止UI显示错误
        _messages.update { current ->
            if (!current.containsKey(groupId)) current + (groupId to emptyList()) else current
        }
        
        // 添加到已加载列表，防止重复加载
        loadedMessageGroups.add(groupId)
        
        // 启动持续监听协程：先用限量查询快速拿到首屏数据，再切换到全量监听
        viewModelScope.launch(SupervisorJob() + Dispatchers.IO + exceptionHandler) {
            try {
                // 阶段1：限量查询，快速拿到最近消息用于首屏显示
                messageRepository.getRecentGroupMessages(groupId, 50)
                    .take(1) // 只取第一次emit，拿到数据后立即进入阶段2
                    .collect { messageList ->
                        // 注意：getRecentGroupMessages 是 DESC 排序，需要反转为 ASC
                        _messages.update { it + (groupId to messageList.reversed()) }
                        if (!firstDataDeferred.isCompleted) {
                            firstDataDeferred.complete(Unit)
                        }
                    }
                
                // 阶段2：切换到全量监听，后续消息变更也会更新
                messageRepository.getGroupMessages(groupId)
                    .catch { e ->
                        if (e is kotlinx.coroutines.CancellationException) {
                            Log.d(TAG, "加载群组 $groupId 的消息流被取消")
                        } else {
                            Log.e(TAG, "加载群组 $groupId 的消息流失败: ${e.message}", e)
                            loadedMessageGroups.remove(groupId)
                        }
                    }
                    .collect { messageList ->
                        _messages.update { it + (groupId to messageList) }
                    }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.e(TAG, "加载群组 $groupId 的消息失败: ${e.message}", e)
                }
                loadedMessageGroups.remove(groupId)
                if (!firstDataDeferred.isCompleted) {
                    firstDataDeferred.complete(Unit)
                }
            }
        }
        
        // 等待首次数据到达
        firstDataDeferred.await()
    }
    
    // 加载群组消息
    private fun loadGroupMessages(groupId: String) {
        if (loadedMessageGroups.contains(groupId)) {
            return // 避免重复加载
        }
        
        // 使用SupervisorJob确保这个协程的失败不影响其他协程
        viewModelScope.launch(SupervisorJob() + Dispatchers.IO + exceptionHandler) {
            try {
                // 先初始化空列表，防止UI显示错误
                _messages.update { current ->
                    if (!current.containsKey(groupId)) current + (groupId to emptyList()) else current
                }
                
                // 添加到已加载列表，防止重复加载
                loadedMessageGroups.add(groupId)
                
                // 监听消息流
                messageRepository.getGroupMessages(groupId)
                    .catch { e ->
                        if (e is kotlinx.coroutines.CancellationException) {
                            Log.d(TAG, "加载群组 $groupId 的消息流被取消")
                        } else {
                            Log.e(TAG, "加载群组 $groupId 的消息流失败: ${e.message}", e)
                            loadedMessageGroups.remove(groupId)
                        }
                    }
                    .collect { messageList ->
                        _messages.update { it + (groupId to messageList) }
                    }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.e(TAG, "加载群组 $groupId 的消息失败: ${e.message}", e)
                }
                // 从已加载列表中移除，允许重试
                loadedMessageGroups.remove(groupId)
            }
        }
    }

    fun sendMessage(groupId: String, content: String) {
        val currentMember = _currentMember.value ?: return
        val message = Message(
            id = UUID.randomUUID().toString(),
            senderId = currentMember.id,
            content = content,
            type = MessageType.TEXT
        )
        
        // 更新内存中的消息列表
        _messages.update { current ->
            val groupMessages = (current[groupId] ?: emptyList()) + message
            current + (groupId to groupMessages)
        }
        
        // 保存消息到数据库
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                messageRepository.saveMessage(message, groupId)
            } catch (e: Exception) {
                Log.e(TAG, "保存消息失败: ${e.message}", e)
            }
        }
    }
    
    fun sendImageMessage(groupId: String, imageUri: Uri, caption: String = "") {
        val currentMember = _currentMember.value ?: return
        
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                // 保存图片到内部存储
                val imagePath = ImageUtils.saveMessageImageToInternalStorage(context, imageUri)
                
                if (imagePath != null) {
                    val message = Message(
                        id = UUID.randomUUID().toString(),
                        senderId = currentMember.id,
                        content = caption,
                        type = MessageType.IMAGE,
                        imagePath = imagePath
                    )
                    
                    // 更新内存中的消息列表
                    _messages.update { current ->
                        val groupMessages = (current[groupId] ?: emptyList()) + message
                        current + (groupId to groupMessages)
                    }
                    
                    // 保存消息到数据库
                    messageRepository.saveMessage(message, groupId)
                } else {
                    Log.e(TAG, "保存图片失败")
                }
            } catch (e: Exception) {
                Log.e(TAG, "发送图片消息失败: ${e.message}", e)
            }
        }
    }
    
    // 删除消息
    fun deleteMessage(groupId: String, messageId: String) {
        val groupMessages = _messages.value[groupId] ?: return
        val messageToDelete = groupMessages.find { it.id == messageId } ?: return
        
        // 从内存中删除
        _messages.update { current ->
            current + (groupId to (current[groupId]?.filter { it.id != messageId } ?: emptyList()))
        }
        
        // 从数据库中删除
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                messageRepository.deleteMessage(messageToDelete, groupId)
            } catch (e: Exception) {
                Log.e(TAG, "删除消息失败: ${e.message}", e)
            }
        }
    }
    
    // 清空群组消息
    fun clearGroupMessages(groupId: String) {
        // 从内存中清空
        _messages.update { it + (groupId to emptyList()) }
        
        // 从数据库中清空
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                messageRepository.clearGroupMessages(groupId)
            } catch (e: Exception) {
                Log.e(TAG, "清空群组消息失败: ${e.message}", e)
            }
        }
    }
    
    // 手动刷新所有群组的消息
    fun refreshAllMessages() {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                // 获取所有群组ID
                val groupIds = _groups.value.map { it.id }
                
                // 清空已加载标记
                loadedMessageGroups.clear()
                
                // 重新加载所有群组消息
                groupIds.forEach { groupId ->
                    loadGroupMessages(groupId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "刷新消息失败: ${e.message}", e)
            }
        }
    }

    // 删除成员（软删除）
    fun deleteMember(member: Member) {
        // 检查不能删除当前成员
        if (member.id == _currentMember.value?.id) {
            Log.e(TAG, "无法删除当前成员: ${member.id}")
            return
        }
        
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                // 标记成员为已删除状态，而不是物理删除
                memberRepository.deleteMember(member.id)
            } catch (e: Exception) {
                Log.e(TAG, "标记成员为删除状态失败: ${e.message}", e)
            }
        }
    }
    
    // 获取群组最新消息
    fun getLatestMessage(groupId: String): Message? {
        return _messages.value[groupId]?.lastOrNull()
    }
    
    // 获取群组未读消息数量
    fun getUnreadCount(groupId: String): Int {
        return _unreadCounts.value[groupId] ?: 0
    }
    
    // 格式化时间显示
    fun formatMessageTime(timestamp: Long): String {
        return com.selves.xnn.util.TimeFormatter.formatTimestamp(timestamp)
    }
    
    // 标记群组所有消息为已读
    fun markGroupMessagesAsRead(groupId: String) {
        val currentMember = _currentMember.value ?: return
        
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                messageReadStatusRepository.markAllGroupMessagesAsRead(groupId, currentMember.id)
            } catch (e: Exception) {
                Log.e(TAG, "标记群组 $groupId 消息为已读失败: ${e.message}", e)
            }
        }
    }
    
    // 标记单个消息为已读
    fun markMessageAsRead(messageId: String) {
        val currentMember = _currentMember.value ?: return
        
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                messageReadStatusRepository.markMessageAsRead(messageId, currentMember.id)
            } catch (e: Exception) {
                Log.e(TAG, "标记消息 $messageId 为已读失败: ${e.message}", e)
            }
        }
    }
    
    // 群聊管理功能
    
    // 添加成员到群聊
    fun addMembersToGroup(groupId: String, membersToAdd: List<Member>) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                // 找到目标群聊
                val currentGroups = _groups.value.toMutableList()
                val groupIndex = currentGroups.indexOfFirst { it.id == groupId }
                
                if (groupIndex != -1) {
                    val group = currentGroups[groupIndex]
                    val updatedMembers = group.members.toMutableList()
                    
                    // 添加新成员（避免重复）
                    membersToAdd.forEach { newMember ->
                        if (updatedMembers.none { it.id == newMember.id }) {
                            updatedMembers.add(newMember)
                        }
                    }
                    
                    // 更新群聊
                    val updatedGroup = group.copy(members = updatedMembers)
                    currentGroups[groupIndex] = updatedGroup
                    _groups.value = currentGroups
                    
                    // 保存到数据库
                    chatGroupRepository.updateGroup(updatedGroup)
                }
            } catch (e: Exception) {
                Log.e(TAG, "添加成员到群聊失败: ${e.message}", e)
            }
        }
    }
    
    // 从群聊中移除成员
    fun removeMembersFromGroup(groupId: String, membersToRemove: List<Member>) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                // 找到目标群聊
                val currentGroups = _groups.value.toMutableList()
                val groupIndex = currentGroups.indexOfFirst { it.id == groupId }
                
                if (groupIndex != -1) {
                    val group = currentGroups[groupIndex]
                    val updatedMembers = group.members.toMutableList()
                    
                    // 移除成员
                    membersToRemove.forEach { memberToRemove ->
                        updatedMembers.removeAll { it.id == memberToRemove.id }
                    }
                    
                    // 更新群聊
                    val updatedGroup = group.copy(members = updatedMembers)
                    currentGroups[groupIndex] = updatedGroup
                    _groups.value = currentGroups
                    
                    // 保存到数据库
                    chatGroupRepository.updateGroup(updatedGroup)
                }
            } catch (e: Exception) {
                Log.e(TAG, "从群聊移除成员失败: ${e.message}", e)
            }
        }
    }
    
    // 更新群聊信息
    fun updateGroupInfo(groupId: String, newName: String, newAvatarUrl: String?) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                // 找到目标群聊
                val currentGroups = _groups.value.toMutableList()
                val groupIndex = currentGroups.indexOfFirst { it.id == groupId }
                
                if (groupIndex != -1) {
                    val group = currentGroups[groupIndex]
                    val updatedGroup = group.copy(
                        name = newName,
                        avatarUrl = newAvatarUrl
                    )
                    currentGroups[groupIndex] = updatedGroup
                    _groups.value = currentGroups
                    
                    // 保存到数据库
                    chatGroupRepository.updateGroup(updatedGroup)
                }
            } catch (e: Exception) {
                Log.e(TAG, "更新群聊信息失败: ${e.message}", e)
            }
        }
    }
    
    // 解散群聊
    fun deleteGroup(groupId: String) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                // 从内存中移除群聊
                val currentGroups = _groups.value.toMutableList()
                val groupToRemove = currentGroups.find { it.id == groupId }
                currentGroups.removeAll { it.id == groupId }
                _groups.value = currentGroups
                
                // 清空群聊消息
                _messages.update { it - groupId }
                
                // 清空未读数量
                _unreadCounts.update { it - groupId }
                
                // 从数据库中删除
                chatGroupRepository.deleteGroupById(groupId)
                messageRepository.clearGroupMessages(groupId)
            } catch (e: Exception) {
                Log.e(TAG, "解散群聊失败: ${e.message}", e)
            }
        }
    }
    
    /**
     * 检查系统是否存在
     */
    private suspend fun checkSystemExists() = withContext(Dispatchers.IO) {
        try {
            val hasSystemResult = systemRepository.hasSystem()
            _hasSystem.value = hasSystemResult
            
            // 检查是否需要引导界面：系统不存在或没有成员
            val membersCount = memberRepository.getAllMembers().first().size
            val needsGuideResult = !hasSystemResult || membersCount == 0
            _needsGuide.value = needsGuideResult
            
            Log.d(TAG, "系统存在检查结果: $hasSystemResult, 成员数量: $membersCount, 需要引导: $needsGuideResult")
        } catch (e: Exception) {
            Log.e(TAG, "检查系统存在性失败: ${e.message}", e)
            _hasSystem.value = false
            _needsGuide.value = true // 出错时默认显示引导界面
        }
    }
    
    /**
     * 创建系统
     */
    fun createSystem(name: String, avatarUrl: String?) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                val system = com.selves.xnn.model.System(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    avatarUrl = avatarUrl,
                    createdAt = java.lang.System.currentTimeMillis(),
                    updatedAt = java.lang.System.currentTimeMillis()
                )
                
                systemRepository.saveSystem(system)
                _hasSystem.value = true
                Log.d(TAG, "系统已创建: $name")
            } catch (e: Exception) {
                Log.e(TAG, "创建系统失败: ${e.message}", e)
            }
        }
    }
    
    /**
     * 完成引导流程
     */
    fun completeGuide() {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            _needsGuide.value = false
            Log.d(TAG, "引导流程已完成")
        }
    }
    
    /**
     * 显示导入备份警告对话框（引导时直接导入）
     */
    fun showImportWarning(inputUri: Uri) {
        // 如果在引导流程中，直接导入，不显示警告
        if (_needsGuide.value == true) {
            importBackup(inputUri)
        } else {
            pendingImportUri = inputUri
            _showImportWarningDialog.value = true
        }
    }
    
    /**
     * 确认导入备份
     */
    fun confirmImportBackup() {
        _showImportWarningDialog.value = false
        pendingImportUri?.let { uri ->
            importBackup(uri)
        }
        pendingImportUri = null
    }
    
    /**
     * 取消导入备份
     */
    fun cancelImportBackup() {
        _showImportWarningDialog.value = false
        pendingImportUri = null
    }
    
    /**
     * 导入备份（内部方法）
     */
    private fun importBackup(inputUri: Uri) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            _isBackupInProgress.value = true
            _backupProgress.value = null
            _backupProgressMessage.value = "正在清除现有数据..."
            _backupImportSuccess.value = false
            
            try {
                _backupProgress.value = 0.3f
                _backupProgressMessage.value = "正在解析备份文件..."
                
                _backupProgress.value = 0.6f
                _backupProgressMessage.value = "正在恢复数据..."
                
                _backupProgress.value = 0.9f
                _backupProgressMessage.value = "正在恢复图片..."
                
                when (val result = backupService.importBackup(inputUri)) {
                    is BackupResult.Success -> {
                        _backupProgress.value = 1.0f
                        _backupProgressMessage.value = "导入完成！"
                        kotlinx.coroutines.delay(500) // 让用户看到完成状态
                        
                        // 导入成功后设置状态
                        _backupImportSuccess.value = true
                        _hasSystem.value = true
                        _needsGuide.value = false
                        
                        // 重置加载状态并重新加载数据
                        membersLoaded = false
                        currentMemberLoaded = false
                        groupsLoaded = false
                        messagesLoaded = false
                        imagesPreloaded = false
                        isLoadingInProgress = false
                        
                        // 清空现有数据
                        _members.value = emptyList()
                        _allMembers.value = emptyList()
                        _currentMember.value = null
                        _groups.value = emptyList()
                        _messages.value = emptyMap()
                        _unreadCounts.value = emptyMap()
                        loadedMessageGroups.clear()
                        
                        // 设置默认当前成员（取第一个成员）
                        setDefaultCurrentMemberAfterImport()
                        
                        // 重新开始加载流程（跳过系统检查）
                        startUnifiedLoading(skipSystemCheck = true)
                        
                        Log.d(TAG, "备份导入成功，重新加载数据")
                    }
                    is BackupResult.Error -> {
                        Log.e(TAG, "导入失败: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "导入失败: ${e.message}", e)
            } finally {
                _isBackupInProgress.value = false
                _backupProgress.value = null
            }
        }
    }
    
    /**
     * 导入备份后设置默认当前成员
     */
    private suspend fun setDefaultCurrentMemberAfterImport() {
        try {
            val allMembers = memberRepository.getAllMembers().first()
            if (allMembers.isNotEmpty()) {
                val firstMember = allMembers.first()
                // 保存为当前成员
                memberPreferences.saveCurrentMemberId(firstMember.id)
                Log.d(TAG, "备份导入后设置默认成员: ${firstMember.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "设置默认成员失败: ${e.message}", e)
        }
    }
    
    // ==================== 首页编辑模式相关方法 ====================
    
    /**
     * 切换首页编辑模式
     */
    fun toggleHomeEditMode() {
        _isHomeEditMode.value = !_isHomeEditMode.value
    }
    
    /**
     * 退出首页编辑模式
     */
    fun exitHomeEditMode() {
        _isHomeEditMode.value = false
    }
    
    /**
     * 保存首页布局配置
     */
    fun saveHomeLayoutConfig(config: HomeLayoutConfig) {
        _homeLayoutConfig.value = config
    }
    
    /**
     * 更新模块顺序
     */
    fun updateModuleOrder(newOrder: List<HomeModuleType>) {
        val currentConfig = _homeLayoutConfig.value
        val newConfig = currentConfig.copy(moduleOrder = newOrder)
        saveHomeLayoutConfig(newConfig)
        Log.d(TAG, "模块顺序已更新: $newOrder")
    }
    
    /**
     * 切换模块显示/隐藏
     */
    fun toggleModuleVisibility(moduleType: HomeModuleType) {
        val currentConfig = _homeLayoutConfig.value
        val newVisibility = currentConfig.moduleVisibility.toMutableMap()
        newVisibility[moduleType] = !(newVisibility[moduleType] ?: true)
        val newConfig = currentConfig.copy(moduleVisibility = newVisibility)
        saveHomeLayoutConfig(newConfig)
    }
    
    /**
     * 更新功能模块配置
     */
    fun updateFunctionModules(newModules: List<FunctionModuleConfig>) {
        val currentConfig = _homeLayoutConfig.value
        val newConfig = currentConfig.copy(functionModules = newModules)
        saveHomeLayoutConfig(newConfig)
    }
    
    /**
     * 切换功能模块的启用状态
     */
    fun toggleFunctionModuleEnabled(moduleId: String) {
        val currentConfig = _homeLayoutConfig.value
        val newModules = currentConfig.functionModules.map { module ->
            if (module.id == moduleId) module.copy(enabled = !module.enabled) else module
        }
        val newConfig = currentConfig.copy(functionModules = newModules)
        saveHomeLayoutConfig(newConfig)
    }
    
    /**
     * 更新待办事项状态（代理方法，供 HomeScreen 使用）
     */
    fun updateTodoStatus(todoId: String, isCompleted: Boolean) {
        // 通过 TodoViewModel 处理，这里留空，由 HomeScreen 直接调用 TodoViewModel
    }
    
    override fun onCleared() {
        super.onCleared()
        // 应用关闭时让当前用户下线
        viewModelScope.launch {
            _currentMember.value?.let { member ->
                onlineStatusRepository.logoutMember(member.id)
                Log.d(TAG, "应用关闭，用户 ${member.name} 已下线")
            }
        }
    }
} 