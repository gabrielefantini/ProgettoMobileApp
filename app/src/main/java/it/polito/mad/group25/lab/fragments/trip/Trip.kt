package it.polito.mad.group25.lab.fragments.trip

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.Blob
import it.polito.mad.group25.lab.utils.datastructure.Identifiable
import it.polito.mad.group25.lab.utils.persistence.AbstractPersistenceAware
import java.io.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class Trip : AbstractPersistenceAware(), Identifiable, Serializable {
    override var id: String? by onChangeUpdateStatus(null)
    var carPic: Blob? by onChangeUpdateStatus(null)
    var carName: String? by onChangeUpdateStatus(null)
    var tripStartDate: Long by onChangeUpdateStatus(System.currentTimeMillis())
    var locations: MutableList<TripLocation> by onChangeUpdateStatus(
        mutableListOf(
            TripLocation(),
            TripLocation(locationTime = Instant.now().plusSeconds(30 * 60).toEpochMilli())
        )
    )
    var seats: Int by onChangeUpdateStatus(0)
    var price: Double by onChangeUpdateStatus(0.0)
    var additionalInfo: MutableList<String> by onChangeUpdateStatus(mutableListOf())
    var ownerId: String? by onChangeUpdateStatus(null)
    var interestedUsers: MutableList<TripUser> by onChangeUpdateStatus(mutableListOf())
    fun getType(): Boolean {
        //TODO
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Trip

        if (id != other.id) return false
        if (carPic != other.carPic) return false
        if (carName != other.carName) return false
        if (tripStartDate != other.tripStartDate) return false
        if (locations != other.locations) return false
        if (seats != other.seats) return false
        if (price != other.price) return false
        if (additionalInfo != other.additionalInfo) return false
        if (ownerId != other.ownerId) return false
        if (interestedUsers != other.interestedUsers) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (carPic?.hashCode() ?: 0)
        result = 31 * result + (carName?.hashCode() ?: 0)
        result = 31 * result + tripStartDate.hashCode()
        result = 31 * result + locations.hashCode()
        result = 31 * result + seats
        result = 31 * result + price.hashCode()
        result = 31 * result + additionalInfo.hashCode()
        result = 31 * result + (ownerId?.hashCode() ?: 0)
        result = 31 * result + interestedUsers.hashCode()
        return result
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

fun Trip.isEditable() = this.tripStartDate > System.currentTimeMillis()

data class TripUser(
    val userId: String = "",
    var isConfirmed: Boolean = false
)
