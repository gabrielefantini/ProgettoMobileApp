package it.polito.mad.group25.lab.utils

fun String.nullOnBlank(): String? = let { if (isBlank()) null else this }