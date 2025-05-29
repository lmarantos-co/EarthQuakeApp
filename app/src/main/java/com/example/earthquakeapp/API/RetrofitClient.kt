package com.example.earthquakeapp.API

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://earthquake.usgs.gov/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())  // This allows Retrofit to handle JSON
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}