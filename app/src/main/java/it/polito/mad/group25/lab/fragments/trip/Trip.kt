package it.polito.mad.group25.lab.fragments.trip

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.properties.Delegates

@RequiresApi(Build.VERSION_CODES.O)
class Trip {
    var id by Delegates.notNull<Int>()
    var carPic: List<Byte>? = null
    var carName: String? = null
    var tripStartDate: LocalDate = LocalDate.now()
    val locations: MutableList<TripLocation> = mutableListOf(
        TripLocation(),
        TripLocation(locationTime = LocalDateTime.now().plusMinutes(30))
    )
    var seats: Int = 0
    var price: Double = 0.0
    val additionalInfo: MutableList<String> = mutableListOf()
}

@RequiresApi(Build.VERSION_CODES.O)
fun Trip.startDateFormatted(): String =
    this.tripStartDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

@RequiresApi(Build.VERSION_CODES.O)
data class TripLocation(
    var location: String = "loc name",
    var locationTime: LocalDateTime = LocalDateTime.now()
)

@RequiresApi(Build.VERSION_CODES.O)
fun TripLocation.timeFormatted(): String =
    this.locationTime.format(DateTimeFormatter.ofPattern("HH:mm"))

@RequiresApi(Build.VERSION_CODES.O)
fun Trip.addTripLocationOrdered(location: String, locationTime: LocalDateTime) {
    val trip = TripLocation(location, locationTime)
    locations.add(trip)
    locations.sortBy { it.locationTime }
}