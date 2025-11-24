package com.bt.quiz2.models

import com.bt.quiz2.LocationData

data class FavouriteLocation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val rating: Float,
    val latitude: Double,
    val longitude: Double
) {
    fun toLocationData(): LocationData = LocationData(latitude, longitude)
}