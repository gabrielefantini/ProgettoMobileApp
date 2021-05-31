package it.polito.mad.group25.lab.fragments.map

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import it.polito.mad.group25.lab.R
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.config.Configuration.*
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapFragment: Fragment(R.layout.map_fragment) {
    private val mapViewModel: MapViewModel by activityViewModels()

    private lateinit var map : MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        map = MapView(inflater.context)
        map.setDestroyMode(false)

        return map
    }

    class MyGpsLocationProvider(context: Context?, private val viewModel: MapViewModel): GpsMyLocationProvider(context){
        override fun onLocationChanged(location: Location) {
            super.onLocationChanged(location)
            if(viewModel.currentLocation != location)
                viewModel.currentLocation = location
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = this.context

        val mapController = map.controller

        val geopoints = mapViewModel.geopoints

        // my location ***
        myLocationOverlay = MyLocationNewOverlay(MyGpsLocationProvider(context,mapViewModel),map)
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
        var pathOverlay = Polyline()
        geopoints.reversed().forEach {
            pathOverlay.addPoint(it)
        }
        pathOverlay.outlinePaint.color = Color.parseColor("#800000FF")
        pathOverlay.outlinePaint.strokeCap = Paint.Cap.ROUND
        map.overlays.add(pathOverlay)
        // *****************

        // last settings ***
        cLat /= geopoints.size
        cLong /= geopoints.size

        map.isTilesScaledToDpi = true
        mapController.setZoom(10.0)

        if(geopoints.size == 0){
            //no locations -> set center to current position (if available, else use default)
            val lastCurrentLocation = mapViewModel.currentLocation
            if(lastCurrentLocation != null){
                mapController.setCenter(GeoPoint(lastCurrentLocation.latitude,lastCurrentLocation.longitude))
            }else{
                mapController.setCenter(GeoPoint(45.0,7.0))
            }
        }else{
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
        map.onPause()
        myLocationOverlay.disableMyLocation()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        map.onDetach()
    }
}