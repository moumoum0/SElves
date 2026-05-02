package com.selves.xnn.data.repository

import com.selves.xnn.data.Mappers.toDomain
import com.selves.xnn.data.Mappers.toEntity
import com.selves.xnn.data.dao.MemberGroupDao
import com.selves.xnn.model.MemberGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemberGroupRepository @Inject constructor(
    private val memberGroupDao: MemberGroupDao
) {
    fun getAllGroups(): Flow<List<MemberGroup>> {
        return memberGroupDao.getAllGroups().map { groups ->
            groups.map { it.toDomain() }
        }
    }

    suspend fun upsertGroup(group: MemberGroup) {
        memberGroupDao.upsertGroup(group.toEntity())
    }

    suspend fun ensureGroupsExist(groupNames: List<String>) {
        if (groupNames.isEmpty()) {
            return
        }

        val existingNames = memberGroupDao.getAllGroupsSync().map { it.name }.toSet()
        groupNames
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .filterNot { it in existingNames }
            .forEach { name ->
                memberGroupDao.upsertGroup(MemberGroup(name = name).toEntity())
            }
    }

    suspend fun updateChildrenParent(oldParentName: String, newParentName: String?) {
        memberGroupDao.updateChildrenParent(oldParentName, newParentName)
    }

    suspend fun getDescendantNames(name: String): Set<String> {
        val result = mutableSetOf<String>()
        suspend fun collect(parentName: String) {
            memberGroupDao.getDirectChildrenSync(parentName).forEach { child ->
                result.add(child.name)
                collect(child.name)
            }
        }
        collect(name)
        return result
    }

    suspend fun deleteGroup(name: String) {
        memberGroupDao.deleteGroupByName(name)
    }
}
