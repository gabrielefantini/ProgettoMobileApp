package it.polito.mad.group25.lab.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Long.toLocalDate() =
    Instant.ofEpochMilli(this)
        .toLocalDate()

fun Long.toLocalDateTime() =
    Instant.ofEpochMilli(this)
        .toLocalDateTime()

fun Long.asFormattedDate(format: String) =
    DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(this))

fun Instant.toLocalDate() = this.atZone(ZoneId.systemDefault()).toLocalDate()
fun Instant.toLocalDateTime() = this.atZone(ZoneId.systemDefault()).toLocalDateTime()