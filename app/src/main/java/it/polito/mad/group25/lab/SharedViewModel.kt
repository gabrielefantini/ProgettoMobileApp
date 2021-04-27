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
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
class SharedViewModel: ViewModel(){
    private var _tripSelected = MutableLiveData<Int>(0)
    private var _tripList = MutableLiveData<MutableList<Trip>>(mutableListOf(trip,trip2)) //togliere i trip d'esempio
    private var _isNew = MutableLiveData<Boolean>(false)

    val tripSelected: LiveData<Int> = _tripSelected
    val tripList: LiveData<MutableList<Trip>> = _tripList
    val isNew: LiveData<Boolean> = _isNew

    fun pushTrip(newTrip: Trip): Int{
        if(newTrip == null) throw RuntimeException("null trip") //TODO gestire meglio
        _tripList.value?.add(newTrip)
        return _tripList.value?.lastIndex!!
    }

    fun popTrip(){
        _tripList.value?.lastIndex?.let { _tripList.value?.removeAt(it) }
    }

    fun selectTrip(tripNumber: Int){
        if(tripNumber < 0 ) throw RuntimeException("invalid number") //TODO gestire meglio
        _tripSelected.value = tripNumber
    }

    fun setNew(b: Boolean){
        _isNew.value = b
    }

}

///trip d'esempio (temporanei) *************
@RequiresApi(Build.VERSION_CODES.O)
val trip = Trip(
        "bitmap",
        "panda",
        LocalDate.now(),
        mutableListOf(
                TripLocation("loc1",LocalTime.now()),
                TripLocation("loc2",LocalTime.now().plusMinutes(30))
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
                TripLocation("loc1",LocalTime.now()),
                TripLocation("loc2",LocalTime.now().plusMinutes(30)),
                TripLocation("loc2",LocalTime.now().plusMinutes(60))
        ),
        3,
        100.0,
        mutableListOf("ccc","ddd")
)
///******************************************