package com.github.ivc.expenses.ui.model

import androidx.compose.ui.text.AnnotatedString
import com.github.ivc.expenses.db.Category
import com.github.ivc.expenses.db.PurchaseEntry
import com.github.ivc.expenses.db.TimeRange

data class Report(
    val title: AnnotatedString,
    val timeRange: TimeRange,
    val categories: List<CategorySummary>,
)

data class CategorySummary(
    val category: Category,
    val title: AnnotatedString,
    val summary: AnnotatedString,
    val total: Double,
    val entries: List<AnnotatedPurchaseEntry>
)

data class AnnotatedPurchaseEntry(
    val entry: PurchaseEntry,
    val title: AnnotatedString,
    val value: AnnotatedString,
    val timestamp: AnnotatedString,
)
