package com.github.ivc.expenses.work

import android.content.Context
import android.icu.text.NumberFormat
import android.provider.Telephony
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.ivc.expenses.db.AppDb
import com.github.ivc.expenses.db.Purchase
import com.github.ivc.expenses.db.Vendor
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

class ImportSms(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

    private enum class SmsQueryColumn(val columnName: String) {
        DATE_SENT(Telephony.Sms.DATE_SENT),
        ADDRESS(Telephony.Sms.ADDRESS),
        BODY(Telephony.Sms.BODY);

        companion object {
            val projection: Array<String> = entries.map { it.columnName }.toTypedArray()
        }
    }

    override suspend fun doWork(): Result {
        val db = AppDb.instance
        val rules = db.smsRules.bySender()
        val vendors = db.vendors.idByName().toMutableMap()
        db.withTransaction {
            forEachSms(since = db.purchases.maxTimestamp()) { dateTime, address, body ->
                rules[address]?.forEach { rule ->
                    rule.regex.matchEntire(body)?.let { match ->
                        val amount = numberFormat.parse(match.groups["AMOUNT"]!!.value).toDouble()
                        val vendorName = match.groups["VENDOR"]!!.value
                        val vendorId = vendors[vendorName] ?: (
                                db.vendors.insert(Vendor(name = vendorName))
                                    .also { vendors[vendorName] = it })
                        db.purchases.insert(
                            Purchase(
                                timestamp = dateTime,
                                amount = amount,
                                currency = rule.currency,
                                vendorId = vendorId,
                            )
                        )
                    }
                }
            }
        }
        return Result.success()
    }

    private suspend fun forEachSms(
        since: ZonedDateTime?,
        action: suspend (ZonedDateTime, String, String) -> Unit
    ) {
        val systemZoneId = ZoneId.systemDefault()
        val selection = since?.let { "${Telephony.Sms.DATE_SENT} > cast(? as INTEGER)" }
        val selectionArg = since?.let {
            arrayOf(it.toInstant().toEpochMilli().toString())
        }

        applicationContext.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            SmsQueryColumn.projection,
            selection,
            selectionArg,
            Telephony.Sms.DATE_SENT,
        ).use {
            it?.let {
                while (it.moveToNext()) {
                    val dateSent = Instant.ofEpochMilli(
                        it.getLong(SmsQueryColumn.DATE_SENT.ordinal),
                    ).atZone(systemZoneId)
                    val address = it.getString(SmsQueryColumn.ADDRESS.ordinal)
                    val body = it.getString(SmsQueryColumn.BODY.ordinal)
                    action(dateSent, address, body)
                }
            }
        }
    }
}
