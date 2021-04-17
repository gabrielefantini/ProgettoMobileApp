package it.polito.mad.group25.lab2.fragments.userprofile

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.navigation.findNavController
import it.polito.mad.group25.lab2.R

class ShowUserProfileFragment : GenericUserProfileFragment(R.layout.show_user_profile_fragment) {

    companion object {
        fun newInstance() = ShowUserProfileFragment()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.menu, menu)

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