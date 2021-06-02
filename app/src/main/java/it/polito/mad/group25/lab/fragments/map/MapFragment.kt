package it.polito.mad.group25.lab.fragments.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.location.Address
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.textfield.TextInputLayout
import it.polito.mad.group25.lab.BuildConfig
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.fragments.trip.TripLocation
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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

abstract class MapFragment(val editMode: Boolean): Fragment(R.layout.map_fragment) {
    private val mapViewModel: MapViewModel by activityViewModels()
    private val tripEditViewModel: TripEditViewModel by activityViewModels()


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
        val saveStop = view.findViewById<ImageButton>(R.id.save_stop)
        // ****

        // other controllers ***
        geocoder = Geocoder(context)
        // ****

        val newPosition = Marker(map)
        var newAddress: Address? = null

        val tripLocation = mapViewModel.selectedTripLocation
        tripLocation?.let {
            val timeInit = it.locationTime
            val locationInit = it.location

            dateStop.text = timeInit.asFormattedDate("dd/MM/yyyy")
            timeStop.text = it.timeFormatted()
            locationStop.setText(locationInit)
            deleteStop.visibility = VISIBLE

            if(it.latitude != null && it.longitude != null){
                setNewPoint(newPosition,GeoPoint(it.latitude!!,it.longitude!!))
                geocoder.getFromLocation(it.latitude!!,it.longitude!!,1)?.let { addrs -> newAddress = addrs[0] }
            }
        }

        dateStop.setOnClickListener {
            openDatePicker(tv = dateStop,activity = requireActivity())
        }

        timeStop.setOnClickListener {
            openTimePicker(tv = timeStop,context = context)
        }

        // textField end icon onclick -> search for a geoPoint (x,y) compatible with text input ***
        view.findViewById<TextInputLayout>(R.id.location_stopLayout).setEndIconOnClickListener {
            val locName = locationStop.text.toString()
            val address = geocoder.getFromLocationName(locName,1)
            if(address.size != 0){
                newAddress = address[0]
                val geoPoint = GeoPoint(newAddress!!.latitude,newAddress!!.longitude)

                setNewPoint(newPosition, geoPoint)
                saveStop.isEnabled = true
            }else {
                showError("$locName not found!")
                map.overlays.remove(newPosition)
                saveStop.isEnabled = false
            }
        }

        // map onclick -> add a marker and find location name ***
        map.overlays.add(object: Overlay(){
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                val projection = mapView.projection
                val geoPoint = projection.fromPixels(e.x.toInt(),e.y.toInt())

                setNewPoint(newPosition, geoPoint as GeoPoint)

                var address = geocoder.getFromLocation(geoPoint.latitude,geoPoint.longitude,1)
                address?.let {
                    newAddress = it[0]
                    locationStop.setText(newAddress!!.locality)
                }
                saveStop.isEnabled = true
                return true
            }
        })

        // saveButton ***
        saveStop.setOnClickListener {
                if (locationStop.text.toString() != "" && timeStop.text.toString() != "--:--" && dateStop.text.toString() != "--/--/----") {
                    if(newPosition.position.latitude == 0.0 && newPosition.position.longitude == 0.0){
                        showError("Invalid location!")
                        return@setOnClickListener
                    }

                    if(newAddress != null) {
                        if (locationStop.text.toString() != newAddress!!.locality) {
                            showError("${locationStop.text} doesn't match marker position!")
                            return@setOnClickListener
                        }
                    }

                    if(tripLocation != null) {
                        val timeInit = tripLocation.locationTime
                        val locationInit = tripLocation.location

                        val t = tripEditViewModel.tripStepList.find {
                            !(it.locationTime < timeInit || it.locationTime > timeInit)
                                    && it.location == locationInit
                        }
                        tripEditViewModel.tripStepList.remove(t)
                    }

                    val formatter: DateTimeFormatter =
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    val date = dateStop.text.toString().split("/")
                        .map { if (it.length < 2) "0$it" else it }
                        .reduce { acc, s -> "$acc/$s" }

                    tripEditViewModel.tripStepList.add(
                        TripLocation(
                            locationStop.text.toString(),
                            LocalDateTime.parse(date + " " + timeStop.text.toString(), formatter)
                                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                            newPosition.position.latitude,
                            newPosition.position.longitude
                        )
                    )
                    tripEditViewModel.tripStepList.sortBy { it.locationTime }

                    activity?.findNavController(R.id.nav_host_fragment_content_main)
                        ?.navigateUp()

                } else Toast.makeText(context, "Fill all fields!", Toast.LENGTH_LONG).show()


        }

        // deleteButton ***
        deleteStop.setOnClickListener {
            if (tripEditViewModel.tripStepList.size > 2) {
                if(tripLocation != null) {
                    val timeInit = tripLocation.locationTime
                    val locationInit = tripLocation.location

                    val t = tripEditViewModel.tripStepList.find {
                        !(it.locationTime < timeInit || it.locationTime > timeInit)
                                && it.location == locationInit
                    }
                    tripEditViewModel.tripStepList.remove(t)

                    activity?.findNavController(R.id.nav_host_fragment_content_main)
                        ?.navigateUp()
                }
            } else
                Toast.makeText(
                    context,
                    "Can't delete a location\nTwo stops minimum needed.",
                    Toast.LENGTH_LONG
                ).show()
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
