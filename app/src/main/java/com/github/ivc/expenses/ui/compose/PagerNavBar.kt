package com.github.ivc.expenses.ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerNavBar(
    state: PagerState,
    page: Int,
    modifier: Modifier = Modifier,
    reverseLayout: Boolean = false,
    content: @Composable () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollToPage = { target: Int -> coroutineScope.launch { state.scrollToPage(target) } }

    val delta = if (reverseLayout) -1 else 1
    val leftPage = page - delta
    val rightPage = page + delta

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(
            onClick = { scrollToPage(leftPage) },
            enabled = state.validPage(leftPage),
        ) {
            Icon(Icons.Default.KeyboardArrowLeft, "Previous")
        }

        content()

        IconButton(
            onClick = { scrollToPage(rightPage) },
            enabled = state.validPage(rightPage),
        ) {
            Icon(Icons.Default.KeyboardArrowRight, "Next")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun PagerState.validPage(page: Int): Boolean {
    return page in 0..<pageCount
}

@Preview
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun PreviewPagerNavBar() {
    Surface(Modifier.width(412.dp)) {
        PagerNavBar(rememberPagerState { 2 }, 1) {
            Text("title")
        }
    }
}
