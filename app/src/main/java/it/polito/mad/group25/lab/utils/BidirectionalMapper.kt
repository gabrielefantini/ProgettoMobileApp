package it.polito.mad.group25.lab.utils

interface BidirectionalMapper<A, B> {

    fun directMap(a: A): B?

    fun reverseMap(b: B): A?

    companion object {
        fun <A> identity() = object : BidirectionalMapper<A, A> {
            override fun directMap(a: A): A = a
            override fun reverseMap(b: A): A = b
        }
    }
}