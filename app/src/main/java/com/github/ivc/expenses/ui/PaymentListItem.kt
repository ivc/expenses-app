package com.github.ivc.expenses.ui

import android.icu.util.CurrencyAmount
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.em
import com.github.ivc.expenses.R
import com.github.ivc.expenses.ui.theme.ExpensesTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle.MEDIUM
import java.time.format.FormatStyle.SHORT
import java.util.Currency

private val timestampFormatter = DateTimeFormatter.ofLocalizedDateTime(MEDIUM, SHORT)

private object Styles {
    val amount = TextStyle(
        fontWeight = FontWeight.Black,
        textAlign = TextAlign.End,
        lineHeight = 0.5.em,
    )
    val fraction = SpanStyle(
        fontSize = 0.75.em,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic,
        textGeometricTransform = TextGeometricTransform(scaleX = 0.5f),
    )
    val currency = SpanStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 0.5.em,
        fontWeight = FontWeight.Normal,
        textGeometricTransform = TextGeometricTransform(scaleX = 1.5f),
    )
}

@Composable
fun PaymentListItem(
    description: String,
    amount: CurrencyAmount,
    modifier: Modifier = Modifier,
    timestamp: LocalDateTime? = null,
    icon: Painter? = null,
    color: Color? = null,
) {
    ListItem(
        modifier = modifier.fillMaxWidth(),
        headlineContent = {
            Text(
                description,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            CurrencyText(
                amount,
                multiline = true,
                style = MaterialTheme.typography.titleLarge.merge(Styles.amount),
                fractionStyle = Styles.fraction,
                currencyStyle = Styles.currency,
            )
        },
        leadingContent = icon?.let { painter ->
            {
                val iconSize = dimensionResource(R.dimen.payment_list_icon_size)
                val iconColor = color ?: LocalContentColor.current
                Icon(painter, null, Modifier.size(iconSize), iconColor)
            }
        },
        supportingContent = timestamp?.let {
            {
                Text(
                    timestampFormatter.format(it),
                    fontStyle = FontStyle.Italic,
                )
            }
        },
    )
}

@Preview
@Composable
fun PaymentListItemPreview() {
    ExpensesTheme {
        Surface {
            PaymentListItem(
                LoremIpsum(3).values.first(),
                CurrencyAmount(1234.567, Currency.getInstance("USD")),
                timestamp = LocalDateTime.now(),
                icon = rememberVectorPainter(Icons.Default.ShoppingCart),
                color = Color.Blue,
            )
        }
    }
}

@Preview
@Composable
fun PaymentListItemWithDefaultColorPreview() {
    ExpensesTheme {
        Surface {
            PaymentListItem(
                LoremIpsum().values.first(),
                CurrencyAmount(1234.567, Currency.getInstance("USD")),
                timestamp = LocalDateTime.now(),
                icon = rememberVectorPainter(Icons.Default.ShoppingCart),
            )
        }
    }
}

@Preview
@Composable
fun PaymentListItemWithoutDetailsPreview() {
    ExpensesTheme {
        Surface {
            PaymentListItem(
                LoremIpsum().values.first(),
                CurrencyAmount(1234.567, Currency.getInstance("USD"))
            )
        }
    }
}
