package it.polito.mad.group25.lab.fragments.trip.list

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.fragments.trip.Trip
import it.polito.mad.group25.lab.fragments.trip.TripViewModel
import it.polito.mad.group25.lab.fragments.trip.startDateFormatted
import it.polito.mad.group25.lab.fragments.trip.timeFormatted
import it.polito.mad.group25.lab.utils.views.fromBlob


interface CardType {

    companion object {
        const val TYPE_MYTRIP = 1
        const val TYPE_OTHERSTRIP = 2
    }
}
class TripCardRecyclerViewAdapter(val tripList: List<Trip>, var currentTrip: TripViewModel, val currentId: String?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var itemView: View
        return when(viewType) {
            CardType.TYPE_MYTRIP -> {
                itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.trip_card_fragment, parent, false)
                MyTripViewHolder(itemView)
            }
            CardType.TYPE_OTHERSTRIP -> {
                itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.trip_card_fragment, parent, false)
                 OthersTripViewHolder(itemView)
            }
            else -> {
                itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.trip_card_fragment, parent, false)
                OthersTripViewHolder(itemView)
            }
        }
    }

    override fun getItemCount(): Int = tripList.size

    override fun getItemViewType(position: Int): Int {
        return if(currentId != null){
            if(currentId == tripList[position].ownerId) 1
            else 2
        } else 1

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int){
        when(getItemViewType(position)){
            1 -> (holder as MyTripViewHolder).bindView(position)
            2 -> (holder as OthersTripViewHolder).bindView(position)
            else -> {
                //error !!
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    inner class MyTripViewHolder(val itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bindView(position: Int){
           val item = tripList[position]
            itemView.findViewById<TextView>(R.id.card_car_name).text = item.carName
            itemView.findViewById<TextView>(R.id.card_departure_date).text= item.startDateFormatted()
            itemView.findViewById<TextView>(R.id.card_trip_time_departure).text = item.locations[0].timeFormatted()
            itemView.findViewById<TextView>(R.id.card_trip_location_departure).text  = item.locations[0].location.toString()
            itemView.findViewById<TextView>(R.id.card_trip_time_arrival).text = item.locations[item.locations.size - 1].timeFormatted()
            itemView.findViewById<TextView>(R.id.card_trip_location_arrival).text =
                item.locations[item.locations.size - 1].location.toString()
            itemView.findViewById<TextView>(R.id.card_seats_text).text = item.seats.toString()
            itemView.findViewById<TextView>(R.id.card_price_text).text = item.price.toString()
            itemView.findViewById<Button>(R.id.editTrip).setOnClickListener { view ->
                currentTrip.trip = item
                view.findNavController().navigate(R.id.showTripEditFragment)
            }
            itemView.findViewById<CardView>(R.id.card).setOnClickListener { view ->
                currentTrip.trip = item
                view.findNavController().navigate(R.id.showTripDetailsFragment)
            }
            tripList[position].carPic?.let { itemView.findViewById<ImageView>(R.id.card_car_image).fromBlob(it) }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    inner class OthersTripViewHolder(val itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bindView(position: Int){
            val item = tripList[position]
            itemView.findViewById<TextView>(R.id.card_car_name).text = item.carName
            itemView.findViewById<TextView>(R.id.card_departure_date).text= item.startDateFormatted()
            itemView.findViewById<TextView>(R.id.card_trip_time_departure).text = item.locations[0].timeFormatted()
            itemView.findViewById<TextView>(R.id.card_trip_location_departure).text  = item.locations[0].location.toString()
            itemView.findViewById<TextView>(R.id.card_trip_time_arrival).text = item.locations[item.locations.size - 1].timeFormatted()
            itemView.findViewById<TextView>(R.id.card_trip_location_arrival).text =
                item.locations[item.locations.size - 1].location.toString()
            itemView.findViewById<TextView>(R.id.card_seats_text).text = item.seats.toString()
            itemView.findViewById<TextView>(R.id.card_price_text).text = item.price.toString()
            itemView.findViewById<Button>(R.id.editTrip).visibility = View.GONE
            itemView.findViewById<CardView>(R.id.card).setOnClickListener { view ->
                currentTrip.trip = item
                view.findNavController().navigate(R.id.showTripDetailsFragment)
            }
            tripList[position].carPic?.let { itemView.findViewById<ImageView>(R.id.card_car_image).fromBlob(it) }
        }
    }
}