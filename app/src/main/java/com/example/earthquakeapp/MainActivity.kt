package com.example.earthquakeapp

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings.Global
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.earthquakeapp.Data.features
import com.example.earthquakeapp.Data.properties
import com.example.earthquakeapp.Globals.SettingsManager
import com.example.earthquakeapp.Globals.global_variables
import com.example.earthquakeapp.Repository.QuakeRepo
import com.example.earthquakeapp.Screens.EarthquakeMapScreen
import com.example.earthquakeapp.Screens.NearbyQuakesScreen
import com.example.earthquakeapp.Screens.QuakeHistoryScreen
import com.example.earthquakeapp.Screens.QuakesMapScreen
import com.example.earthquakeapp.ViewModels.EarthquakeViewModel
import com.example.earthquakeapp.ViewModels.QuakeHistoryViewModel
import com.example.earthquakeapp.Worker.QuakeWorker
import com.example.earthquakeapp.ui.theme.EarthQuakeAppTheme
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = QuakeRepo()
        val viewModel = EarthquakeViewModel(repository)
        val historyViewModel = QuakeHistoryViewModel(repository)


        setContent {
            EarthQuakeAppTheme {



                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val navController = rememberNavController()
                val quakes by viewModel.earthquakeList.collectAsState()

                val pollingIntervalState = remember { mutableStateOf(SettingsManager.pollingInterval) }
                val minMagnitudeState = remember { mutableStateOf(SettingsManager.pollingMagnitude) }
                val nearbyRadiusIntervalState = remember { mutableStateOf(SettingsManager.nearbyQuakesRadius.toLong()) }
                val nearbyMinMagnitudeState = remember { mutableStateOf(SettingsManager.nearbyQuakesMinMag) }
                val nearbyQuakesPeriodState = remember { mutableStateOf(SettingsManager.nearbyQuakesPeriod) }
                val quakeAlert = remember {
                    mutableStateOf<Boolean>(true)
                }
                val quakeAlertMag = remember {
                    mutableStateOf<Float>(SettingsManager.pollingMagnitude)
                }

                val quakeAlertPeriod = remember {
                    mutableStateOf<Long>(SettingsManager.pollingInterval)
                }
                val interval = SettingsManager.pollingInterval.coerceAtLeast(15L)
                LaunchedEffect(
                    SettingsManager.quakeAlert,
                    quakeAlertMag.value,
                    quakeAlertPeriod.value
                ) {
                    if (SettingsManager.quakeAlert) {
                        scheduleQuakeWork(applicationContext)
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        DrawerContent(
                            pollingInterval = pollingIntervalState.value.toString(),
                            onPollingIntervalChange = {
                                it.toLongOrNull()?.let { value ->
                                    pollingIntervalState.value = value
                                    SettingsManager.pollingInterval = value
                                }
                            },
                            minMagnitude = minMagnitudeState.value.toString(),
                            onMinMagnitudeChange = {
                                it.toFloatOrNull()?.let { value ->
                                    minMagnitudeState.value = value
                                    SettingsManager.pollingMagnitude = value
                                }
                            },
                            nearbyQuakesRadius = nearbyRadiusIntervalState.value.toString(),
                            onNearbyQuakesRadiusChange = {
                                it.toLongOrNull()?.let { value ->
                                    nearbyRadiusIntervalState.value = value
                                    SettingsManager.nearbyQuakesRadius = value.toInt()
                                }
                            },
                            nearbyQuakesMinMag = nearbyMinMagnitudeState.value.toString(),
                            onNearbyQuakesMinMagChange = {
                                it.toFloatOrNull()?.let { value ->
                                    nearbyMinMagnitudeState.value = value
                                    SettingsManager.nearbyQuakesMinMag = value
                                }
                            },
                            nearbyQuakesPeriod = nearbyQuakesPeriodState.value,
                            onNearbyQuakesPeriodChange = {
                                nearbyQuakesPeriodState.value = it
                                SettingsManager.nearbyQuakesPeriod = it
                            },
                            quakeAlert
                        )
                    }
                )
                 {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Earthquake Tracker")
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.seismology),
                                            contentDescription = "Earthquake Image",
                                            tint = Color.Unspecified
                                        )
                                    }
                                },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        scope.launch { drawerState.open() }
                                    }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                                    }
                                },
                                actions = {
                                    IconButton(onClick = {
                                        scope.launch { navController.navigate("nearbyQuakes") }
                                    }) {
                                        Icon(Icons.Default.LocationOn, contentDescription = "Nearby Quakes")
                                    }

                                    IconButton(onClick = {
                                        scope.launch { navController.navigate("map_screen") }
                                    }) {
                                        Icon(Icons.Filled.AddCircle, contentDescription = "Quake Map Markers")
                                    }

                                    IconButton(onClick = {
                                        scope.launch { navController.navigate("history") }
                                    }) {
                                        Icon(Icons.Filled.DateRange, contentDescription = "History Quakes")
                                    }
                                }
                            )
                        }
                    ) { paddingValues ->
                        if (quakeAlert.value)
                        {
                            scheduleQuakeWork(applicationContext)
                        }
                        NavHost(
                            navController = navController,
                            startDestination = "main",
                            modifier = Modifier.padding(paddingValues)
                        ) {
                            composable("main") {
                                EarthquakeScreen(viewModel, navController)
                            }
                            composable("map") { backStackEntry ->
                                val props = navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.get<features>("quakeProps")

                                if (props != null) {
                                    EarthquakeMapScreen(
                                        quakeProps = props,
                                        navController = navController
                                    )
                                } else {
                                    Text("Earthquake details not available.")
                                }
                            }
                            composable("nearbyQuakes") {
                                NearbyQuakesScreen(viewModel , onQuakeClick =  { clickedQuake ->
                                    navController.currentBackStackEntry?.savedStateHandle?.set("quakeProps", clickedQuake)
                                    navController.navigate("map") },
                                    onBackClick = { navController.popBackStack() })
                            }
                            composable("map_screen") {
                                QuakesMapScreen(quakeList = quakes, navController) // pass the list from your ViewModel
                            }
                            composable("history") {
                                QuakeHistoryScreen(historyViewModel)
                            }
                            }
                        }
                    }
                }
            }
        }
    }

