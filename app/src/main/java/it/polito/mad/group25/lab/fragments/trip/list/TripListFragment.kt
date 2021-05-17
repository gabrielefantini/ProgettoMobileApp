package it.polito.mad.group25.lab.fragments.trip.list

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad.group25.lab.AuthenticationContext
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.fragments.trip.TripViewModel
import it.polito.mad.group25.lab.fragments.trip.filter.FilterField
import it.polito.mad.group25.lab.fragments.trip.filter.TripFilterViewModel


class TripListFragment : Fragment() {

    //Initializing sharedViewModel
    private val tripListViewModel: TripListViewModel by activityViewModels()
    private val tripViewModel: TripViewModel by activityViewModels()
    private val authenticationContext: AuthenticationContext by activityViewModels()
    private val tripFilterViewModel: TripFilterViewModel by activityViewModels()

    private var columnCount = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
        setHasOptionsMenu(true)
        tripFilterViewModel.flushFilter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.trip_list_fragment, container, false)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater){
        inflater.inflate(R.menu.menu_filter_trip_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_trip_list_filter -> {
                activity?.findNavController(R.id.nav_host_fragment_content_main)
                    ?.navigate(R.id.action_TripListFragment_to_TripFilterFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = view.findViewById<RecyclerView>(R.id.list)
        //pass an observable to MyTripCarRecyclerViewAdapter
        tripListViewModel.trips.observe(viewLifecycleOwner, { tripMap ->
            // Set the adapter
            val userId = authenticationContext.userId()
            val tripList =
                tripMap.values
                    .toList()
                    .filter { trip ->  trip.ownerId == userId}

            if (tripList.isEmpty()) {
                view.findViewById<TextView>(R.id.textView2).visibility = View.VISIBLE
                list.visibility = View.GONE
            } else {
                //filtro in base al filtro attivo
                val tripFilter = tripFilterViewModel.getFilter()
                val filteredTrip =
                    tripList
                        .filter{ trip ->
                            //tripFilter ---> Map<String, String>
                            // key -> tipo
                            // value -> valore
                            tripFilter.keys.fold(true) { acc, key ->
                                acc && enumValueOf<FilterField>(key).operator(trip, tripFilter[key]!!)
                            }
                        }
                view.findViewById<TextView>(R.id.textView2).visibility = View.GONE
                list.visibility = View.VISIBLE
                with(list) {
                    layoutManager = when {
                        columnCount <= 1 -> LinearLayoutManager(context)
                        else -> GridLayoutManager(context, columnCount)
                    }
                    adapter = TripCardRecyclerViewAdapter(filteredTrip, tripViewModel, userId)
                }
            }
        })

        val addNewTripButton = view.findViewById<FloatingActionButton>(R.id.addTrip)
        addNewTripButton.setOnClickListener {
            tripViewModel.trip.value = tripListViewModel.createNewTrip()
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
