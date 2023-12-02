package com.github.ivc.expenses.db

import android.icu.util.Currency
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.MapColumn
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "sms_rule")
data class SmsRule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val regex: Regex,
    val currency: Currency,
)

@Dao
interface SmsRuleDao {
    @Query("SELECT * FROM sms_rule")
    suspend fun bySender(): Map<@MapColumn("sender") String, List<SmsRule>>
}
