package it.polito.mad.group25.lab.fragments.trip.list

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.fasterxml.jackson.core.type.TypeReference
import it.polito.mad.group25.lab.fragments.trip.Trip
import it.polito.mad.group25.lab.utils.persistence.ConcurrentPersistor
import it.polito.mad.group25.lab.utils.persistence.impl.PersistenceAwareLiveData
import it.polito.mad.group25.lab.utils.persistence.impl.PersistenceAwareMutableMap
import it.polito.mad.group25.lab.utils.persistence.impl.persistenceAwareMutableMapOf
import it.polito.mad.group25.lab.utils.viewmodel.PersistableViewModel

class TripListViewModel(application: Application) : PersistableViewModel(application) {
    val trips: PersistenceAwareLiveData<PersistenceAwareMutableMap<Int, Trip>> by ConcurrentPersistor(
        PersistenceAwareLiveData(persistenceAwareMutableMapOf()),
        object : TypeReference<PersistenceAwareLiveData<PersistenceAwareMutableMap<Int, Trip>>>() {}
    )

    private companion object {
        var index = 0
    }

    var userId = ""

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNewTrip(): Trip {
        if (trips.value!!.isNotEmpty() && index == 0)
            index = trips.value!!.keys.maxOf { it } + 1
        return Trip().apply { id = index++ }
    }


    fun removeTrip(trip: Trip) {
        trips.value?.remove(trip.id)
    }

    fun putTrip(trip: Trip) {
        if (trip.id > index) index = trip.id + 1
        trips.value?.put(trip.id, trip)
    }

}
