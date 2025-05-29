package com.example.earthquakeapp.Screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.earthquakeapp.Data.features
import com.google.android.gms.common.Feature
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuakesMapScreen(
    quakeList: List<features>,
    navController: NavController
) {
    val context = LocalContext.current
    val mapUiSettings by remember { mutableStateOf(MapUiSettings(zoomControlsEnabled = true)) }
    val mapProperties by remember { mutableStateOf(MapProperties(mapType = MapType.NORMAL)) }

    val latLngList = quakeList.map {
        LatLng(it.geometry.coordinates[1].toDouble(), it.geometry.coordinates[0].toDouble())
    }

    val bounds = remember(latLngList) {
        if (latLngList.isNotEmpty()) {
            val builder = LatLngBounds.builder()
            latLngList.forEach { builder.include(it) }
            builder.build()
        } else {
            LatLngBounds(LatLng(0.0, 0.0), LatLng(0.0, 0.0))
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bounds.center, 3f)
    }

    LaunchedEffect(bounds) {
        if (latLngList.isNotEmpty()) {
            cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }

    BackHandler {
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quake Map Overview") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            properties = mapProperties,
            uiSettings = mapUiSettings,
            cameraPositionState = cameraPositionState
        ) {
            quakeList.forEach { quake ->
                val lat = quake.geometry.coordinates[1]
                val lng = quake.geometry.coordinates[0]
                val place = quake.properties.place
                val magnitude = quake.properties.mag

                Marker(
                    state = MarkerState(position = LatLng(lat.toDouble(), lng.toDouble())),
                    title = "M$magnitude: $place",
                    snippet = "Depth: ${quake.geometry.coordinates.getOrNull(2)} km"
                )
            }
        }
    }
}


