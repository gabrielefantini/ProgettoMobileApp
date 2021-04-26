package it.polito.mad.group25.lab.fragments.userprofile

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.AndroidViewModel
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.utils.viewmodel.PersistableContainer
import it.polito.mad.group25.lab.utils.viewmodel.Persistor
import it.polito.mad.group25.lab.utils.views.fromFile
import java.io.File

abstract class GenericUserProfileFragment(
    contentLayoutId: Int
) : Fragment(contentLayoutId) {

    protected val userProfileViewModel: UserProfileViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.fullName).text = userProfileViewModel.fullName
        view.findViewById<TextView>(R.id.nickName).text = userProfileViewModel.nickName
        view.findViewById<TextView>(R.id.email).text = userProfileViewModel.email
        view.findViewById<TextView>(R.id.location).text = userProfileViewModel.location

        view.findViewById<ImageView>(R.id.profilePic)
            .fromFile(userProfileViewModel.userProfilePhotoFile)
    }
}

class UserProfileViewModel(application: Application) : AndroidViewModel(application),
    PersistableContainer {

    var fullName: String? by Persistor(null)
    var nickName: String? by Persistor(null)
    var email: String? by Persistor(null)
    var location: String? by Persistor(null)
    var userProfilePhotoFile: File by Persistor(
        File(application.filesDir, "userProfilePicture")
    )

    override fun getContext(): Context = getApplication()

}

