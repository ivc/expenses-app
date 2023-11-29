package com.github.ivc.expenses.ui.screens.monthly

import android.icu.util.Currency
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ivc.expenses.db.Category
import com.github.ivc.expenses.ui.compose.PagerTitleBar
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthlyScreen(currency: Currency, model: MonthlyViewModel = viewModel()) {
    val pagesByCurrency by model.pages.collectAsState()
    val pages = pagesByCurrency[currency] ?: MonthlyPageCollection.empty
    val months = pages.months
    val pagerState = rememberPagerState { months.size }
    val expandedState = remember { mutableStateOf(setOf<Long>()) }

    if (months.isEmpty()) {
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
            val month = months[pageNumber]
            val page = pages.pagesByMonth[month] ?: MonthlyPageState.empty

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val coroutineScope = rememberCoroutineScope()
                PagerTitleBar(
                    title = buildAnnotatedString {
                        append(month.toString())
                        appendLine()
                        append(page.total.toString())
                    },
                    onLeft = when (pageNumber) {
                        months.size - 1 -> null
                        else -> { -> coroutineScope.launch { pagerState.scrollToPage(pageNumber + 1) } }
                    },
                    onRight = when (pageNumber) {
                        0 -> null
                        else -> { -> coroutineScope.launch { pagerState.scrollToPage(pageNumber - 1) } }
                    },
                )
                PurchasesByCategory(page, expandedState)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PurchasesByCategory(page: MonthlyPageState, expandedState: MutableState<Set<Long>>) {
    LazyColumn {
        for (categorySummary in page.categorySummaries) {
            val catId = categorySummary.category.id
            stickyHeader {
                CategoryListItem(
                    category = categorySummary.category,
                    categorySummary.total,
                    onClick = {
                        expandedState.value = when (expandedState.value.contains(catId)) {
                            true -> expandedState.value.minus(catId)
                            else -> expandedState.value.plus(catId)
                        }
                    })
            }
            if (expandedState.value.contains(catId)) {
                items(categorySummary.purchases) { purchase ->
                    PurchaseListItem(
                        timestamp = purchase.timestamp,
                        vendor = purchase.vendor.name,
                        amount = purchase.amount,
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryListItem(category: Category, total: FormattedDouble, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(
                text = category.name,
                minLines = 1,
                maxLines = 1,
            )
        },
        leadingContent = {
            Icon(
                painter = painterResource(id = category.icon.builtin.id),
                contentDescription = category.name,
                tint = Color(category.color),
            )
        },
        trailingContent = {
            CurrencyText(total)
        },
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .clickable { onClick() },
    )
}

@Composable
fun PurchaseListItem(timestamp: FormattedTimestamp, vendor: String, amount: FormattedDouble) {
    ListItem(
        headlineContent = {
            Text(
                vendor,
                minLines = 1,
                maxLines = 1,
            )
        },
        trailingContent = { CurrencyText(amount) },
        overlineContent = {
            Text(
                timestamp.toString(),
                minLines = 1,
                maxLines = 1,
            )
        },
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth(),
    )
}

@Composable
fun CurrencyText(amount: FormattedDouble, style: TextStyle = MaterialTheme.typography.labelLarge) {
    Text(
        text = amount.toString(),
        style = style,
        minLines = 1,
        maxLines = 1,
    )
}
