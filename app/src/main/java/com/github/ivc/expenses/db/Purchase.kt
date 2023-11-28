package com.github.ivc.expenses.db

import android.icu.util.Currency
import android.icu.util.CurrencyAmount
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.ZonedDateTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Vendor::class,
            parentColumns = ["id"],
            childColumns = ["vendor_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
            deferred = false,
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE,
            deferred = false,
        ),
    ],
    indices = [
        Index("vendor_id"),
        Index("category_id"),
    ],
)
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

data class TimeRange(val start: ZonedDateTime, val end: ZonedDateTime)

@Dao
interface PurchaseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(purchase: Purchase): Long

    @Query(
        """SELECT min(timestamp) `start`, max(timestamp) `end`
        FROM purchase WHERE currency = :currency"""
    )
    fun timeRange(currency: Currency): Flow<TimeRange>
}
