package com.github.ivc.expenses.ui.screens.monthly

import android.icu.util.Currency
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ivc.expenses.db.Category
import com.github.ivc.expenses.db.PurchaseEntry
import com.github.ivc.expenses.ui.compose.CategoryListItem
import com.github.ivc.expenses.ui.compose.PagerTitleBar
import com.github.ivc.expenses.ui.compose.PurchaseEntryListItem
import com.github.ivc.expenses.util.toCurrencyString
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthlyScreen(currency: Currency, model: MonthlyViewModel = viewModel()) {
    val locale = remember { Locale.getDefault() }
    val reportsByCurrency by model.monthlyReports.collectAsState()
    val reports = reportsByCurrency[currency] ?: listOf()
    val pagerState = rememberPagerState { reports.size }
    val coroutineScope = rememberCoroutineScope()
    val expandedState = remember { mutableStateOf(setOf<Long>()) }

    fun toggleCategory(catId: Long) {
        expandedState.value =
            when (expandedState.value.contains(catId)) {
                true -> expandedState.value.minus(catId)
                else -> expandedState.value.plus(catId)
            }
    }

    fun scrollToNextPage(pageNumber: Int): (() -> Unit)? {
        if (pageNumber == reports.size - 1) {
            return null
        }
        return { coroutineScope.launch { pagerState.scrollToPage(pageNumber + 1) } }
    }

    fun scrollToPreviousPage(pageNumber: Int): (() -> Unit)? {
        if (pageNumber == 0) {
            return null
        }
        return { coroutineScope.launch { pagerState.scrollToPage(pageNumber - 1) } }
    }

    if (reports.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Empty", style = MaterialTheme.typography.displayLarge)
        }
    } else {
        HorizontalPager(
            state = pagerState,
            reverseLayout = true,
            beyondBoundsPageCount = 1,
            modifier = Modifier.fillMaxSize(),
        ) { pageNumber ->
            val report = reports[pageNumber]

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PagerTitleBar(
                    title = buildAnnotatedString {
                        append(report.yearMonth.year.toString())
                        appendLine()
                        append(report.yearMonth.month.getDisplayName(TextStyle.FULL, locale))
                        appendLine()
                        append(report.total.toCurrencyString())
                    },
                    onLeft = scrollToNextPage(pageNumber),
                    onRight = scrollToPreviousPage(pageNumber),
                )

                LazyColumn {
                    for (categorySummary in report.categories) {
                        val category = categorySummary.category ?: Category.Other
                        val catId = category.id
                        stickyHeader {
                            CategoryListItem(
                                category = category,
                                total = categorySummary.total,
                                onClick = { toggleCategory(catId) })
                        }
                        if (expandedState.value.contains(catId)) {
                            items(
                                items = categorySummary.purchases,
                                contentType = { PurchaseEntry::class },
                            ) {
                                PurchaseEntryListItem(it)
                            }
                        }
                    }
                }
            }
        }
    }
}
