package com.github.ivc.expenses.db

import android.icu.util.Currency
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.ZonedDateTime

@RunWith(AndroidJUnit4::class)
class PurchaseDaoTestDaoTest {
    private lateinit var db: AppDb
    private lateinit var dao: PurchaseDao

    @Before
    fun setup() {
        db = Room
            .inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AppDb::class.java,
            )
            .addTypeConverter(Converters())
            .build()
        dao = db.purchases()
    }

    @After
    fun cleanup() {
        db.close()
    }

    @Test
    fun insert() = runTest {
        val value = Purchase(
            timestamp = ZonedDateTime.now(),
            amount = 123.0,
            currency = Currency.getInstance("USD"),
            vendorId = 1,
        )
        val got = dao.insert(value)
        assertThat(got).isEqualTo(1)
    }
}
