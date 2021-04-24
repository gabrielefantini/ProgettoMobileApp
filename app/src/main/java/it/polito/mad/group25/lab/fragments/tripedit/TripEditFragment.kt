package it.polito.mad.group25.lab.fragments.tripedit

import android.app.Application
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
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
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.SharedViewModel
import it.polito.mad.group25.lab.fragments.tripdetails.TripLocationAdapter
import it.polito.mad.group25.lab.fragments.tripdetails.getDurationFormatted
import it.polito.mad.group25.lab.utils.entities.TripLocation
import it.polito.mad.group25.lab.utils.entities.startDateFormatted
import it.polito.mad.group25.lab.utils.viewmodel.PersistableContainer
import java.util.*

abstract class TripEditFragment(
    contentLayoutId: Int
) : Fragment(contentLayoutId) {

    private lateinit var tripEditViewModel: TripEditViewModel

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var takePictureLauncher: ActivityResultLauncher<Void>
    private lateinit var pickPictureLauncher: ActivityResultLauncher<String>

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

        sharedViewModel.tripSelected.observe(viewLifecycleOwner,{ tripId ->
            var trip = sharedViewModel.tripList.value?.get(tripId)
            if(trip != null){

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveProfileEdit -> {
                saveEdits()
                activity?.findNavController(R.id.nav_host_fragment_content_main)
                    ?.navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveEdits() {
        // TODO
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
