package com.github.ivc.expenses.ui.screens.monthly

import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.util.Currency
import android.icu.util.ULocale
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ivc.expenses.db.Category
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


private val headerFormat = DateTimeFormatter.ofPattern("yyyy\nMMMM")
private val itemFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
private val currencyFormat =
    NumberFormatter.withLocale(ULocale.getDefault())
        .precision(Precision.currency(Currency.CurrencyUsage.STANDARD))

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthlyScreen(currency: Currency, model: MonthlyViewModel = viewModel()) {
    val pagesByCurrency by model.pages.collectAsState()
    val pages = pagesByCurrency[currency] ?: MonthlyPageCollection.empty
    val months = pages.months
    val pagerState = rememberPagerState { months.size }
    val expandedState = remember { mutableStateOf(setOf<Long>()) }

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            reverseLayout = true,
        ) {
            val month = months[it]
            val page = pages.pagesByMonth[month] ?: MonthlyPageState.empty

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MonthTitle(month, page.total, pagerState)
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
                        expandedState.value =
                            when (expandedState.value.contains(catId)) {
                                true -> expandedState.value.minus(catId)
                                else -> expandedState.value.plus(catId)
                            }
                    }
                )
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
fun CategoryListItem(category: Category, total: Double, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(text = category.name)
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
        modifier = Modifier.clickable { onClick() },
    )
}

@Composable
fun PurchaseListItem(timestamp: ZonedDateTime, vendor: String, amount: Double) {
    ListItem(
        headlineContent = { Text(vendor) },
        trailingContent = { CurrencyText(amount) },
        overlineContent = { Text(itemFormatter.format(timestamp)) }
    )
}

@Composable
fun CurrencyText(amount: Double, style: TextStyle = MaterialTheme.typography.labelLarge) {
    Text(
        text = currencyFormat.format(amount).toString(),
        style = style,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthTitle(timestamp: ZonedDateTime, total: Double, pagerState: PagerState) {
    val coroutineScope = rememberCoroutineScope()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        IconButton(
            onClick = {
                coroutineScope.launch {
                    pagerState.scrollToPage(pagerState.settledPage + 1, 0f)
                }
            },
            enabled = pagerState.canScrollForward,
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous Month",
                modifier = Modifier.size(32.dp),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                timestamp.format(headerFormat),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
            )
            CurrencyText(total, style = MaterialTheme.typography.headlineLarge)
        }

        IconButton(
            onClick = {
                coroutineScope.launch {
                    pagerState.scrollToPage(pagerState.settledPage - 1, 0f)
                }
            },
            enabled = pagerState.canScrollBackward,
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Next Month",
                modifier = Modifier.size(32.dp),
            )
        }
    }
}