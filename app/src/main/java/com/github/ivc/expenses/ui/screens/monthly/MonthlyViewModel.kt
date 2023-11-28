package com.github.ivc.expenses.ui.screens.monthly

import android.icu.util.Currency
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ivc.expenses.db.AppDb
import com.github.ivc.expenses.db.Category
import com.github.ivc.expenses.db.Purchase
import com.github.ivc.expenses.db.TimeRange
import com.github.ivc.expenses.db.Vendor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters.firstDayOfMonth

class MonthlyPageCollection(
    purchases: List<Purchase>,
    vendorsById: Map<Long, Vendor>,
    categoriesById: Map<Long, Category>,
) {
    val pagesByMonth: Map<ZonedDateTime, MonthlyPageState> =
        purchases.groupBy {
            it.timestamp.truncatedTo(ChronoUnit.DAYS).with(firstDayOfMonth())
        }.toSortedMap().mapValues { purchasesByMonth ->
            MonthlyPageState(
                purchasesByMonth.value,
                vendorsById,
                categoriesById,
            )
        }
    val months: List<ZonedDateTime> = pagesByMonth.keys.sortedDescending()

    companion object {
        val empty = MonthlyPageCollection(listOf(), mapOf(), mapOf())
    }
}

class MonthlyPageState(
    purchases: List<Purchase>,
    vendorsById: Map<Long, Vendor>,
    categoriesById: Map<Long, Category>,
) {
    val total = purchases.sumOf { it.amount }
    val categorySummaries: List<CategorySummary> = purchases
        .groupBy {
            it.categoryId ?: vendorsById[it.vendorId]?.categoryId
        }.entries.map { entry ->
            val categoryId: Long? = entry.key
            val purchasesPerCategory: List<Purchase> = entry.value
            val category = categoryId?.let { categoriesById[it] }
            return@map CategorySummary(
                category = category ?: Category.Other,
                total = purchasesPerCategory.sumOf { it.amount },
                purchases = purchasesPerCategory.map {
                    PurchaseSummary(
                        timestamp = it.timestamp,
                        amount = it.amount,
                        vendor = vendorsById[it.vendorId]!!,
                    )
                }.sortedByDescending { it.timestamp }
            )
        }.sortedByDescending { it.total }

    companion object {
        val empty = MonthlyPageState(listOf(), mapOf(), mapOf())
    }
}

data class CategorySummary(
    val category: Category,
    val total: Double,
    val purchases: List<PurchaseSummary>
)

data class PurchaseSummary(
    val timestamp: ZonedDateTime,
    val amount: Double,
    val vendor: Vendor,
)

class MonthlyViewModel(private val db: AppDb = AppDb.instance) : ViewModel() {
    private val vendorsById = db.vendors.indexById().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = mapOf(),
    )
    private val categoriesById = db.categories.indexById().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = mapOf(),
    )
    private val purchases = db.purchases.all().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = listOf(),
    )

    val pages: StateFlow<Map<Currency, MonthlyPageCollection>> =
        combine(purchases, vendorsById, categoriesById, ::toPageCollectionByCurrency).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = mapOf(),
        )

    fun months(currency: Currency): StateFlow<List<ZonedDateTime>> =
        db.purchases.timeRange(currency).map(::months).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf(),
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L

        fun months(timeRange: TimeRange): List<ZonedDateTime> {
            val start = timeRange.start.truncatedTo(ChronoUnit.DAYS).with(firstDayOfMonth())
            val end = timeRange.end.truncatedTo(ChronoUnit.DAYS).with(firstDayOfMonth())
            return generateSequence(end) {
                it.minusMonths(1)
            }.takeWhile { start.isBefore(it) || start.isEqual(it) }.toList()
        }

        fun toPageCollectionByCurrency(
            purchases: List<Purchase>,
            vendorsById: Map<Long, Vendor>,
            categoriesById: Map<Long, Category>
        ): Map<Currency, MonthlyPageCollection> {
            if (purchases.isEmpty() || vendorsById.isEmpty() || categoriesById.isEmpty()) {
                return mapOf()
            }
            return purchases.groupBy {
                it.currency
            }.mapValues { purchasesByCurrency ->
                MonthlyPageCollection(
                    purchasesByCurrency.value,
                    vendorsById,
                    categoriesById,
                )
            }
        }
    }
}
