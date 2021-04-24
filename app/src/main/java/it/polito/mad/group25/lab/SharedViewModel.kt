package it.polito.mad.group25.lab

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.group25.lab.utils.entities.Trip
import it.polito.mad.group25.lab.utils.entities.TripLocation
import java.lang.RuntimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class SharedViewModel: ViewModel(){
    private var _tripSelected = MutableLiveData<Int>(0)
    private var _tripList = MutableLiveData<MutableList<Trip>>(mutableListOf(trip,trip2,trip,trip2)) //togliere i trip d'esempio

    val tripSelected: LiveData<Int> = _tripSelected
    val tripList: LiveData<MutableList<Trip>> = _tripList

    fun addTrip(newTrip: Trip){
        if(newTrip == null) throw RuntimeException("null trip") //TODO gestire meglio
        _tripList.value?.add(newTrip)
    }

    fun selectTrip(tripNumber: Int){
        if(tripNumber < 0 ) throw RuntimeException("invalid number") //TODO gestire meglio
        _tripSelected.value = tripNumber
    }

}

///trip d'esempio (temporanei) *************
@RequiresApi(Build.VERSION_CODES.O)
val trip = Trip(
        "bitmap",
        "panda",
        LocalDate.now(),
        mutableListOf(
                TripLocation("loc1",LocalDateTime.now()),
                TripLocation("loc2",LocalDateTime.now().plusMinutes(30))
        ),
        2,
        20.0,
        mutableListOf("aaa","bbb")
)

@RequiresApi(Build.VERSION_CODES.O)
val trip2 = Trip(
        "bitmap",
        "idea",
        LocalDate.now().plusDays(1),
        mutableListOf(
                TripLocation("loc1",LocalDateTime.now()),
                TripLocation("loc2",LocalDateTime.now().plusMinutes(30)),
                TripLocation("loc2",LocalDateTime.now().plusMinutes(60))
        ),
        3,
        100.0,
        mutableListOf("ccc","ddd")
)
///******************************************