package it.polito.mad.group25.lab.fragments.map

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
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

class MapFragment: Fragment(R.layout.map_fragment) {
    private val mapViewModel: MapViewModel by activityViewModels()

    private lateinit var map : MapView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = this.context

        getInstance().load(context,androidx.preference.PreferenceManager.getDefaultSharedPreferences(context))

        map = view.findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)

        val mapController = map.controller

        val geopoints = mapViewModel.geopoints

        var cLat = 0.0  //center latitude
        var cLong = 0.0 //center longitude

        // adding location markers ***
        geopoints.forEachIndexed { id, gp ->
            val marker = Marker(map)
            marker.position = gp
            marker.title = "loc $id"
            marker.snippet = "description"
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
        mapController.setZoom(12.0)
        mapController.setCenter(GeoPoint(cLat,cLong))
        // *****************
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}