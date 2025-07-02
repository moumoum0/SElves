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
import com.example.myapplication.model.Member
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CoroutineExceptionHandler
import java.util.*
import javax.inject.Inject
import android.content.Context
import com.example.myapplication.util.ImageUtils

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
    
    // 加载状态
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 消息管理
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()
    
    // 未读消息数量管理
    private val _unreadCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadCounts: StateFlow<Map<String, Int>> = _unreadCounts.asStateFlow()
    
    // 已加载消息的群组ID列表
    private val loadedMessageGroups = mutableSetOf<String>()
    
    // 异常处理器
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable is kotlinx.coroutines.CancellationException) {
            Log.d(TAG, "协程被取消: ${throwable.message}")
        } else {
            Log.e(TAG, "协程异常: ${throwable.message}", throwable)
        }
    }
    
    init {
        loadMembers()
        loadSavedMember()
        loadGroups()
    }
    
    private fun loadMembers() {
        // 加载未删除的成员（用于UI显示）
        viewModelScope.launch(exceptionHandler) {
            memberRepository.getAllMembers()
                .collect { memberList ->
                    _members.value = memberList
                    Log.d(TAG, "已加载 ${memberList.size} 个活跃成员")
                    
                    // 立即预加载头像到内存缓存
                    if (memberList.isNotEmpty()) {
                        ImageUtils.preloadAvatarsToMemory(
                            context = context,
                            avatarPaths = memberList.mapNotNull { it.avatarUrl },
                            coroutineScope = viewModelScope
                        )
                    }
                }
        }
        
        // 加载所有成员包括已删除的（用于消息显示）
        viewModelScope.launch(exceptionHandler) {
            memberRepository.getAllMembersIncludingDeleted()
                .collect { memberList ->
                    _allMembers.value = memberList
                    Log.d(TAG, "已加载 ${memberList.size} 个成员（包括已删除成员）")
                }
        }
    }
    
    private fun loadGroups() {
        viewModelScope.launch(exceptionHandler) {
            try {
                chatGroupRepository.getAllGroups()
                    .collect { groupList ->
                        _groups.value = groupList
                        Log.d(TAG, "已加载 ${groupList.size} 个群聊")
                        
                        // 加载所有群组的消息和未读数量
                        groupList.forEach { group ->
                            loadGroupMessages(group.id)
                            loadUnreadCount(group.id)
                        }
                    }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "加载群聊任务被取消")
                } else {
                    Log.e(TAG, "加载群聊失败: ${e.message}", e)
                }
                // 确保即使加载失败也不会阻塞UI
                _isLoading.value = false
            }
        }
    }
    
    private fun loadSavedMember() {
        viewModelScope.launch(exceptionHandler) {
            memberPreferences.currentMemberId.collect { memberId ->
                if (!memberId.isNullOrEmpty()) {
                    memberRepository.getMemberById(memberId)?.let { member ->
                        _currentMember.value = member
                        Log.d(TAG, "已加载保存的成员: ${member.name}")
                    }
                }
                _isLoading.value = false
            }
        }
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
            Log.d(TAG, "已保存当前成员ID: ${member.id}")
        }
    }
    
    // 创建群聊
    fun createGroup(name: String, creator: Member): ChatGroup {
        val groupId = UUID.randomUUID().toString()
        
        // 创建群聊对象
        val newGroup = ChatGroup(
            id = groupId,
            name = name,
            members = listOf(creator),
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
                Log.d(TAG, "群聊已保存到数据库: ${newGroup.name}")
                
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
        
        Log.d(TAG, "创建群聊: ${newGroup.name}")
        
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
            content = content
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
} 