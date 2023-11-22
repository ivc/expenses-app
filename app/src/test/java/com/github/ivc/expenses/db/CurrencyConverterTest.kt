package com.github.ivc.expenses.db

import android.icu.util.Currency
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Test

class CurrencyConverterTest {
    private val conv = CurrencyConverter()
    private val isoCode = "USD"
    private val mockedCurrency = mockk<Currency> {
        every { currencyCode } returns isoCode
    }

    @After
    fun cleanup() {
        unmockkAll()
    }

    @Test
    fun marshal() {
        val got = conv.marshal(mockedCurrency)
        assertThat(got).isEqualTo(isoCode)
    }

    @Test
    fun unmarshal() {
        mockkStatic(Currency::class)
        every { Currency.getInstance(isoCode) } returns mockedCurrency

        val got = conv.unmarshal(isoCode)
        assertThat(got).isEqualTo(mockedCurrency)
    }
}