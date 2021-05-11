package it.polito.mad.group25.lab.fragments.userprofile

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.navigation.findNavController
import it.polito.mad.group25.lab.R

class ShowUserProfileFragment :
    GenericUserProfileFragment(R.layout.show_user_profile_fragment) {



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        visualizeUserData(view, true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        userProfileViewModel.shownUser.value?.let {
            if (it.id == authenticationContext.userId())
                inflater.inflate(R.menu.menu, menu)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editProfile -> {
                activity?.findNavController(R.id.nav_host_fragment_content_main)
                    ?.navigate(R.id.action_showUserProfileFragment_to_editUserProfileFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}