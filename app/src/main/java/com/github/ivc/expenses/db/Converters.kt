package com.github.ivc.expenses.db

import android.icu.util.Currency
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@ProvidedTypeConverter
class Converters(
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val currencyResolver: (String) -> Currency = Currency::getInstance,
    private val instantResolver: (Long) -> Instant = Instant::ofEpochSecond,
) {
    @TypeConverter
    fun stringToCategoryIconRef(value: String): CategoryIconRef {
        val name = value.removePrefix(BuiltinCategoryIcon.prefix)
        val builtin = BuiltinCategoryIcon.byName(name) ?: BuiltinCategoryIcon.Unknown
        return CategoryIconRef(value, builtin)
    }

    @TypeConverter
    fun categoryIconRefToString(value: CategoryIconRef): String = value.url

    @TypeConverter
    fun stringToCurrency(value: String): Currency = currencyResolver(value)

    @TypeConverter
    fun currencyToString(value: Currency): String = value.currencyCode

    @TypeConverter
    fun zonedDateTimeToLong(value: ZonedDateTime): Long = value.toEpochSecond()

    @TypeConverter
    fun longToZonedDateTime(value: Long): ZonedDateTime {
        val ts = instantResolver(value)
        return ts.atZone(zoneId)
    }

    @TypeConverter
    fun regexToString(value: Regex): String {
        return value.pattern
    }

    @TypeConverter
    fun stringToRegex(value: String): Regex {
        return Regex(value)
    }
}