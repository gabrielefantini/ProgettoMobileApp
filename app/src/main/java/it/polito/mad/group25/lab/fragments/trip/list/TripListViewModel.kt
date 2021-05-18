package it.polito.mad.group25.lab.fragments.trip.list

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import it.polito.mad.group25.lab.fragments.trip.Trip
import it.polito.mad.group25.lab.utils.persistence.awareds.PersistenceAwareMutableLiveMap
import it.polito.mad.group25.lab.utils.persistence.awareds.persistenceAwareMutableLiveMapOf
import it.polito.mad.group25.lab.utils.persistence.instantiator.Persistors
import it.polito.mad.group25.lab.utils.persistence.observers.ToastOnErrorPersistenceObserver
import it.polito.mad.group25.lab.utils.viewmodel.PersistableViewModel
import java.util.*

class TripListViewModel(application: Application) : PersistableViewModel(application) {
    val trips: PersistenceAwareMutableLiveMap<String, Trip>
            by Persistors.liveFirestoreMap(
                collection = "trips",
                default = persistenceAwareMutableLiveMapOf(),
                observer = ToastOnErrorPersistenceObserver(application)
            )

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNewTrip(): Trip {
        return Trip()
    }

    private fun generateNewId(): String {
        var id: String
        do {
            id = UUID.randomUUID().toString()
        } while (trips.value!!.containsKey(id))
        return id
    }


    fun removeTrip(trip: Trip) {
        trips.value?.remove(trip.id)
    }

    fun putTrip(trip: Trip) {
        trip.id = generateNewId()
        trips.value?.put(trip.id!!, trip)
    }

}
