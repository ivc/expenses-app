package com.github.ivc.expenses.db

import android.icu.util.Currency
import android.icu.util.CurrencyAmount
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import java.time.ZonedDateTime

@Entity
data class Purchase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: ZonedDateTime,
    val amount: Double,
    val currency: Currency,
    @ColumnInfo(name = "vendor_id") val vendorId: Long,
    @ColumnInfo(name = "category_id") val categoryId: Long? = null,
) {
    @Ignore
    val currencyAmount = CurrencyAmount(amount, currency)
}

@Dao
interface PurchaseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(purchase: Purchase): Long
}
