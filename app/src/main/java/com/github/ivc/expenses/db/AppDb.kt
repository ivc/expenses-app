package com.github.ivc.expenses.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    version = 1,
    exportSchema = false,
    entities = [
        Category::class,
        Vendor::class,
        Purchase::class,
    ]
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract val categories: CategoryDao
    abstract val vendors: VendorDao
    abstract val purchases: PurchaseDao

    companion object {
        private lateinit var _instance: AppDb
        val instance get() = _instance

        fun initialize(builder: Builder<AppDb>) {
            if (::_instance.isInitialized) {
                throw IllegalStateException("AppDb already initialized")
            }
            _instance = builder.addTypeConverter(Converters()).build()
        }
    }
}
