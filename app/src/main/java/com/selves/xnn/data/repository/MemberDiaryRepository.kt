package com.selves.xnn.data.repository

import com.selves.xnn.data.Mappers.toDomain
import com.selves.xnn.data.Mappers.toEntity
import com.selves.xnn.data.dao.MemberDiaryDao
import com.selves.xnn.model.MemberDiary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemberDiaryRepository @Inject constructor(
    private val memberDiaryDao: MemberDiaryDao
) {
    fun getDiariesByMember(memberId: String): Flow<List<MemberDiary>> {
        return memberDiaryDao.getDiariesByMember(memberId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getRecentDiariesByMember(memberId: String, limit: Int = 2): Flow<List<MemberDiary>> {
        return memberDiaryDao.getRecentDiariesByMember(memberId, limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun upsertDiary(diary: MemberDiary) {
        memberDiaryDao.upsertDiary(diary.toEntity())
    }

    suspend fun deleteDiary(id: String) {
        memberDiaryDao.deleteDiaryById(id)
    }
}
