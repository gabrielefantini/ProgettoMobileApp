package it.polito.mad.group25.lab.utils.entities

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class Trip (
        var carPic: String,
        var carName: String,
        var tripStartDate: LocalDate,
        val locations: MutableList<TripLocation>,
        var seats: Int,
        var price: Double,
        val additionalInfo: MutableList<String>
        )

@RequiresApi(Build.VERSION_CODES.O)
fun Trip.startDateFormatted(): String = this.tripStartDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

data class TripLocation(var location: String, var locationTime: LocalTime)

@RequiresApi(Build.VERSION_CODES.O)
fun TripLocation.timeFormatted(): String = this.locationTime.format(DateTimeFormatter.ofPattern("HH:mm"))

@RequiresApi(Build.VERSION_CODES.O)
fun Trip.addTripOrdered (location: String, locationTime: LocalTime) {
        val trip = TripLocation(location, locationTime)
        locations.add(trip)
        locations.sortBy { it.locationTime }
}