package com.example.earthquakeapp.Screens

import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.Marker

import com.google.maps.android.compose.rememberMarkerState

import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.earthquakeapp.Data.features
import com.example.earthquakeapp.Data.properties
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EarthquakeMapScreen(
    quakeProps: features,
    navController: NavController
) {
    var showDetailsDialog by remember { mutableStateOf(false) }

    val epicenter = LatLng(
        quakeProps.geometry.coordinates[1].toDouble(),
        quakeProps.geometry.coordinates[0].toDouble()
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(epicenter, 6f)
    }

    val markerState = MarkerState(position = epicenter)

    LaunchedEffect(epicenter) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(epicenter, 6f))
    }

    BackHandler {
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Epicenter: ${quakeProps.properties.place}",
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDetailsDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Earthquake Info")
                    }
                }
            )
        }
    ) { padding ->
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = markerState,
                title = quakeProps.properties.place,
                snippet = "Magnitude: ${quakeProps.properties.mag}"
            )
        }

        if (showDetailsDialog) {
            AlertDialog(
                onDismissRequest = { showDetailsDialog = false },
                title = { Text("Earthquake Details") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Place: ${quakeProps.properties.place}")
                        Text("Magnitude: ${quakeProps.properties.mag}")
                        Text("Alert Level: ${quakeProps.properties.alertLevel ?: "N/A"}")
                        Text("Felt Reports: ${quakeProps.properties.feltReports ?: "N/A"}")
                        Text("CDI: ${quakeProps.properties.cdi ?: "N/A"}")
                        Text("MMI: ${quakeProps.properties.mmi ?: "N/A"}")
                        Text("Tsunami: ${if (quakeProps.properties.tsuname == 1) "Yes" else "No"}")
                        Text("Significance: ${quakeProps.properties.sig}")
                        Text("Status: ${quakeProps.properties.status}")
                        Text("Time: ${
                            Instant.ofEpochMilli(quakeProps.properties.time)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                        }")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDetailsDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}


