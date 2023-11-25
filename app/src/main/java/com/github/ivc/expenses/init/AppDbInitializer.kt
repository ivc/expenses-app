package com.github.ivc.expenses.init

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase.Callback
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.startup.Initializer
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkManagerInitializer
import com.github.ivc.expenses.db.AppDb
import com.github.ivc.expenses.work.ImportAppDb
import com.github.ivc.expenses.work.InitAppDb
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AppDbCallback(private val workManager: WorkManager) : Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        workManager.enqueue(
            OneTimeWorkRequestBuilder<InitAppDb>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        )
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        workManager.enqueue(
            OneTimeWorkRequestBuilder<ImportAppDb>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        )
    }
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
