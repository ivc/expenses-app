package com.github.ivc.expenses.work


import android.content.Context
import android.icu.util.Currency
import android.util.Log
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.ivc.expenses.db.AppDb
import com.github.ivc.expenses.db.Purchase
import com.github.ivc.expenses.db.Vendor
import kotlinx.coroutines.flow.first
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.random.Random

class GenerateSampleData(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        Log.d(this::class.java.simpleName, "")
        val nVendors = 50
        val nPurchases = 5000
        val rng = Random(1701012852)
        val db = AppDb.instance
        val categories = db.categories.all().first()
        val timestamp = ZonedDateTime.of(2023, 11, 11, 1, 2, 3, 456, ZoneId.systemDefault())
        val words = LoremIpsum(300).values.first().split(Regex("[^a-zA-Z0-9]+")).toList()

        db.withTransaction {
            val vendorIds: List<Long> = (0..nVendors).map {
                val name = generateSequence {
                    words.random(rng)
                }.take(1 + rng.nextInt(4)).joinToString(" ")
                val category = categories[rng.nextInt(categories.size)]
                val vendor = Vendor(
                    name = name,
                    categoryId = category.id,
                )
                return@map db.vendors.insert(vendor)
            }.toList()

            for (i in 0..nPurchases) {
                val purchase = Purchase(
                    timestamp = timestamp.minusMinutes(rng.nextLong(60 * 24 * 180)),
                    amount = rng.nextDouble(10000.0),
                    vendorId = vendorIds[rng.nextInt(vendorIds.size)],
                    currency = Currency.getInstance("USD"),
                )
                db.purchases.insert(purchase)
            }
        }
        return Result.success()
    }
}
