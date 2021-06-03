package it.polito.mad.group25.lab.fragments.userprofile

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
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

        censure(view)
        visualizeRating(view)
    }

    private fun censure(view: View) {
        userProfileViewModel.shownUser.observe(viewLifecycleOwner) {
            if (it == null)
                return@observe

            val censurableVisibility =
                if (it.id != authenticationContext.userId())
                    View.GONE else View.VISIBLE

            view.findViewById<Button>(R.id.bought_trip_list_shortcut_button).visibility =
                censurableVisibility
            view.findViewById<Button>(R.id.trip_list_shortcut_button).visibility =
                censurableVisibility
            view.findViewById<TextView>(R.id.nickNameSection).visibility = censurableVisibility
            view.findViewById<TextView>(R.id.locationSection).visibility = censurableVisibility
        }
    }

    private fun visualizeRating(view: View) {
        userProfileViewModel.shownUser.observe(viewLifecycleOwner) { it ->
            if (it == null)
                return@observe

            val driverRate = reviewViewModel.reviews.values.toList()
                .filter { reviewVM -> reviewVM.reviewed == it.id && reviewVM.isReviewedDriver == true }
                .map { reviewVM -> reviewVM.stars }
            if (driverRate.isNotEmpty()) {
                var avg = 0F
                driverRate.forEach { avg += it!! }
                avg /= driverRate.size
                view.findViewById<RatingBar>(R.id.driver_rate_bar).rating = avg
            } else
                view.findViewById<RatingBar>(R.id.driver_rate_bar).rating = 0F

            val passengerRate = reviewViewModel.reviews.values.toList()
                .filter { reviewVM -> reviewVM.reviewed == it.id && reviewVM.isReviewedDriver == false }
                .map { reviewVM -> reviewVM.stars }
            if (passengerRate.isNotEmpty()) {
                var avg = 0F
                passengerRate.forEach { avg += it!! }
                avg /= passengerRate.size
                view.findViewById<RatingBar>(R.id.passenger_rate_bar).rating = avg
            } else
                view.findViewById<RatingBar>(R.id.passenger_rate_bar).rating = 0F
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