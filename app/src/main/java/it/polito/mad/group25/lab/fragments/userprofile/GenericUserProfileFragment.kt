package it.polito.mad.group25.lab.fragments.userprofile

import android.app.Application
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.utils.persistence.Persistors
import it.polito.mad.group25.lab.utils.views.fromByteList

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
        userProfileViewModel.shownUser.observe(viewLifecycleOwner) {
            view.findViewById<TextView>(R.id.fullName).text = it.fullName
            view.findViewById<TextView>(R.id.nickName).text = it.nickName
            view.findViewById<TextView>(R.id.email).text = it.email
            view.findViewById<TextView>(R.id.location).text = it.location
            view.findViewById<ImageView>(R.id.profilePic)
                .fromByteList(it.userProfilePhotoFile)
        }
    }
}

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {
    var shownUser: MutableLiveData<UserProfile> by Persistors.liveFirestore(default = MutableLiveData())
}

data class UserProfile(
    var id: String,
    var fullName: String,
    var nickName: String,
    var email: String,
    var location: String,
    var userProfilePhotoFile: List<Byte>
)
