package com.github.ivc.expenses.ui.screens.monthly

import android.icu.util.Currency
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
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ivc.expenses.ui.compose.PagerTitleBar
import com.github.ivc.expenses.ui.model.Report
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthlyScreen(currency: Currency, model: MonthlyViewModel = viewModel()) {
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
                    title = report.title,
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
fun PurchasesByCategory(report: Report, expandedState: MutableState<Set<Long>>) {
    LazyColumn {
        for (categorySummary in report.categories) {
            val catId = categorySummary.category.id
            stickyHeader {
                CategoryListItem(
                    icon = categorySummary.category.icon.builtin.id,
                    color = categorySummary.category.color,
                    title = categorySummary.title,
                    summary = categorySummary.summary,
                    onClick = {
                        expandedState.value = when (expandedState.value.contains(catId)) {
                            true -> expandedState.value.minus(catId)
                            else -> expandedState.value.plus(catId)
                        }
                    })
            }
            if (expandedState.value.contains(catId)) {
                items(categorySummary.entries) { entry ->
                    PurchaseListItem(
                        entry.timestamp,
                        entry.title,
                        entry.value,
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
    title: AnnotatedString,
    summary: AnnotatedString,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(text = title, minLines = 1, maxLines = 1)
        },
        leadingContent = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title.text,
                tint = Color(color),
            )
        },
        trailingContent = {
            Text(summary)
        },
        modifier = Modifier.clickable { onClick() },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PurchaseListItem(timestamp: AnnotatedString, title: AnnotatedString, value: AnnotatedString) {
    ListItem(
        headlineContent = {
            Text(text = title, minLines = 1, maxLines = 1)
        },
        trailingContent = {
            Text(value)
        },
        overlineContent = {
            Text(timestamp)
        },
        modifier = Modifier.combinedClickable(
            onClick = {},
        ),
    )
}
