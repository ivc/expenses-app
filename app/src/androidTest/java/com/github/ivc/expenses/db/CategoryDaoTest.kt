package com.github.ivc.expenses.db

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.ivc.expenses.db.BuiltinCategoryIcon.Default
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {
    private lateinit var db: AppDb
    private lateinit var dao: CategoryDao

    @Before
    fun setup() {
        db = Room
            .inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext<Context>(),
                AppDb::class.java,
            )
            .addTypeConverter(Converters())
            .build()
        dao = db.categories()
    }

    @After
    fun cleanup() {
        db.close()
    }

    @Test
    fun insert() = runTest {
        val value = Category(
            name = "test",
            icon = CategoryIconRef(url = "builtin:default", builtin = Default),
            color = Color.Green.toArgb(),
        )
        val got = dao.insert(value)
        assertThat(got).isEqualTo(1)
    }
}
