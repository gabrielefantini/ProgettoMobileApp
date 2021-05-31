package it.polito.mad.group25.lab.fragments.trip.list.impl

import it.polito.mad.group25.lab.fragments.trip.Trip
import it.polito.mad.group25.lab.fragments.trip.list.GenericTripListFragment

class BoughtTripsListFragment : GenericTripListFragment(false) {

    override fun filterTrip(trip: Trip): Boolean =
        trip.interestedUsers.filter { it.isConfirmed }.map { it.userId }
            .contains(authenticationContext.userId())

    override fun boughtTrip(): Boolean {
        return true
    }

}