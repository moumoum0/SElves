package com.selves.xnn.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.selves.xnn.data.entity.MemberGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberGroupDao {
    @Query("SELECT * FROM member_groups ORDER BY name COLLATE NOCASE ASC")
    fun getAllGroups(): Flow<List<MemberGroupEntity>>

    @Query("SELECT * FROM member_groups WHERE name = :name LIMIT 1")
    suspend fun getGroupByName(name: String): MemberGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGroup(group: MemberGroupEntity)

    @Query("SELECT * FROM member_groups ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAllGroupsSync(): List<MemberGroupEntity>

    @Query("SELECT * FROM member_groups WHERE parentName = :parentName ORDER BY name COLLATE NOCASE ASC")
    suspend fun getDirectChildrenSync(parentName: String): List<MemberGroupEntity>

    @Query("UPDATE member_groups SET parentName = :newParentName WHERE parentName = :oldParentName")
    suspend fun updateChildrenParent(oldParentName: String, newParentName: String?)

    @Query("DELETE FROM member_groups WHERE name = :name")
    suspend fun deleteGroupByName(name: String)

    @Query("DELETE FROM member_groups")
    suspend fun deleteAll()
}
