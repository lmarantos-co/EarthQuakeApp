package com.example.earthquakeapp.API

import com.example.earthquakeapp.Data.EarthQuakeResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("earthquakes/feed/v1.0/summary/{period}.geojson")
    suspend fun getEarthquakes(@Path("period") period: String): EarthQuakeResponse
}
