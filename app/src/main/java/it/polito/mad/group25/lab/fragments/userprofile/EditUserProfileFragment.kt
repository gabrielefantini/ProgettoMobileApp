package it.polito.mad.group25.lab.fragments.userprofile

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.textfield.TextInputLayout
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.utils.fragment.showError
import it.polito.mad.group25.lab.utils.views.isCompliant
import it.polito.mad.group25.lab.utils.views.setConstraints
import it.polito.mad.group25.lab.utils.views.toFile
import java.io.File

class EditUserProfileFragment :
    GenericUserProfileFragment(R.layout.edit_user_profile_fragment) {

    private lateinit var takePictureLauncher: ActivityResultLauncher<Void>
    private lateinit var pickPictureLauncher: ActivityResultLauncher<String>
    private lateinit var viewModel: EditUserProfileViewModel
    private lateinit var fullNameTextInputLayout: TextInputLayout
    private lateinit var usernameTextInputLayout: TextInputLayout
    private lateinit var emailTextInputLayout: TextInputLayout
    private lateinit var locationTextInputLayout: TextInputLayout

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

        fullNameTextInputLayout =
            requireView().findViewById<TextInputLayout>(R.id.fullNameTextLayout)
                .apply {
                    setConstraints(
                        R.id.fullName,
                        resources.getString(R.string.fullNameMissingError),
                        CharSequence::isNotBlank
                    )
                }
        usernameTextInputLayout =
            requireView().findViewById<TextInputLayout>(R.id.nickNameTextLayout)
                .apply {
                    setConstraints(
                        R.id.nickName,
                        resources.getString(R.string.nickNameMissingError),
                        CharSequence::isNotBlank
                    )
                }
        emailTextInputLayout = requireView().findViewById<TextInputLayout>(R.id.emailTextLayout)
            .apply {
                setConstraints(
                    R.id.email,
                    resources.getString(R.string.emailMissingError),
                    CharSequence::isNotBlank
                )
            }
        locationTextInputLayout =
            requireView().findViewById<TextInputLayout>(R.id.locationTextLayout)
                .apply {
                    setConstraints(
                        R.id.location,
                        resources.getString(R.string.locationMissingError),
                        CharSequence::isNotBlank
                    )
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
                if (saveEdits())
                    activity?.findNavController(R.id.nav_host_fragment_content_main)
                        ?.navigateUp()
                else {
                    showError(resources.getString(R.string.provideAllInfoError))
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveEdits(): Boolean {
        if (!(fullNameTextInputLayout.isCompliant()
                    && usernameTextInputLayout.isCompliant()
                    && emailTextInputLayout.isCompliant()
                    && locationTextInputLayout.isCompliant())
        ) return false

        userProfileViewModel.fullName =
            requireActivity().findViewById<TextView>(R.id.fullName).text.toString()

        userProfileViewModel.nickName =
            requireActivity().findViewById<TextView>(R.id.nickName).text.toString()

        userProfileViewModel.email =
            requireActivity().findViewById<TextView>(R.id.email).text.toString()

        userProfileViewModel.location =
            requireActivity().findViewById<TextView>(R.id.location).text.toString()

        requireView().findViewById<ImageView>(R.id.profilePic).toFile()
            ?.let { userProfileViewModel.userProfilePhotoFile = it }

        fireDataChanges()
        return true
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