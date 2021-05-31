package it.polito.mad.group25.lab.fragments.trip.list.impl

import it.polito.mad.group25.lab.fragments.trip.Trip
import it.polito.mad.group25.lab.fragments.trip.list.GenericTripListFragment


class TripListFragment : GenericTripListFragment(true) {

    override fun filterTrip(trip: Trip): Boolean = trip.ownerId == authenticationContext.userId()
    override fun boughtTrip(): Boolean {
        return false
    }


}
