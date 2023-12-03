package com.github.ivc.expenses.ui.compose

import android.icu.util.Currency
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.viewModelFactory
import com.github.ivc.expenses.db.AppDb
import com.github.ivc.expenses.db.PurchaseEntry
import com.github.ivc.expenses.util.toCurrencyString
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.time.ZonedDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PurchaseSummaryList(
    currency: Currency,
    start: ZonedDateTime,
    end: ZonedDateTime,
    onPurchaseEntryLongClick: (PurchaseEntry) -> Unit = {},
    expandedCategoriesState: SnapshotStateMap<Long, Boolean> = mutableStateMapOf(),
    model: PurchaseSummaryModel = PurchaseSummaryModel.viewModel(currency, start, end),
) {
    val purchases by model.purchasesByCategory.collectAsState()
    val totals by model.categoryTotals.collectAsState()
    val total = totals.sumOf { it.total }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Total: ${total.toCurrencyString()}")
        LazyColumn(Modifier.fillMaxSize()) {
            for (summary in totals) {
                val category = summary.category
                val categoryTotal = summary.total
                stickyHeader(
                    key = "category-${category.id}",
                ) {
                    CategoryListItem(
                        category = category,
                        total = categoryTotal,
                        onClick = {
                            expandedCategoriesState[category.id] =
                                !(expandedCategoriesState[category.id] ?: false)
                        },
                    )
                }
                if (expandedCategoriesState[category.id] == true) {
                    items(
                        items = purchases[category] ?: listOf(),
                        contentType = { PurchaseEntry::class },
                        key = { "purchase-${it.purchase.id}" },
                    ) { entry ->
                        PurchaseEntryListItem(
                            entry = entry,
                            onLongClick = onPurchaseEntryLongClick,
                        )
                    }
                }
            }
        }
    }
}

class PurchaseSummaryModel(
    currency: Currency,
    startDate: ZonedDateTime,
    endDate: ZonedDateTime,
    db: AppDb = AppDb.instance,
) : ViewModel() {
    val purchasesByCategory = db.purchases.listByCategory(currency, startDate, endDate).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = mapOf(),
    )

    val categoryTotals = db.purchases.categorySummaries(currency, startDate, endDate).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = listOf(),
    )

    companion object {
        @Composable
        fun viewModel(
            currency: Currency,
            startDate: ZonedDateTime,
            endDate: ZonedDateTime
        ): PurchaseSummaryModel {
            val currencyCode = currency.currencyCode
            val startEpochSeconds = startDate.toEpochSecond()
            val endEpochSeconds = endDate.toEpochSecond()
            return androidx.lifecycle.viewmodel.compose.viewModel(
                key = "$currencyCode.$startEpochSeconds-$endEpochSeconds",
                factory = factory,
                extras = MutableCreationExtras().apply {
                    set(CurrencyKey, currency)
                    set(StartDateKey, startDate)
                    set(EndDateKey, endDate)
                },
            )
        }

        private const val TIMEOUT_MILLIS = 5_000L

        private object CurrencyKey : CreationExtras.Key<Currency>
        private object StartDateKey : CreationExtras.Key<ZonedDateTime>
        private object EndDateKey : CreationExtras.Key<ZonedDateTime>

        private val factory = viewModelFactory {
            addInitializer(PurchaseSummaryModel::class) {
                val currency = get(CurrencyKey)!!
                val startDate = get(StartDateKey)!!
                val endDate = get(EndDateKey)!!
                return@addInitializer PurchaseSummaryModel(
                    currency = currency,
                    startDate = startDate,
                    endDate = endDate,
                )
            }
        }
    }
}