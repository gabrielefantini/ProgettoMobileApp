package it.polito.mad.group25.lab.fragments.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.VISIBLE
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.textfield.TextInputLayout
import it.polito.mad.group25.lab.BuildConfig
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.fragments.trip.edit.TripEditViewModel
import it.polito.mad.group25.lab.fragments.trip.edit.openDatePicker
import it.polito.mad.group25.lab.fragments.trip.edit.openTimePicker
import it.polito.mad.group25.lab.fragments.trip.timeFormatted
import it.polito.mad.group25.lab.utils.asFormattedDate
import it.polito.mad.group25.lab.utils.fragment.showError
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.config.Configuration.*
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

abstract class MapFragment(val editMode: Boolean): Fragment(R.layout.map_fragment) {
    private val mapViewModel: MapViewModel by activityViewModels()

    private lateinit var map : MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var gpsProvider: GpsMyLocationProvider
    private lateinit var geocoder: Geocoder

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()

        getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        map = view.findViewById(R.id.map)
        map.setDestroyMode(false)

        ActivityCompat.requestPermissions(requireActivity(),
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),1)

        val mapController = map.controller

        val geopoints = mapViewModel.geopoints

        // my location ***
        gpsProvider = GpsMyLocationProvider(context)

        myLocationOverlay = MyLocationNewOverlay(gpsProvider,map)
        myLocationOverlay.enableMyLocation()
        map.overlays.add(myLocationOverlay)
        // ******

        var cLat = 0.0  //center latitude
        var cLong = 0.0 //center longitude

        // adding location markers ***
        geopoints.forEachIndexed { id, gp ->
            val marker = Marker(map)
            marker.position = gp
            marker.title = "loc $id"
            marker.snippet = when(id){
                0 -> "Departure"
                geopoints.lastIndex -> "Destination"
                else -> "Intermediate stop"
            }
            map.overlays.add(marker)

            cLat += gp.latitude
            cLong += gp.longitude
        }
        // *****************

        // adding path ***
        val pathOverlay = Polyline()
        geopoints.reversed().forEach {
            pathOverlay.addPoint(it)
        }
        pathOverlay.outlinePaint.color = Color.parseColor("#800000FF")
        pathOverlay.outlinePaint.strokeCap = Paint.Cap.ROUND
        map.overlays.add(pathOverlay)
        // *****************

        // last settings ***
        map.isTilesScaledToDpi = true
        mapController.setZoom(10.0)

        if(geopoints.size == 0){
            val lastCurrentLocation = mapViewModel.currentLocation
            if(lastCurrentLocation != null){
                // set center to last current location
                mapController.setCenter(GeoPoint(lastCurrentLocation.latitude,lastCurrentLocation.longitude))
            }else{
                // default setting
                mapController.setCenter(GeoPoint(45.0,7.0))
            }
        }else{
            cLat /= geopoints.size
            cLong /= geopoints.size

            mapController.setCenter(GeoPoint(cLat,cLong))
        }
        // *****************

        if(editMode){
            tripLocationEditForm(view,context,requireActivity())
        }
    }

    private fun tripLocationEditForm(view: View,context: Context,activity: Activity){
        view.findViewById<LinearLayout>(R.id.editForm).visibility = VISIBLE

        // component tree ***
        val locationStop = view.findViewById<EditText>(R.id.location_stop)
        val dateStop = view.findViewById<TextView>(R.id.dateStop)
        val timeStop = view.findViewById<TextView>(R.id.time_stop)
        val deleteStop = view.findViewById<ImageButton>(R.id.deleteStop)
        // ****

        // other controllers ***
        geocoder = Geocoder(context)
        // ****

        val tripLocation = mapViewModel.selectedTripLocation
        tripLocation?.let {
            val timeInit = it.locationTime
            val locationInit = it.location

            dateStop.text = timeInit.asFormattedDate("dd/MM/yyyy")
            timeStop.text = it.timeFormatted()
            locationStop.setText(locationInit)
            deleteStop.visibility = VISIBLE
        }

        dateStop.setOnClickListener {
            openDatePicker(tv = dateStop,activity = requireActivity())
        }

        timeStop.setOnClickListener {
            openTimePicker(tv = timeStop,context = context)
        }

        val newPosition = Marker(map)

        // textField end icon onclick -> search for a geoPoint (x,y) compatible with text input ***
        view.findViewById<TextInputLayout>(R.id.location_stopLayout).setEndIconOnClickListener {
            val locName = locationStop.text.toString()
            val address = geocoder.getFromLocationName(locName,1)
            if(address.size != 0){
                val addr = address[0]
                val geoPoint = GeoPoint(addr.latitude,addr.longitude)

                setNewPoint(newPosition, geoPoint)

            }else
                showError("$locName not found!")
        }

        // map onclick -> add a marker and find location name ***
        map.overlays.add(object: Overlay(){
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                val projection = mapView.projection
                val geoPoint = projection.fromPixels(e.x.toInt(),e.y.toInt())

                setNewPoint(newPosition, geoPoint as GeoPoint)

                var address = geocoder.getFromLocation(geoPoint.latitude,geoPoint.longitude,1)
                address?.let {
                    locationStop.setText(it[0].locality)
                }
                return true
            }
        })

        // saveButton ***
        view.findViewById<ImageButton>(R.id.save_stop).setOnClickListener {
            tripLocation?.let {
                //TODO
            }?: kotlin.run {
                //TODO
            }
        }

        // deleteButton ***
        deleteStop.setOnClickListener {
            //TODO
        }

    }

    private fun setNewPoint(marker: Marker,geoPoint: GeoPoint){
        //reset previous marker
        map.overlays.remove(marker)

        //add new marker and centers the map on it
        map.controller.setCenter(geoPoint)
        marker.position = GeoPoint(geoPoint.latitude,geoPoint.longitude)
        map.overlays.add(marker)
    }

    override fun onResume() {
        super.onResume()
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        map.onResume()
        myLocationOverlay.enableMyLocation()
    }

    override fun onPause() {
        //save last current location
        mapViewModel.currentLocation = gpsProvider.lastKnownLocation

        map.onPause()
        myLocationOverlay.disableMyLocation()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        map.onDetach()
    }
}
