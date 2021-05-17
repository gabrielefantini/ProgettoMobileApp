package it.polito.mad.group25.lab.fragments.trip.filter

import it.polito.mad.group25.lab.fragments.trip.Trip
import java.util.*


enum class FilterField {
    departureDate{
        override fun operator(trip: Trip, filterValue: String): Boolean {
            TODO("Not yet implemented")
        }
    },
    arrivalDate {
        override fun operator(trip: Trip, filterValue: String): Boolean {
            TODO("Not yet implemented")
        }

    },
    departureTime {
        override fun operator(trip: Trip, filterValue: String): Boolean {
            TODO("Not yet implemented")
        }
    },
    arrivalTime{
        override fun operator(trip: Trip, filterValue: String): Boolean {
            TODO("Not yet implemented")
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
            TODO("Not yet implemented")
        }
    },
    seats {
         override fun operator(trip: Trip, filterValue: String): Boolean {
            return trip.seats == filterValue.toInt()
        }
    };
    abstract fun operator(trip: Trip, filterValue: String): Boolean
}
