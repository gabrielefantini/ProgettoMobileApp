package it.polito.mad.group25.lab2.fragments.userprofile

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.findNavController
import it.polito.mad.group25.lab2.R
import it.polito.mad.group25.lab2.utils.views.toFile

class EditUserProfileFragment : GenericUserProfileFragment(R.layout.edit_user_profile_fragment) {

    companion object {
        fun newInstance() = EditUserProfileFragment()
    }

    private lateinit var currentView: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentView = view
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.photo -> {
                registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
                    currentView.findViewById<ImageView>(R.id.profilePic).setImageBitmap(it)
                }.launch(null)
                true
            }
            R.id.gallery -> {
                registerForActivityResult(ActivityResultContracts.GetContent()) {
                    currentView.findViewById<ImageView>(R.id.profilePic).setImageURI(it)
                }.launch("image/*")
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.menu_foto, menu)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveProfileEdit -> {
                saveEdits()
                activity?.findNavController(R.id.nav_host_fragment_content_main)
                    ?.navigate(R.id.action_editUserProfileFragment_to_showUserProfileFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveEdits() {
        userProfileViewModel.fullName =
            currentView.findViewById<EditText>(R.id.fullName).text.toString()
        userProfileViewModel.nickName =
            currentView.findViewById<EditText>(R.id.nickName).text.toString()
        userProfileViewModel.email =
            currentView.findViewById<EditText>(R.id.email).text.toString()
        userProfileViewModel.location =
            currentView.findViewById<EditText>(R.id.location).text.toString()
        currentView.findViewById<ImageView>(R.id.profilePic).toFile()
            ?.let { userProfileViewModel.userProfilePhotoFile = it }
    }

}