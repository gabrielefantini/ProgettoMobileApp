package it.polito.mad.group25.lab.fragments.trip.details

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.navigation.findNavController
import it.polito.mad.group25.lab.R

class ShowTripDetailsFragment : TripDetailsFragment(R.layout.trip_details_fragment) {

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editProfile -> {
                activity?.findNavController(R.id.nav_host_fragment_content_main)
                    ?.navigate(R.id.action_showTripDetailsFragment_to_showTripEditFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}