package com.example.earthquakeapp.ViewModels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.earthquakeapp.API.ApiService
import com.example.earthquakeapp.Data.features
import com.example.earthquakeapp.Repository.QuakeRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class QuakeHistoryViewModel(private val repository: QuakeRepo) : ViewModel() {

    private val _selectedRange = MutableStateFlow(30)
    val selectedRange: StateFlow<Int> = _selectedRange

    private val _quakeHistory = MutableStateFlow<List<features>>(emptyList())
    val quakeHistory: StateFlow<List<features>> = _quakeHistory


    init {
        fetchQuakes()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setDateRange(days: Int) {
        _selectedRange.value = days
        fetchQuakes()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchQuakes() {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(_selectedRange.value.toLong())

        val startTime = startDate.toString()
        val endTime = endDate.toString()

        viewModelScope.launch {
            try {
                val response = repository.getEarthquakes(
                    startTime = startTime,
                    endTime = endTime,
                    minMagnitude = 2.5
                )
                _quakeHistory.value = response.getOrNull()!!.features
            } catch (e: Exception) {
                Log.e("HistoryFetch", "Error: ${e.message}")
            }
        }
    }
}
