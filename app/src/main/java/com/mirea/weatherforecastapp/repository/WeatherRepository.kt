package com.mirea.weatherforecastapp.repository

import com.mirea.weatherforecastapp.server.ApiService

class WeatherRepository(private val api:ApiService) {

    fun getCurrentWeather(lat: Double, lon:Double, unit:String)=
        api.getCurrentWeather(lat, lon, unit, "ff4863428e59926c2b6b5f4cd6828dc4")

    fun getFiveDaysForecast(lat: Double, lon:Double, unit:String)=
        api.getFiveDaysForecast(lat, lon, unit, "ff4863428e59926c2b6b5f4cd6828dc4")
}