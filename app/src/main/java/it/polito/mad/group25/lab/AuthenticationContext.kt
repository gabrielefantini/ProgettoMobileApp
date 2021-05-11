package it.polito.mad.group25.lab

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import it.polito.mad.group25.lab.utils.persistence.Persistors
import it.polito.mad.group25.lab.utils.viewmodel.PersistableViewModel

object AuthenticationContext {
    lateinit var userID: String
}

class AuthContext(application: Application): PersistableViewModel(application){
    var authUser: MutableLiveData<FirebaseUser?> = MutableLiveData(null)

    var rememberMe: MutableLiveData<Boolean> by Persistors.sharedPreferences(
        default = MutableLiveData(false)
    )

    fun userId(): String? = authUser.value?.uid
}