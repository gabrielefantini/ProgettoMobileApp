package it.polito.mad.group25.lab.fragments.trip.filter

import it.polito.mad.group25.lab.fragments.trip.Trip
import it.polito.mad.group25.lab.utils.asFormattedDate
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds


enum class FilterField {
    departureDate{
        override fun operator(trip: Trip, filterValue: String): Boolean {
            val formatter = SimpleDateFormat("dd/MM/yyyy")
            val filterDate = formatter.parse(filterValue).time
            val tripDate = formatter.parse(formatter.format(Date(trip.locations[0].locationTime))).time
            return tripDate >= filterDate
        }
    },
    arrivalDate {
        override fun operator(trip: Trip, filterValue: String): Boolean {
            val formatter = SimpleDateFormat("dd/MM/yyyy")
            val filterDate = formatter.parse(filterValue).time
            val tripDate = formatter.parse(formatter.format(Date(trip.locations[0].locationTime))).time
            return tripDate <= filterDate
        }

    },
    departureTime {
        override fun operator(trip: Trip, filterValue: String): Boolean {
            val formatter = SimpleDateFormat("HH:mm")
            val filterTime = formatter.parse(filterValue).time
            val tripTime = formatter.parse(formatter.format(Date(trip.locations[0].locationTime))).time
            return tripTime >= filterTime
        }
    },
    arrivalTime{
        override fun operator(trip: Trip, filterValue: String): Boolean {
            val formatter = SimpleDateFormat("HH:mm")
            val filterTime = formatter.parse(filterValue).time
            val tripTime = formatter.parse(formatter.format(Date(trip.locations[0].locationTime))).time
            return tripTime <= filterTime
        }
    },
    departurePlace {
         override fun operator(trip: Trip, filterValue: String): Boolean {
            return trip.locations[0].location == filterValue
        }

    },
    arrivalPlace{
         override fun operator(trip: Trip, filterValue: String): Boolean {
            return trip.locations[trip.locations.size - 1].location == filterValue
        }
    },
    price {
        override fun operator(trip: Trip, filterValue: String): Boolean {
            return trip.price <= filterValue.toDouble()
        }
    },
    duration {
        override fun operator(trip: Trip, filterValue: String): Boolean {
            return (filterValue.toLong()*6000) >= (trip.locations[trip.locations.size - 1].locationTime - trip.locations[0].locationTime)
        }
    },
    seats {
         override fun operator(trip: Trip, filterValue: String): Boolean {
            return trip.seats == filterValue.toInt()
        }
    };
    abstract fun operator(trip: Trip, filterValue: String): Boolean
}
