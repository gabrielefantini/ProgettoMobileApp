package it.polito.mad.group25.lab.fragments.triplist

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.utils.entities.Trip



class MyTripCardRecyclerViewAdapter(val tripList: List<Trip>):
    RecyclerView.Adapter<MyTripCardRecyclerViewAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.trip_card_fragment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = tripList[position]

        holder.carName.text = item.carName
    }

    override fun getItemCount(): Int = tripList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val carName: TextView = view.findViewById(R.id.carName)
    }
}