package it.polito.mad.group25.lab.fragments.userprofile

import android.app.Application
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import it.polito.mad.group25.lab.AuthenticationContext
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.UserProfile
import it.polito.mad.group25.lab.utils.persistence.instantiator.Persistors
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLivePersistenceObserver
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreLivePersistorDelegate
import it.polito.mad.group25.lab.utils.viewmodel.PersistableViewModel
import it.polito.mad.group25.lab.utils.views.fromBlob
import kotlin.reflect.jvm.isAccessible

abstract class GenericUserProfileFragment(
    contentLayoutId: Int
) : Fragment(contentLayoutId) {

    protected val userProfileViewModel: UserProfileViewModel by activityViewModels()
    protected val authenticationContext: AuthenticationContext by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    fun visualizeUserData(view: View, hideSensitiveDataIfNecessary: Boolean) {
        userProfileViewModel.shownUser.observe(viewLifecycleOwner) {
            if (it == null)
                return@observe

            view.findViewById<TextView>(R.id.nickName).text = it.nickName
            it.userProfilePhotoFile?.let { it1 ->
                view.findViewById<ImageView>(R.id.profilePic)
                    .fromBlob(it1)
            }

            if (hideSensitiveDataIfNecessary && it.id != authenticationContext.userId())
                return@observe

            view.findViewById<TextView>(R.id.fullName).text = it.fullName
            view.findViewById<TextView>(R.id.email).text = it.email
            view.findViewById<TextView>(R.id.location).text = it.location


        }
    }
}

class UserProfileViewModel(application: Application) : PersistableViewModel(application) {

    val shownUser: MutableLiveData<UserProfile>
            by Persistors.simpleLiveFirestore(
                collection = "users",
                lazyInit = true,
                default = MutableLiveData(),
                observer = object :
                    FirestoreLivePersistenceObserver<DocumentSnapshot, MutableLiveData<UserProfile>> {
                    // do no persist any value set here! It's just for reading!
                    override fun beforePerformingPersistence(value: MutableLiveData<UserProfile>)
                            : MutableLiveData<UserProfile>? = null
                }
            )


    private val persistor =
        this::shownUser.apply { isAccessible = true }
            .getDelegate() as FirestoreLivePersistorDelegate<MutableLiveData<UserProfile?>, UserProfileViewModel>

    fun showUser(id: String) {
        persistor.loadAnotherDocument(id)
    }

}

