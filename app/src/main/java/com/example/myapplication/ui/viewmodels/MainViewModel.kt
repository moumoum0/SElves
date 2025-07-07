package com.example.myapplication.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.MemberPreferences
import com.example.myapplication.data.repository.ChatGroupRepository
import com.example.myapplication.data.repository.MessageRepository
import com.example.myapplication.data.repository.MemberRepository
import com.example.myapplication.data.repository.MessageReadStatusRepository
import com.example.myapplication.model.ChatGroup
import com.example.myapplication.model.Message
import com.example.myapplication.model.MessageType
import com.example.myapplication.model.Member
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
import com.example.myapplication.util.ImageUtils
import kotlinx.coroutines.CompletableDeferred

/**
 * 应用加载状态枚举
 */
enum class LoadingPhase {
    INITIALIZING,           // 初始化阶段
    LOADING_MEMBERS,        // 加载成员数据
    LOADING_CURRENT_MEMBER, // 加载当前成员
    LOADING_GROUPS,         // 加载群组数据
    LOADING_MESSAGES,       // 加载消息数据
    PRELOADING_IMAGES,      // 预加载图片
    COMPLETED               // 加载完成
}

/**
 * 加载状态数据类
 */
data class LoadingState(
    val isLoading: Boolean = true,
    val currentPhase: LoadingPhase = LoadingPhase.INITIALIZING,
    val progress: Float = 0f,
    val message: String = "正在初始化..."
)

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val memberRepository: MemberRepository,
    private val chatGroupRepository: ChatGroupRepository,
    private val messageRepository: MessageRepository,
    private val messageReadStatusRepository: MessageReadStatusRepository,
    private val memberPreferences: MemberPreferences
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
    
    // 已加载消息的群组ID列表
    private val loadedMessageGroups = mutableSetOf<String>()
    
    // 加载阶段跟踪
    private var membersLoaded = false
    private var currentMemberLoaded = false
    private var groupsLoaded = false
    private var messagesLoaded = false
    private var imagesPreloaded = false
    
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
        startUnifiedLoading()
    }
    
    /**
     * 开始统一的加载流程
     */
    private fun startUnifiedLoading() {
        viewModelScope.launch(exceptionHandler) {
            try {
                updateLoadingState(LoadingPhase.INITIALIZING, 0f, "正在初始化应用...")
                
                // 阶段1：并行加载基础数据
                updateLoadingState(LoadingPhase.LOADING_MEMBERS, 0.1f, "正在加载成员数据...")
                val membersJob = async { loadMembers() }
                
                updateLoadingState(LoadingPhase.LOADING_CURRENT_MEMBER, 0.2f, "正在加载当前成员...")
                val currentMemberJob = async { loadSavedMember() }
                
                // 等待基础数据加载完成
                awaitAll(membersJob, currentMemberJob)
                
                // 阶段2：加载群组数据
                updateLoadingState(LoadingPhase.LOADING_GROUPS, 0.4f, "正在加载群组数据...")
                loadGroups()
                
                // 阶段3：加载消息数据
                updateLoadingState(LoadingPhase.LOADING_MESSAGES, 0.6f, "正在加载消息数据...")
                loadInitialMessages()
                
                // 阶段4：预加载图片
                updateLoadingState(LoadingPhase.PRELOADING_IMAGES, 0.8f, "正在预加载图片...")
                preloadImages()
                
                // 完成加载
                updateLoadingState(LoadingPhase.COMPLETED, 1.0f, "加载完成")
                completeLoading()
                
            } catch (e: Exception) {
                Log.e(TAG, "统一加载流程失败: ${e.message}", e)
                completeLoading()
            }
        }
    }
    

    
    /**
     * 更新加载状态
     */
    private fun updateLoadingState(phase: LoadingPhase, progress: Float, message: String) {
        _loadingState.value = LoadingState(
            isLoading = true,
            currentPhase = phase,
            progress = progress,
            message = message
        )
        Log.d(TAG, "加载状态更新: $phase - $message (${(progress * 100).toInt()}%)")
    }
    
    /**
     * 完成加载
     */
    private fun completeLoading() {
        _loadingState.value = LoadingState(
            isLoading = false,
            currentPhase = LoadingPhase.COMPLETED,
            progress = 1.0f,
            message = "加载完成"
        )
        Log.d(TAG, "应用加载完成")
    }
    

    
    /**
     * 加载初始消息数据
     */
    private suspend fun loadInitialMessages() {
        val groups = _groups.value
        if (groups.isNotEmpty()) {
            // 为每个群组加载消息
            groups.forEach { group ->
                loadGroupMessages(group.id)
                loadUnreadCount(group.id)
            }
        }
        messagesLoaded = true
    }
    
    /**
     * 预加载图片
     */
    private suspend fun preloadImages() {
        val members = _members.value
        val allMembers = _allMembers.value
        
        // 预加载成员头像
        if (members.isNotEmpty()) {
            ImageUtils.preloadAvatarsToMemory(
                context = context,
                avatarPaths = members.mapNotNull { it.avatarUrl },
                coroutineScope = viewModelScope
            )
        }
        
        // 预加载消息中的图片
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
    
    private suspend fun loadMembers() {
        // 创建一个CompletableDeferred来等待数据加载完成
        val membersDeferred = CompletableDeferred<Unit>()
        val allMembersDeferred = CompletableDeferred<Unit>()
        
        // 加载未删除的成员（用于UI显示）
        val membersJob = viewModelScope.launch(exceptionHandler) {
            memberRepository.getAllMembers()
                .collect { memberList ->
                    _members.value = memberList
                    Log.d(TAG, "已加载 ${memberList.size} 个活跃成员")
                    if (!membersLoaded) {
                        membersDeferred.complete(Unit)
                    }
                }
        }
        
        // 加载所有成员包括已删除的（用于消息显示）
        val allMembersJob = viewModelScope.launch(exceptionHandler) {
            memberRepository.getAllMembersIncludingDeleted()
                .collect { memberList ->
                    _allMembers.value = memberList
                    Log.d(TAG, "已加载 ${memberList.size} 个成员（包括已删除成员）")
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
        val groupsDeferred = CompletableDeferred<Unit>()
        
        viewModelScope.launch(exceptionHandler) {
            try {
                // 监听当前成员变化，当成员变化时重新加载群聊
                combine(
                    chatGroupRepository.getAllGroups(),
                    _currentMember
                ) { allGroups, currentMember ->
                    if (currentMember != null) {
                        // 过滤出当前成员所属的群聊
                        allGroups.filter { group ->
                            group.members.any { it.id == currentMember.id }
                        }
                    } else {
                        emptyList()
                    }
                }.collect { groupList ->
                    _groups.value = groupList
                    Log.d(TAG, "已加载 ${groupList.size} 个群聊")
                    if (!groupsLoaded) {
                        groupsLoaded = true
                        groupsDeferred.complete(Unit)
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "加载群聊任务被取消")
                } else {
                    Log.e(TAG, "加载群聊失败: ${e.message}", e)
                }
                if (!groupsLoaded) {
                    groupsLoaded = true
                    groupsDeferred.complete(Unit)
                }
            }
        }
        
        groupsDeferred.await()
    }
    
    private suspend fun loadSavedMember() {
        val memberDeferred = CompletableDeferred<Unit>()
        
        viewModelScope.launch(exceptionHandler) {
            memberPreferences.currentMemberId
                .take(1) // 只取第一个值，这里保持不变因为我们只需要初始值
                .collect { memberId ->
                    if (!memberId.isNullOrEmpty()) {
                        memberRepository.getMemberById(memberId)?.let { member ->
                            _currentMember.value = member
                            Log.d(TAG, "已加载保存的成员: ${member.name}")
                        }
                    }
                    currentMemberLoaded = true
                    memberDeferred.complete(Unit)
                }
        }
        
        memberDeferred.await()
    }
    
    // 创建成员
    fun createMember(name: String, avatarUrl: String?) {
        val memberId = UUID.randomUUID().toString()
        Log.d(TAG, "开始创建成员 - ID: $memberId, 名称: $name")
        
        val member = Member(
            id = memberId,
            name = name,
            avatarUrl = avatarUrl
        )
        
        // 使用单一协程进行所有操作，避免并发问题
        viewModelScope.launch(exceptionHandler) {
            try {
                Log.d(TAG, "正在保存成员到数据库...")
                memberRepository.saveMember(member)
                Log.d(TAG, "成员保存成功，设置为当前成员")
                
                // 保存当前成员ID
                _currentMember.value = member
                memberPreferences.saveCurrentMemberId(member.id)
                Log.d(TAG, "已完成成员创建和设置: ${member.name}")
            } catch (e: Exception) {
                Log.e(TAG, "创建成员失败: ${e.message}", e)
            }
        }
    }
    
    // 设置当前成员
    fun setCurrentMember(member: Member) {
        _currentMember.value = member
        
        // 重新加载所有群组的未读数量
        _groups.value.forEach { group ->
            loadUnreadCount(group.id)
        }
        
        viewModelScope.launch(exceptionHandler) {
            memberPreferences.saveCurrentMemberId(member.id)
            Log.d(TAG, "已保存当前成员ID: ${member.id}, 将重新加载群聊列表")
        }
    }
    
    // 创建群聊
    fun createGroup(name: String, selectedMembers: List<Member>, creator: Member): ChatGroup {
        val groupId = UUID.randomUUID().toString()
        
        // 创建群聊对象，创建者为群主
        val newGroup = ChatGroup(
            id = groupId,
            name = name,
            members = selectedMembers,
            ownerId = creator.id,
            createdAt = System.currentTimeMillis()
        )
        
        // 更新群组列表
        val currentGroups = _groups.value.toMutableList()
        currentGroups.add(newGroup)
        _groups.value = currentGroups
        
        // 保存到数据库
        viewModelScope.launch(exceptionHandler) {
            try {
                chatGroupRepository.saveGroup(newGroup)
                Log.d(TAG, "群聊已保存到数据库: ${newGroup.name}, 群主: ${creator.name}, 成员数: ${selectedMembers.size}")
                
                // 为新群组初始化消息列表和未读数量
                _messages.value = _messages.value.toMutableMap().apply {
                    this[groupId] = emptyList()
                }
                loadedMessageGroups.add(groupId)
                loadUnreadCount(groupId)
            } catch (e: Exception) {
                Log.e(TAG, "保存群聊失败: ${e.message}", e)
            }
        }
        
        Log.d(TAG, "创建群聊: ${newGroup.name}, 群主: ${creator.name}, 成员数: ${selectedMembers.size}")
        
        return newGroup
    }
    
    // 加载群组未读消息数量
    private fun loadUnreadCount(groupId: String) {
        val currentMember = _currentMember.value ?: return
        
        viewModelScope.launch(exceptionHandler) {
            try {
                messageReadStatusRepository.getUnreadMessageCountFlow(groupId, currentMember.id)
                    .collect { count ->
                        val updatedCounts = _unreadCounts.value.toMutableMap()
                        updatedCounts[groupId] = count
                        _unreadCounts.value = updatedCounts
                        Log.d(TAG, "群组 $groupId 未读消息数量: $count")
                    }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "加载群组 $groupId 未读数量任务被取消")
                } else {
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
    
    // 加载群组消息
    private fun loadGroupMessages(groupId: String) {
        if (loadedMessageGroups.contains(groupId)) {
            return // 避免重复加载
        }
        
        // 使用SupervisorJob确保这个协程的失败不影响其他协程
        viewModelScope.launch(SupervisorJob() + exceptionHandler) {
            try {
                // 先初始化空列表，防止UI显示错误
                val currentMessages = _messages.value.toMutableMap()
                if (!currentMessages.containsKey(groupId)) {
                    currentMessages[groupId] = emptyList()
                    _messages.value = currentMessages
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
                            // 从已加载列表中移除，允许重试
                            loadedMessageGroups.remove(groupId)
                        }
                    }
                    .collect { messageList ->
                        val updatedMessages = _messages.value.toMutableMap()
                        updatedMessages[groupId] = messageList
                        _messages.value = updatedMessages
                        Log.d(TAG, "已加载群组 $groupId 的 ${messageList.size} 条消息")
                    }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "加载群组 $groupId 的消息任务被取消")
                } else {
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
        val currentMessages = _messages.value.toMutableMap()
        val groupMessages = currentMessages[groupId]?.toMutableList() ?: mutableListOf()
        groupMessages.add(message)
        currentMessages[groupId] = groupMessages
        _messages.value = currentMessages
        
        // 保存消息到数据库
        viewModelScope.launch(exceptionHandler) {
            try {
                messageRepository.saveMessage(message, groupId)
                Log.d(TAG, "消息已保存到数据库: ${message.id}")
            } catch (e: Exception) {
                Log.e(TAG, "保存消息失败: ${e.message}", e)
            }
        }
        
        Log.d(TAG, "发送消息: ${message.content} 到群组: $groupId")
    }
    
    fun sendImageMessage(groupId: String, imageUri: Uri, caption: String = "") {
        val currentMember = _currentMember.value ?: return
        
        viewModelScope.launch(exceptionHandler) {
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
                    val currentMessages = _messages.value.toMutableMap()
                    val groupMessages = currentMessages[groupId]?.toMutableList() ?: mutableListOf()
                    groupMessages.add(message)
                    currentMessages[groupId] = groupMessages
                    _messages.value = currentMessages
                    
                    // 保存消息到数据库
                    messageRepository.saveMessage(message, groupId)
                    Log.d(TAG, "图片消息已保存到数据库: ${message.id}")
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
        val currentMessages = _messages.value.toMutableMap()
        val groupMessages = currentMessages[groupId]?.toMutableList() ?: return
        
        // 找到要删除的消息
        val messageToDelete = groupMessages.find { it.id == messageId } ?: return
        
        // 从内存中删除
        groupMessages.remove(messageToDelete)
        currentMessages[groupId] = groupMessages
        _messages.value = currentMessages
        
        // 从数据库中删除
        viewModelScope.launch(exceptionHandler) {
            try {
                messageRepository.deleteMessage(messageToDelete, groupId)
                Log.d(TAG, "已从数据库删除消息: $messageId")
            } catch (e: Exception) {
                Log.e(TAG, "删除消息失败: ${e.message}", e)
            }
        }
    }
    
    // 清空群组消息
    fun clearGroupMessages(groupId: String) {
        // 从内存中清空
        val currentMessages = _messages.value.toMutableMap()
        currentMessages[groupId] = emptyList()
        _messages.value = currentMessages
        
        // 从数据库中清空
        viewModelScope.launch(exceptionHandler) {
            try {
                messageRepository.clearGroupMessages(groupId)
                Log.d(TAG, "已清空群组 $groupId 的所有消息")
            } catch (e: Exception) {
                Log.e(TAG, "清空群组消息失败: ${e.message}", e)
            }
        }
    }
    
    // 手动刷新所有群组的消息
    fun refreshAllMessages() {
        viewModelScope.launch(exceptionHandler) {
            try {
                // 获取所有群组ID
                val groupIds = _groups.value.map { it.id }
                
                // 清空已加载标记
                loadedMessageGroups.clear()
                
                // 重新加载所有群组消息
                groupIds.forEach { groupId ->
                    loadGroupMessages(groupId)
                }
                
                Log.d(TAG, "已触发刷新所有群组消息")
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
        
        viewModelScope.launch(exceptionHandler) {
            try {
                // 标记成员为已删除状态，而不是物理删除
                memberRepository.deleteMember(member.id)
                Log.d(TAG, "已标记成员为删除状态: ${member.name}, ID: ${member.id}")
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
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "刚刚" // 1分钟内
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前" // 1小时内
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前" // 24小时内
            else -> {
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = timestamp
                "${calendar.get(java.util.Calendar.MONTH) + 1}/${calendar.get(java.util.Calendar.DAY_OF_MONTH)}"
            }
        }
    }
    
    // 标记群组所有消息为已读
    fun markGroupMessagesAsRead(groupId: String) {
        val currentMember = _currentMember.value ?: return
        
        viewModelScope.launch(exceptionHandler) {
            try {
                messageReadStatusRepository.markAllGroupMessagesAsRead(groupId, currentMember.id)
                Log.d(TAG, "已标记群组 $groupId 的所有消息为已读")
            } catch (e: Exception) {
                Log.e(TAG, "标记群组 $groupId 消息为已读失败: ${e.message}", e)
            }
        }
    }
    
    // 标记单个消息为已读
    fun markMessageAsRead(messageId: String) {
        val currentMember = _currentMember.value ?: return
        
        viewModelScope.launch(exceptionHandler) {
            try {
                messageReadStatusRepository.markMessageAsRead(messageId, currentMember.id)
                Log.d(TAG, "已标记消息 $messageId 为已读")
            } catch (e: Exception) {
                Log.e(TAG, "标记消息 $messageId 为已读失败: ${e.message}", e)
            }
        }
    }
    
    // 群聊管理功能
    
    // 添加成员到群聊
    fun addMembersToGroup(groupId: String, membersToAdd: List<Member>) {
        viewModelScope.launch(exceptionHandler) {
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
                    Log.d(TAG, "已添加 ${membersToAdd.size} 个成员到群聊: ${group.name}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "添加成员到群聊失败: ${e.message}", e)
            }
        }
    }
    
    // 从群聊中移除成员
    fun removeMembersFromGroup(groupId: String, membersToRemove: List<Member>) {
        viewModelScope.launch(exceptionHandler) {
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
                    Log.d(TAG, "已从群聊 ${group.name} 中移除 ${membersToRemove.size} 个成员")
                }
            } catch (e: Exception) {
                Log.e(TAG, "从群聊移除成员失败: ${e.message}", e)
            }
        }
    }
    
    // 更新群聊名称
    fun updateGroupName(groupId: String, newName: String) {
        viewModelScope.launch(exceptionHandler) {
            try {
                // 找到目标群聊
                val currentGroups = _groups.value.toMutableList()
                val groupIndex = currentGroups.indexOfFirst { it.id == groupId }
                
                if (groupIndex != -1) {
                    val group = currentGroups[groupIndex]
                    val updatedGroup = group.copy(name = newName)
                    currentGroups[groupIndex] = updatedGroup
                    _groups.value = currentGroups
                    
                    // 保存到数据库
                    chatGroupRepository.updateGroup(updatedGroup)
                    Log.d(TAG, "已更新群聊名称: ${group.name} -> $newName")
                }
            } catch (e: Exception) {
                Log.e(TAG, "更新群聊名称失败: ${e.message}", e)
            }
        }
    }
    
    // 解散群聊
    fun deleteGroup(groupId: String) {
        viewModelScope.launch(exceptionHandler) {
            try {
                // 从内存中移除群聊
                val currentGroups = _groups.value.toMutableList()
                val groupToRemove = currentGroups.find { it.id == groupId }
                currentGroups.removeAll { it.id == groupId }
                _groups.value = currentGroups
                
                // 清空群聊消息
                val currentMessages = _messages.value.toMutableMap()
                currentMessages.remove(groupId)
                _messages.value = currentMessages
                
                // 清空未读数量
                val currentUnreadCounts = _unreadCounts.value.toMutableMap()
                currentUnreadCounts.remove(groupId)
                _unreadCounts.value = currentUnreadCounts
                
                // 从数据库中删除
                chatGroupRepository.deleteGroupById(groupId)
                messageRepository.clearGroupMessages(groupId)
                
                Log.d(TAG, "已解散群聊: ${groupToRemove?.name}")
            } catch (e: Exception) {
                Log.e(TAG, "解散群聊失败: ${e.message}", e)
            }
        }
    }
} 