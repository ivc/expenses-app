package com.github.ivc.expenses.ui

import android.icu.math.BigDecimal
import android.icu.text.NumberFormat
import android.icu.util.CurrencyAmount
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.em
import com.github.ivc.expenses.ui.theme.ExpensesTheme
import java.util.Currency

private object CurrencyTextFormat {
    val Integer: NumberFormat = NumberFormat.getIntegerInstance().also {
        it.isGroupingUsed = true
        it.minimumIntegerDigits = 1
        it.roundingMode = BigDecimal.ROUND_FLOOR
    }

    val Fraction: NumberFormat = NumberFormat.getNumberInstance().also {
        it.maximumIntegerDigits = 0
        it.maximumFractionDigits = 2
        it.minimumFractionDigits = 2
    }
}

@Composable
fun CurrencyText(
    amount: CurrencyAmount,
    modifier: Modifier = Modifier,
    fractionStyle: SpanStyle? = null,
    currencyStyle: SpanStyle? = null,
    multiline: Boolean = false,
    style: TextStyle = LocalTextStyle.current,
) {
    val text = buildAnnotatedString {
        val integer = CurrencyTextFormat.Integer.format(amount.number)
        val fraction = CurrencyTextFormat.Fraction.format(amount.number)
        val currency = amount.currency.currencyCode

        append(integer)
        withStyle(fractionStyle) { append(fraction) }
        append(if (multiline) "\n" else " ")
        withStyle(currencyStyle) { append(currency) }
    }

    val maxLines = if (multiline) 2 else 1
    Text(text, modifier, maxLines = maxLines, style = style)
}

inline fun <R : Any> AnnotatedString.Builder.withStyle(
    style: SpanStyle?,
    block: AnnotatedString.Builder.() -> R,
): R {
    return style?.let { withStyle(it, block) } ?: block()
}

private class BooleanPreviewParameter :
    CollectionPreviewParameterProvider<Boolean>(listOf(true, false))

@Preview
@Composable
fun CurrencyTextPreview(@PreviewParameter(BooleanPreviewParameter::class) multiline: Boolean) {
    val currency = Currency.getInstance("USD")
    val amount = CurrencyAmount(12345.6789, currency)

    ExpensesTheme {
        Surface {
            CurrencyText(
                amount,
                multiline = multiline,
                style = MaterialTheme.typography.titleMedium,
                fractionStyle = SpanStyle(fontSize = 0.5.em),
                currencyStyle = SpanStyle(fontStyle = FontStyle.Italic),
            )
        }
    }
}
