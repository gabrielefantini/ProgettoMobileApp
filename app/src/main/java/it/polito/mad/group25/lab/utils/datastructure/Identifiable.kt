package it.polito.mad.group25.lab.utils.datastructure

interface Identifiable {
    var id: String?
}

abstract class IdentifiableObject : Identifiable {
    override var id: String? = null
}