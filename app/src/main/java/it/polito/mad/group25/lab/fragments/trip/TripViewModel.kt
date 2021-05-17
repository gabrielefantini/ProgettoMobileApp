package it.polito.mad.group25.lab.fragments.trip

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.observers.OnLiveDataValueChangesLoadDocumentFirestoreObserver
import it.polito.mad.group25.lab.utils.persistence.instantiator.Persistors
import it.polito.mad.group25.lab.utils.persistence.observers.ChainedObserver
import it.polito.mad.group25.lab.utils.persistence.observers.ToastOnErrorPersistenceObserver

class TripViewModel(application: Application) : AndroidViewModel(application) {
    var trip: MutableLiveData<Trip?> by Persistors.simpleLiveFirestore(
        collection = "trips",
        default = MutableLiveData(null),
        lazyInit = true,
        observer = ChainedObserver.startingFrom<MutableLiveData<Trip?>>
            (OnLiveDataValueChangesLoadDocumentFirestoreObserver())
            .wrappedBy { ToastOnErrorPersistenceObserver(application, it) }
            .build()
    )

    fun addCurrentUserToSet(userId: String) {
        trip.value!!.interestedUsers.add(TripUser(userId))
    }
}