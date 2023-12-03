package com.github.ivc.expenses.init

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase.Callback
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.startup.Initializer
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST
import androidx.work.WorkManager
import androidx.work.WorkManagerInitializer
import com.github.ivc.expenses.BuildConfig
import com.github.ivc.expenses.db.AppDb
import com.github.ivc.expenses.work.GenerateSampleData
import com.github.ivc.expenses.work.InitAppDb

class AppDbCallback(private val workManager: WorkManager) : Callback() {
    private var shouldInit = false
    override fun onCreate(db: SupportSQLiteDatabase) {
        shouldInit = true
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        if (!shouldInit) {
            return
        }

        val initAppDbRequest = OneTimeWorkRequestBuilder<InitAppDb>()
            .setExpedited(RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        val generateSampleDataRequest = OneTimeWorkRequestBuilder<GenerateSampleData>()
            .setExpedited(RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        when (BuildConfig.DEBUG) {
            true -> workManager
                .beginWith(initAppDbRequest)
                .then(generateSampleDataRequest)
                .enqueue()

            else -> workManager
                .enqueue(initAppDbRequest)
        }
    }
}

@Suppress("unused")
class AppDbInitializer : Initializer<AppDb> {
    override fun create(context: Context): AppDb {
        val workManager = WorkManager.getInstance(context)
        val appDbCallback = AppDbCallback(workManager)
        val builder = when (BuildConfig.DEBUG) {
            true -> Room.inMemoryDatabaseBuilder(context.applicationContext, AppDb::class.java)
            else -> Room.databaseBuilder(
                context.applicationContext,
                AppDb::class.java,
                "expenses.db"
            )
        }.addCallback(appDbCallback)
        AppDb.initialize(builder)
        return AppDb.instance
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(WorkManagerInitializer::class.java)
    }
}
