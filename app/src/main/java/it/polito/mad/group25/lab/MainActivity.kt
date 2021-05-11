package it.polito.mad.group25.lab

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import it.polito.mad.group25.lab.databinding.ActivityMainBinding
import it.polito.mad.group25.lab.fragments.userprofile.UserProfileViewModel
import it.polito.mad.group25.lab.utils.views.fromByteList

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var activityMainBinding: ActivityMainBinding

    private lateinit var userProfileViewModel: UserProfileViewModel

    /*companion object {
        init {
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        AuthenticationContext.userID = "3"

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
                R.id.TripListFragment,
                R.id.OthersTripListFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        navView.menu.findItem(R.id.nav_user_profile).setOnMenuItemClickListener {
            navController.navigate(R.id.showUserProfileFragment)
            drawerLayout.closeDrawers()
            true
        }

        navView.menu.findItem(R.id.nav_trip_list).setOnMenuItemClickListener {
            navController.navigate(R.id.TripListFragment)
            drawerLayout.closeDrawers()
            true
        }

        navView.menu.findItem(R.id.nav_others_trip_list).setOnMenuItemClickListener {
            navController.navigate(R.id.OthersTripListFragment)
            drawerLayout.closeDrawers()
            true
        }

        userProfileViewModel = ViewModelProvider(this).get(UserProfileViewModel::class.java)

        userProfileViewModel.shownUser.observe(this) { data ->
            if (data == null)
                return@observe

            val parent = activityMainBinding.navView.getHeaderView(0)

            parent.findViewById<ImageView>(R.id.nav_header_user_profile_pic)
                ?.run { this.fromByteList(data.userProfilePhotoFile) }

            parent.findViewById<TextView>(R.id.nav_header_user_profile_nick)
                ?.run { text = data.nickName }
            parent.findViewById<TextView>(R.id.nav_header_user_profile_email)
                ?.run { text = data.email }
        }

        updateNavHeaderUserInfo()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    private fun updateNavHeaderUserInfo() {


    }

}