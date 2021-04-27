package it.polito.mad.group25.lab.fragments.trip

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
data class Trip(
    val id: Int,
    var carPic: String = "",
    var carName: String = "car name",
    var tripStartDate: LocalDate = LocalDate.now(),
    val locations: MutableList<TripLocation> = mutableListOf(
        TripLocation(),
        TripLocation(locationTime = LocalTime.now().plusMinutes(30))
    ),
    var seats: Int = 0,
    var price: Double = 0.0,
    val additionalInfo: MutableList<String> = mutableListOf()
)

@RequiresApi(Build.VERSION_CODES.O)
fun Trip.startDateFormatted(): String =
    this.tripStartDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

@RequiresApi(Build.VERSION_CODES.O)
data class TripLocation(
    var location: String = "loc name",
    var locationTime: LocalTime = LocalTime.now()
)

@RequiresApi(Build.VERSION_CODES.O)
fun TripLocation.timeFormatted(): String =
    this.locationTime.format(DateTimeFormatter.ofPattern("HH:mm"))

@RequiresApi(Build.VERSION_CODES.O)
fun Trip.addTripLocationOrdered(location: String, locationTime: LocalTime) {
    val trip = TripLocation(location, locationTime)
    locations.add(trip)
    locations.sortBy { it.locationTime }
}