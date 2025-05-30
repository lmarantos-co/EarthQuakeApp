package com.example.earthquakeapp.Screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.earthquakeapp.Data.features
import com.example.earthquakeapp.ViewModels.QuakeHistoryViewModel

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.Instant
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun QuakeHistoryScreen(
    viewModel: QuakeHistoryViewModel
) {
    val quakes by viewModel.quakeHistory.collectAsState()
    val selectedRange by viewModel.selectedRange.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // Dropdown for date range
        RangeDropdown(selectedRange) { newRange ->
            viewModel.setDateRange(newRange)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Line Chart
        if (quakes.isNotEmpty()) {
            QuakeLineChart(quakes)
        } else {
            Text("Loading or no data available...")
        }
    }
}

@Composable
fun RangeDropdown(selectedRange: Int, onRangeSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(7, 30, 60)

    Box {
        TextButton(onClick = { expanded = true }) {
            Text("Last $selectedRange Days")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { days ->
                DropdownMenuItem(onClick = {
                    onRangeSelected(days)
                    expanded = false
                }, text = { Text("Last $days Days") })
            }
        }
    }
}


@SuppressLint("NewApi")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun QuakeLineChart(quakes: List<features>) {
    val context = LocalContext.current

    // Group by date, sort by date ascending
    val grouped = quakes.groupBy {
        Instant.ofEpochMilli(it.properties.time)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }.toSortedMap()

    // Map dates to average magnitude (Float)
    val entries = grouped.entries.mapIndexed { index, (_, list) ->
        Entry(
            index.toFloat(),
            list.mapNotNull { it.properties.mag }.map { it.toFloat() }.average().toFloat()
        )
    }
    // For labeling x-axis, keep dates as strings
    val dateLabels = grouped.keys.map { it.toString() }

    AndroidView(factory = { ctx ->
        LineChart(ctx).apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            axisRight.isEnabled = false // Hide right Y-axis

            // Configure X axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index >= 0 && index < dateLabels.size) dateLabels[index] else ""
                    }
                }
                labelRotationAngle = -45f
                textSize = 10f
            }

            // Configure Y axis (left)
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 10f
            }
        }
    },
        update = { chart ->

            val dataSet = LineDataSet(entries, "Avg Magnitude").apply {
                lineWidth = 2f
                circleRadius = 4f
                setDrawValues(false)
                color = android.graphics.Color.RED
                setCircleColor(android.graphics.Color.RED)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            chart.data = LineData(dataSet)
            chart.invalidate() // refresh chart
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}


