package com.team1.bohemian

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

data class MapItemData(
    val id: String?,
    var title: String?,
    var tags: MutableList<String>?,
    val images: MutableList<String>?
)

data class ReviewData(
    val id: String?,
    val uid: String?,
    var title: String?,
    var contents: String?,
    var tags: MutableList<String>?,
)

@Entity(tableName = "location_data")
data class CurrentLocationData(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val country: String,
    val city: String
)