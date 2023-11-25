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
    private lateinit var dao: VendorDao

    @Before
    fun setup() {
        db = Room
            .inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AppDb::class.java,
            )
            .addTypeConverter(Converters())
            .build()
        dao = db.vendors()
    }

    @After
    fun cleanup() {
        db.close()
    }

    @Test
    fun insert() = runTest {
        val value = Vendor(name = "test")
        val got = dao.insert(value)
        assertThat(got).isEqualTo(1)
    }

    @Test
    fun insertWithCategory() = runTest {
        val categoryId = db.insertCategory(name = "test", icon = "test", color = 0)
        val value = Vendor(
            name = "test",
            categoryId = categoryId,
        )
        val got = dao.insert(value)
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
            dao.insert(value)
        } catch (exc: SQLiteConstraintException) {
            thrown = true
            assertThat(exc).hasMessageThat().contains("FOREIGN KEY")
        }
        assertThat(thrown).isTrue()
    }
}
