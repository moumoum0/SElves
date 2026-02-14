package com.selves.xnn.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.selves.xnn.data.dao.LocationRecordDao
import com.selves.xnn.data.entity.LocationRecordEntity
import com.selves.xnn.model.LocationRecord
import com.selves.xnn.model.TrackingStats
import java.time.LocalDateTime
import java.util.UUID

@Singleton
class LocationRecordRepository @Inject constructor(
    private val locationRecordDao: LocationRecordDao
) {
    
    // 获取所有位置记录
    fun getAllLocationRecords(): Flow<List<LocationRecord>> {
        return locationRecordDao.getAllLocationRecords().map { entities ->
            entities.map { it.toLocationRecord() }
        }
    }
    
    // 获取指定成员的位置记录
    fun getLocationRecordsByMember(memberId: String): Flow<List<LocationRecord>> {
        return locationRecordDao.getLocationRecordsByMember(memberId).map { entities ->
            entities.map { it.toLocationRecord() }
        }
    }
    
    // 获取指定日期的位置记录
    fun getLocationRecordsByDate(date: LocalDateTime, memberId: String): Flow<List<LocationRecord>> {
        return locationRecordDao.getLocationRecordsByDate(date, memberId).map { entities ->
            entities.map { it.toLocationRecord() }
        }
    }
    
    // 获取日期范围内的位置记录（所有成员共享，按时间正序）
    fun getLocationRecordsByDateRange(
        startDate: LocalDateTime, 
        endDate: LocalDateTime
    ): Flow<List<LocationRecord>> {
        return locationRecordDao.getLocationRecordsByDateRangeAllMembers(startDate, endDate).map { entities ->
            entities.map { it.toLocationRecord() }
        }
    }
    
    // 获取日期范围内的位置记录（所有成员共享，按时间倒序）
    fun getLocationRecordsByDateRangeDesc(
        startDate: LocalDateTime, 
        endDate: LocalDateTime
    ): Flow<List<LocationRecord>> {
        return locationRecordDao.getLocationRecordsByDateRangeAllMembersDesc(startDate, endDate).map { entities ->
            entities.map { it.toLocationRecord() }
        }
    }
    
    // 根据ID获取位置记录
    suspend fun getLocationRecordById(id: String): LocationRecord? {
        return locationRecordDao.getLocationRecordById(id)?.toLocationRecord()
    }
    
    // 获取轨迹统计信息
    suspend fun getTrackingStats(memberId: String): TrackingStats {
        val totalRecords = locationRecordDao.getTotalRecordsCount(memberId)
        val todayRecords = locationRecordDao.getTodayRecordsCount(memberId)
        val lastRecordTime = locationRecordDao.getLastRecordTime(memberId)
        
        return TrackingStats(
            totalRecords = totalRecords,
            todayRecords = todayRecords,
            lastRecordTime = lastRecordTime
        )
    }
    
    // 添加位置记录
    suspend fun addLocationRecord(locationRecord: LocationRecord): Long {
        return locationRecordDao.insertLocationRecord(locationRecord.toEntity())
    }
    
    // 添加新的位置记录（简化版本）
    suspend fun addLocationRecord(
        latitude: Double,
        longitude: Double,
        altitude: Double? = null,
        accuracy: Float? = null,
        address: String? = null,
        memberId: String,
        note: String? = null
    ): Long {
        val record = LocationRecordEntity(
            id = UUID.randomUUID().toString(),
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            accuracy = accuracy,
            address = address,
            timestamp = LocalDateTime.now(),
            memberId = memberId,
            note = note
        )
        return locationRecordDao.insertLocationRecord(record)
    }
    
    // 批量添加位置记录
    suspend fun addLocationRecords(locationRecords: List<LocationRecord>) {
        val entities = locationRecords.map { it.toEntity() }
        locationRecordDao.insertLocationRecords(entities)
    }
    
    // 更新位置记录
    suspend fun updateLocationRecord(locationRecord: LocationRecord) {
        locationRecordDao.updateLocationRecord(locationRecord.toEntity())
    }
    
    // 删除位置记录
    suspend fun deleteLocationRecord(locationRecord: LocationRecord) {
        locationRecordDao.deleteLocationRecord(locationRecord.toEntity())
    }
    
    // 根据ID删除位置记录
    suspend fun deleteLocationRecordById(id: String) {
        locationRecordDao.deleteLocationRecordById(id)
    }
    
    // 删除指定成员的所有位置记录
    suspend fun deleteAllLocationRecordsByMember(memberId: String) {
        locationRecordDao.deleteAllLocationRecordsByMember(memberId)
    }
    
    // 清理旧的位置记录
    suspend fun cleanOldLocationRecords(cutoffDate: LocalDateTime) {
        locationRecordDao.deleteOldLocationRecords(cutoffDate)
    }
}

// 扩展函数：Entity转Model
private fun LocationRecordEntity.toLocationRecord(): LocationRecord {
    return LocationRecord(
        id = id,
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        accuracy = accuracy,
        address = address,
        timestamp = timestamp,
        memberId = memberId,
        note = note
    )
}

// 扩展函数：Model转Entity
private fun LocationRecord.toEntity(): LocationRecordEntity {
    return LocationRecordEntity(
        id = id,
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        accuracy = accuracy,
        address = address,
        timestamp = timestamp,
        memberId = memberId,
        note = note
    )
}

