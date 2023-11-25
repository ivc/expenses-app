package com.github.ivc.expenses.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class InitAppDb(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val tag = this::class.java.simpleName
        Log.d(tag, "doWork")
        return Result.success()
    }
}
