package com.github.ivc.expenses.ui.screens.monthly

import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.util.Currency
import android.icu.util.ULocale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ivc.expenses.db.AppDb
import com.github.ivc.expenses.db.Category
import com.github.ivc.expenses.db.MonthlyReportsMap
import com.github.ivc.expenses.db.PurchaseEntry
import com.github.ivc.expenses.db.TimeRange
import com.github.ivc.expenses.ui.model.AnnotatedPurchaseEntry
import com.github.ivc.expenses.ui.model.CategorySummary
import com.github.ivc.expenses.ui.model.Report
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.format.DateTimeFormatter

private val monthFormatter = DateTimeFormatter.ofPattern("MMMM")
private val fullDateFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
private val currencyFormatter = NumberFormatter
    .withLocale(ULocale.getDefault())
    .precision(Precision.currency(Currency.CurrencyUsage.STANDARD))

class MonthlyViewModel(db: AppDb = AppDb.instance) : ViewModel() {
    private val categoriesById = db.categories.indexById().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = mapOf(),
    )

    val monthlyReports: StateFlow<Map<Currency, List<Report>>> =
        combine(
            db.purchases.monthlyReports(),
            categoriesById,
        ) { monthlyReportsMapsByCurrency, categories ->
            monthlyReportsMapsByCurrency.mapValues {
                toListOfReports(it.value, categories)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = mapOf(),
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L

        private fun toListOfReports(
            reportsMap: MonthlyReportsMap,
            categories: Map<Long, Category>,
        ): List<Report> =
            reportsMap.asSequence().sortedByDescending { it.key.dateTime }.map { entry ->
                val dateTime = entry.key.dateTime
                val categoriesList = toListOfCategorySummaries(entry.value, categories)
                val total = categoriesList.sumOf { it.total }

                return@map Report(
                    timeRange = TimeRange(dateTime, dateTime.plusMonths(1)),
                    categories = categoriesList,
                    title = buildAnnotatedString {
                        append(dateTime.year.toString())
                        appendLine()
                        append(monthFormatter.format(dateTime))
                        appendLine()
                        append(currencyFormatter.format(total))
                    },
                )
            }.toList()

        private fun toListOfCategorySummaries(
            categoriesMap: Map<Long, List<PurchaseEntry>>,
            categories: Map<Long, Category>,
        ): List<CategorySummary> = categoriesMap.map { entry ->
            val groupCategory = categories[entry.key] ?: Category.Other
            val purchaseEntries = entry.value
            val total = purchaseEntries.sumOf { it.purchase.amount }
            return@map CategorySummary(
                total = total,
                category = groupCategory,
                entries = purchaseEntries.map(::toAnnotatedPurchaseEntry),
                title = AnnotatedString(groupCategory.name),
                summary = AnnotatedString(currencyFormatter.format(total).toString()),
            )
        }.sortedByDescending { it.total }

        private fun toAnnotatedPurchaseEntry(entry: PurchaseEntry) = AnnotatedPurchaseEntry(
            entry = entry,
            title = AnnotatedString(entry.vendor.name),
            value = AnnotatedString(currencyFormatter.format(entry.purchase.amount).toString()),
            timestamp = AnnotatedString(fullDateFormatter.format(entry.purchase.timestamp)),
        )
    }
}
