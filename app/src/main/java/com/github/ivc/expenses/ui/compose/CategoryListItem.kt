package com.github.ivc.expenses.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ivc.expenses.db.Category
import com.github.ivc.expenses.util.toCurrencyString

@Composable
fun CategoryListItem(
    category: Category,
    total: Double,
    onClick: () -> Unit = {},
) {
    ListItem(
        headlineContent = {
            Text(text = category.name, minLines = 1, maxLines = 1)
        },
        leadingContent = {
            Icon(
                painter = category.painter,
                contentDescription = category.name,
                tint = Color(category.color),
            )
        },
        trailingContent = {
            Text(total.toCurrencyString())
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Preview
@Composable
fun PreviewCategoryListItem() {
    Surface(Modifier.width(412.dp)) {
        CategoryListItem(
            category = Category.Other,
            total = 123.0,
        )
    }
}
