package com.github.ivc.expenses.ui.screens.monthly

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthlyScreen(model: MonthlyViewModel = viewModel()) {
    val periods by model.monthlyRanges.collectAsState()

    Column(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = rememberPagerState { periods.size },
            modifier = Modifier.fillMaxSize(),
            reverseLayout = true,
        ) {
            Text("${periods[periods.size-1-it]}")
        }
    }
}
