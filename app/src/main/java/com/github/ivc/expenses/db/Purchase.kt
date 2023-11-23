package com.github.ivc.expenses.db

import android.icu.util.Currency
import android.icu.util.CurrencyAmount
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.ZonedDateTime

@Entity
data class Purchase(
    @PrimaryKey val id: Int,
    val timestamp: ZonedDateTime,
    val amount: Double,
    val currency: Currency,
    @ColumnInfo(name = "vendor_id") val vendorId: Int,
    @ColumnInfo(name = "category_id") val categoryId: Int?,
) {
    @Ignore
    val currencyAmount = CurrencyAmount(amount, currency)
}

@Entity
data class Vendor(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(name = "category_id") val categoryId: Int?,
)
