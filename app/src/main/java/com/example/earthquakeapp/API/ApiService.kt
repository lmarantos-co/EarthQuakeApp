package com.example.earthquakeapp.API

import com.example.earthquakeapp.Data.EarthQuakeResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("earthquakes/feed/v1.0/summary/{period}.geojson")
    suspend fun getEarthquakes(@Path("period") period: String): EarthQuakeResponse

    @GET("fdsnws/event/1/query")
    suspend fun getEarthquakes(
        @Query("format") format: String = "geojson",
        @Query("starttime") startTime: String,
        @Query("endtime") endTime: String,
        @Query("minmagnitude") minMagnitude: Double
    ): EarthQuakeResponse
}
