package com.github.ivc.expenses.init

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase.Callback
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.startup.Initializer
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkManagerInitializer
import com.github.ivc.expenses.BuildConfig
import com.github.ivc.expenses.db.AppDb
import com.github.ivc.expenses.work.GenerateSampleData
import com.github.ivc.expenses.work.ImportAppDb
import com.github.ivc.expenses.work.InitAppDb
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AppDbCallback(private val workManager: WorkManager) : Callback() {
    private var shouldInit = false
    override fun onCreate(db: SupportSQLiteDatabase) {
        shouldInit = true
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        if (!shouldInit) {
            workManager.enqueue(workRequest<ImportAppDb>())
            return
        }

        var work = workManager.beginWith(workRequest<InitAppDb>())
        if (BuildConfig.DEBUG) {
            work = work.then(workRequest<GenerateSampleData>())
        }
        work = work.then(workRequest<ImportAppDb>())
        work.enqueue()
    }

}

private inline fun <reified W : ListenableWorker> workRequest(): OneTimeWorkRequest {
    return OneTimeWorkRequestBuilder<W>()
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .build()
}

@Suppress("unused")
class AppDbInitializer : Initializer<AppDb> {
    override fun create(context: Context): AppDb {
        val workManager = WorkManager.getInstance(context)
        val appDbCallback = AppDbCallback(workManager)
        val builder = Room
            .inMemoryDatabaseBuilder(context.applicationContext, AppDb::class.java)
            .addCallback(appDbCallback)
        AppDb.initialize(builder)
        GlobalScope.launch {
            // FIXME: remove once db.instance has actual usage
            val vendorsCount = AppDb.instance.vendors.idByName().size
            Log.d(this::class.java.simpleName, "vendors count: $vendorsCount")
        }
        return AppDb.instance
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(WorkManagerInitializer::class.java)
    }
}
