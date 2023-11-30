package com.github.ivc.expenses.ui.screens.monthly

import android.icu.number.LocalizedNumberFormatter
import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.util.Currency
import android.icu.util.ULocale
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ivc.expenses.db.Category
import com.github.ivc.expenses.db.MonthlyReport
import com.github.ivc.expenses.ui.compose.PagerTitleBar
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val currencyFormatter: LocalizedNumberFormatter = NumberFormatter
    .withLocale(ULocale.getDefault())
    .precision(Precision.currency(Currency.CurrencyUsage.STANDARD))

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthlyScreen(currency: Currency, model: MonthlyViewModel = viewModel()) {
    val locale = remember { Locale.getDefault() }
    val reportsByCurrency by model.monthlyReports.collectAsState()
    val reports = reportsByCurrency[currency] ?: listOf()
    val pagerState = rememberPagerState { reports.size }
    val expandedState = remember { mutableStateOf(setOf<Long>()) }

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
                val coroutineScope = rememberCoroutineScope()
                PagerTitleBar(
                    title = buildAnnotatedString {
                        append(report.yearMonth.year.toString())
                        appendLine()
                        append(report.yearMonth.month.getDisplayName(TextStyle.FULL, locale))
                        appendLine()
                        append(currencyFormatter.format(report.total))
                    },
                    onLeft = when (pageNumber) {
                        reports.size - 1 -> null
                        else -> { -> coroutineScope.launch { pagerState.scrollToPage(pageNumber + 1) } }
                    },
                    onRight = when (pageNumber) {
                        0 -> null
                        else -> { -> coroutineScope.launch { pagerState.scrollToPage(pageNumber - 1) } }
                    },
                )
                PurchasesByCategory(report, expandedState)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PurchasesByCategory(report: MonthlyReport, expandedState: MutableState<Set<Long>>) {
    LazyColumn {
        for (categorySummary in report.categories) {
            val category = categorySummary.category ?: Category.Other
            val catId = category.id
            stickyHeader {
                CategoryListItem(
                    icon = category.icon.builtin.id,
                    color = category.color,
                    title = category.name,
                    total = categorySummary.total,
                    onClick = {
                        expandedState.value = when (expandedState.value.contains(catId)) {
                            true -> expandedState.value.minus(catId)
                            else -> expandedState.value.plus(catId)
                        }
                    })
            }
            if (expandedState.value.contains(catId)) {
                items(categorySummary.purchases) { entry ->
                    PurchaseListItem(
                        entry.purchase.timestamp,
                        entry.vendor.name,
                        entry.purchase.amount,
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryListItem(
    @DrawableRes icon: Int,
    @ColorInt color: Int,
    title: String,
    total: Double,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(text = title, minLines = 1, maxLines = 1)
        },
        leadingContent = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = Color(color),
            )
        },
        trailingContent = {
            Text(currencyFormatter.format(total).toString())
        },
        modifier = Modifier.clickable { onClick() },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PurchaseListItem(timestamp: ZonedDateTime, title: String, value: Double) {
    ListItem(
        headlineContent = {
            Text(text = title, minLines = 1, maxLines = 1)
        },
        trailingContent = {
            Text(currencyFormatter.format(value).toString())
        },
        overlineContent = {
            Text(DateTimeFormatter.RFC_1123_DATE_TIME.format(timestamp))
        },
        modifier = Modifier.combinedClickable(
            onClick = {},
        ),
    )
}