//drawer screen
@Composable
fun DrawerContent(
    pollingInterval: String,
    onPollingIntervalChange: (String) -> Unit,
    minMagnitude: String,
    onMinMagnitudeChange: (String) -> Unit,
    nearbyQuakesRadius: String,
    onNearbyQuakesRadiusChange: (String) -> Unit,
    nearbyQuakesMinMag: String,
    onNearbyQuakesMinMagChange: (String) -> Unit,
    nearbyQuakesPeriod: QuakePeriod,
    onNearbyQuakesPeriodChange: (QuakePeriod) -> Unit,
    quakeAlert : MutableState<Boolean>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(color = Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Application Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = pollingInterval,
            onValueChange = onPollingIntervalChange,
            label = { Text("Polling Interval (min)") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = minMagnitude,
            onValueChange = onMinMagnitudeChange,
            label = { Text("Minimum Magnitude") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nearbyQuakesRadius,
            onValueChange = onNearbyQuakesRadiusChange,
            label = { Text("Nearby Quakes Radius (km)") }, // <-- Fixed label
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nearbyQuakesMinMag,
            onValueChange = onNearbyQuakesMinMagChange,
            label = { Text("Minimum Nearby Quake Magnitude") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Time Period For Nearby Quakes", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
        QuakePeriodDropdown(
            selectedPeriod = nearbyQuakesPeriod,
            onPeriodSelected = onNearbyQuakesPeriodChange
        )
        Row(horizontalArrangement = Arrangement.Center)
        {
            Text(text = "Enable Quake Alerts", fontSize = 18.sp)
            Checkbox(checked = (SettingsManager.quakeAlert == true),
                onCheckedChange = {
                    SettingsManager.quakeAlert = it
                    quakeAlert.value = it
                })
        }
    }
}




enum class QuakePeriod(val period: String) {
    ALL_DAY("all_day"),
    ALL_WEEK("all_week"),
    ALL_MONTH("all_month");

    companion object {
        fun fromIndex(index: Int): QuakePeriod {
            return values().getOrElse(index) { ALL_DAY }
        }

        fun toIndex(period: QuakePeriod): Int {
            return values().indexOf(period)
        }
    }
}

@Composable
fun EarthquakeScreen(viewModel: EarthquakeViewModel , navController: NavController) {
    val earthquakes by viewModel.earthquakeList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()


    var magnitudeInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }
    var quakePeriod by remember {
        mutableStateOf<QuakePeriod>(QuakePeriod.ALL_DAY)
    }

    // Pagination
    var currentPage by remember { mutableStateOf(0) }
    val itemsPerPage = 10
    val totalPages = (earthquakes.size + itemsPerPage - 1) / itemsPerPage

    LaunchedEffect(Unit) {
        viewModel.loadEarthquakes("all_day") // or "all_week"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
            .padding(24.dp)
    )
    {
        Spacer(modifier = Modifier.height(10.dp))
        Text("Filter Earthquakes", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically)
        {
            Text("Min Magnitude: ${magnitudeInput.toFloatOrNull() ?: 0f}"
            , modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp).weight(1f))
            Icon(imageVector = ImageVector.vectorResource(R.drawable.magnitude),
                contentDescription = "magnitude icon" ,
                tint = Color.Unspecified)

        }
        Slider(
            value = magnitudeInput.toFloatOrNull() ?: 0f,
            onValueChange = {
                magnitudeInput = it.toString()
                viewModel.updateMinMagnitude(it)
            },
            valueRange = 0f..10f,
            steps = 0,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically)
        {
            TextField(
                value = locationInput,
                onValueChange = {
                    locationInput = it
                    viewModel.updateLocationFilter(it) // ✅ Correct viewModel call
                    currentPage = 0
                },
                label = { Text("Input Location") },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color.Blue,
                    unfocusedIndicatorColor = Color.Gray,
                    textColor = Color.Black,
                    cursorColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth().weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text) // ✅ TEXT not Number
            )
            Spacer(modifier = Modifier.weight(1f).width(10.dp))
            Icon(imageVector = ImageVector.vectorResource(R.drawable.location) ,
                contentDescription = "location",
                tint = Color.Unspecified)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically)
        {
            QuakePeriodDropdown(
                selectedPeriod = quakePeriod,
                onPeriodSelected = {
                    quakePeriod = it
                    viewModel.loadEarthquakes(it.period)
                }
            )
            Spacer(modifier = Modifier.weight(1f).width(10.dp))
            Icon(imageVector = ImageVector.vectorResource(R.drawable.period) ,
                contentDescription = "period",
                tint = Color.Unspecified)        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (error != null) {
            Text("Error: $error", color = Color.Red)
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                val pageItems = earthquakes.drop(currentPage * itemsPerPage).take(itemsPerPage)
                items(pageItems.size) { quake ->
                    EarthquakeItem(pageItems[quake], navController)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { if (currentPage > 0) currentPage-- },
                    enabled = currentPage > 0
                ) {
                    Text("Previous")
                }
                Text(
                    "Page ${currentPage + 1} of $totalPages",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Button(
                    onClick = { if (currentPage < totalPages - 1) currentPage++ },
                    enabled = currentPage < totalPages - 1
                ) {
                    Text("Next")
                }
            }
        }
    }
}

@Composable
fun EarthquakeItem(features: features, navController: NavController) {
    val Orange = Color(0xFFFF9800) // Material Design Orange 500
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                color = when (features.properties.alertLevel) {
                    "green" -> Color.Green
                    "yellow" -> Color.Yellow
                    "orange" -> Orange  // Make sure you’ve defined `Orange = Color(0xFFFF9800)`
                    null -> Color.Gray  // fallback/default color
                    else -> Color.Red
                }
            )
            .clickable {
                navController.currentBackStackEntry?.savedStateHandle?.set("quakeProps", features)
                navController.navigate("map")            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Place: ${features.properties.place}", fontWeight = FontWeight.Bold)
            Text("Magnitude: ${features.properties.mag}")
            Text("Time: ${Date(features.properties.time.toLong())}")
        }
    }
}

@Composable
fun QuakePeriodDropdown(
    selectedPeriod: QuakePeriod,
    onPeriodSelected: (QuakePeriod) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxWidth()
        .wrapContentSize(Alignment.TopStart)) {

        Text(
            text = selectedPeriod.period.replace("_", " ").replaceFirstChar { it.uppercase() },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            QuakePeriod.values().forEach { period ->
                DropdownMenuItem(
                    text = {
                        Text(period.period.replace("_", " ").replaceFirstChar { it.uppercase() })
                    },
                    onClick = {
                        onPeriodSelected(period)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun scheduleQuakeWork(context: Context) {
    val interval = SettingsManager.pollingInterval.coerceAtLeast(15L)

    val request = PeriodicWorkRequestBuilder<QuakeWorker>(
        interval, TimeUnit.MINUTES
    ).build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "quake_check",
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}




