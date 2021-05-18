package it.polito.mad.group25.lab.fragments.userprofile

import android.app.Application
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import it.polito.mad.group25.lab.AuthenticationContext
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.UserProfile
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreDocumentChanger
import it.polito.mad.group25.lab.utils.persistence.instantiator.Persistors
import it.polito.mad.group25.lab.utils.persistence.observers.ChainedObserver
import it.polito.mad.group25.lab.utils.persistence.observers.MakeReadOnlyObserver
import it.polito.mad.group25.lab.utils.persistence.observers.ToastOnErrorPersistenceObserver
import it.polito.mad.group25.lab.utils.viewmodel.PersistableViewModel
import it.polito.mad.group25.lab.utils.views.fromBlob

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

            it.userProfilePhotoFile?.let { it1 ->
                view.findViewById<ImageView>(R.id.profilePic)
                    .fromBlob(it1)
            }
            view.findViewById<TextView>(R.id.fullName).text = it.fullName
            view.findViewById<TextView>(R.id.email).text = it.email

            if (hideSensitiveDataIfNecessary && it.id != authenticationContext.userId())
                return@observe

            view.findViewById<TextView>(R.id.nickName).text = it.nickName
            view.findViewById<TextView>(R.id.location).text = it.location
        }
    }
}

class UserProfileViewModel(application: Application) : PersistableViewModel(application) {

    private val shownUserDocumentChanger: FirestoreDocumentChanger<MutableLiveData<UserProfile>> =
        FirestoreDocumentChanger()

    val shownUser: MutableLiveData<UserProfile>
            by Persistors.simpleLiveFirestore(
                collection = "users",
                lazyInit = true,
                default = MutableLiveData(),
                documentChanger = shownUserDocumentChanger,
                observer = ChainedObserver.startingFrom(MakeReadOnlyObserver<MutableLiveData<UserProfile>>())
                    .wrappedBy { ToastOnErrorPersistenceObserver(application, it) }.build()
            )

    fun showUser(id: String) {
        shownUserDocumentChanger.changeDocument(id)
    }

}

