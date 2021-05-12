package it.polito.mad.group25.lab

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.Blob
import it.polito.mad.group25.lab.utils.datastructure.IdentifiableObject
import it.polito.mad.group25.lab.utils.persistence.Persistors
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLivePersistorDelegate
import it.polito.mad.group25.lab.utils.viewmodel.PersistableViewModel
import it.polito.mad.group25.lab.utils.views.toBlob
import kotlin.reflect.jvm.isAccessible


class AuthenticationContext(application: Application) : PersistableViewModel(application) {


    val authUser: LiveData<FirebaseUser?> = MutableLiveData(null)

    var userData: MutableLiveData<UserProfile?> by Persistors.simpleLiveFirestore(
        collection = "users",
        default = MutableLiveData(null),
        lazyInit = true
    )

    var rememberMe: Boolean by Persistors.sharedPreferences(default = false)


    private var imageLoader: ImageLoader = ImageLoader.Builder(getContext())
        .availableMemoryPercentage(0.25)
        .crossfade(false)
        .build()


    fun loginUser(user: FirebaseUser) {
        authUser as MutableLiveData<FirebaseUser?>
        authUser.value = user

        val persistor = (this::userData.apply { isAccessible = true }
            .getDelegate() as FirestoreLivePersistorDelegate<MutableLiveData<UserProfile?>, AuthenticationContext>)

        // check if the user is already saved on the db by loading it.
        // if it is absent then save it.
        persistor.loadAnotherDocument(user.uid).get().addOnCompleteListener {
            if (!it.isSuccessful) throw it.exception!!
            if (!it.result!!.exists()) {
                userData.value =
                    UserProfile(user.uid, user.displayName, null, user.email, null, null)
                loadAndSaveImage(user.photoUrl, user.uid) //magari l'utente ha cambiato immagine su google
            }
        }
    }

    fun logoutUser() {
        authUser as MutableLiveData<FirebaseUser?>
        authUser.value = null
        userData.value = null
    }


    fun userId(): String? = userData.value?.id

    private fun loadAndSaveImage(uri: Uri?, oldId: String) {
        val userPhotoRequest =
            ImageRequest.Builder(getContext()).data(uri)
                .target({
                    if (it != null) {
                        if (userId() == oldId)
                            userData.value =
                                userData.value?.copy(userProfilePhotoFile = it.toBlob())
                    }
                }).build()
        imageLoader.enqueue(userPhotoRequest)
    }

}

data class UserProfile(
    var fullName: String?,
    var nickName: String?,
    var email: String?,
    var location: String?,
    var userProfilePhotoFile: Blob?
) : IdentifiableObject() {
    constructor(
        id: String?,
        fullName: String?,
        nickName: String?,
        email: String?,
        location: String?,
        userProfilePhotoFile: Blob?
    ) : this(fullName, nickName, email, location, userProfilePhotoFile) {
        this.id = id
    }

    constructor() : this(null, null, null, null, null, null)
}

