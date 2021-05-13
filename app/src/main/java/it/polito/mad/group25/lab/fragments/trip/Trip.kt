package it.polito.mad.group25.lab.fragments.trip

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.Blob
import it.polito.mad.group25.lab.utils.datastructure.Identifiable
import it.polito.mad.group25.lab.utils.datastructure.IdentifiableObject
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceAware
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class Trip : AbstractPersistenceAware(), Identifiable {
    override var id: String? by onChangeUpdateStatus(null)
    var carPic: Blob? by onChangeUpdateStatus(null)
    var carName: String? by onChangeUpdateStatus(null)
    var tripStartDate: Long by onChangeUpdateStatus(System.currentTimeMillis())
    val locations: MutableList<TripLocation> by onChangeUpdateStatus(
        mutableListOf(
            TripLocation(),
            TripLocation(locationTime = Instant.now().plusSeconds(30 * 60).toEpochMilli())
        )
    )
    var seats: Int by onChangeUpdateStatus(0)
    var price: Double by onChangeUpdateStatus(0.0)
    val additionalInfo: MutableList<String> by onChangeUpdateStatus(mutableListOf())
    var ownerId: String? by onChangeUpdateStatus(null)
    val interestedUsers: MutableList<String> by onChangeUpdateStatus(mutableListOf())
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