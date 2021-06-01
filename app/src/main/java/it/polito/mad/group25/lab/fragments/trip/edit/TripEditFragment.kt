package it.polito.mad.group25.lab.fragments.trip.edit

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import it.polito.mad.group25.lab.AuthenticationContext
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.fragments.map.MapViewModel
import it.polito.mad.group25.lab.fragments.trip.*
import it.polito.mad.group25.lab.fragments.trip.details.TripDetailsFragment
import it.polito.mad.group25.lab.fragments.trip.details.getDurationFormatted
import it.polito.mad.group25.lab.fragments.trip.list.TripListViewModel
import it.polito.mad.group25.lab.fragments.userprofile.UserProfileViewModel
import it.polito.mad.group25.lab.utils.asFormattedDate
import it.polito.mad.group25.lab.utils.fragment.showError
import it.polito.mad.group25.lab.utils.persistence.impl.SharedPreferencesPersistableContainer
import it.polito.mad.group25.lab.utils.toLocalDate
import it.polito.mad.group25.lab.utils.toLocalDateTime
import it.polito.mad.group25.lab.utils.views.*
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

abstract class TripEditFragment(
    contentLayoutId: Int
) : Fragment(contentLayoutId) {

    private lateinit var tripEditViewModel: TripEditViewModel
    private val tripViewModel: TripViewModel by activityViewModels()
    private val tripListViewModel: TripListViewModel by activityViewModels()
    private val authenticationContext: AuthenticationContext by activityViewModels()
    private val userProfileViewModel: UserProfileViewModel by activityViewModels()
    private val mapViewModel: MapViewModel by activityViewModels()

    private lateinit var takePictureLauncher: ActivityResultLauncher<Void>
    private lateinit var pickPictureLauncher: ActivityResultLauncher<String>

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

        val context = requireContext()

        val rv = view.findViewById<RecyclerView>(R.id.tripList)
        val additionalInfoChips = view.findViewById<ChipGroup>(R.id.additionalInfoChips)
        val depDate = view.findViewById<TextView>(R.id.departureDate)


        tripViewModel.trip.observe(viewLifecycleOwner, { trip ->
            if (trip != null) {

                trip.carPic?.let {
                    view.findViewById<ImageView>(R.id.carImage)
                        .fromBlob(it)
                }

                val interestedText = view.findViewById<TextView>(R.id.interestedUsers)
                if (trip.id == null) interestedText.visibility = GONE
                else interestedText.visibility = VISIBLE

                val seatsTaken = trip.interestedUsers.filter { it.isConfirmed }.size
                seatsLayout.setConstraints(
                    R.id.seatsText,
                    "Please provide valid seats between 0 and ${7-seatsTaken} (considering also the assigned ones)",
                    true
                ) { it.isNotBlank() && it.toString().toInt().let { s -> s in 0..7-seatsTaken } }

                if (tripEditViewModel.tripStepList.isEmpty())
                    trip.locations.forEach {
                        tripEditViewModel.tripStepList.add(it)
                    }
                updateDuration(view)

                view.findViewById<EditText>(R.id.carName).setText(trip.carName)
                depDate.text = tripEditViewModel.tripStepList[0].locationTime.asFormattedDate("dd/MM/yyyy")
                view.findViewById<EditText>(R.id.seatsText).setText(trip.seats.toString())
                view.findViewById<EditText>(R.id.priceText).setText(trip.price.toString())


                rv.layoutManager = LinearLayoutManager(context)
                rv.adapter = TripAdapter(tripEditViewModel.tripStepList, context)

                if (additionalInfoChips.childCount != 0)
                    additionalInfoChips.removeAllViews()

                if (tripEditViewModel.tripDet.isEmpty())
                    trip.additionalInfo.forEach {
                        tripEditViewModel.tripDet.add(it)
                    }

                tripEditViewModel.tripDet.forEach {
                    additionalInfoChips.addView(getChip(additionalInfoChips, it))
                }

                view.findViewById<ImageView>(R.id.carImage)
                    .fromFile(File(requireActivity().dataDir, "tripPhoto" + trip.id))

                tripEditViewModel.tempProfileDrawable?.let {
                    view.findViewById<ImageView>(R.id.carImage).setImageDrawable(it)
                }


                val imageButton = view.findViewById<ImageButton>(R.id.changeCarPicButton)
                registerForContextMenu(imageButton)
                imageButton.setOnClickListener {
                    it.showContextMenu()
                }

                // addButton
                view.findViewById<FloatingActionButton>(R.id.addTripStop).setOnClickListener {
                    navigateToMapFragment(null)
                }

                val addDetail = view.findViewById<FloatingActionButton>(R.id.addDetail)
                addDetail.setOnClickListener {
                    val layout = view.findViewById<LinearLayout>(R.id.addDetailLayout)
                    if (layout.visibility == VISIBLE) {
                        view.findViewById<EditText>(R.id.insertDetail).text.clear()
                        layout.visibility = INVISIBLE
                    } else {
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
                                tripEditViewModel.tripDet.add(detText.text.toString())
                                trip.additionalInfo.add(detText.text.toString())
                                layout.visibility = INVISIBLE

                                additionalInfoChips.addView(
                                    getChip(
                                        additionalInfoChips,
                                        detText.text.toString()
                                    )
                                )
                                detText.text.clear()
                            }
                        }
                    }
                }

                setUpRemoveButton(view)
                setUpInterestedUsers(view)
            }
        })
    }

    fun setUpInterestedUsers(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.userList)
        rv.layoutManager = LinearLayoutManager(context)
        if(tripEditViewModel.interestedUsersTmp != tripViewModel.trip.value!!.interestedUsers) {
            tripEditViewModel.interestedUsersTmp = mutableListOf()
            tripEditViewModel.interestedUsersTmp.addAll(tripViewModel.trip.value!!.interestedUsers)
        }
        rv.adapter = TripUsersEditAdapter(
            tripViewModel.trip.value!!.interestedUsers,
            tripEditViewModel.interestedUsersTmp,
            userProfileViewModel,
            view.findViewById<TextInputEditText>(R.id.seatsText)
        )

    }

    fun setUpRemoveButton(view: View) {
        val remove_button = view.findViewById<Button>(R.id.remove_button)
        remove_button.setOnClickListener {
            tripListViewModel.removeTrip(tripViewModel.trip.value!!)
            view.findNavController().navigate(R.id.TripListFragment)
        }

    }

    fun navigateToMapFragment(locationId: Int?){
        //TODO passare i geopoints tramite tripStepList
        mapViewModel.selectedTripLocation = if(locationId != null) {
            tripEditViewModel.tripStepList[locationId]
        } else null
        requireActivity().findNavController(R.id.nav_host_fragment_content_main)
            .navigate(R.id.action_showTripEditFragment_to_ShowEditMap)
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

        val tripDb = tripViewModel.trip.value!!
        val tripSel = Trip()
        tripSel.id = tripDb.id
        tripSel.carName = tripDb.carName
        tripSel.additionalInfo = tripDb.additionalInfo
        tripSel.carPic = tripDb.carPic
        tripSel.interestedUsers = mutableListOf()
        tripSel.locations = tripDb.locations
        tripSel.ownerId = tripDb.ownerId
        tripSel.price = tripDb.price
        tripSel.seats = tripDb.seats
        tripSel.tripStartDate = tripDb.tripStartDate

        if (tripEditViewModel.tripStepList
                .minByOrNull { l -> l.locationTime }?.locationTime?.let { it < System.currentTimeMillis() } == true
        ) {
            showError("Please provide a valid departure which is after the current time!")
            return false
        }

        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        var date = view?.findViewById<TextView>(R.id.departureDate)?.text.toString()
        date = date.split("/").map { it -> if (it.length < 2) "0$it" else it }
            .reduce { acc, s -> "$acc/$s" }
        LocalDate.parse(date, formatter).also {
            val startDate = tripSel.tripStartDate.toLocalDate()
            if (startDate != it) {
                tripSel.tripStartDate =
                    it.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
        }

        view?.findViewById<EditText>(R.id.carName)?.text.toString().also {
            if (tripSel.carName != it) {
                tripSel.carName = it
            }
        }

        view?.findViewById<ImageView>(R.id.carImage)?.also {
            tripSel.carPic = it.toBlob()
        }

        val seats = view?.findViewById<EditText>(R.id.seatsText)?.text.toString().toInt()

        if(tripSel.seats != seats) {
            tripSel.seats = seats
            Log.d("Seats", "Seats updated")
        }

        tripSel.interestedUsers.addAll(tripEditViewModel.interestedUsersTmp)

        view?.findViewById<EditText>(R.id.priceText)?.text.toString().toDouble().also {
            if (tripSel.price != it) {
                tripSel.price = it
            }
        }


        tripEditViewModel.tripStepList.also {
            if (tripSel.locations != tripEditViewModel.tripStepList) {
                tripSel.locations.clear()
                tripEditViewModel.tripStepList.forEach { tl -> tripSel.locations.add(tl) }
            }
        }

        tripEditViewModel.tripDet.also {
            if (tripSel.additionalInfo != it) {
                tripSel.additionalInfo.clear()
                it.forEach { td -> tripSel.additionalInfo.add(td) }
            }
        }

        tripSel.ownerId = authenticationContext.userId()

        if (tripDb.id == null) tripListViewModel.putTrip(tripSel)
        else tripViewModel.trip.value!!.apply {
            this.doOnTransaction {
                this.id = tripSel.id
                this.carPic = tripSel.carPic
                this.carName = tripSel.carName
                this.tripStartDate = tripSel.tripStartDate
                this.locations = tripSel.locations
                this.seats = tripSel.seats
                this.price = tripSel.price
                this.additionalInfo = tripSel.additionalInfo
                this.ownerId = tripSel.ownerId
                this.interestedUsers = tripSel.interestedUsers
            }
        }
        return true
    }


    inner class TripAdapter(var list: List<TripLocation>, val context: Context?) :
        RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

        inner class TripViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val location: TextView = v.findViewById<TextView>(R.id.trip_location)
            val time: TextView = v.findViewById<TextView>(R.id.trip_time)
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
                navigateToMapFragment(position)
            }

            holder.time.setOnClickListener {
                navigateToMapFragment(position)
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
    private fun getChip(chipGroup: ChipGroup, chipText: String): Chip {
        val chip = Chip(context)
        chip.text = chipText

        chip.isClickable = false //test
        chip.isCheckable = false

        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            TransitionManager.beginDelayedTransition(chipGroup)
            tripEditViewModel.tripDet.remove(chipText)
            chipGroup.removeView(chip)
        }
        return chip
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateDuration(view: View) {
        val last = tripEditViewModel.tripStepList.lastIndex
        view.findViewById<TextView>(R.id.durationText).text = getDurationFormatted(
            tripEditViewModel.tripStepList[0].locationTime.toLocalDateTime(),
            tripEditViewModel.tripStepList[last].locationTime.toLocalDateTime()
        )
    }

    inner class TripUsersEditAdapter(
        private val list: List<TripUser>,
        val interestedUsersTmp: MutableList<TripUser>,
        private val usersData: UserProfileViewModel,
        val seats: TextInputEditText
    ) :
        RecyclerView.Adapter<TripUsersEditAdapter.TripUsersViewHolder>() {

        inner class TripUsersViewHolder(v: View, interestedUsersTmp: MutableList<TripUser>, private val usersData: UserProfileViewModel) :
            RecyclerView.ViewHolder(v) {
            private val username: TextView = v.findViewById(R.id.username)
            private val proPic: ImageView = v.findViewById(R.id.proPic)
            private val checkBox = v.findViewById<CheckBox>(R.id.confirm_user)

            fun bind(t: TripUser, interestedUsersTmp: MutableList<TripUser>) {
                Log.d("bindFun", interestedUsersTmp.size.toString())
                usersData.showUser(t.userId)
                usersData.shownUser.observe(viewLifecycleOwner,{ user_ ->
                    user_.fullName?.let { username.text = it }
                    user_.userProfilePhotoFile?.let { proPic.fromBlob(it) }
                })

                username.setOnClickListener {
                    userProfileViewModel.showUser(t.userId)
                    requireActivity().findNavController(R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_showTripEditFragment_to_showUserProfileFragment)
                }
                if (!t.isConfirmed) {
                    Log.d("bind", "!isConfirmed user ${t.userId}")
                    checkBox.visibility = VISIBLE
                    checkBox.setOnClickListener {
                        val seatsAvailable = seats.text.toString().toInt()
                        if(checkBox.isChecked == true) {
                            if (seatsAvailable > 0) {
                                seats.setText((seatsAvailable - 1).toString())
                                interestedUsersTmp.find { it.userId == t.userId }?.isConfirmed = true
                            }
                            else {
                                showError("No more seats available!")
                                checkBox.isChecked = false
                            }
                        }
                        else {
                            seats.setText((seatsAvailable + 1).toString())
                            interestedUsersTmp.find { it.userId == t.userId }?.isConfirmed = false
                        }
                        Log.d("checkBox", interestedUsersTmp.toString())
                    }
                }
                else {
                    Log.d("bind", "isConfirmed user ${t.userId}")
                    checkBox.visibility = INVISIBLE
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripUsersViewHolder {
            val layout = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            return TripUsersViewHolder(layout, interestedUsersTmp, usersData)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onBindViewHolder(holder: TripUsersViewHolder, position: Int) {
            holder.bind(list[position], interestedUsersTmp)
        }

        override fun getItemCount(): Int = list.size

        override fun getItemViewType(position: Int): Int = R.layout.trip_user_line

    }

}
@SuppressLint("SetTextI18n")
@RequiresApi(Build.VERSION_CODES.O)
fun openDatePicker(initTime: LocalDateTime? = null, tv: TextView, activity: Activity) {
    val calendar = Calendar.getInstance()
    initTime?.also {
        calendar.time = Date.from(initTime.toInstant(ZoneOffset.UTC))
    }

    val sDay = calendar.get(Calendar.DAY_OF_MONTH)
    val sMonth = calendar.get(Calendar.MONTH)
    val sYear = calendar.get(Calendar.YEAR)
    val datePickerDialog = DatePickerDialog(
        activity,
        { _, year, month, day ->
            // Display Selected date in TextView
            tv.text = "$day/${month + 1}/$year"
        },
        sYear, sMonth, sDay
    )
    datePickerDialog.datePicker.minDate = System.currentTimeMillis()
    datePickerDialog.show()

}

@SuppressLint("SimpleDateFormat")
@RequiresApi(Build.VERSION_CODES.O)
fun openTimePicker(initTime: LocalDateTime? = null, tv: TextView,context: Context) {
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

class TripEditViewModel(application: Application) : AndroidViewModel(application),
    SharedPreferencesPersistableContainer {

    var interestedUsersTmp = mutableListOf<TripUser>()
    var tripDet: MutableList<String> = mutableListOf()
    var tripStepList: MutableList<TripLocation> = mutableListOf()

    var tempProfileDrawable: Drawable? = null

    override fun getContext(): Context = getApplication()

}

