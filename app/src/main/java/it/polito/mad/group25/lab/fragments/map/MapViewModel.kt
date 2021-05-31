package it.polito.mad.group25.lab.fragments.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import kotlin.math.round

class MapViewModel(application: Application): AndroidViewModel(application) {
    lateinit var geopoints: MutableList<GeoPoint>

    fun distance(path: Polyline): Double = (path.distance/1000).round(2)
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}