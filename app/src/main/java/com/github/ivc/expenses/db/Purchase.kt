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
import java.util.Locale

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
) {
    companion object {
        val Preview
            get() = PurchaseEntry(
                purchase = Purchase(
                    timestamp = ZonedDateTime.now(),
                    amount = 123.0,
                    currency = Currency.getInstance(Locale.getDefault()),
                    vendorId = 1,
                ),
                vendor = Vendor(name = "test"),
                category = Category.Preview,
            )
    }
}

data class DateTimeRange(
    val start: ZonedDateTime,
    val end: ZonedDateTime,
)

data class CategorySummary(
    @Embedded val category: Category,
    val total: Double,
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
        ORDER BY purchase_timestamp DESC
        """
    )
    fun listByCategory(
        currency: Currency,
        startDate: ZonedDateTime,
        endDate: ZonedDateTime,
    ): Flow<Map<Category, List<PurchaseEntry>>>

    @Query(
        """
        SELECT c.*, sum(purchase_amount) total
        FROM purchase p
        JOIN vendor v ON
            v.vendor_id = p.purchase_vendor_id
        LEFT OUTER JOIN category c ON
            c.category_id = coalesce(p.purchase_category_id, v.vendor_category_id)
        WHERE 1=1
            AND p.purchase_currency = :currency
            AND p.purchase_timestamp >= :startDate
            AND p.purchase_timestamp < :endDate
        GROUP BY category_id, category_name, category_icon, category_color
        ORDER BY total DESC
        """
    )
    fun categorySummaries(
        currency: Currency,
        startDate: ZonedDateTime,
        endDate: ZonedDateTime,
    ): Flow<List<CategorySummary>>

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
