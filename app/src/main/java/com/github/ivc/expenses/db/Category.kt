package com.github.ivc.expenses.db

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.github.ivc.expenses.R
import kotlinx.coroutines.flow.Flow

@Entity
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: CategoryIconRef,
    @ColorInt val color: Int,
) {
    companion object {
        val Other = Category(
            id = 0,
            name = "Other", // TODO: move to resource
            icon = BuiltinCategoryIcon.Default.ref,
            color = Color.Gray.toArgb(),
        )

        fun defaultCategories(context: Context): List<Category> {
            val r = context.applicationContext.resources
            val theme = context.applicationContext.theme
            return listOf(
                Category(
                    name = r.getString(R.string.cat_apartment),
                    icon = BuiltinCategoryIcon.Apartment.ref,
                    color = r.getColor(R.color.DarkTurquoise, theme),
                ),
                Category(
                    name = r.getString(R.string.cat_health),
                    icon = BuiltinCategoryIcon.Health.ref,
                    color = r.getColor(R.color.Tomato, theme),
                ),
                Category(
                    name = r.getString(R.string.cat_business),
                    icon = BuiltinCategoryIcon.Work.ref,
                    color = r.getColor(R.color.BurlyWood, theme),
                ),
                Category(
                    name = r.getString(R.string.cat_transport),
                    icon = BuiltinCategoryIcon.Car.ref,
                    color = r.getColor(R.color.Gold, theme),
                ),
                Category(
                    name = r.getString(R.string.cat_shopping),
                    icon = BuiltinCategoryIcon.Shopping.ref,
                    color = r.getColor(R.color.SkyBlue, theme),
                ),
                Category(
                    name = r.getString(R.string.cat_groceries),
                    icon = BuiltinCategoryIcon.ShoppingCart.ref,
                    color = r.getColor(R.color.YellowGreen, theme),
                ),
                Category(
                    name = r.getString(R.string.cat_bar),
                    icon = BuiltinCategoryIcon.Bar.ref,
                    color = r.getColor(R.color.HotPink, theme),
                ),
                Category(
                    name = r.getString(R.string.cat_fast_food),
                    icon = BuiltinCategoryIcon.FastFood.ref,
                    color = r.getColor(R.color.DarkOrange, theme),
                ),
                Category(
                    name = r.getString(R.string.cat_subscriptions),
                    icon = BuiltinCategoryIcon.Subscriptions.ref,
                    color = r.getColor(R.color.FireBrick, theme),
                ),
                Category(
                    name = r.getString(R.string.cat_games),
                    icon = BuiltinCategoryIcon.Games.ref,
                    color = r.getColor(R.color.MediumPurple, theme),
                ),
            )
        }
    }
}

data class CategoryIconRef(
    val url: String,
    val builtin: BuiltinCategoryIcon,
)

enum class BuiltinCategoryIcon(@DrawableRes val id: Int) {
    Apartment(R.drawable.ic_cat_apartment),
    Bar(R.drawable.ic_cat_bar),
    Car(R.drawable.ic_cat_car),
    FastFood(R.drawable.ic_cat_fast_food),
    Games(R.drawable.ic_cat_games),
    Health(R.drawable.ic_cat_health),
    Shopping(R.drawable.ic_cat_shopping),
    ShoppingCart(R.drawable.ic_cat_shopping_cart),
    Subscriptions(R.drawable.ic_cat_subscriptions),
    Work(R.drawable.ic_cat_work),
    Default(R.drawable.ic_cat_default),
    Unknown(R.drawable.ic_cat_unknown);

    val ref get() = CategoryIconRef(prefix + name, this)

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

    @Query("SELECT * FROM category")
    fun all(): Flow<List<Category>>

    @Query("SELECT * FROM category")
    fun indexById(): Flow<Map<@MapColumn("id") Long, Category>>
}
