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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ivc.expenses.db.Category
import com.github.ivc.expenses.db.MonthlyReport
import com.github.ivc.expenses.db.PurchaseEntry
import com.github.ivc.expenses.ui.compose.CategoryListItem
import com.github.ivc.expenses.ui.compose.CategorySelectorDialog
import com.github.ivc.expenses.ui.compose.PagerTitleBar
import com.github.ivc.expenses.ui.compose.PurchaseEntryListItem
import com.github.ivc.expenses.ui.compose.painter
import com.github.ivc.expenses.ui.compose.totalText
import com.github.ivc.expenses.util.toCurrencyString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MonthlyScreen(currency: Currency, model: MonthlyViewModel = viewModel()) {
    val reportsByCurrency by model.monthlyReports.collectAsState()
    val reports = reportsByCurrency[currency] ?: listOf()
    val categories by model.categories.collectAsState()
    val pagerState = rememberPagerState { reports.size }
    val coroutineScope = rememberCoroutineScope()
    val ioCoroutineScope = rememberCoroutineScope { Dispatchers.IO }
    var selectedEntry by model.selectedEntry
    var selectedCategory by model.selectedCategory

    if (reports.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Empty", style = MaterialTheme.typography.displayLarge)
        }
    } else {
        when (selectedCategory) {
            null -> HorizontalPager(
                state = pagerState,
                reverseLayout = true,
                modifier = Modifier.fillMaxSize(),
            ) { pageNumber ->
                val report = reports[pageNumber]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PagerTitleBar(
                        title = report.titleText,
                        currentPage = pageNumber,
                        totalPages = reports.size,
                        reversed = true,
                        scrollToPage = { targetPage ->
                            coroutineScope.launch {
                                pagerState.scrollToPage(targetPage, 0f)
                            }
                        },
                    )
                    Column(Modifier.fillMaxSize()) {
                        report.categories.forEach { categorySummary ->
                            CategoryListItem(
                                summary = categorySummary,
                                onClick = {
                                    selectedCategory = categorySummary.category ?: Category.Other
                                })
                        }
                    }
                }
            }

            else ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
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
                    // TODO: remove 'find'
                    val selectedSummary = reports[pagerState.currentPage].categories.find {
                        (it.category ?: Category.Other) == selectedCategory
                    }

                    InputChip(
                        selected = true,
                        onClick = { selectedCategory = null },
                        label = { Text("${selectedCategory!!.name}: ${selectedSummary!!.totalText}") },
                        leadingIcon = { Icon(selectedCategory!!.painter, selectedCategory!!.name) }
                    )

                    LazyColumn(Modifier.fillMaxSize()) {
                        items(
                            items = selectedSummary?.purchases ?: listOf(),
                            contentType = { PurchaseEntry::class },
                            key = { "purchase-${it.purchase.id}" },
                        ) { entry ->
                            PurchaseEntryListItem(
                                entry = entry,
                                onLongClick = { selectedEntry = it },
                            )
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
