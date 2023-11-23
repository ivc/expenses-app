package com.github.ivc.expenses.db

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
}
