package it.polito.mad.group25.lab.fragments.tripdetails

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.utils.entities.TripLocation
import it.polito.mad.group25.lab.utils.viewmodel.PersistableContainer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

abstract class TripDetailsFragment(
    contentLayoutId: Int
) : Fragment(contentLayoutId) {

    private lateinit var tripDetailsViewModel: TripDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tripDetailsViewModel = ViewModelProvider(this).get(TripDetailsViewModel::class.java)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = this.context

        /*view.findViewById<TextView>(R.id.carName).text = "---"
        view.findViewById<TextView>(R.id.departureDate).text = "---"
        view.findViewById<TextView>(R.id.seatsText).text = "---"*/

        val rv = view.findViewById<RecyclerView>(R.id.tripList)
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = TripAdapter(tripDetailsViewModel.tripLocations)

        val additionalInfoChips = view.findViewById<ChipGroup>(R.id.additionalInfoChips)
        tripDetailsViewModel.chips.forEach {
            var chip = Chip(context)
            chip.text = it
            additionalInfoChips.addView(chip)
        }
    }

}

class TripDetailsViewModel(application: Application): AndroidViewModel(application),
        PersistableContainer{

    //andrà sostituito con il view model condiviso
    var chips = listOf("chip1","chip2")

    @RequiresApi(Build.VERSION_CODES.O)
    var tripLocations = mutableListOf(
            Trip2("loc1",LocalDateTime.now()),
            Trip2("loc2",LocalDateTime.now().plusMinutes(30)),
            Trip2("loc3",LocalDateTime.now().plusMinutes(60)),
            Trip2("loc4",LocalDateTime.now().plusMinutes(90)))


    //

    override fun getContext(): Context = getApplication()
}

@RequiresApi(Build.VERSION_CODES.O)
class Trip2 (l:String, d:LocalDateTime) {
    var location: String = l
    var time: String = d.format(DateTimeFormatter.ofPattern("HH:mm"))
}


class TripAdapter(val list:List<Trip2>): RecyclerView.Adapter<TripAdapter.TripViewHolder>(){

    class TripViewHolder(v:View): RecyclerView.ViewHolder(v){
        val location = v.findViewById<TextView>(R.id.trip_location)
        val time = v.findViewById<TextView>(R.id.trip_time)

        fun bind(t:Trip2){
            location.text = t.location
            time.text = t.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(viewType,parent,false)
        return TripViewHolder(layout)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        if(list.size == 1) return R.layout.trip_destination_line
        return when(position){
            0 -> R.layout.trip_departure_line
            list.size-1 -> R.layout.trip_destination_line
            else -> R.layout.trip_line
        }
    }

}