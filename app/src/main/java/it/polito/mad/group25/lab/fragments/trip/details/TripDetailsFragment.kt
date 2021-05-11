package it.polito.mad.group25.lab.fragments.trip.details

import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.fragments.trip.*
import it.polito.mad.group25.lab.fragments.trip.list.TripListViewModel
import it.polito.mad.group25.lab.utils.fragment.showError
import it.polito.mad.group25.lab.utils.views.fromFile
import java.io.File
import it.polito.mad.group25.lab.fragments.trip.TripLocation
import it.polito.mad.group25.lab.fragments.trip.TripViewModel
import it.polito.mad.group25.lab.fragments.trip.startDateFormatted
import it.polito.mad.group25.lab.fragments.trip.timeFormatted
import it.polito.mad.group25.lab.utils.toLocalDateTime
import it.polito.mad.group25.lab.utils.views.fromBlob
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

abstract class TripDetailsFragment(
    contentLayoutId: Int
) : Fragment(contentLayoutId) {

    private val tripViewModel: TripViewModel by activityViewModels()
    private val tripListViewModel: TripListViewModel by activityViewModels()
    private var isOwner = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if(tripListViewModel.userId == tripViewModel.trip.ownerId)
            isOwner = true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater){
        if(isOwner)
            inflater.inflate(R.menu.menu, menu)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        val context = this.context

        val rv = view.findViewById<RecyclerView>(R.id.tripList)
        val additionalInfoChips = view.findViewById<ChipGroup>(R.id.additionalInfoChips)

        //probabilmente il trip sarà MutableLiveData e sarà da "osservare" in quanto la lista di interessati può variare nel tempo oppure può non essere più disponibile
        val trip = tripViewModel.trip

        view.findViewById<TextView>(R.id.carName).text = trip.carName
        view.findViewById<TextView>(R.id.departureDate).text = trip.startDateFormatted()
        view.findViewById<TextView>(R.id.seatsText).text = trip.seats.toString()
        view.findViewById<TextView>(R.id.priceText).text = trip.price.toString()

        val last = trip.locations.lastIndex
        view.findViewById<TextView>(R.id.durationText).text =
            getDurationFormatted(
                trip.locations[0].locationTime.toLocalDateTime(),
                trip.locations[last].locationTime.toLocalDateTime()
            )

        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = TripLocationAdapter(trip.locations)

        if (additionalInfoChips.childCount != 0)
            additionalInfoChips.removeAllViews()

        if(trip.additionalInfo.size == 0){
            additionalInfoChips.visibility = GONE
            view.findViewById<TextView>(R.id.noOtherInfos).visibility = VISIBLE
        }else {
            trip.additionalInfo.forEach {
                val chip = Chip(context)
                chip.text = it
                additionalInfoChips.addView(chip)
            }
        }
        trip.carPic?.let {
            view.findViewById<ImageView>(R.id.carImage)
                .fromBlob(it)
        }

        val div = view.findViewById<View>(R.id.dividerInfo)
        val intUserText = view.findViewById<TextView>(R.id.interestedUsers)
        val rv2 = view.findViewById<RecyclerView>(R.id.userList)

        if(!isOwner) {
            //normal user
            var fab = view.findViewById<FloatingActionButton>(R.id.tripDetailsFab)

            fab.visibility = VISIBLE
            rv2.visibility = GONE
            div.visibility = GONE
            intUserText.visibility = GONE

            fab.setOnClickListener {
                showError("Sent confirmation request to the trip's owner!")
                tripViewModel.addCurrentUserToSet(tripListViewModel.userId)
                tripListViewModel.putTrip(tripViewModel.trip)
            }

        }else{
            //trip owner
            if(tripViewModel.trip.interestedUsers.size == 0){
                //no interested users
                view.findViewById<TextView>(R.id.noIntUsers).visibility = VISIBLE
                rv2.visibility = GONE

            }else {
                //at least one interested user
                rv2.layoutManager = LinearLayoutManager(context)
                rv2.adapter = TripUsersAdapter(
                    tripViewModel.trip.interestedUsers.toList()
                )

            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getDurationFormatted(first: LocalDateTime, last: LocalDateTime): String {
    val durationMin = ChronoUnit.MINUTES.between(first, last).toInt()
    var hours = durationMin / 60
    val min = durationMin % 60

    val days = hours / 24
    if (days != 0)
        hours -= 24

    return "${if (days != 0) "${days}d" else ""} ${if (hours != 0) "${hours}h" else ""} ${if (min != 0) "${min}min" else ""}"
}

class TripLocationAdapter(private val list: List<TripLocation>) :
    RecyclerView.Adapter<TripLocationAdapter.TripLocationViewHolder>() {

    class TripLocationViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val location: TextView = v.findViewById(R.id.trip_location)
        private val time: TextView = v.findViewById(R.id.trip_time)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(t: TripLocation) {
            location.text = t.location
            time.text = t.timeFormatted()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripLocationViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return TripLocationViewHolder(layout)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: TripLocationViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        //caso limite iniziale: solo una tappa
        if (list.size == 1) return R.layout.trip_departure_line
        //altrimenti
        return when (position) {
            0 -> R.layout.trip_departure_line
            list.size - 1 -> R.layout.trip_destination_line
            else -> R.layout.trip_line
        }
    }
}

/*class TripUser (val userId: String){
    var isConfirmed: Boolean = false
}*/

class TripUsersAdapter(private val list: List<String>) :
    RecyclerView.Adapter<TripUsersAdapter.TripUsersViewHolder>() {

    class TripUsersViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val username: TextView = v.findViewById(R.id.username)

        fun bind(t: String) {
            username.text = "user $t"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripUsersViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return TripUsersViewHolder(layout)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: TripUsersViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int = R.layout.trip_user_line

}