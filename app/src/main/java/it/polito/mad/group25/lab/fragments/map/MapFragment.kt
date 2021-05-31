package it.polito.mad.group25.lab.fragments.map

import android.Manifest
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import it.polito.mad.group25.lab.BuildConfig
import it.polito.mad.group25.lab.R
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.config.Configuration.*
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapFragment: Fragment(R.layout.map_fragment) {
    private val mapViewModel: MapViewModel by activityViewModels()

    private lateinit var map : MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var gpsProvider: GpsMyLocationProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = this.context

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