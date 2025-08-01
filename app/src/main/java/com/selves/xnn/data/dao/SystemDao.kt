package com.selves.xnn.data.dao

import androidx.room.*
import com.selves.xnn.data.entity.SystemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemDao {
    
    @Query("SELECT * FROM systems ORDER BY createdAt DESC LIMIT 1")
    fun getCurrentSystem(): Flow<SystemEntity?>
    
    @Query("SELECT * FROM systems WHERE id = :systemId")
    suspend fun getSystemById(systemId: String): SystemEntity?
    
    @Query("SELECT COUNT(*) FROM systems")
    suspend fun getSystemCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystem(system: SystemEntity)
    
    @Update
    suspend fun updateSystem(system: SystemEntity)
    
    @Delete
    suspend fun deleteSystem(system: SystemEntity)
    
    @Query("DELETE FROM systems WHERE id = :systemId")
    suspend fun deleteSystemById(systemId: String)

    // 备份用的同步查询方法
    @Query("SELECT * FROM systems ORDER BY createdAt ASC")
    suspend fun getAllSystemsSync(): List<SystemEntity>

    @Query("DELETE FROM systems")
    suspend fun deleteAll()
} 