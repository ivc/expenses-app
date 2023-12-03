package com.github.ivc.expenses.ui.compose

import android.icu.util.Currency
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ivc.expenses.db.AppDb
import com.github.ivc.expenses.db.Category
import com.github.ivc.expenses.db.CategorySummary
import com.github.ivc.expenses.db.PurchaseEntry
import com.github.ivc.expenses.util.toCurrencyString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.time.ZonedDateTime
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PurchaseSummaryList(
    currency: Currency,
    start: ZonedDateTime,
    end: ZonedDateTime,
    onPurchaseEntryLongClick: (PurchaseEntry) -> Unit = {},
    expandedCategoriesState: SnapshotStateMap<Long, Boolean> = mutableStateMapOf(),
    model: PurchaseSummaryModel = viewModel(key = PurchaseSummaryModel.key(currency, start, end)) {
        PurchaseSummaryModel(currency, start, end)
    },
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

@Preview
@Composable
fun PreviewPurchaseSummaryList() {
    val currency = Currency.getInstance(Locale.getDefault())
    val start = ZonedDateTime.now()
    val end = ZonedDateTime.now()
    Surface(Modifier.width(412.dp)) {
        PurchaseSummaryList(
            currency, start, end,
            model = PurchaseSummaryModel(
                currency, start, end,
                purchasesByCategoryFlow = flow {
                    emit(
                        mapOf(
                            Category.Preview to listOf(PurchaseEntry.Preview)
                        )
                    )
                },
                categoryTotalsFlow = flow {
                    emit(listOf(CategorySummary(Category.Preview, 123.0)))
                }
            )
        )
    }
}

class PurchaseSummaryModel(
    currency: Currency,
    startDate: ZonedDateTime,
    endDate: ZonedDateTime,
    purchasesByCategoryFlow: Flow<Map<Category, List<PurchaseEntry>>> =
        AppDb.instance.purchases.listByCategory(currency, startDate, endDate),
    categoryTotalsFlow: Flow<List<CategorySummary>> =
        AppDb.instance.purchases.categorySummaries(currency, startDate, endDate)
) : ViewModel() {
    val purchasesByCategory = purchasesByCategoryFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = mapOf(),
    )

    val categoryTotals = categoryTotalsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = listOf(),
    )

    companion object {
        fun key(currency: Currency, startDate: ZonedDateTime, endDate: ZonedDateTime): String {
            val code = currency.currencyCode
            val start = startDate.toEpochSecond()
            val end = endDate.toEpochSecond()
            return "$code/$start-$end"
        }

        private const val TIMEOUT_MILLIS = 5_000L
    }
}