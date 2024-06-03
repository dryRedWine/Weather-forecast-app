package com.mirea.weatherforecastapp.viewmodel

import androidx.lifecycle.ViewModel
import com.mirea.weatherforecastapp.repository.WeatherRepository
import com.mirea.weatherforecastapp.server.ApiClient
import com.mirea.weatherforecastapp.server.ApiService

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    constructor() : this(
        WeatherRepository(
            (ApiClient().getClient().create(ApiService::class.java))
        )
    )

    fun loadCurrentWeather(lat: Double, lon: Double, unit: String) =
        repository.getCurrentWeather(lat, lon, unit)

    fun loadFiveDaysForecast(lat: Double, lon: Double, unit: String) =
        repository.getFiveDaysForecast(lat, lon, unit)
}