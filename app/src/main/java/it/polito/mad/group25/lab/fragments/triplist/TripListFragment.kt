package it.polito.mad.group25.lab.fragments.triplist

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.SharedViewModel
import it.polito.mad.group25.lab.fragments.tripdetails.Trip2
import it.polito.mad.group25.lab.utils.entities.Trip
import it.polito.mad.group25.lab.utils.entities.TripLocation
import java.time.LocalDateTime
import java.util.*


class TripListFragment : Fragment() {

    //Initializing sharedViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()


    private var columnCount = 1



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.trip_list_fragment, container, false)

        sharedViewModel.tripList.observe( viewLifecycleOwner, { tripList ->
            // Set the adapter
            if (view is RecyclerView) {
                with(view) {
                    layoutManager = when {
                        columnCount <= 1 -> LinearLayoutManager(context)
                        else -> GridLayoutManager(context, columnCount)
                    }
                    adapter = MyTripCardRecyclerViewAdapter(tripList)
                }
            }
        })

        return view
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