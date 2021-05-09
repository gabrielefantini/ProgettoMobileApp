package it.polito.mad.group25.lab.fragments.trip

import androidx.lifecycle.ViewModel

class TripViewModel : ViewModel() {
    lateinit var trip: Trip

    //trip dovrebbe avere un campo ownerId ed una MutableSet di userId (o user) che rappresenti il set di interessati al viaggio
    //il singolo user del set dovrebbe avere anche un campo boolean "isConfirmed" per quando viene confermato
    var ownerId: Int = 1    //campo di trip, owner del viaggio
    var userId: Int = 1     //id dell'attuale user dell'app
    var userSet = mutableSetOf(TripUser(2),TripUser(3))

    fun addCurrentUserToSet(){
        userSet.add(TripUser(userId))
    }

}

data class TripUser(
    val userId: Int,
    val isConfirmed: Boolean = false
)