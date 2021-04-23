package it.polito.mad.group25.lab

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.group25.lab.utils.entities.Trip
import it.polito.mad.group25.lab.utils.entities.TripLocation
import java.lang.RuntimeException
import java.time.LocalDateTime
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class SharedViewModel: ViewModel(){
    private var _tripSelected = MutableLiveData<Int>(0)
    private var _tripList = MutableLiveData<MutableList<Trip>>(mutableListOf())

    val tripSelected: LiveData<Int> = _tripSelected
    val tripList: LiveData<MutableList<Trip>> = _tripList

    fun addTrip(newTrip: Trip){
        if(newTrip == null) throw RuntimeException("null trip") //TODO gestire meglio
        _tripList.value?.add(newTrip)
    }

    fun selectTrip(tripNumber: Int){
        if(tripNumber < 0 ) throw RuntimeException("invalid number") //TODO gestire meglio
        _tripSelected.value =  tripNumber
    }

}