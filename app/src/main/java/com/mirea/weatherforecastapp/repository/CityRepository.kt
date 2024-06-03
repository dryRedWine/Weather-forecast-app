package com.mirea.weatherforecastapp.repository

import com.mirea.weatherforecastapp.server.ApiService

class CityRepository(private val api: ApiService) {

    fun getCitiesList(q: String, limit: Int) =
        api.getCitiesList(q, limit, "ff4863428e59926c2b6b5f4cd6828dc4")
}