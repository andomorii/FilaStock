package br.com.filastock.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BR = Locale("pt", "BR")

fun Double.grams(decimals: Int = 0): String = String.format(BR, "%,." + decimals + "f g", this)

fun Double.money(): String = NumberFormat.getCurrencyInstance(BR).format(this)

fun Long.dateTime(): String = SimpleDateFormat("dd/MM/yyyy HH:mm", BR).format(Date(this))

/** Aceita "12,5" ou "12.5". */
fun String.toDoubleBr(): Double? = trim().replace(',', '.').toDoubleOrNull()
