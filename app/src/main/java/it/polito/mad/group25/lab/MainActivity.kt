package it.polito.mad.group25.lab

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import it.polito.mad.group25.lab.databinding.ActivityMainBinding
import it.polito.mad.group25.lab.fragments.userprofile.UserProfileData
import it.polito.mad.group25.lab.fragments.userprofile.UserProfileDataChangeListener
import it.polito.mad.group25.lab.fragments.userprofile.UserProfileViewModel
import it.polito.mad.group25.lab.utils.entities.Trip
import it.polito.mad.group25.lab.utils.views.fromFile

class MainActivity : AppCompatActivity(), UserProfileDataChangeListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var activityMainBinding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        setSupportActionBar(activityMainBinding.appBarMain.toolbar)


        val drawerLayout: DrawerLayout = activityMainBinding.drawerLayout
        val navView: NavigationView = activityMainBinding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.showUserProfileFragment,
                R.id.editUserProfileFragment,
                R.id.showTripDetailsFragment,
                R.id.showTripEditFragment,
                R.id.TripListFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        navView.menu.findItem(R.id.nav_user_profile).setOnMenuItemClickListener {
            navController.navigate(R.id.showUserProfileFragment)
            drawerLayout.closeDrawers()
            true
        }
        navView.menu.findItem(R.id.nav_trip_details).setOnMenuItemClickListener {
            navController.navigate(R.id.showTripDetailsFragment)
            drawerLayout.closeDrawers()
            true
        }
        navView.menu.findItem(R.id.nav_trip_edit).setOnMenuItemClickListener {
            navController.navigate(R.id.showTripEditFragment)
            drawerLayout.closeDrawers()
            true
        }
        navView.menu.findItem(R.id.nav_trip_list).setOnMenuItemClickListener {
            navController.navigate(R.id.TripListFragment)
            drawerLayout.closeDrawers()
            true
        }


        updateNavHeaderUserInfo(
            UserProfileData.fromViewModel(
                ViewModelProvider(this).get(UserProfileViewModel::class.java)
            )
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    override fun onUserProfileDataChanged(data: UserProfileData) = updateNavHeaderUserInfo(data)

    private fun updateNavHeaderUserInfo(data: UserProfileData) {
        val parent = activityMainBinding.navView.getHeaderView(0)
        parent.findViewById<ImageView>(R.id.nav_header_user_profile_pic)
            ?.run { data.imageProfile?.also{ fromFile(data.imageProfile) } }

        parent.findViewById<TextView>(R.id.nav_header_user_profile_nick)
            ?.run { data.nickName?.also { text = it } }

        parent.findViewById<TextView>(R.id.nav_header_user_profile_email)
            ?.run { data.email?.also { text = it } }
    }

}