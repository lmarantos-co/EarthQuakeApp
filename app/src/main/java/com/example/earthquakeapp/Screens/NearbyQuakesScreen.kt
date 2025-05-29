package com.example.earthquakeapp.Screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.earthquakeapp.Data.features
import com.example.earthquakeapp.ViewModels.EarthquakeViewModel
import com.google.accompanist.permissions.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import kotlin.math.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun NearbyQuakesScreen(
    viewModel: EarthquakeViewModel,
    onQuakeClick: (features) -> Unit,
    onBackClick: () -> Unit // <-- Add this callback for back navigation
) {
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var filteredQuakes by remember { mutableStateOf<List<features>>(emptyList()) }

    val earthquakeList by viewModel.earthquakeList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(locationPermissionState.status.isGranted, earthquakeList) {
        if (locationPermissionState.status.isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLocation = LatLng(location.latitude, location.longitude)
                    filteredQuakes = filterQuakesNearLocation(
                        earthquakeList,
                        location.latitude,
                        location.longitude,
                        100.0 // radius in km
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Earthquakes") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                error != null -> {
                    Text(
                        text = "Error: $error",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                !locationPermissionState.status.isGranted -> {
                    Text("Location permission is needed to find earthquakes near you.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                        Text("Grant Location Permission")
                    }
                }

                userLocation == null -> {
                    Text("Getting your location...", modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                filteredQuakes.isEmpty() -> {
                    Text("No earthquakes nearby in the last period.", modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                else -> {
                    Text(
                        text = "Your Location: ${userLocation!!.latitude}, ${userLocation!!.longitude}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn {
                        items(filteredQuakes) { quake ->
                            EarthquakeItem(quake = quake, onClick = { onQuakeClick(quake) })
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EarthquakeItem(quake: features, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = quake.properties.place,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Magnitude: ${quake.properties.mag}",
            color = when {
                quake.properties.mag >= 6f -> Color.Red
                quake.properties.mag >= 4f -> Color(0xFFFFA500) // Orange
                else -> Color.Green
            },
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(quake.properties.time))}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

fun filterQuakesNearLocation(
    quakes: List<features>,
    userLat: Double,
    userLng: Double,
    radiusKm: Double
): List<features> {
    return quakes.filter { quake ->
        val quakeLat = quake.geometry.coordinates.getOrNull(1)?.toDouble() ?: 0.0
        val quakeLng = quake.geometry.coordinates.getOrNull(0)?.toDouble() ?: 0.0
        val distance = calculateDistance(userLat, userLng, quakeLat, quakeLng)
        distance <= radiusKm
    }
}

fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadius = 6371.0 // km

    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)

    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
}
