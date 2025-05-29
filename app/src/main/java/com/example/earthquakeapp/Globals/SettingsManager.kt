package com.example.earthquakeapp.Globals

import com.example.earthquakeapp.QuakePeriod

object SettingsManager {
    var pollingInterval: Long = 15L // Default 15 minutes
    var pollingMagnitude: Float = 4.0f
    var nearbyQuakesRadius : Int = 100
    var nearbyQuakesMinMag : Float = 4.0f
    var nearbyQuakesPeriod : QuakePeriod = QuakePeriod.ALL_MONTH
}