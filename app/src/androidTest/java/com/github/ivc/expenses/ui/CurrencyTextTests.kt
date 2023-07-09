package com.github.ivc.expenses.ui

import android.icu.util.CurrencyAmount
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import java.util.Currency

class CurrencyTextTests {
    private val currency = Currency.getInstance("USD")
    private val amount = CurrencyAmount(12345.678, currency)
    private val amountText = "12,345.68"

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun singleLine() {
        composeTestRule.setContent {
            CurrencyText(amount = amount)
        }

        composeTestRule.onNode(
            hasText("$amountText ${currency.currencyCode}")
        ).assertExists()
    }

    @Test
    fun multiLine() {
        composeTestRule.setContent {
            CurrencyText(amount = amount, multiline = true)
        }

        composeTestRule.onNode(
            hasText("$amountText\n${currency.currencyCode}")
        ).assertExists()
    }
}
