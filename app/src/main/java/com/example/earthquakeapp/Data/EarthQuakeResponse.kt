package com.example.earthquakeapp.Data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.net.URL

class EarthQuakeResponse {

    @SerializedName("type")
    var type: String = ""

    @SerializedName("metadata")
    var metadata: metadata = metadata(null , null, null, null, null , null)

    @SerializedName("features")
    var features: List<features> = arrayListOf<features>()

    @SerializedName("bbox")
    var bbox: List<Float> = arrayListOf<Float>()

}

data class metadata
    (
    val generated : Long?,
    val url : URL?,
    val title : String?,
    val status : Int?,
    val api : String?,
    val count : Int?
            )
@Parcelize
data class features(
    val type: String?,
    val properties : properties,
    val geometry : geometry,
    val id : String?,
) : Parcelable

@Parcelize
data class properties(
    val mag: Float,
    val place : String,
    val time : Long,
    val updated : Long,
    @SerializedName("tz") val timeZone: Int?,      // Timezone offset in minutes (nullable)
    @SerializedName("felt") val feltReports: Int?, // Number of people who reported feeling it
    @SerializedName("cdi") val cdi: Double?,       // Community Internet Intensity Map (1-10)
    @SerializedName("mmi") val mmi: Double?,       // Modified Mercalli Intensity (1-10)
    @SerializedName("alert") val alertLevel: String?, // Alert level (green, yellow, orange, red)
    val url : URL,
    val detail : URL,
    val status : String,
    val tsuname : Int,
    val sig : Int,
    val net : String,
    val code : String,
    val ids : String,
    val sources : String,
    val types : String,
    val nst : Int,
    val dmin : Float,
    val rms : Float,
    val gap : Float,
    val magType : String,
    val type : String,
    val title : String
) : Parcelable

@Parcelize
data class geometry (
    val type: String,
    val coordinates : List<Float> = arrayListOf<Float>()
) : Parcelable
