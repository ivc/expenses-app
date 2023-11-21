package com.github.ivc.expenses.db

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.github.ivc.expenses.R

@Entity
data class Category(
    @PrimaryKey val id: Int,
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
    Unknown(R.drawable.ic_cat_unknown),
}

class CategoryIconConverters {
    @TypeConverter
    fun unmarshal(value: String): CategoryIconRef {
        val builtinName = value.removePrefix(builtinPrefix)
        val builtin = builtinCategoryIcons[builtinName] ?: BuiltinCategoryIcon.Unknown
        return CategoryIconRef(value, builtin)
    }

    @TypeConverter
    fun marshal(value: CategoryIconRef) = value.url

    companion object {
        const val builtinPrefix = "builtin:"
        val builtinCategoryIcons = BuiltinCategoryIcon.entries.associateBy { it.name }
    }
}
