package it.polito.mad.group25.lab.fragments.trip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.observers.MakeReadOnlyFirestoreLivePersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.instantiator.Persistors

class TripViewModel : ViewModel() {
    var trip: LiveData<Trip?> by Persistors.simpleLiveFirestore(
        collection = "trips",
        default = MutableLiveData(null),
        lazyInit = true,
        observer = MakeReadOnlyFirestoreLivePersistenceObserver(this::trip)
    )

    fun addCurrentUserToSet(userId: String) {
        trip.value!!.interestedUsers.add(userId)
    }
}