package it.polito.mad.group25.lab

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.Blob
import it.polito.mad.group25.lab.utils.datastructure.Identifiable
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceAware
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreDocumentChanger
import it.polito.mad.group25.lab.utils.persistence.instantiator.Persistors
import it.polito.mad.group25.lab.utils.persistence.observers.ToastOnErrorPersistenceObserver
import it.polito.mad.group25.lab.utils.viewmodel.PersistableViewModel
import it.polito.mad.group25.lab.utils.views.toBlob


class AuthenticationContext(application: Application) : PersistableViewModel(application) {


    val authUser: LiveData<FirebaseUser?> = MutableLiveData(null)

    private val userDataDocumentChanger: FirestoreDocumentChanger<MutableLiveData<UserProfile?>> =
        FirestoreDocumentChanger()

    var userData: MutableLiveData<UserProfile?> by Persistors.simpleLiveFirestore(
        collection = "users",
        default = MutableLiveData(null),
        lazyInit = true,
        documentChanger = userDataDocumentChanger,
        observer = ToastOnErrorPersistenceObserver(application)
    )

    var rememberMe: Boolean by Persistors.sharedPreferences(default = false)


    private var imageLoader: ImageLoader = ImageLoader.Builder(getContext())
        .availableMemoryPercentage(0.25)
        .crossfade(false)
        .build()


    fun loginUser(user: FirebaseUser) {
        authUser as MutableLiveData<FirebaseUser?>
        authUser.value = user
        // check if the user is already saved on the db by loading it.
        // if it is absent then save it.
        userDataDocumentChanger.changeDocument(user.uid).get().addOnCompleteListener {
            if (!it.isSuccessful) throw it.exception!!
            if (!it.result!!.exists()) {
                userData.value =
                    UserProfile(user.uid, user.displayName, null, user.email, null, null)
                loadAndSaveImage(user.photoUrl, user.uid)
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
                    if (userId() == oldId)
                        userData.value?.userProfilePhotoFile = it?.toBlob()

                }).build()
        imageLoader.enqueue(userPhotoRequest)
    }

}

data class UserProfile(
    override var id: String?,
    var fullName: String?,
    var nickName: String?,
    var email: String?,
    var location: String?,
) : AbstractPersistenceAware(), Identifiable {

    var userProfilePhotoFile: Blob? by onChangeUpdateStatus(null)

    constructor(
        id: String?,
        fullName: String?,
        nickName: String?,
        email: String?,
        location: String?,
        userProfilePhotoFile: Blob?
    ) : this(id, fullName, nickName, email, location) {
        this.userProfilePhotoFile = userProfilePhotoFile
    }

    constructor() : this(null, null, null, null, null, null)
}

