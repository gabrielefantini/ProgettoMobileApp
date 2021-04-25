package it.polito.mad.group25.lab.fragments.tripedit

import android.app.Activity
import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.SharedViewModel
import it.polito.mad.group25.lab.fragments.tripdetails.TripLocationAdapter
import it.polito.mad.group25.lab.fragments.tripdetails.getDurationFormatted
import it.polito.mad.group25.lab.utils.entities.TripLocation
import it.polito.mad.group25.lab.utils.entities.addTripOrdered
import it.polito.mad.group25.lab.utils.entities.startDateFormatted
import it.polito.mad.group25.lab.utils.viewmodel.PersistableContainer
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*

abstract class TripEditFragment(
    contentLayoutId: Int
) : Fragment(contentLayoutId) {

    private lateinit var tripEditViewModel: TripEditViewModel

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var takePictureLauncher: ActivityResultLauncher<Void>
    private lateinit var pickPictureLauncher: ActivityResultLauncher<String>
    private var idTrip: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tripEditViewModel = ViewModelProvider(this).get(TripEditViewModel::class.java)
        setHasOptionsMenu(true)

        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
                val carPic = requireView().findViewById<ImageView>(R.id.carImage)
                carPic.setImageBitmap(it)
                carPic.drawable?.let { d -> tripEditViewModel.tempCarDrawable = d }
            }
        pickPictureLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) {
                val carPic = requireView().findViewById<ImageView>(R.id.carImage)
                carPic.setImageURI(it)
                carPic.drawable?.let { d -> tripEditViewModel.tempCarDrawable = d }
            }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        val context = this.context

        val rv = view.findViewById<RecyclerView>(R.id.tripList)
        val additionalInfoChips = view.findViewById<ChipGroup>(R.id.additionalInfoChips)
        val depDate = view.findViewById<TextView>(R.id.departureDate)
        val duration = view.findViewById<TextView>(R.id.durationText)


        sharedViewModel.tripSelected.observe(viewLifecycleOwner,{ tripId ->
            var trip = sharedViewModel.tripList.value?.get(tripId)
            if(trip != null){
                idTrip = tripId

                view.findViewById<EditText>(R.id.carName).setText(trip.carName)
                depDate.text = trip.startDateFormatted()
                view.findViewById<EditText>(R.id.seatsText).setText(trip.seats.toString())
                view.findViewById<EditText>(R.id.priceText).setText(trip.price.toString())

                val last = trip.locations.lastIndex
                view.findViewById<TextView>(R.id.durationText).text = getDurationFormatted(trip.locations[0].locationTime,trip.locations[last].locationTime)

                rv.layoutManager = LinearLayoutManager(context)
                rv.adapter = TripLocationAdapter(trip.locations)

                if (additionalInfoChips.childCount != 0)
                    additionalInfoChips.removeAllViews()

                trip.additionalInfo.forEach {
                    var chip = Chip(context)
                    chip.text = it
                    additionalInfoChips.addView(chip)
                }

                val tripSize = trip.locations.size
                if (tripSize != 0) {
                    val time1 = sharedViewModel.tripList.value?.get(idTrip)!!.locations[0].locationTime
                    val time2 = sharedViewModel.tripList.value?.get(idTrip)!!.locations[tripSize-1].locationTime
                    duration.text = getDurationFormatted(time1, time2)
                }
                else duration.text = "-"
            }
        })


        //da vedere gestione immagine
        tripEditViewModel.tempCarDrawable?.let {
            view.findViewById<ImageView>(R.id.carImage).setImageDrawable(it)
        }

        depDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            var datePickerDialog = DatePickerDialog(
                    this.requireActivity(),
                    DatePickerDialog.OnDateSetListener { view, year, month, day ->
                        // Display Selected date in TextView
                        depDate.text = ("$day/${month+1}/$year")
                    },
                    year, month, day
            )
            datePickerDialog.show()
        }

        val imageButton = view.findViewById<ImageButton>(R.id.changeCarPicButton)
        registerForContextMenu(imageButton)
        imageButton.setOnClickListener {
            it.showContextMenu()
        }

        val addButton = view.findViewById<FloatingActionButton>(R.id.addTripStop)
        addButton.setOnClickListener {
            val layout = view.findViewById<LinearLayout>(R.id.add_fields_layout)
            if (layout.visibility == VISIBLE) layout.visibility = GONE
            else {
                layout.visibility = VISIBLE
                val time_stop = view.findViewById<TextView>(R.id.time_stop)
                time_stop.setOnClickListener {
                    val cal = Calendar.getInstance()
                    val timeSetListener = TimePickerDialog.OnTimeSetListener{timePicker:TimePicker, hour:Int, minute:Int ->
                        cal.set(Calendar.HOUR_OF_DAY, hour)
                        cal.set(Calendar.MINUTE, minute)
                        time_stop.text = SimpleDateFormat("HH:mm").format(cal.time)
                    }
                    TimePickerDialog(context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                }
                val save_button = view.findViewById<ImageButton>(R.id.save_stop)
                val location_stop = view.findViewById<EditText>(R.id.location_stop)
                save_button.setOnClickListener {
                    if(location_stop.text.toString()!= "" && time_stop.text.toString()!="--:--") {
                        val trip = sharedViewModel.tripList.value?.get(idTrip)!!
                        trip.addTripOrdered(location_stop.text.toString(), LocalTime.parse(time_stop.text.toString()))
                        val tripSize = trip.locations.size
                        if (tripSize != 0) {
                            val time1 = sharedViewModel.tripList.value?.get(idTrip)!!.locations[0].locationTime
                            val time2 = sharedViewModel.tripList.value?.get(idTrip)!!.locations[tripSize-1].locationTime
                            duration.text = getDurationFormatted(time1, time2)
                        }
                        location_stop.text.clear()
                        time_stop.text = "--:--"
                        layout.visibility = GONE
                        val rv_list = view.findViewById<RecyclerView>(R.id.tripList)
                        rv_list.adapter?.notifyDataSetChanged()
                    }
                    else Toast.makeText(context, "Fill all fields!", Toast.LENGTH_LONG).show()

                }
            }
        }

    }


    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = requireActivity().menuInflater
        inflater.inflate(R.menu.menu_edit_propic, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.photo -> {
                takePictureLauncher.launch(null)
                true
            }
            R.id.gallery -> {
                pickPictureLauncher.launch("image/*")
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.menu_foto, menu)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveProfileEdit -> {
                saveEdits()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveEdits() {
        if (idTrip != -1) {
            val tripSel = sharedViewModel.tripList.value?.get(idTrip)
            if (tripSel != null) {
                try {
                    val price = view?.findViewById<EditText>(R.id.priceText)?.text.toString().toDouble()
                    val seats = view?.findViewById<EditText>(R.id.seatsText)?.text.toString().toInt()
                    tripSel.carName = view?.findViewById<EditText>(R.id.carName)?.text.toString()
                    tripSel.seats = seats
                    tripSel.price = price
                    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    var date = view?.findViewById<TextView>(R.id.departureDate)?.text.toString()
                    date = date.split("/").map { it -> if (it.length < 2) "0"+it else it}.reduce { acc, s ->  acc+"/"+s}
                    tripSel.tripStartDate = LocalDate.parse(date, formatter)
                    tripSel.carPic = tripEditViewModel.tempCarDrawable.toString()
                    activity?.findNavController(R.id.nav_host_fragment_content_main)
                            ?.navigateUp()
                }
                catch (e: Exception) {
                    Toast.makeText(context, "Fill all fields properly!", Toast.LENGTH_LONG).show()
                }

            }
        }
    }
}

class TripEditViewModel(application: Application) : AndroidViewModel(application),
    PersistableContainer {

    override fun getContext(): Context = getApplication()

    //da vedere gestione immagine
    var tempCarDrawable: Drawable? = null
}

/*data class Trip2
    (
    var location: String,
    var time: String,
)*/

/*
class TripAdapter(val list: List<Trip2>) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    class TripViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val location = v.findViewById<TextView>(R.id.trip_location)
        val time = v.findViewById<TextView>(R.id.trip_time)

        fun bind(t: Trip2) {
            location.text = t.location
            time.text = t.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return TripViewHolder(layout)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> R.layout.trip_departure_line
            list.size - 1 -> R.layout.trip_destination_line
            else -> R.layout.trip_line
        }
    }

}*/
