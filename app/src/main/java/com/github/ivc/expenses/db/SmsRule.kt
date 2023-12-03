package com.github.ivc.expenses.db

import android.icu.util.Currency
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = SmsRule.TABLE)
data class SmsRule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val regex: Regex,
    val currency: Currency,
) {
    companion object {
        const val TABLE = "sms_rule"
    }
}

@Dao
interface SmsRuleDao {
    @Insert
    suspend fun insert(smsRule: SmsRule): Long

    @Query("SELECT * FROM ${SmsRule.TABLE}")
    suspend fun bySender(): Map<@MapColumn("sender") String, List<SmsRule>>
}
