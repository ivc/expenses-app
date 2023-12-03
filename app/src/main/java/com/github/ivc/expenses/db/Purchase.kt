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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Month
import java.time.Year
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

data class YearMonth(
    val year: Year,
    val month: Month,
) : Comparable<YearMonth> {
    override fun compareTo(other: YearMonth): Int {
        return when (val cmpYear = this.year.compareTo(other.year)) {
            0 -> this.month.compareTo(other.month)
            else -> cmpYear
        }
    }
}

data class MonthlyReport(
    val yearMonth: YearMonth,
    val categories: List<CategorySummary>,
) {
    val total: Double by lazy { categories.sumOf { it.total } }
}

data class CategorySummary(
    val category: Category,
    val purchases: List<PurchaseEntry>,
) {
    val total: Double by lazy { purchases.sumOf { it.purchase.amount } }
}

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
        """
    )
    fun purchaseEntriesByCurrencyByYearMonthByCategory(): Flow<
            Map<@MapColumn("purchase_currency") Currency,
                    Map<@MapColumn("purchase_timestamp") YearMonth,
                            Map<Category, List<PurchaseEntry>>>>>

    @Query("SELECT DISTINCT purchase_currency FROM purchase ORDER BY purchase_currency")
    fun currencies(): Flow<List<Currency>>

    @Query("SELECT max(purchase_timestamp) FROM purchase")
    suspend fun maxTimestamp(): ZonedDateTime?

    fun monthlyReports(): Flow<Map<Currency, List<MonthlyReport>>> {
        return purchaseEntriesByCurrencyByYearMonthByCategory().distinctUntilChanged()
            .map { everyResult ->
                everyResult.mapValues { byCurrencyEntry ->
                    byCurrencyEntry.value.entries.map { byYearMonthEntry ->
                        MonthlyReport(
                            yearMonth = byYearMonthEntry.key,
                            categories = byYearMonthEntry.value.entries.map { byCategoryEntry ->
                                CategorySummary(
                                    category = byCategoryEntry.key,
                                    purchases = byCategoryEntry.value.sortedByDescending { it.purchase.timestamp },
                                )
                            }.sortedByDescending { it.total }
                        )
                    }.sortedByDescending { it.yearMonth }
                }
            }
    }
}
