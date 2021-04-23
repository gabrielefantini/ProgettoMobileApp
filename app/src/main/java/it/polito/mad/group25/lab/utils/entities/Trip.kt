package it.polito.mad.group25.lab.utils.entities

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

data class Trip (
        val carPic: String,
        val carName: String,
        val tripStartDate: LocalDateTime,
        val locations: MutableList<TripLocation>,
        val seats: Int,
        val price: Double,
        val additionalInfo: MutableList<String>
        )

@RequiresApi(Build.VERSION_CODES.O)
inline fun Trip.startDateFormatted(): String = this.tripStartDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

data class TripLocation(var location: String,var locationTime: LocalDateTime)

@RequiresApi(Build.VERSION_CODES.O)
inline fun TripLocation.timeFormatted(): String = this.locationTime.format(DateTimeFormatter.ofPattern("HH:mm"))