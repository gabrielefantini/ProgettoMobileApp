package it.polito.mad.group25.lab.utils.entities

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

data class TripLocation(
        val locationTime: Date,
        val location: String
        )