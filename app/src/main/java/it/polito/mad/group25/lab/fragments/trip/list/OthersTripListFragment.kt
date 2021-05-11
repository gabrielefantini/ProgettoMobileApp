package it.polito.mad.group25.lab.fragments.trip.list

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.fragments.trip.Trip
import it.polito.mad.group25.lab.fragments.trip.TripViewModel
import it.polito.mad.group25.lab.fragments.trip.startDateFormatted
import it.polito.mad.group25.lab.fragments.trip.timeFormatted
import it.polito.mad.group25.lab.utils.views.fromBlob


class OthersTripListFragment : Fragment() {

    //Initializing sharedViewModel
    private val tripListViewModel: TripListViewModel by activityViewModels()
    private val tripViewModel: TripViewModel by activityViewModels()


    private var columnCount = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.trip_list_fragment, container, false)


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = view.findViewById<RecyclerView>(R.id.list)
        //pass an observable to MyTripCarRecyclerViewAdapter
        tripListViewModel.trips.observe(viewLifecycleOwner, { tripMap ->
            // Set the adapter
            if (tripMap.size == 0) {
                view.findViewById<TextView>(R.id.textView2).visibility = View.VISIBLE
                list.visibility = View.GONE
            } else {
                view.findViewById<TextView>(R.id.textView2).visibility = View.GONE
                list.visibility = View.VISIBLE
                with(list) {
                    layoutManager = when {
                        columnCount <= 1 -> LinearLayoutManager(context)
                        else -> GridLayoutManager(context, columnCount)
                    }
                    adapter = TripCardRecyclerViewAdapter(tripMap.values.toList(), tripViewModel)
                }
            }
        })

        val addNewTripButton = view.findViewById<FloatingActionButton>(R.id.addTrip)
        addNewTripButton.setOnClickListener {
            tripViewModel.trip = tripListViewModel.createNewTrip()
            view.findNavController().navigate(R.id.showTripEditFragment)
        }
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            TripListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}