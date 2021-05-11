package it.polito.mad.group25.lab

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.Blob
import it.polito.mad.group25.lab.utils.persistence.Persistors
import it.polito.mad.group25.lab.utils.viewmodel.PersistableViewModel
import it.polito.mad.group25.lab.utils.views.toBlob


class AuthenticationContext(application: Application) : PersistableViewModel(application) {

    companion object {
        val STORED_USERID_KEY = "user_id"
    }

    val authUser: LiveData<FirebaseUser?> = MutableLiveData(null)

    var userData: MutableLiveData<UserProfile?> by Persistors.simpleLiveFirestore(
        collection = "users",
        default = MutableLiveData(null),
        lazyInit = true
    )
    private var userID: String by Persistors.sharedPreferences("", STORED_USERID_KEY)

    var rememberMe: Boolean by Persistors.sharedPreferences(default = false)


    private var imageLoader: ImageLoader = ImageLoader.Builder(getContext())
        .availableMemoryPercentage(0.25)
        .crossfade(false)
        .build()


    fun loginUser(user: FirebaseUser) {
        authUser as MutableLiveData<FirebaseUser?>
        authUser.value = user
        userData.value = UserProfile(user.uid, user.displayName, null, user.email, null, null)
        loadAndSaveImage(user.photoUrl, user.uid)
        userID = user.uid
    }

    fun logoutUser() {
        authUser as MutableLiveData<FirebaseUser?>
        authUser.value = null
        userData.value = null
        userID = ""
    }

    fun userId(): String = userID

    private fun loadAndSaveImage(uri: Uri?, oldId: String) {
        val userPhotoRequest =
            ImageRequest.Builder(getContext()).data(uri)
                .target({
                    if (it != null) {
                        if (userId() == oldId)
                            userData.value?.userProfilePhotoFile = it.toBlob()
                    }
                }).build()
        imageLoader.enqueue(userPhotoRequest)
    }

}

data class UserProfile(
    var id: String,
    var fullName: String?,
    var nickName: String?,
    var email: String?,
    var location: String?,
    var userProfilePhotoFile: Blob?
)

