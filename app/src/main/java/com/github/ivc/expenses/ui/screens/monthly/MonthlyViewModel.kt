package com.github.ivc.expenses.ui.screens.monthly

import android.icu.util.Currency
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ivc.expenses.db.AppDb
import com.github.ivc.expenses.db.Category
import com.github.ivc.expenses.db.PurchaseEntry
import com.github.ivc.expenses.db.Vendor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import java.time.temporal.ChronoUnit
import java.util.Locale

class MonthlyViewModel(private val db: AppDb = AppDb.instance) : ViewModel() {
    val expandedCategories = mutableStateMapOf<Long, Boolean>()
    val selectedEntry = mutableStateOf<PurchaseEntry?>(null)

    private val currencyFlow = db.purchases.currencies().mapNotNull { it.firstOrNull() }
    val currency = currencyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = Currency.getInstance(Locale.getDefault()),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val months = currencyFlow.flatMapLatest {
        db.purchases.dateTimeRange(it)
    }.mapNotNull { it }.map { dateTimeRange ->
        val start = dateTimeRange.start.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1)
        val end = dateTimeRange.end.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1)
        return@map generateSequence(end) {
            it.minusMonths(1)
        }.takeWhile {
            !it.isBefore(start)
        }.toList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = listOf(),
    )

    suspend fun setVendorCategory(vendor: Vendor, category: Category) {
        val updated = vendor.copy(categoryId = category.id)
        db.vendors.update(updated)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
