package com.selves.xnn.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.selves.xnn.data.entity.LocationRecordEntity
import java.time.LocalDateTime

@Dao
interface LocationRecordDao {
    
    @Query("SELECT * FROM location_records ORDER BY timestamp DESC")
    fun getAllLocationRecords(): Flow<List<LocationRecordEntity>>
    
    @Query("SELECT * FROM location_records WHERE memberId = :memberId ORDER BY timestamp DESC")
    fun getLocationRecordsByMember(memberId: String): Flow<List<LocationRecordEntity>>
    
    @Query("SELECT * FROM location_records WHERE DATE(timestamp) = DATE(:date) AND memberId = :memberId ORDER BY timestamp ASC")
    fun getLocationRecordsByDate(date: LocalDateTime, memberId: String): Flow<List<LocationRecordEntity>>
    
    @Query("SELECT * FROM location_records WHERE timestamp BETWEEN :startDate AND :endDate AND memberId = :memberId ORDER BY timestamp ASC")
    fun getLocationRecordsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime, memberId: String): Flow<List<LocationRecordEntity>>
    
    @Query("SELECT * FROM location_records WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp ASC")
    fun getLocationRecordsByDateRangeAllMembers(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<LocationRecordEntity>>
    
    @Query("SELECT * FROM location_records WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getLocationRecordsByDateRangeAllMembersDesc(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<LocationRecordEntity>>
    
    @Query("SELECT * FROM location_records WHERE id = :id")
    suspend fun getLocationRecordById(id: String): LocationRecordEntity?
    
    @Query("SELECT COUNT(*) FROM location_records WHERE memberId = :memberId")
    suspend fun getTotalRecordsCount(memberId: String): Int
    
    @Query("SELECT COUNT(*) FROM location_records WHERE DATE(timestamp) = DATE('now') AND memberId = :memberId")
    suspend fun getTodayRecordsCount(memberId: String): Int
    
    @Query("SELECT MAX(timestamp) FROM location_records WHERE memberId = :memberId")
    suspend fun getLastRecordTime(memberId: String): LocalDateTime?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationRecord(record: LocationRecordEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationRecords(records: List<LocationRecordEntity>)
    
    @Update
    suspend fun updateLocationRecord(record: LocationRecordEntity)
    
    @Delete
    suspend fun deleteLocationRecord(record: LocationRecordEntity)
    
    @Query("DELETE FROM location_records WHERE id = :id")
    suspend fun deleteLocationRecordById(id: String)
    
    @Query("DELETE FROM location_records WHERE memberId = :memberId")
    suspend fun deleteAllLocationRecordsByMember(memberId: String)
    
    @Query("DELETE FROM location_records WHERE timestamp < :cutoffDate")
    suspend fun deleteOldLocationRecords(cutoffDate: LocalDateTime)
}

