package it.polito.mad.group25.lab.fragments.trip.edit

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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.fragments.trip.TripLocation
import it.polito.mad.group25.lab.fragments.trip.TripViewModel
import it.polito.mad.group25.lab.fragments.trip.details.getDurationFormatted
import it.polito.mad.group25.lab.fragments.trip.list.TripListViewModel
import it.polito.mad.group25.lab.fragments.trip.startDateFormatted
import it.polito.mad.group25.lab.fragments.trip.timeFormatted
import it.polito.mad.group25.lab.utils.persistence.PersistableContainer
import it.polito.mad.group25.lab.utils.views.fromFile
import it.polito.mad.group25.lab.utils.views.toFile
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

abstract class TripEditFragment(
    contentLayoutId: Int
) : Fragment(contentLayoutId) {

    private lateinit var tripEditViewModel: TripEditViewModel
    private val tripViewModel: TripViewModel by activityViewModels()
    private val tripListViewModel: TripListViewModel by activityViewModels()

    private lateinit var takePictureLauncher: ActivityResultLauncher<Void>
    private lateinit var pickPictureLauncher: ActivityResultLauncher<String>

    private var tripDet: MutableList<String> = mutableListOf()
    private var tripStepList: MutableList<TripLocation> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tripEditViewModel = ViewModelProvider(this).get(TripEditViewModel::class.java)
        setHasOptionsMenu(true)

        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
                val carPic = requireView().findViewById<ImageView>(R.id.carImage)
                carPic.setImageBitmap(it)
                carPic.drawable?.let { d -> tripEditViewModel.tempProfileDrawable = d }
            }
        pickPictureLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) {
                val carPic = requireView().findViewById<ImageView>(R.id.carImage)
                carPic.setImageURI(it)
                carPic.drawable?.let { d -> tripEditViewModel.tempProfileDrawable = d }
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

        val trip = tripViewModel.trip
        trip.locations.forEach {
            tripStepList.add(it)
        }
        view.findViewById<EditText>(R.id.carName).setText(trip.carName)
        depDate.text = trip.startDateFormatted()
        view.findViewById<EditText>(R.id.seatsText).setText(trip.seats.toString())
        view.findViewById<EditText>(R.id.priceText).setText(trip.price.toString())

        val last = trip.locations.lastIndex
        view.findViewById<TextView>(R.id.durationText).text = getDurationFormatted(
            trip.locations[0].locationTime,
            trip.locations[last].locationTime
        )

        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = TripAdapter(tripStepList, context)

        if (additionalInfoChips.childCount != 0)
            additionalInfoChips.removeAllViews()

        trip.additionalInfo.forEach {
            tripDet.add(it)
        }
        tripDet.forEach {
            var chip = Chip(context)
            chip.text = it
            additionalInfoChips.addView(chip)
        }

        val tripSize = tripStepList.size
        if (tripSize != 0) {
            val time1 = tripStepList[0].locationTime
            val time2 = tripStepList[tripSize - 1].locationTime
            duration.text = getDurationFormatted(time1, time2)
        } else duration.text = "-"

        view.findViewById<ImageView>(R.id.carImage)
            .fromFile(File(requireActivity().dataDir, "tripPhoto"+tripViewModel.trip.id))

        tripEditViewModel.tempProfileDrawable?.let {
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
                    depDate.text = ("$day/${month + 1}/$year")
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
            if (layout.visibility == VISIBLE) {
                view.findViewById<TextView>(R.id.time_stop).text = "--:--"
                view.findViewById<EditText>(R.id.location_stop).text.clear()
                layout.visibility = GONE
                val deleteStop = view.findViewById<ImageButton>(R.id.deleteStop)
                deleteStop.visibility = GONE
            } else {
                layout.visibility = VISIBLE
                val time_stop = view.findViewById<TextView>(R.id.time_stop)
                val save_button = view.findViewById<ImageButton>(R.id.save_stop)
                val deleteStop = view.findViewById<ImageButton>(R.id.deleteStop)
                deleteStop.visibility = GONE
                val location_stop = view.findViewById<EditText>(R.id.location_stop)

                time_stop.text = "--:--"
                location_stop.text.clear()
                save_button.tooltipText = ""

                time_stop.setOnClickListener {
                    val cal = Calendar.getInstance()
                    val timeSetListener =
                        TimePickerDialog.OnTimeSetListener { timePicker: TimePicker, hour: Int, minute: Int ->
                            cal.set(Calendar.HOUR_OF_DAY, hour)
                            cal.set(Calendar.MINUTE, minute)
                            time_stop.text = SimpleDateFormat("HH:mm").format(cal.time)
                        }
                    TimePickerDialog(
                        context,
                        timeSetListener,
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE),
                        true
                    ).show()
                }

                save_button.setOnClickListener {
                    if (location_stop.text.toString() != "" && time_stop.text.toString() != "--:--") {
                        System.out.println(time_stop.text.toString())
                        val t = TripLocation(
                            location_stop.text.toString(),
                            LocalTime.parse(time_stop.text.toString())
                        )
                        tripStepList.add(t)
                        tripStepList.sortBy { it.locationTime }
                        val tripSize = trip.locations.size
                        if (tripSize != 0) {
                            val time1 = trip.locations[0].locationTime
                            val time2 = trip.locations[tripSize - 1].locationTime
                            duration.text = getDurationFormatted(time1, time2)
                        }
                        location_stop.text.clear()
                        time_stop.text = "--:--"
                        layout.visibility = GONE
                        val rv_list = view.findViewById<RecyclerView>(R.id.tripList)
                        rv_list.adapter?.notifyDataSetChanged()
                    } else Toast.makeText(context, "Fill all fields!", Toast.LENGTH_LONG).show()

                }
            }
        }

        val addDetail = view.findViewById<FloatingActionButton>(R.id.addDetail)
        addDetail.setOnClickListener {
            val layout = view.findViewById<LinearLayout>(R.id.addDetailLayout)
            if (layout.visibility == VISIBLE) layout.visibility = GONE
            else {
                layout.visibility = VISIBLE
                val add_button = view.findViewById<ImageButton>(R.id.addDetBut)
                val detText = view.findViewById<EditText>(R.id.insertDetail)
                add_button.setOnClickListener {
                    if (detText.text.toString() == "") Toast.makeText(
                        context,
                        "Insert a text!",
                        Toast.LENGTH_LONG
                    ).show()
                    else {
                        tripDet.add(detText.text.toString())
                        trip.additionalInfo.add(detText.text.toString())
                        layout.visibility = GONE
                        var chip = Chip(context)
                        chip.text = detText.text.toString()
                        additionalInfoChips.addView(chip)
                        detText.text.clear()

                    }
                }
            }
        }

        tripEditViewModel.selectedTripLocationId.observe(viewLifecycleOwner,
            { locationId ->
                if (locationId != null) {
                    val layout = view.findViewById<LinearLayout>(R.id.add_fields_layout)
                    layout.visibility = VISIBLE

                    val tripLocationTime = view.findViewById<TextView>(R.id.time_stop)
                    val tripLocationName = view.findViewById<EditText>(R.id.location_stop)
                    val saveButton = view.findViewById<ImageButton>(R.id.save_stop)
                    val deleteStop = view.findViewById<ImageButton>(R.id.deleteStop)
                    deleteStop.visibility = VISIBLE

                    val timeInit = tripStepList[locationId].locationTime
                    val locationInit = tripStepList[locationId].location

                    tripLocationTime.text = tripStepList[locationId].timeFormatted()
                    tripLocationName.setText(tripStepList[locationId].location)
                    saveButton.tooltipText = "Update changes"

                    tripLocationTime.setOnClickListener {
                        val cal = Calendar.getInstance()
                        val timeSetListener =
                            TimePickerDialog.OnTimeSetListener { timePicker: TimePicker, hour: Int, minute: Int ->
                                cal.set(Calendar.HOUR_OF_DAY, hour)
                                cal.set(Calendar.MINUTE, minute)
                                tripLocationTime.text = SimpleDateFormat("HH:mm").format(cal.time)
                            }
                        TimePickerDialog(
                            context,
                            timeSetListener,
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            true
                        ).show()
                    }


                    saveButton.setOnClickListener {
                        //qui modifichi la lista e poi aggiorni l'adapter
                        val t = tripStepList.find {
                            !(it.locationTime.isBefore(timeInit) || it.locationTime.isAfter(timeInit))
                                    && it.location.equals(locationInit)
                        }
                        tripStepList.remove(t)
                        val trip = TripLocation(
                            tripLocationName.text.toString(),
                            LocalTime.parse(tripLocationTime.text.toString())
                        )
                        tripStepList.add(trip)
                        tripStepList.sortBy { it.locationTime }
                        tripLocationTime.text = "--:--"
                        tripLocationName.text.clear()
                        layout.visibility = GONE
                        val rv_list = view.findViewById<RecyclerView>(R.id.tripList)
                        rv_list.adapter?.notifyDataSetChanged()
                    }


                    deleteStop.setOnClickListener {
                        if (tripStepList.size > 2) {
                            val t = tripStepList.find {
                                !(it.locationTime.isBefore(timeInit) || it.locationTime.isAfter(
                                    timeInit
                                ))
                                        && it.location.equals(locationInit)
                            }
                            tripStepList.remove(t)
                            tripLocationTime.text = "--:--"
                            tripLocationName.text.clear()
                            layout.visibility = GONE
                            deleteStop.visibility = GONE
                            val rv_list = view.findViewById<RecyclerView>(R.id.tripList)
                            rv_list.adapter?.notifyDataSetChanged()
                        } else
                            Toast.makeText(
                                context,
                                "Can't delete a location\nTwo stops minimum needed.",
                                Toast.LENGTH_LONG
                            ).show()
                    }
                }
            })

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
        val tripSel = tripViewModel.trip
        val seats = view?.findViewById<EditText>(R.id.seatsText)?.text.toString().toInt()
        if (seats < 1 || seats > 7) Toast.makeText(
            context,
            "The number of seats must be between 1 and 7",
            Toast.LENGTH_LONG
        ).show()
        else {
            view?.findViewById<EditText>(R.id.carName)?.text.toString().also {
                if (tripSel.carName != it) {
                    tripSel.carName = it
                }
            }

            view?.findViewById<ImageView>(R.id.carImage)?.also {
                tripSel.carPic = it.toFile()!!
            }

            seats.also {
                if (tripSel.seats != it) {

                    tripSel.seats = it
                }
            }
            seats.also {
                if (tripSel.seats != it) {

                    tripSel.seats = it
                }
            }

            view?.findViewById<EditText>(R.id.priceText)?.text.toString().toDouble().also {
                if (tripSel.price != it) {

                    tripSel.price = it
                }
            }

            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            var date = view?.findViewById<TextView>(R.id.departureDate)?.text.toString()
            date = date.split("/").map { it -> if (it.length < 2) "0" + it else it }
                .reduce { acc, s -> "$acc/$s" }
            LocalDate.parse(date, formatter).also {
                if (tripSel.tripStartDate != it) {

                    tripSel.tripStartDate = it
                }
            }


            tripStepList.also {
                if (tripSel.locations != tripStepList) {

                    tripSel.locations.clear()
                    tripStepList.forEach { tl -> tripSel.locations.add(tl) }
                }
            }

            tripSel.additionalInfo.clear()
            tripDet.forEach {
                tripSel.additionalInfo.add(it)
            }

            tripListViewModel.updateTrip(tripSel)

            activity?.findNavController(R.id.nav_host_fragment_content_main)
                ?.navigateUp()
        }
    }


    inner class TripAdapter(var list: List<TripLocation>, val context: Context?) :
        RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

        inner class TripViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val location = v.findViewById<TextView>(R.id.trip_location)
            val time = v.findViewById<TextView>(R.id.trip_time)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
            val layout = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            return TripViewHolder(layout)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
            val item = list[position]

            holder.location.text = item.location
            holder.time.text = item.timeFormatted()

            holder.location.setOnClickListener {
                tripEditViewModel.selectTripLocation(position)
            }

            holder.time.setOnClickListener {
                tripEditViewModel.selectTripLocation(position)
            }
        }

        override fun getItemCount(): Int = list.size

        override fun getItemViewType(position: Int): Int {
            return when (position) {
                0 -> R.layout.trip_departure_line
                list.size - 1 -> R.layout.trip_destination_line
                else -> R.layout.trip_line
            }
        }

    }

}

class TripEditViewModel(application: Application) : AndroidViewModel(application),
    PersistableContainer {

    private var _selectedTripLocationId = MutableLiveData<Int>(null)
    val selectedTripLocationId: LiveData<Int> = _selectedTripLocationId
    fun selectTripLocation(id: Int) {
        _selectedTripLocationId.value = id
    }

    var tempProfileDrawable: Drawable? = null

    override fun getContext(): Context = getApplication()

}


