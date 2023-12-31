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

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDb::class.java,
        ).addTypeConverter(Converters()).build()
    }

    @After
    fun cleanup() {
        db.close()
    }

    @Test
    fun insert() = runTest {
        val vendorId = db.insertVendor(name = "test")
        val value = Purchase(
            timestamp = ZonedDateTime.now(),
            amount = 123.0,
            currency = Currency.getInstance("USD"),
            vendorId = vendorId,
        )
        val got = db.purchases.insert(value)
        assertThat(got).isEqualTo(1)
    }

    @Test
    fun insertWithCategory() = runTest {
        val vendorId = db.insertVendor(name = "test")
        val categoryId = db.insertCategory(name = "test", icon = "test", color = 0)
        val value = Purchase(
            timestamp = ZonedDateTime.now(),
            amount = 123.0,
            currency = Currency.getInstance("USD"),
            vendorId = vendorId,
            categoryId = categoryId,
        )
        val got = db.purchases.insert(value)
        assertThat(got).isEqualTo(1)
    }

    @Test
    fun insertWithMissingVendor() = runTest {
        val value = Purchase(
            timestamp = ZonedDateTime.now(),
            amount = 123.0,
            currency = Currency.getInstance("USD"),
            vendorId = 1,
        )
        expectForeignKeyViolation {
            db.purchases.insert(value)
        }
    }

    @Test
    fun insertWithMissingCategory() = runTest {
        val vendorId = db.insertVendor(name = "test")
        val value = Purchase(
            timestamp = ZonedDateTime.now(),
            amount = 123.0,
            currency = Currency.getInstance("USD"),
            vendorId = vendorId,
            categoryId = 1,
        )
        expectForeignKeyViolation {
            db.purchases.insert(value)
        }
    }
}
