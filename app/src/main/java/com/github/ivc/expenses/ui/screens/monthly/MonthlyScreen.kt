package com.github.ivc.expenses.ui.screens.monthly

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ivc.expenses.ui.compose.CategorySelectorDialog
import com.github.ivc.expenses.ui.compose.PagerNavBar
import com.github.ivc.expenses.ui.compose.PurchaseSummaryList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthlyScreen(model: MonthlyViewModel = viewModel()) {
    val currency by model.currency.collectAsState()
    val categories by model.categories.collectAsState()
    val months by model.months.collectAsState()
    val pagerState = rememberPagerState { months.size }
    val ioCoroutineScope = rememberCoroutineScope { Dispatchers.IO }
    var selectedEntry by model.selectedEntry

    if (months.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Empty", style = MaterialTheme.typography.displayLarge)
        }
    } else {
        if (selectedEntry != null) {
            CategorySelectorDialog(
                entry = selectedEntry!!,
                categories = categories,
                onDismiss = { selectedEntry = null },
                onConfirm = { category ->
                    selectedEntry?.vendor?.let { vendor ->
                        ioCoroutineScope.launch {
                            model.setVendorCategory(vendor, category)
                        }
                    }
                    selectedEntry = null
                },
            )
        }

        HorizontalPager(
            state = pagerState,
            reverseLayout = true,
            beyondBoundsPageCount = 1,
            modifier = Modifier.fillMaxSize(),
        ) { pageNumber ->
            val month = months[pageNumber]

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PagerNavBar(
                    state = pagerState,
                    page = pageNumber,
                    reverseLayout = true,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append(month.year.toString())
                            appendLine()
                            append(month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()))
                        },
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                }

                PurchaseSummaryList(
                    currency = currency,
                    start = month,
                    end = month.with(TemporalAdjusters.firstDayOfNextMonth()),
                    expandedCategoriesState = model.expandedCategories,
                    onPurchaseEntryLongClick = { selectedEntry = it },
                )
            }
        }
    }
}
