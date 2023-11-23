package com.github.ivc.expenses.db

import android.icu.util.Currency
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class CurrencyConverterTest {
    private val conv = Converters(
        currencyResolver = { mockedCurrency },
        zoneId = mockk(),
        instantResolver = mockk(),
    )

    private val isoCode = "USD"
    private val mockedCurrency = mockk<Currency> {
        every { currencyCode } returns isoCode
    }

    @Test
    fun currencyToString() {
        val got = conv.currencyToString(mockedCurrency)
        assertThat(got).isEqualTo(isoCode)
    }

    @Test
    fun stringToCurrency() {
        val got = conv.stringToCurrency(isoCode)
        assertThat(got).isEqualTo(mockedCurrency)
    }
}