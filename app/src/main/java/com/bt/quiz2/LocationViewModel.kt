package com.bt.quiz2

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.bt.quiz2.models.FavouriteLocation
import com.bt.quiz2.repository.LocationsRepository

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LocationsRepository(application)

    private val _favouriteLocations = mutableStateOf<List<FavouriteLocation>>(emptyList())
    val favouriteLocations: State<List<FavouriteLocation>> = _favouriteLocations
    private val _userLocation = mutableStateOf<LocationData?>(null)
    val userLocation: State<LocationData?> = _userLocation

    init {
        loadFavourites()
    }

    private fun loadFavourites() {
        _favouriteLocations.value = repository.getFavourites()
    }

    fun addOrUpdateFavourite(id: String?, title: String, desc: String, rating: Float, location: LocationData) {
        val newFav = FavouriteLocation(
            id = id ?: java.util.UUID.randomUUID().toString(),
            title = title,
            description = desc,
            rating = rating,
            latitude = location.latitude,
            longitude = location.longitude
        )
        repository.addOrUpdateFavourite(newFav)
        loadFavourites()
    }

    fun deleteFavourite(favLocation: FavouriteLocation) {
        repository.deleteFavourite(favLocation)
        loadFavourites()
    }

    fun updateLocation(newLocation: LocationData) {
        _userLocation.value = newLocation
    }
}