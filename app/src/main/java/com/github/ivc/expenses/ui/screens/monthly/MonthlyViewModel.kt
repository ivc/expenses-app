package com.github.ivc.expenses.ui.screens.monthly

import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.util.Currency
import android.icu.util.ULocale
import androidx.compose.runtime.Stable
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
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters.firstDayOfMonth

private fun headerTimestampFormatter(value: ZonedDateTime): String =
    DateTimeFormatter.ofPattern("yyyy\nMMMM").format(value)

private fun itemTimestampFormatter(value: ZonedDateTime): String =
    DateTimeFormatter.RFC_1123_DATE_TIME.format(value)

private fun currencyFormatter(value: Double): String {
    return NumberFormatter
        .withLocale(ULocale.getDefault())
        .precision(Precision.currency(Currency.CurrencyUsage.STANDARD))
        .format(value)
        .toString()
}

@Stable
class FormattedValue<T : Comparable<T>>(private val value: T, formatter: (T) -> String) :
    Comparable<FormattedValue<T>> {
    private val formatted = formatter(value)
    override fun compareTo(other: FormattedValue<T>): Int {
        return value.compareTo(other.value)
    }

    override fun toString(): String {
        return formatted
    }
}

typealias FormattedTimestamp = FormattedValue<ZonedDateTime>
typealias FormattedDouble = FormattedValue<Double>

@Stable
class MonthlyPageCollection(
    purchases: List<Purchase>,
    vendorsById: Map<Long, Vendor>,
    categoriesById: Map<Long, Category>,
) {
    val pagesByMonth: Map<FormattedTimestamp, MonthlyPageState> =
        purchases.groupBy {
            it.timestamp.truncatedTo(ChronoUnit.DAYS).with(firstDayOfMonth())
        }.mapValues { purchasesByMonth ->
            MonthlyPageState(
                purchasesByMonth.value,
                vendorsById,
                categoriesById,
            )
        }.mapKeys {
            FormattedTimestamp(it.key, ::headerTimestampFormatter)
        }.toSortedMap()
    val months: List<FormattedTimestamp> = pagesByMonth.keys.sortedDescending()

    companion object {
        val empty = MonthlyPageCollection(listOf(), mapOf(), mapOf())
    }
}

@Stable
class MonthlyPageState(
    purchases: List<Purchase>,
    vendorsById: Map<Long, Vendor>,
    categoriesById: Map<Long, Category>,
) {
    val total = FormattedDouble(purchases.sumOf { it.amount }, ::currencyFormatter)
    val categorySummaries: List<CategorySummary> = purchases
        .groupBy {
            it.categoryId ?: vendorsById[it.vendorId]?.categoryId
        }.entries.map { entry ->
            val categoryId: Long? = entry.key
            val purchasesPerCategory: List<Purchase> = entry.value
            val category = categoryId?.let { categoriesById[it] }
            return@map CategorySummary(
                category = category ?: Category.Other,
                total = FormattedDouble(
                    purchasesPerCategory.sumOf { it.amount },
                    ::currencyFormatter,
                ),
                purchases = purchasesPerCategory.map {
                    PurchaseSummary(
                        timestamp = FormattedTimestamp(it.timestamp, ::itemTimestampFormatter),
                        amount = FormattedDouble(
                            it.amount,
                            ::currencyFormatter
                        ),
                        vendor = vendorsById[it.vendorId]!!,
                    )
                }.sortedByDescending { it.timestamp }
            )
        }.sortedByDescending { it.total }

    companion object {
        val empty = MonthlyPageState(listOf(), mapOf(), mapOf())
    }
}

@Stable
data class CategorySummary(
    val category: Category,
    val total: FormattedDouble,
    val purchases: List<PurchaseSummary>
)

@Stable
data class PurchaseSummary(
    val timestamp: FormattedTimestamp,
    val amount: FormattedDouble,
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
