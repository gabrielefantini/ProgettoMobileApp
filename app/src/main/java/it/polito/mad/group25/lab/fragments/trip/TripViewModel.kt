package it.polito.mad.group25.lab.fragments.trip

import androidx.lifecycle.ViewModel

class TripViewModel : ViewModel() {
    lateinit var trip: Trip

    fun addCurrentUserToSet(userId: String){
        trip.interestedUsers.add(userId)
    }
}