package it.polito.mad.group25.lab.fragments.userprofile

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import it.polito.mad.group25.lab.R

class ShowUserProfileFragment :
    GenericUserProfileFragment(R.layout.show_user_profile_fragment) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        visualizeUserData(view, true)
        view.findViewById<Button>(R.id.bought_trip_list_shortcut_button).setOnClickListener {
            findNavController()
                .navigate(R.id.action_showUserProfileFragment_to_BoughtTripsListFragment)
        }
        view.findViewById<Button>(R.id.trip_list_shortcut_button).setOnClickListener {
            findNavController()
                .navigate(R.id.action_showUserProfileFragment_to_TripListFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        userProfileViewModel.shownUser.observe(this) {
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