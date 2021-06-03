package it.polito.mad.group25.lab.fragments.userprofile

import android.app.Application
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import it.polito.mad.group25.lab.AuthenticationContext
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.UserProfile
import it.polito.mad.group25.lab.fragments.review.ReviewViewModel
import it.polito.mad.group25.lab.utils.persistence.impl.firestore.FirestoreDocumentChanger
import it.polito.mad.group25.lab.utils.persistence.instantiator.Persistors
import it.polito.mad.group25.lab.utils.persistence.observers.ChainedObserver
import it.polito.mad.group25.lab.utils.persistence.observers.MakeReadOnlyObserver
import it.polito.mad.group25.lab.utils.persistence.observers.ToastOnErrorPersistenceObserver
import it.polito.mad.group25.lab.utils.viewmodel.PersistableViewModel
import it.polito.mad.group25.lab.utils.views.fromBlob

abstract class GenericUserProfileFragment(
    contentLayoutId: Int
) : Fragment(contentLayoutId) {

    protected val userProfileViewModel: UserProfileViewModel by activityViewModels()
    protected val authenticationContext: AuthenticationContext by activityViewModels()
    protected val reviewViewModel: ReviewViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }


    fun visualizeUserData(view: View, hideSensitiveDataIfNecessary: Boolean) {
        userProfileViewModel.shownUser.observe(viewLifecycleOwner) { it ->
            val userId = it.id
            if (it == null)
                return@observe

            it.userProfilePhotoFile?.let { it1 ->
                view.findViewById<ImageView>(R.id.profilePic)
                    .fromBlob(it1)
            }
            view.findViewById<TextView>(R.id.fullName).text = it.fullName
            view.findViewById<TextView>(R.id.email).text = it.email

            val driver_rate = reviewViewModel.reviews.values.toList()
                .filter { reviewVM -> reviewVM.reviewed == userId && reviewVM.isReviewedDriver == true }
                .map { reviewVM -> reviewVM.stars }
            if (driver_rate.isNotEmpty()) {
                var avg = 0F
                driver_rate.forEach { avg += it!! }
                avg = avg / driver_rate.size
                view.findViewById<RatingBar>(R.id.driver_rate_bar).rating = avg
            } else
                view.findViewById<RatingBar>(R.id.driver_rate_bar).rating = 0F

            val passenger_rate = reviewViewModel.reviews.values.toList()
                .filter { reviewVM -> reviewVM.reviewed == userId && reviewVM.isReviewedDriver == false }
                .map { reviewVM -> reviewVM.stars }
            if (passenger_rate.isNotEmpty()) {
                var avg = 0F
                passenger_rate.forEach { avg += it!! }
                avg = avg / passenger_rate.size
                view.findViewById<RatingBar>(R.id.passenger_rate_bar).rating = avg
            } else
                view.findViewById<RatingBar>(R.id.passenger_rate_bar).rating = 0F

            val nickname = view.findViewById<TextView>(R.id.nickName)
            val location = view.findViewById<TextView>(R.id.location)
            nickname.text = it.nickName
            location.text = it.location

            val censurableVisibility =
                if (hideSensitiveDataIfNecessary && it.id != authenticationContext.userId())
                    GONE else VISIBLE
            view.findViewById<Button>(R.id.bought_trip_list_shortcut_button).visibility =
                censurableVisibility
            view.findViewById<Button>(R.id.trip_list_shortcut_button).visibility =
                censurableVisibility
            view.findViewById<TextView>(R.id.nickNameSection).visibility = censurableVisibility
            view.findViewById<TextView>(R.id.locationSection).visibility = censurableVisibility
            nickname.visibility = censurableVisibility
            location.visibility = censurableVisibility


        }
    }
}

class UserProfileViewModel(application: Application) : PersistableViewModel(application) {

    private val shownUserDocumentChanger: FirestoreDocumentChanger<MutableLiveData<UserProfile>> =
        FirestoreDocumentChanger()

    val shownUser: MutableLiveData<UserProfile>
            by Persistors.simpleLiveFirestore(
                collection = "users",
                lazyInit = true,
                default = MutableLiveData(),
                documentChanger = shownUserDocumentChanger,
                observer = ChainedObserver.startingFrom(MakeReadOnlyObserver<MutableLiveData<UserProfile>>())
                    .wrappedBy { ToastOnErrorPersistenceObserver(application, it) }.build()
            )

    fun showUser(id: String) {
        shownUserDocumentChanger.changeDocument(id)
    }

}

