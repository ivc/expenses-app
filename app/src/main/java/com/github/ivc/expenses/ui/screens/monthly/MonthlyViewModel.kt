package com.github.ivc.expenses.ui.screens.monthly

import android.icu.util.Currency
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ivc.expenses.db.AppDb
import com.github.ivc.expenses.db.TimeRange
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters.firstDayOfMonth

class MonthlyViewModel(private val db: AppDb = AppDb.instance) : ViewModel() {
    fun months(currency: Currency): StateFlow<List<ZonedDateTime>> =
        db.purchases.timeRange(currency).map(::months).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf(),
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L

        fun months(timeRange: TimeRange): List<ZonedDateTime> {
            val start = timeRange.start.truncatedTo(ChronoUnit.DAYS).with(firstDayOfMonth())
            val end = timeRange.end.truncatedTo(ChronoUnit.DAYS).with(firstDayOfMonth())
            return generateSequence(end) {
                it.minusMonths(1)
            }.takeWhile { start.isBefore(it) || start.isEqual(it) }.toList()
        }
    }
}
