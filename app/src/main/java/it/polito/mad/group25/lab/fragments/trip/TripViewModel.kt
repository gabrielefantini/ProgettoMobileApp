package it.polito.mad.group25.lab.fragments.trip

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.observers.OnLiveDataValueChangesLoadDocumentFirestoreObserver
import it.polito.mad.group25.lab.utils.persistence.instantiator.Persistors

class TripViewModel : ViewModel() {
    var trip: MutableLiveData<Trip?> by Persistors.simpleLiveFirestore(
        collection = "trips",
        default = MutableLiveData(null),
        lazyInit = true,
        observer = OnLiveDataValueChangesLoadDocumentFirestoreObserver()
    )

    fun addCurrentUserToSet(userId: String) {
        trip.value!!.interestedUsers.add(TripUser(userId))
    }
}