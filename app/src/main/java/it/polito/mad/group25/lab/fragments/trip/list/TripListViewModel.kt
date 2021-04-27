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

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNewTrip(): Trip = Trip().apply { id = index++ }

    fun addTrip(newTrip: Trip) {
        if (newTrip.id > index) index = newTrip.id + 1
        trips.value?.put(newTrip.id, newTrip)
    }

    fun removeTrip(trip: Trip) {
        trips.value?.remove(trip.id)
    }

    fun updateTrip(trip: Trip) {
        trips.value?.put(trip.id, trip)
    }

}
