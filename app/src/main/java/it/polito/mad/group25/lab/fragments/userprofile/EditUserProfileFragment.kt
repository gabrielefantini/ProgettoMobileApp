package it.polito.mad.group25.lab.fragments.userprofile

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.utils.nullOnBlank
import it.polito.mad.group25.lab.utils.views.toFile
import java.io.File

class EditUserProfileFragment :
    GenericUserProfileFragment(false, R.layout.edit_user_profile_fragment) {

    private lateinit var takePictureLauncher: ActivityResultLauncher<Void>
    private lateinit var pickPictureLauncher: ActivityResultLauncher<String>
    private lateinit var viewModel: EditUserProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(EditUserProfileViewModel::class.java)

        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
                val profilePic = requireView().findViewById<ImageView>(R.id.profilePic)
                profilePic.setImageBitmap(it)
                profilePic.drawable?.let { d -> viewModel.tempProfileDrawable = d }
            }
        pickPictureLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) {
                val profilePic = requireView().findViewById<ImageView>(R.id.profilePic)
                profilePic.setImageURI(it)
                profilePic.drawable?.let { d -> viewModel.tempProfileDrawable = d }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.tempProfileDrawable?.let {
            view.findViewById<ImageView>(R.id.profilePic).setImageDrawable(it)
        }

        val cameraButton = view.findViewById<ImageButton>(R.id.changeUserProfilePicButton)
        registerForContextMenu(cameraButton)
        cameraButton.setOnClickListener {
            it.showContextMenu()
        }
        val camera = view.findViewById<ImageView>(R.id.profilePic)
        registerForContextMenu(camera)
        camera.setOnClickListener {
            it.showContextMenu()
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = requireActivity().menuInflater
        inflater.inflate(R.menu.menu_edit_propic, menu)
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.photo -> {
                takePictureLauncher.launch(null)
                true
            }
            R.id.gallery -> {
                pickPictureLauncher.launch("image/*")
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
        requireView().findViewById<EditText>(R.id.fullName).text.toString().nullOnBlank()
            ?.also { userProfileViewModel.fullName = it }
        requireView().findViewById<EditText>(R.id.nickName).text.toString().nullOnBlank()
            ?.also { userProfileViewModel.nickName = it }
        requireView().findViewById<EditText>(R.id.email).text.toString().nullOnBlank()
            ?.also { userProfileViewModel.email = it }
        requireView().findViewById<EditText>(R.id.location).text.toString().nullOnBlank()
            ?.also { userProfileViewModel.location = it }

        requireView().findViewById<ImageView>(R.id.profilePic).toFile()
            ?.let { userProfileViewModel.userProfilePhotoFile = it }
        fireDataChanges()
    }

    private fun fireDataChanges() {
        val act = activity
        if (act is UserProfileDataChangeListener) {
            act.onUserProfileDataChanged(
                UserProfileData.fromViewModel(userProfileViewModel)
            )
        }
    }
}

class EditUserProfileViewModel : ViewModel() {
    var tempProfileDrawable: Drawable? = null
}

data class UserProfileData(
    val fullName: String?,
    val nickName: String?,
    val email: String?,
    val location: String?,
    val imageProfile: File?
) {
    companion object {
        fun fromViewModel(
            userProfileViewModel: UserProfileViewModel,
        ) = UserProfileData(
            userProfileViewModel.fullName,
            userProfileViewModel.nickName,
            userProfileViewModel.email,
            userProfileViewModel.location,
            userProfileViewModel.userProfilePhotoFile
        )
    }
}

interface UserProfileDataChangeListener {
    fun onUserProfileDataChanged(data: UserProfileData)
}