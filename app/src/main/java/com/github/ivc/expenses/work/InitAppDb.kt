package com.github.ivc.expenses.work

import android.content.Context
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.ivc.expenses.db.AppDb
import com.github.ivc.expenses.db.Category

class InitAppDb(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = AppDb.instance
        val categories = db.categories
        db.withTransaction {
            Category.defaultCategories(context).forEach {
                categories.insert(it)
            }
        }
        return Result.success()
    }
}
