package it.polito.mad.group25.lab.fragments.trip

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.Blob
import it.polito.mad.group25.lab.utils.datastructure.Identifiable
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceAware
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class Trip : AbstractPersistenceAware(), Identifiable {
    override var id: String? = null
        set(value) {
            field = value
            statusUpdated()
        }
    var carPic: Blob? = null
        set(value) {
            field = value
            statusUpdated()
        }
    var carName: String? = null
        set(value) {
            field = value
            statusUpdated()
        }
    var tripStartDate: Long = System.currentTimeMillis()
        set(value) {
            field = value
            statusUpdated()
        }
    var locations: MutableList<TripLocation> = mutableListOf(
        TripLocation(),
        TripLocation(locationTime = Instant.now().plusSeconds(30 * 60).toEpochMilli())
    )
        set(value) {
            field = value
            statusUpdated()
        }
    var seats: Int = 0
        set(value) {
            field = value
            statusUpdated()
        }
    var price: Double = 0.0
        set(value) {
            field = value
            statusUpdated()
        }
    var additionalInfo: MutableList<String> = mutableListOf()
        set(value) {
            field = value
            statusUpdated()
        }
    var ownerId: String? = null
        set(value) {
            field = value
            statusUpdated()
        }
    var interestedUsers: MutableList<String> = mutableListOf()
        set(value) {
            field = value
            statusUpdated()
        }

    fun getType(): Boolean {
        //TODO
        return true
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun Trip.startDateFormatted(): String =
    DateTimeFormatter.ofPattern("dd/MM/yyyy")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(this.tripStartDate))


@RequiresApi(Build.VERSION_CODES.O)
data class TripLocation(
    var location: String = "loc name",
    var locationTime: Long = System.currentTimeMillis()
)

@RequiresApi(Build.VERSION_CODES.O)
fun TripLocation.timeFormatted(): String =
    DateTimeFormatter.ofPattern("HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(this.locationTime))

@RequiresApi(Build.VERSION_CODES.O)
fun Trip.addTripLocationOrdered(location: String, locationTime: Instant) {
    val trip = TripLocation(location, locationTime.toEpochMilli())
    locations.add(trip)
    locations.sortBy { it.locationTime }
}