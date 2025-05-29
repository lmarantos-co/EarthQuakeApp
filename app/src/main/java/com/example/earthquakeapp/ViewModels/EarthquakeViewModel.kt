package com.example.earthquakeapp.ViewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.earthquakeapp.Data.features
import com.example.earthquakeapp.Data.properties
import com.example.earthquakeapp.QuakePeriod
import com.example.earthquakeapp.Repository.QuakeRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EarthquakeViewModel(private val repository: QuakeRepo) : ViewModel() {

    private val _earthquakeList = MutableStateFlow<List<features>>(emptyList())
    val earthquakeList: StateFlow<List<features>> = _earthquakeList.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()



    // Filters
    var minMagnitude by mutableStateOf<Float?>(null)
    var locationFilter by mutableStateOf<String?>(null)
    var periodFilter by mutableStateOf<QuakePeriod>(QuakePeriod.ALL_DAY)

    //polling variables
    var polling_interval by mutableStateOf<Int?>(null)
    var polling_magintude by mutableStateOf<Float?>(null)



    fun loadEarthquakes(period: String = "all_hour") {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.fetchEarthquakes(period)
            _isLoading.value = false

            result.onSuccess { response ->
                val filtered = response.features.filter { feature ->
                    val magOk = minMagnitude?.let {
                        (feature.properties.mag >= it)
                                || minMagnitude == null
                    } ?: true
                    val locationOk = locationFilter?.let { (feature.properties.place.contains(it, ignoreCase = true) || locationFilter == null) } ?: true
                    magOk as Boolean && locationOk as Boolean
                }
                    _earthquakeList.value = filtered
                    _error.value = null
            }.onFailure { exception ->
                Log.d("API","Api error:${exception.localizedMessage!!}")
                _error.value = result.exceptionOrNull()?.localizedMessage ?: "Unknown error"
            }
        }
    }

    fun updateMinMagnitude(value: Float?) {
        minMagnitude = value
        loadEarthquakes()
    }

    fun updateLocationFilter(value: String?) {
        locationFilter = value
        loadEarthquakes()
    }
}
