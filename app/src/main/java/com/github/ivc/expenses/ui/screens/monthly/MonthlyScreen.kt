package com.github.ivc.expenses.ui.screens.monthly

import android.icu.util.Currency
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ivc.expenses.db.Category
import com.github.ivc.expenses.db.MonthlyReport
import com.github.ivc.expenses.db.PurchaseEntry
import com.github.ivc.expenses.ui.compose.CategoryListItem
import com.github.ivc.expenses.ui.compose.PagerTitleBar
import com.github.ivc.expenses.ui.compose.PurchaseEntryListItem
import com.github.ivc.expenses.util.toCurrencyString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthlyScreen(currency: Currency, model: MonthlyViewModel = viewModel()) {
    val reportsByCurrency by model.monthlyReports.collectAsState()
    val reports = reportsByCurrency[currency] ?: listOf()
    val pagerState = rememberPagerState { reports.size }
    val expandedCategories = model.expandedCategories
    val scroller = PagerScroller(pagerState, reports.size, rememberCoroutineScope())

    if (reports.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Empty", style = MaterialTheme.typography.displayLarge)
        }
    } else {
        HorizontalPager(
            state = pagerState,
            reverseLayout = true,
            beyondBoundsPageCount = 36, // TODO: optimize slow composition instead
            modifier = Modifier.fillMaxSize(),
        ) { pageNumber ->
            val report by remember { derivedStateOf { reports[pageNumber] } }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PagerTitleBar(
                    title = report.titleText,
                    onLeft = scroller.next(pageNumber),
                    onRight = scroller.prev(pageNumber),
                )

                LazyColumn {
                    for (categorySummary in report.categories) {
                        val catId = (categorySummary.category ?: Category.Other).id
                        stickyHeader(
                            key = "category-${catId}",
                        ) {
                            CategoryListItem(
                                summary = categorySummary,
                                onClick = { expandedCategories.toggle(catId) })
                        }
                        if (expandedCategories[catId] == true) {
                            items(
                                items = categorySummary.purchases,
                                contentType = { PurchaseEntry::class },
                                key = { "purchase-${it.purchase.id}" },
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

val MonthlyReport.titleText
    @Composable get() = remember {
        buildAnnotatedString {
            append(yearMonth.year.toString())
            appendLine()
            append(yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()))
            appendLine()
            append(total.toCurrencyString())
        }
    }

fun SnapshotStateMap<Long, Boolean>.toggle(id: Long) {
    when (contains(id)) {
        true -> remove(id)
        else -> this[id] = true
    }
}

class PagerScroller @OptIn(ExperimentalFoundationApi::class) constructor(
    private val state: PagerState,
    private val pages: Int,
    private val scope: CoroutineScope,
) {
    @OptIn(ExperimentalFoundationApi::class)
    fun next(page: Int): (() -> Unit)? {
        if (page == pages - 1) {
            return null
        }
        return { scope.launch { state.scrollToPage(page + 1) } }
    }

    @OptIn(ExperimentalFoundationApi::class)
    fun prev(page: Int): (() -> Unit)? {
        if (page == 0) {
            return null
        }
        return { scope.launch { state.scrollToPage(page - 1) } }
    }
}
