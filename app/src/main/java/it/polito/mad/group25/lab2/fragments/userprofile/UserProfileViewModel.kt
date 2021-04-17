package it.polito.mad.group25.lab2.fragments.userprofile

import android.app.Application
import android.content.SharedPreferences
import it.polito.mad.group25.lab2.utils.persistence.DifferentiatedSerdeStrategy
import it.polito.mad.group25.lab2.utils.persistence.impl.FileSharedPreferencesSerde
import it.polito.mad.group25.lab2.utils.viewmodel.PersistentAndroidViewModel
import java.io.File

class UserProfileViewModel(application: Application) : PersistentAndroidViewModel(application) {

    var fullName: String? = null
    var nickName: String? = null
    var email: String? = null
    var location: String? = null
    lateinit var userProfilePhotoFile: File

    override fun customSerdeStrategies(): List<DifferentiatedSerdeStrategy<*, SharedPreferences, SharedPreferences.Editor>> =
        listOf(
            FileSharedPreferencesSerde(
                getApplication<Application>().filesDir,
                "userProfilePicture"
            )
        )
}