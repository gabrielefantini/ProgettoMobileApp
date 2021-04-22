package it.polito.mad.group25.lab.utils.entities

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

data class Trip (
        val carPic: String,
        val carName: String,
        val tripStartDate: Date,
        val locations: MutableList<TripLocation>,
        val seats: Int,
        val price: Double,
        val additionalInfo: MutableList<String>
        )

class TripLocation(var location: String,time: LocalDateTime) {
        @RequiresApi(Build.VERSION_CODES.O)
        val locationTime = time.format(DateTimeFormatter.ofPattern("HH:mm"))
}