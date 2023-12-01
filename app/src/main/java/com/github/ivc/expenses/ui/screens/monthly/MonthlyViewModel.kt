package com.github.ivc.expenses.ui.screens.monthly

import android.icu.util.Currency
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ivc.expenses.db.AppDb
import com.github.ivc.expenses.db.MonthlyReport
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MonthlyViewModel(db: AppDb = AppDb.instance) : ViewModel() {
    val expandedCategories = mutableStateMapOf<Long, Boolean>()
    val monthlyReports: StateFlow<Map<Currency, List<MonthlyReport>>> =
        db.purchases.monthlyReports().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = mapOf(),
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
