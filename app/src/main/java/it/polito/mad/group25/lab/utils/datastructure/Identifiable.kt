package it.polito.mad.group25.lab.utils.datastructure

interface Identifiable {
    var id: String?
}

abstract class IdentifiableObject : Identifiable {
    override var id: String? = null

    override fun equals(other: Any?): Boolean {
        if (other !is Identifiable) return false
        return id != null && id == other.id
    }

}