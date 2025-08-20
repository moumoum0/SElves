package com.selves.xnn.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "location_records")
data class LocationRecordEntity(
    @PrimaryKey
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val accuracy: Float? = null,
    val address: String? = null,
    val timestamp: LocalDateTime,
    val memberId: String,
    val note: String? = null
)

