package it.polito.mad.group25.lab2.fragments.userprofile

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import it.polito.mad.group25.lab2.R
import it.polito.mad.group25.lab2.utils.views.fromFile

abstract class GenericUserProfileFragment(contentLayoutId: Int) : Fragment(contentLayoutId) {

    protected lateinit var userProfileViewModel: UserProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userProfileViewModel = ViewModelProvider(this).get(UserProfileViewModel::class.java)
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