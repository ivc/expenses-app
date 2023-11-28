package com.github.ivc.expenses.ui.screens.monthly

import com.github.ivc.expenses.db.TimeRange
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class MonthlyViewModelTest {
    @Test
    fun months() {
        val zone = ZoneId.ofOffset("", ZoneOffset.ofHours(7))
        val timestamp = ZonedDateTime.of(
            2023,
            11,
            11,
            1,
            2,
            3,
            456,
            zone,
        )
        val want = listOf(
            ZonedDateTime.of(2023, 11, 1, 0, 0, 0, 0, zone),
            ZonedDateTime.of(2023, 10, 1, 0, 0, 0, 0, zone),
            ZonedDateTime.of(2023, 9, 1, 0, 0, 0, 0, zone),
        )
        val timeRange = TimeRange(
            timestamp.minusMonths(2),
            timestamp,
        )
        val got = MonthlyViewModel.months(timeRange)
        assertThat(got).isEqualTo(want)
    }
}