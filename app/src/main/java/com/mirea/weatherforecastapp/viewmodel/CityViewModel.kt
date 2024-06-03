package com.mirea.weatherforecastapp.viewmodel

import androidx.lifecycle.ViewModel
import com.mirea.weatherforecastapp.repository.CityRepository
import com.mirea.weatherforecastapp.repository.WeatherRepository
import com.mirea.weatherforecastapp.server.ApiClient
import com.mirea.weatherforecastapp.server.ApiService

class CityViewModel(private val repository: CityRepository) : ViewModel() {

    constructor() : this(CityRepository(ApiClient().getClient().create(ApiService::class.java)))

    fun loadCitiesList(q: String, limit: Int) =
        repository.getCitiesList(q, limit)

}