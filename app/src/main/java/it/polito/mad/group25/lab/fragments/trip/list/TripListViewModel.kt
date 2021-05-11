package it.polito.mad.group25.lab.fragments.trip.list

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.polito.mad.group25.lab.fragments.trip.Trip
import it.polito.mad.group25.lab.utils.persistence.Persistors
import it.polito.mad.group25.lab.utils.persistence.awareds.PersistenceAwareMutableMap
import it.polito.mad.group25.lab.utils.persistence.awareds.persistenceAwareMutableMapOf
import it.polito.mad.group25.lab.utils.viewmodel.PersistableViewModel
import java.util.*

class TripListViewModel(application: Application) : PersistableViewModel(application) {
    val trips: LiveData<PersistenceAwareMutableMap<String, Trip>>
            by Persistors.liveFirestore(
                collection = "trips",
                default = MutableLiveData(persistenceAwareMutableMapOf())
            )


    @RequiresApi(Build.VERSION_CODES.O)
    fun createNewTrip(): Trip {
        return Trip().apply { id = generateNewId() }
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
        trips.value?.put(trip.id!!, trip)
    }

}
