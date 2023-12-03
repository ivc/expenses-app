package com.github.ivc.expenses.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ivc.expenses.db.Category
import com.github.ivc.expenses.db.CategorySummary
import com.github.ivc.expenses.util.toCurrencyString

@Composable
fun CategoryListItem(
    summary: CategorySummary,
    onClick: () -> Unit = {},
) {
    ListItem(
        headlineContent = {
            Text(text = summary.category.name, minLines = 1, maxLines = 1)
        },
        leadingContent = {
            Icon(
                painter = summary.category.painter,
                contentDescription = summary.category.name,
                tint = Color(summary.category.color),
            )
        },
        trailingContent = {
            Text(text = summary.totalText, minLines = 1, maxLines = 1)
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Preview
@Composable
fun PreviewCategoryListItem() {
    Surface(Modifier.width(412.dp)) {
        CategoryListItem(
            CategorySummary(
                category = Category.Preview,
                purchases = listOf(),
            )
        )
    }
}

val CategorySummary.totalText
    @Composable get() = remember {
        total.toCurrencyString()
    }