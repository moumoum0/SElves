package com.selves.xnn.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selves.xnn.data.repository.MemberDiaryRepository
import com.selves.xnn.model.MemberDiary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val memberDiaryRepository: MemberDiaryRepository
) : ViewModel() {

    private val TAG = "DiaryViewModel"

    private val _currentMemberId = MutableStateFlow<String?>(null)

    val diaries: StateFlow<List<MemberDiary>> = _currentMemberId
        .filterNotNull()
        .flatMapLatest { memberId ->
            memberDiaryRepository.getDiariesByMember(memberId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentDiaries: StateFlow<List<MemberDiary>> = _currentMemberId
        .filterNotNull()
        .flatMapLatest { memberId ->
            memberDiaryRepository.getRecentDiariesByMember(memberId, 2)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCurrentMember(memberId: String) {
        _currentMemberId.value = memberId
    }

    fun createDiary(title: String, content: String) {
        val memberId = _currentMemberId.value ?: return
        val now = System.currentTimeMillis()
        val diary = MemberDiary(
            id = UUID.randomUUID().toString(),
            memberId = memberId,
            title = title.trim(),
            content = content.trim(),
            createdAt = now,
            updatedAt = now
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                memberDiaryRepository.upsertDiary(diary)
                Log.d(TAG, "创建日记成功: ${diary.id}")
            } catch (e: Exception) {
                Log.e(TAG, "创建日记失败: ${e.message}", e)
            }
        }
    }

    fun updateDiary(id: String, title: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val existing = diaries.value.find { it.id == id } ?: return@launch
                val updated = existing.copy(
                    title = title.trim(),
                    content = content.trim(),
                    updatedAt = System.currentTimeMillis()
                )
                memberDiaryRepository.upsertDiary(updated)
                Log.d(TAG, "更新日记成功: $id")
            } catch (e: Exception) {
                Log.e(TAG, "更新日记失败: ${e.message}", e)
            }
        }
    }

    fun deleteDiary(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                memberDiaryRepository.deleteDiary(id)
                Log.d(TAG, "删除日记成功: $id")
            } catch (e: Exception) {
                Log.e(TAG, "删除日记失败: ${e.message}", e)
            }
        }
    }
}
