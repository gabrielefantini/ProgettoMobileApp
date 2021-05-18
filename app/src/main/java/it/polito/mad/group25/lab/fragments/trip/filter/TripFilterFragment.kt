package it.polito.mad.group25.lab.fragments.trip.filter

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.text.isDigitsOnly
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.utils.views.setConstraints
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class TripFilterFragment: Fragment() {

    private val tripFilterViewModel: TripFilterViewModel by activityViewModels()
    private lateinit var temporaryMap: MutableMap<String, TemporaryMapValue>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.trip_filter_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        temporaryMap = mutableMapOf()
        //popolo la map delle view del filterFragment
        temporaryMap[FilterField.departurePlace.name] = TemporaryMapValue(view.findViewById(R.id.departurePlace),false)
        temporaryMap[FilterField.departureDate.name] = TemporaryMapValue(view.findViewById(R.id.departureStartDate),false)
        temporaryMap[FilterField.departureTime.name] = TemporaryMapValue(view.findViewById(R.id.departureStartTime),false)
        temporaryMap[FilterField.arrivalPlace.name] = TemporaryMapValue(view.findViewById(R.id.arrivalPlace),false)
        temporaryMap[FilterField.arrivalDate.name] = TemporaryMapValue(view.findViewById(R.id.arrivalStartDate),false)
        temporaryMap[FilterField.arrivalTime.name] = TemporaryMapValue(view.findViewById(R.id.arrivalStartTime),false)
        temporaryMap[FilterField.price.name] = TemporaryMapValue(view.findViewById(R.id.price),false)
        temporaryMap[FilterField.duration.name] = TemporaryMapValue(view.findViewById(R.id.duration),false)
        temporaryMap[FilterField.seats.name] = TemporaryMapValue(view.findViewById(R.id.seats),false)


        val filter = tripFilterViewModel.getFilter()

        //se il filtro non è vuoto, popolo la view con i filtri già esistenti
        if (filter.isNotEmpty()){
            filter.map {
                val view = temporaryMap[it.key]?.view
                if(temporaryMap[it.key]!!.view is TextView) {
                    view as TextView
                    view.text = it.value
                    temporaryMap[it.key]?.isModified = true
                }
                if(temporaryMap[it.key]!!.view::class.java is TextInputEditText) {
                    view as TextInputEditText
                    view.setText(it.value)
                    temporaryMap[it.key]?.isModified = true
                }
            }
        }

        //date and time pickers-----------------------------------------
        temporaryMap[FilterField.departureTime.name]?.view?.setOnClickListener{
                    openTimePicker(item = temporaryMap[FilterField.departureTime.name]!!)
        }
        temporaryMap[FilterField.arrivalTime.name]?.view?.setOnClickListener{
            openTimePicker(item = temporaryMap[FilterField.arrivalTime.name]!!)
        }
        temporaryMap[FilterField.arrivalDate.name]?.view?.setOnClickListener{
            openDatePicker(item = temporaryMap[FilterField.arrivalDate.name]!!)
        }
        temporaryMap[FilterField.departureDate.name]?.view?.setOnClickListener{
            openDatePicker(item = temporaryMap[FilterField.departureDate.name]!!)
        }

        //attacco un handler a tutti i textInputEditText
        temporaryMap.map { temporaryItem ->
            if(temporaryItem.value.view is TextInputEditText){
                (temporaryItem.value.view as TextInputEditText).doAfterTextChanged {
                    temporaryItem.value.isModified = true
                }
            }
        }
        //gestione del pulsante "applica filtri"

        val applyFilterButton = view.findViewById<Button>(R.id.apply)
        applyFilterButton.setOnClickListener{
            val changesToFilter = temporaryMap.filter { it.value.isModified }
            if(changesToFilter.isNotEmpty()) {
                changesToFilter.map {
                    val text = (it.value.view as TextView).text
                    tripFilterViewModel.getFilter()[it.key] = text.toString()
                }
                activity?.findNavController(R.id.nav_host_fragment_content_main)
                    ?.popBackStack()
            }
            else {
                //doNothing
            }
        }
        val discardFilterButton = view.findViewById<Button>(R.id.discard)
        discardFilterButton.setOnClickListener{
            tripFilterViewModel.flushFilter()
            activity?.findNavController(R.id.nav_host_fragment_content_main)
                ?.popBackStack()
        }
    }
    //funzioni utili per time and data picker

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openDatePicker(initTime: LocalDateTime? = null, item: TemporaryMapValue) {
        val tv = item.view as TextView

        val calendar = Calendar.getInstance()
        initTime?.also {
            calendar.time = Date.from(initTime.toInstant(ZoneOffset.UTC))
        }
        val sDay = calendar.get(Calendar.DAY_OF_MONTH)
        val sMonth = calendar.get(Calendar.MONTH)
        val sYear = calendar.get(Calendar.YEAR)
        val datePickerDialog = DatePickerDialog(
            this.requireActivity(),
            { _, year, month, day ->
                // Display Selected date in TextView
                tv.text = "$day/${month + 1}/$year"
                item.isModified = true
            },
            sYear, sMonth, sDay
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun openTimePicker(initTime: LocalDateTime? = null, item: TemporaryMapValue) {
        val tv = item.view as TextView

        val cal = Calendar.getInstance()
        initTime?.also {
            cal.time = Date.from(initTime.toInstant(ZoneOffset.UTC))
        }
        val timeSetListener =
            TimePickerDialog.OnTimeSetListener { _, hour: Int, minute: Int ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                tv.text = SimpleDateFormat("HH:mm").format(cal.time)
                item.isModified = true
            }
        TimePickerDialog(
            context,
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }
}
//classe per capire i filtri attivi
data class TemporaryMapValue(val view: View, var isModified: Boolean)