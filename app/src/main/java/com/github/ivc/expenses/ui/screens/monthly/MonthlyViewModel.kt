package com.github.ivc.expenses.ui.screens.monthly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ivc.expenses.db.AppDb
import com.github.ivc.expenses.db.TimeRange
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Period
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters.firstDayOfMonth
import java.time.temporal.TemporalAdjusters.firstDayOfNextMonth

class MonthlyViewModel(db: AppDb = AppDb.instance) : ViewModel() {
    val monthlyRanges: StateFlow<List<TimeRange>> = db.purchases.timeRange().map {
        val start = it.start.truncatedTo(ChronoUnit.DAYS).with(firstDayOfMonth())
        val end = it.end.truncatedTo(ChronoUnit.DAYS).with(firstDayOfMonth())
        val period = Period.between(
            start.toLocalDate(),
            end.toLocalDate(),
        )
        return@map (0..period.months).map {
            TimeRange(
                start.plusMonths(it.toLong()),
                start.plusMonths((it + 1).toLong()),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = listOf(
            TimeRange(
                ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).with(firstDayOfMonth()),
                ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).with(firstDayOfNextMonth()),
            )
        ),
    )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
