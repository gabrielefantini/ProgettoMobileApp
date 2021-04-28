package it.polito.mad.group25.lab.fragments.trip.edit

import android.annotation.SuppressLint
import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.transition.TransitionManager
import android.view.*
import android.view.View.*
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
import com.google.android.material.textfield.TextInputLayout
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.fragments.trip.TripLocation
import it.polito.mad.group25.lab.fragments.trip.TripViewModel
import it.polito.mad.group25.lab.fragments.trip.details.getDurationFormatted
import it.polito.mad.group25.lab.fragments.trip.list.TripListViewModel
import it.polito.mad.group25.lab.fragments.trip.startDateFormatted
import it.polito.mad.group25.lab.fragments.trip.timeFormatted
import it.polito.mad.group25.lab.utils.fragment.showError
import it.polito.mad.group25.lab.utils.persistence.PersistableContainer
import it.polito.mad.group25.lab.utils.views.fromFile
import it.polito.mad.group25.lab.utils.views.isCompliant
import it.polito.mad.group25.lab.utils.views.setConstraints
import it.polito.mad.group25.lab.utils.views.toFile
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
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

    private lateinit var carNameLayout: TextInputLayout
    private lateinit var priceLayout: TextInputLayout
    private lateinit var seatsLayout: TextInputLayout


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
        //con requireActivity() crasha dopo la rotazione
        carNameLayout = view.findViewById(R.id.carNameLayout)
        priceLayout = view.findViewById(R.id.priceTextLayout)
        seatsLayout = view.findViewById(R.id.seatsTextLayout)


        carNameLayout.setConstraints(
            R.id.carName,
            "Please provide car name",
            checker = CharSequence::isNotBlank
        )
        priceLayout.setConstraints(
            R.id.priceText,
            "Please provide trip price",
            true,
            CharSequence::isNotBlank
        )
        seatsLayout.setConstraints(
            R.id.seatsText,
            "Please provide valid seats between 1 and 7",
            true
        ) { it.isNotBlank() && it.toString().toInt().let { s -> s in 1..7 } }


        val context = this.context

        val rv = view.findViewById<RecyclerView>(R.id.tripList)
        val additionalInfoChips = view.findViewById<ChipGroup>(R.id.additionalInfoChips)
        val depDate = view.findViewById<TextView>(R.id.departureDate)

        val trip = tripViewModel.trip
        trip.locations.forEach {
            tripStepList.add(it)
        }
        updateDuration(view)

        view.findViewById<EditText>(R.id.carName).setText(trip.carName)
        depDate.text = trip.startDateFormatted()
        view.findViewById<EditText>(R.id.seatsText).setText(trip.seats.toString())
        view.findViewById<EditText>(R.id.priceText).setText(trip.price.toString())


        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = TripAdapter(tripStepList, context)

        if (additionalInfoChips.childCount != 0)
            additionalInfoChips.removeAllViews()

        trip.additionalInfo.forEach {
            tripDet.add(it)
        }
        tripDet.forEach {
            additionalInfoChips.addView(getChip(additionalInfoChips,it))
        }

        view.findViewById<ImageView>(R.id.carImage)
            .fromFile(File(requireActivity().dataDir, "tripPhoto" + tripViewModel.trip.id))

        tripEditViewModel.tempProfileDrawable?.let {
            view.findViewById<ImageView>(R.id.carImage).setImageDrawable(it)
        }

        depDate.setOnClickListener {
            openDatePicker(tv = depDate)
        }

        val imageButton = view.findViewById<ImageButton>(R.id.changeCarPicButton)
        registerForContextMenu(imageButton)
        imageButton.setOnClickListener {
            it.showContextMenu()
        }

        //aggiunta nuova TripLocation ***************************************

        val dateStop = view.findViewById<TextView>(R.id.dateStop)
        val timeStop = view.findViewById<TextView>(R.id.time_stop)
        val locationStop = view.findViewById<EditText>(R.id.location_stop)

        val deleteStop = view.findViewById<ImageButton>(R.id.deleteStop)
        val saveButton = view.findViewById<ImageButton>(R.id.save_stop)

        val addButton = view.findViewById<FloatingActionButton>(R.id.addTripStop)
        addButton.setOnClickListener {
            val layout = view.findViewById<LinearLayout>(R.id.add_fields_layout)
            if (layout.visibility == VISIBLE) {

                dateStop.text = "--/--/----"
                timeStop.text = "--:--"
                locationStop.text.clear()

                layout.visibility = GONE
                deleteStop.visibility = GONE
            } else {
                layout.visibility = VISIBLE
                deleteStop.visibility = GONE


                dateStop.text = "--/--/----"
                timeStop.text = "--:--"
                locationStop.text.clear()

                dateStop.setOnClickListener {
                    openDatePicker(tv = dateStop)
                }

                timeStop.setOnClickListener {
                    openTimePicker(tv = timeStop)
                }

                saveButton.setOnClickListener {
                    if (locationStop.text.toString() != "" && timeStop.text.toString() != "--:--" && dateStop.text.toString() != "--/--/----") {
                        //System.out.println(time_stop.text.toString())
                        val date = dateStop.text.toString().split("/")
                            .map { if (it.length < 2) "0$it" else it }
                            .reduce { acc, s -> "$acc/$s" }
                        val formatter: DateTimeFormatter =
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                        val t = TripLocation(
                            locationStop.text.toString(),
                            LocalDateTime.parse(
                                date + " " + timeStop.text.toString(),
                                formatter
                            )
                        )
                        tripStepList.add(t)
                        tripStepList.sortBy { it.locationTime }

                        updateDuration(view)

                        dateStop.text = "--/--/----"
                        timeStop.text = "--:--"
                        locationStop.text.clear()
                        layout.visibility = GONE

                        rv.adapter?.notifyDataSetChanged()
                    } else Toast.makeText(context, "Fill all fields!", Toast.LENGTH_LONG).show()

                }
            }
        }

        //*****************************************************************************

        val addDetail = view.findViewById<FloatingActionButton>(R.id.addDetail)
        addDetail.setOnClickListener {
            val layout = view.findViewById<LinearLayout>(R.id.addDetailLayout)
            if (layout.visibility == VISIBLE) layout.visibility = INVISIBLE
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
                        layout.visibility = INVISIBLE

                        additionalInfoChips.addView(getChip(additionalInfoChips,detText.text.toString()))
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
                    deleteStop.visibility = VISIBLE

                    val timeInit = tripStepList[locationId].locationTime
                    val locationInit = tripStepList[locationId].location

                    dateStop.text = tripStepList[locationId].locationTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    timeStop.text = tripStepList[locationId].timeFormatted()
                    locationStop.setText(tripStepList[locationId].location)

                    dateStop.setOnClickListener {
                        openDatePicker(timeInit,dateStop)
                    }

                    timeStop.setOnClickListener {
                        openTimePicker(timeInit,timeStop)
                    }

                    saveButton.setOnClickListener {
                        val t = tripStepList.find {
                            !(it.locationTime.isBefore(timeInit) || it.locationTime.isAfter(timeInit))
                                    && it.location == locationInit
                        }
                        tripStepList.remove(t)
                        val formatter: DateTimeFormatter =
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                        val date = dateStop.text.toString().split("/")
                                .map { if (it.length < 2) "0$it" else it }
                                .reduce { acc, s -> "$acc/$s" }

                        tripStepList.add(
                                TripLocation(
                                        locationStop.text.toString(),
                                        LocalDateTime.parse(date+" "+timeStop.text.toString(), formatter)
                                )
                        )

                        tripStepList.sortBy { it.locationTime }

                        updateDuration(view)

                        dateStop.text = "--/--/----"
                        timeStop.text = "--:--"
                        locationStop.text.clear()

                        layout.visibility = GONE
                        rv.adapter?.notifyDataSetChanged()
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

                            updateDuration(view)

                            dateStop.text = "--/--/----"
                            timeStop.text = "--:--"
                            locationStop.text.clear()

                            layout.visibility = GONE
                            deleteStop.visibility = GONE
                            rv.adapter?.notifyDataSetChanged()
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
                if (saveEdits())
                    activity?.findNavController(R.id.nav_host_fragment_content_main)
                        ?.navigateUp()
                else showError("Please provide all required info")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveEdits(): Boolean {
        if (!(carNameLayout.isCompliant() && priceLayout.isCompliant() && seatsLayout.isCompliant()))
            return false

        val tripSel = tripViewModel.trip
        val seats = view?.findViewById<EditText>(R.id.seatsText)?.text.toString().toInt()

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
        date = date.split("/").map { it -> if (it.length < 2) "0$it" else it }
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

        tripDet.also {
            if(tripSel.additionalInfo != it){
                tripSel.additionalInfo.clear()
                it.forEach { td -> tripSel.additionalInfo.add(td) }
            }
        }

        tripListViewModel.putTrip(tripSel)

        return true
    }


    inner class TripAdapter(var list: List<TripLocation>, val context: Context?) :
        RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

        inner class TripViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val location: TextView = v.findViewById<TextView>(R.id.trip_location)
            val time : TextView = v.findViewById<TextView>(R.id.trip_time)
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

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun getChip(chipGroup: ChipGroup, chipText: String): Chip{
        val chip = Chip(context)
        chip.text = chipText

        chip.isClickable = false //test
        chip.isCheckable = false

        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            TransitionManager.beginDelayedTransition(chipGroup)
            tripDet.remove(chipText)
            chipGroup.removeView(chip)
        }
        return chip
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openDatePicker(initTime: LocalDateTime? = null,tv: TextView){
            val calendar = Calendar.getInstance()
            initTime?.also {
                calendar.time = Date.from(initTime.toInstant(ZoneOffset.UTC))
            }
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val datePickerDialog = DatePickerDialog(
                    this.requireActivity(),
                    { _, _, _, _ ->
                        // Display Selected date in TextView
                        tv.text = ("$day/${month + 1}/$year")
                    },
                    year, month, day
            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
            datePickerDialog.show()

    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun openTimePicker(initTime: LocalDateTime? = null, tv: TextView){
        val cal = Calendar.getInstance()
        initTime?.also {
            cal.time = Date.from(initTime.toInstant(ZoneOffset.UTC))
        }
        val timeSetListener =
                TimePickerDialog.OnTimeSetListener { _, hour: Int, minute: Int ->
                    cal.set(Calendar.HOUR_OF_DAY, hour)
                    cal.set(Calendar.MINUTE, minute)
                    tv.text = SimpleDateFormat("HH:mm").format(cal.time)
                }
        TimePickerDialog(
                context,
                timeSetListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
        ).show()


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateDuration(view: View){
        val last = tripStepList.lastIndex
        view.findViewById<TextView>(R.id.durationText).text = getDurationFormatted(
                tripStepList[0].locationTime,
                tripStepList[last].locationTime
        )
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


