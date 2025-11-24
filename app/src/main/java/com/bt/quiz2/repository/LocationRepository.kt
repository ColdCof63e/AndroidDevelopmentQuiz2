package com.bt.quiz2.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.collections.toMutableList
import com.bt.quiz2.models.FavouriteLocation


class LocationsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("fav_locations", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val KEY = "saved_places"

    fun getFavourites(): List<FavouriteLocation> {
        val json = prefs.getString(KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<FavouriteLocation>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addFavourite(location: FavouriteLocation) {
        val currentList = getFavourites().toMutableList()
        currentList.add(location)
        saveList(currentList)
    }

    fun deleteFavourite(location: FavouriteLocation) {
        val currentList = getFavourites().toMutableList()
        currentList.removeAll { it.id == location.id }
        saveList(currentList)
    }

    private fun saveList(list: List<FavouriteLocation>) {
        val json = gson.toJson(list)
        prefs.edit().putString(KEY, json).apply()
    }
}