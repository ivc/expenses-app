package com.github.ivc.expenses.ui.compose

import android.icu.util.Currency
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.ivc.expenses.db.Category
import com.github.ivc.expenses.db.Purchase
import com.github.ivc.expenses.db.PurchaseEntry
import com.github.ivc.expenses.db.Vendor
import com.github.ivc.expenses.ui.theme.ExpensesTheme
import java.time.ZonedDateTime
import java.util.Locale

private val categoryButtonColors
    @Composable get() = IconButtonDefaults.iconToggleButtonColors(
        checkedContainerColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelectorDialog(
    onDismiss: () -> Unit,
    onConfirm: (Category?) -> Unit,
    categories: List<Category>,
    entry: PurchaseEntry,
) {
    var selected by remember { mutableStateOf(entry.category) }
    Dialog(
        onDismissRequest = { onDismiss() },
    ) {
        Card {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    entry.category?.name ?: Category.Other.name,
                )
                FlowRow {
                    for (category in categories.plus(null)) {
                        IconToggleButton(
                            checked = category == selected,
                            colors = categoryButtonColors,
                            onCheckedChange = { selected = category },
                        ) {
                            val iconCategory = category ?: Category.Other
                            Icon(
                                painter = iconCategory.painter,
                                contentDescription = iconCategory.name,
                                tint = Color(iconCategory.color),
                            )
                        }
                    }
                }
                TextButton(onClick = { onConfirm(selected) }) {
                    Text("Set Category")
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewCategorySelectorDialog() {
    ExpensesTheme(darkTheme = true) {
        Surface(Modifier.width(412.dp)) {
            CategorySelectorDialog(
                onDismiss = { },
                onConfirm = {},
                categories = Category.defaultCategories(LocalContext.current),
                entry = PurchaseEntry(
                    purchase = Purchase(
                        timestamp = ZonedDateTime.now(),
                        amount = 123.0,
                        currency = Currency.getInstance(Locale.getDefault()),
                        vendorId = 1,
                    ),
                    vendor = Vendor(name = "test"),
                    category = null,
                ),
            )
        }
    }
}