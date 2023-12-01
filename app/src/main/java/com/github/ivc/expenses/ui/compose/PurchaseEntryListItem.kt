package com.github.ivc.expenses.ui.compose

import android.icu.util.Currency
import android.icu.util.ULocale
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ivc.expenses.db.Category
import com.github.ivc.expenses.db.Purchase
import com.github.ivc.expenses.db.PurchaseEntry
import com.github.ivc.expenses.db.Vendor
import com.github.ivc.expenses.util.toCurrencyString
import com.github.ivc.expenses.util.toRfc1123String
import java.time.ZonedDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PurchaseEntryListItem(entry: PurchaseEntry, onLongClick: (PurchaseEntry) -> Unit = {}) {
    ListItem(
        headlineContent = {
            Text(text = entry.vendor.name, minLines = 1, maxLines = 1)
        },
        trailingContent = {
            Text(text = entry.amountText, minLines = 1, maxLines = 1)
        },
        overlineContent = {
            Text(text = entry.timestampText, minLines = 1, maxLines = 1)
        },
        modifier = Modifier.combinedClickable(
            onClick = {},
            onLongClick = { onLongClick(entry) },
        ),
    )
}

@Preview
@Composable
fun PreviewPurchaseEntryListItem() {
    Surface(Modifier.width(412.dp)) {
        PurchaseEntryListItem(
            entry = PurchaseEntry(
                purchase = Purchase(
                    timestamp = ZonedDateTime.now(),
                    amount = 123.0,
                    currency = Currency.getInstance(ULocale.getDefault()),
                    vendorId = 0,
                ),
                vendor = Vendor(name = "Test Vendor"),
                category = Category.Other,
            ),
        )
    }
}

val PurchaseEntry.timestampText
    @Composable get() = purchase.timestamp.toRfc1123String()

val PurchaseEntry.amountText
    @Composable get() = purchase.amount.toCurrencyString()