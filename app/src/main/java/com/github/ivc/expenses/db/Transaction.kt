package com.github.ivc.expenses.db

import android.icu.math.BigDecimal
import android.icu.util.Currency
import android.icu.util.CurrencyAmount
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity
data class Transaction(
    @PrimaryKey val id: Int,
    val timestamp: ZonedDateTime,
    val amount: BigDecimal,
    val currency: Currency,
    @ColumnInfo(name= "vendor_id") val vendorId: Int,
    @ColumnInfo(name = "category_id") val categoryId: Int?,
) {
    val currencyAmount = CurrencyAmount(amount, currency)
}

@Entity
data class Vendor(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(name = "category_id") val categoryId: Int?,
)

class CurrencyConverter {
    @TypeConverter
    fun marshal(value: Currency): String = value.currencyCode

    @TypeConverter
    fun unmarshal(value: String): Currency = Currency.getInstance(value)
}

class ZonedDateTimeConverter {
    @TypeConverter
    fun marshal(value: ZonedDateTime): Long = value.toEpochSecond()

    @TypeConverter
    fun unmarshal(value: Long): ZonedDateTime {
        val tz = ZoneId.systemDefault()
        val ts = Instant.ofEpochSecond(value)
        return ts.atZone(tz)
    }
}