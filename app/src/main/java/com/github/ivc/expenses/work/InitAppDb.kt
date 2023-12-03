package com.github.ivc.expenses.work

import android.content.Context
import android.icu.util.Currency
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.ivc.expenses.db.AppDb
import com.github.ivc.expenses.db.Category
import com.github.ivc.expenses.db.SmsRule
import org.apache.commons.csv.CSVFormat

class InitAppDb(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = AppDb.instance
        db.withTransaction {
            Category.defaultCategories(context).forEach {
                db.categories.insert(it)
            }
            loadSmsRules().forEach {
                db.smsRules.insert(it)
            }
        }
        return Result.success()
    }

    private enum class SmsRuleCsvColumn {
        SENDER,
        REGEX,
        CURRENCY,
    }

    private fun loadSmsRules(): List<SmsRule> {
        val assetDir = "sms_rules"
        val parser = CSVFormat.Builder
            .create(CSVFormat.DEFAULT)
            .setIgnoreSurroundingSpaces(true)
            .setCommentMarker('#')
            .setHeader(SmsRuleCsvColumn::class.java)
            .build()
        return when (val assets = context.assets.list(assetDir)) {
            null -> listOf()
            else -> assets.flatMap { asset ->
                context.assets.open("$assetDir/$asset").bufferedReader().use { reader ->
                    parser.parse(reader).map { record ->
                        SmsRule(
                            sender = record.get(SmsRuleCsvColumn.SENDER),
                            regex = Regex(record.get(SmsRuleCsvColumn.REGEX)),
                            currency = Currency.getInstance(record.get(SmsRuleCsvColumn.CURRENCY)),
                        )
                    }
                }
            }
        }
    }
}