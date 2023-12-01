package com.github.ivc.expenses.util

import android.icu.number.LocalizedNumberFormatter
import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.util.Currency
import android.icu.util.ULocale
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val currencyFormatter: LocalizedNumberFormatter = NumberFormatter
    .withLocale(ULocale.getDefault())
    .precision(Precision.currency(Currency.CurrencyUsage.STANDARD))

// TODO: switch from Double to CurrencyAmount
fun Double.toCurrencyString(): String = currencyFormatter.format(this).toString()

fun ZonedDateTime.toRfc1123String(): String = DateTimeFormatter.RFC_1123_DATE_TIME.format(this)
