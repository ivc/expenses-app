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
            parentColumns = ["vendor_id"],
            childColumns = ["purchase_vendor_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
            deferred = false,
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["category_id"],
            childColumns = ["purchase_category_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE,
            deferred = false,
        ),
    ],
    indices = [
        Index("purchase_vendor_id"),
        Index("purchase_category_id"),
        Index("purchase_currency", "purchase_timestamp"),
    ],
)
data class Purchase(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "purchase_id")
    val id: Long = 0,

    @ColumnInfo(name = "purchase_timestamp")
    val timestamp: ZonedDateTime,

    @ColumnInfo(name = "purchase_amount")
    val amount: Double,

    @ColumnInfo(name = "purchase_currency")
    val currency: Currency,

    @ColumnInfo(name = "purchase_vendor_id")
    val vendorId: Long,

    @ColumnInfo(name = "purchase_category_id")
    val categoryId: Long? = null,
)

data class PurchaseEntry(
    @Embedded val purchase: Purchase,
    @Embedded val vendor: Vendor,
    @Embedded val category: Category,
)

data class DateTimeRange(
    val start: ZonedDateTime,
    val end: ZonedDateTime,
)

@Dao
interface PurchaseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(purchase: Purchase): Long

    @Query(
        """
        SELECT p.*, v.*, c.*
        FROM purchase p
        JOIN vendor v ON
            v.vendor_id = p.purchase_vendor_id
        LEFT OUTER JOIN category c ON
            c.category_id = coalesce(p.purchase_category_id, v.vendor_category_id)
        WHERE 1=1
            AND p.purchase_currency = :currency
            AND p.purchase_timestamp >= :startDate
            AND p.purchase_timestamp < :endDate
        """
    )
    fun list(
        currency: Currency,
        startDate: ZonedDateTime,
        endDate: ZonedDateTime,
    ): Flow<List<PurchaseEntry>>

    @Query(
        """
        SELECT min(purchase_timestamp) `start`, max(purchase_timestamp) `end`
        FROM purchase
        WHERE purchase_currency = :currency
        """
    )
    fun dateTimeRange(currency: Currency?): Flow<DateTimeRange?>

    @Query("SELECT DISTINCT purchase_currency FROM purchase ORDER BY purchase_currency")
    fun currencies(): Flow<List<Currency>>

    @Query("SELECT max(purchase_timestamp) FROM purchase")
    suspend fun maxTimestamp(): ZonedDateTime?
}
