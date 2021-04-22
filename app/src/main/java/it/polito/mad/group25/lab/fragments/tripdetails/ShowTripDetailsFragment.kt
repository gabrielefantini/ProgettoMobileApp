package it.polito.mad.group25.lab.fragments.tripdetails

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.navigation.findNavController
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.utils.fragment.showError

class ShowTripDetailsFragment : TripDetailsFragment(R.layout.trip_details_fragment) {

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = inflater.inflate(R.menu.menu,menu)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editProfile -> {
                showError("item selected")  //TODO: collegamento con TripEditFragment
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}