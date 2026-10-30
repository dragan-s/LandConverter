package com.djes021.landconverter.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "land_units",
    indices = [
        Index(
            value = ["countryCode", "unitId"],
            unique = true
        )
    ]
)
data class LandUnitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val countryCode: String, // e.g. "rs", "ba", "me", "mk", "hr"
    val unitId: String,      // e.g. "sqm", "ar", "ha", "dulum", "motika", etc.
    val displayName: String,
    val valueInSqMeters: Double,
    val isSystem: Boolean    // true for m2, ar, ha (fixed, read-only)
)
