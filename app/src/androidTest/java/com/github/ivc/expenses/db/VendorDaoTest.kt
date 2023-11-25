package com.github.ivc.expenses.db

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VendorDaoTest {
    private lateinit var db: AppDb

    @Before
    fun setup() {
        db = Room
            .inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AppDb::class.java,
            )
            .addTypeConverter(Converters())
            .build()
    }

    @After
    fun cleanup() {
        db.close()
    }

    @Test
    fun insert() = runTest {
        val value = Vendor(name = "test")
        val got = db.vendors.insert(value)
        assertThat(got).isEqualTo(1)
    }

    @Test
    fun insertWithCategory() = runTest {
        val categoryId = db.insertCategory(name = "test", icon = "test", color = 0)
        val value = Vendor(
            name = "test",
            categoryId = categoryId,
        )
        val got = db.vendors.insert(value)
        assertThat(got).isEqualTo(1)
    }

    @Test
    fun insertWithMissingCategory() = runTest {
        var thrown = false
        val value = Vendor(
            name = "test",
            categoryId = 1,
        )
        try {
            db.vendors.insert(value)
        } catch (exc: SQLiteConstraintException) {
            thrown = true
            assertThat(exc).hasMessageThat().contains("FOREIGN KEY")
        }
        assertThat(thrown).isTrue()
    }

    @Test
    fun queryIdByName() = runTest {
        val want = mapOf(
            "vendor1" to 1L,
            "vendor2" to 2L,
        )
        want.forEach {
            db.insertVendor(it.value, it.key)
        }
        val got = db.vendors.idByName()
        assertThat(got).isEqualTo(want)
    }
}
