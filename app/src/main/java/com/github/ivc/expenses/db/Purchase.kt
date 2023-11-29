package com.github.ivc.expenses.db

import android.icu.util.Currency
import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.ZonedDateTime

@Stable
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
)

data class PurchaseEntry(
    @Embedded val purchase: Purchase,
    @Embedded("purchase_vendor_") val vendor: Vendor,
    @ColumnInfo("group_category_id") val groupCategoryId: Long?,
)

data class TimeRange(val start: ZonedDateTime, val end: ZonedDateTime)

data class MonthlyReportDateTime(val dateTime: ZonedDateTime)

typealias MonthlyReportsMap = Map<@MapColumn("timestamp") MonthlyReportDateTime,
        Map<@MapColumn("group_category_id") Long, List<PurchaseEntry>>>

typealias MonthlyReportsMapsByCurrency = Map<@MapColumn("currency") Currency, MonthlyReportsMap>

@Dao
interface PurchaseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(purchase: Purchase): Long

    @Query(
        """SELECT min(timestamp) `start`, max(timestamp) `end`
        FROM purchase WHERE currency = :currency"""
    )
    fun timeRange(currency: Currency): Flow<TimeRange>

    @Query(
        """
        SELECT
            p.id,
            p.timestamp,
            p.currency,
            p.amount,
            p.vendor_id,
            p.category_id,
            v.id purchase_vendor_id,
            v.name purchase_vendor_name,
            v.category_id purchase_vendor_category_id,
            coalesce(p.category_id, v.category_id) group_category_id
        FROM purchase p
        JOIN vendor v ON p.vendor_id = v.id
        """
    )
    fun monthlyReports(): Flow<MonthlyReportsMapsByCurrency>
}
