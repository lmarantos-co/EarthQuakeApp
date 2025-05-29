package com.example.earthquakeapp.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.earthquakeapp.Repository.QuakeRepo

class EarthquakeViewModelFactory(
    private val repository: QuakeRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EarthquakeViewModel::class.java)) {
            return EarthquakeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
