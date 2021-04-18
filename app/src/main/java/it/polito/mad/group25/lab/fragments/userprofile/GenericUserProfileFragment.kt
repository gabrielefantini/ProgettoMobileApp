package it.polito.mad.group25.lab.fragments.userprofile

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.utils.persistence.impl.FileSharedPreferencesSerde
import it.polito.mad.group25.lab.utils.viewmodel.PersistOnChange
import it.polito.mad.group25.lab.utils.viewmodel.PersistableContainer
import it.polito.mad.group25.lab.utils.views.fromFile
import java.io.File

abstract class GenericUserProfileFragment(
    private val nullSafeAssignment: Boolean,
    contentLayoutId: Int
) : Fragment(contentLayoutId) {

    protected lateinit var userProfileViewModel: UserProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userProfileViewModel = ViewModelProvider(this).get(UserProfileViewModel::class.java)
        setHasOptionsMenu(true)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (nullSafeAssignment) {
            userProfileViewModel.fullName?.also {
                view.findViewById<TextView>(R.id.fullName).text = it
            }
            userProfileViewModel.nickName?.also {
                view.findViewById<TextView>(R.id.nickName).text = it
            }
            userProfileViewModel.email?.also { view.findViewById<TextView>(R.id.email).text = it }
            userProfileViewModel.location?.also {
                view.findViewById<TextView>(R.id.location).text = it
            }
        } else {
            view.findViewById<TextView>(R.id.fullName).text = userProfileViewModel.fullName
            view.findViewById<TextView>(R.id.nickName).text = userProfileViewModel.nickName
            view.findViewById<TextView>(R.id.email).text = userProfileViewModel.email
            view.findViewById<TextView>(R.id.location).text = userProfileViewModel.location
        }
        view.findViewById<ImageView>(R.id.profilePic)
            .fromFile(userProfileViewModel.userProfilePhotoFile)
    }
}

class UserProfileViewModel(application: Application) : AndroidViewModel(application),
    PersistableContainer {

    var fullName: String? by PersistOnChange(null)
    var nickName: String? by PersistOnChange(null)
    var email: String? by PersistOnChange(null)
    var location: String? by PersistOnChange(null)
    var userProfilePhotoFile: File by PersistOnChange(
        FileSharedPreferencesSerde(application.filesDir, "userProfilePicture")
    )

    override fun getContext(): Context = getApplication()

}

