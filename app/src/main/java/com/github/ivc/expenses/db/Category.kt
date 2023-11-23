package com.github.ivc.expenses.db

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import com.github.ivc.expenses.R

@Entity
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: CategoryIconRef,
    @ColorInt val color: Int,
)

data class CategoryIconRef(
    val url: String,
    val builtin: BuiltinCategoryIcon,
)

enum class BuiltinCategoryIcon(@DrawableRes val id: Int) {
    Default(R.drawable.ic_cat_default),
    Unknown(R.drawable.ic_cat_unknown);

    companion object {
        const val prefix = "builtin:"
        private val _byName = entries.associateBy { it.name }
        fun byName(name: String): BuiltinCategoryIcon? = _byName[name]
    }
}

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category): Long
}
