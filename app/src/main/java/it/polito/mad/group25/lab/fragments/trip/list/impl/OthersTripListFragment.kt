package it.polito.mad.group25.lab.fragments.trip.list.impl

import it.polito.mad.group25.lab.fragments.trip.Trip
import it.polito.mad.group25.lab.fragments.trip.list.GenericTripListFragment


class OthersTripListFragment : GenericTripListFragment(true) {

    override fun filterTrip(trip: Trip): Boolean = trip.ownerId != authenticationContext.userId() &&
            trip.tripStartDate > System.currentTimeMillis() + 60 * 1000

}