package com.github.ivc.expenses.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey


@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE,
            deferred = false,
        ),
    ],
    indices = [
        Index("category_id"),
    ],
)
data class Vendor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "category_id") val categoryId: Long? = null,
)

@Dao
interface VendorDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vendor: Vendor): Long
}
