package com.example.earthquakeapp.Repository

import com.example.earthquakeapp.API.ApiService
import com.example.earthquakeapp.API.RetrofitClient
import com.example.earthquakeapp.Data.EarthQuakeResponse

class QuakeRepo {

    suspend fun fetchEarthquakes(period: String): Result<EarthQuakeResponse> {
        return try {
            val response = RetrofitClient.apiService.getEarthquakes(period)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEarthquakes(
        startTime: String,
        endTime: String,
        minMagnitude: Double = 2.5
    ): Result<EarthQuakeResponse> {
        return try {
            val response = RetrofitClient.apiService.getEarthquakes(
                startTime = startTime,
                endTime = endTime,
                minMagnitude = minMagnitude
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}