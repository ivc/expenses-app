package com.github.ivc.expenses.ui.screens.monthly

import android.icu.util.Currency
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ivc.expenses.db.AppDb
import com.github.ivc.expenses.db.Category
import com.github.ivc.expenses.db.MonthlyReport
import com.github.ivc.expenses.db.PurchaseEntry
import com.github.ivc.expenses.db.Vendor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MonthlyViewModel(private val db: AppDb = AppDb.instance) : ViewModel() {
    val expandedCategories = mutableStateMapOf<Long, Boolean>()
    val selectedEntry = mutableStateOf<PurchaseEntry?>(null)
    val monthlyReports: StateFlow<Map<Currency, List<MonthlyReport>>> =
        db.purchases.monthlyReports().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = mapOf(),
        )
    val categories: StateFlow<List<Category>> = db.categories.all().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = listOf(),
    )

    suspend fun setVendorCategory(vendor: Vendor, category: Category?) {
        val updated = vendor.copy(categoryId = category?.id)
        Log.d(this::class.java.simpleName, "setVendorCategory($vendor, $category) via update($updated)")
        db.vendors.update(updated)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
