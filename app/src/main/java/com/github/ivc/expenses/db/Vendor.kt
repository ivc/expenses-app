package com.github.ivc.expenses.db

import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Stable
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["category_id"],
            childColumns = ["vendor_category_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE,
            deferred = false,
        ),
    ],
    indices = [
        Index("vendor_category_id"),
    ],
)
data class Vendor(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("vendor_id")
    val id: Long = 0,

    @ColumnInfo("vendor_name")
    val name: String,

    @ColumnInfo(name = "vendor_category_id")
    val categoryId: Long? = null,
)

@Dao
interface VendorDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vendor: Vendor): Long

    @Query("SELECT vendor_id, vendor_name FROM vendor")
    suspend fun idByName(): Map<
            @MapColumn("vendor_name") String,
            @MapColumn("vendor_id") Long,
            >

    @Query("SELECT * FROM vendor")
    fun indexById(): Flow<Map<@MapColumn("vendor_id") Long, Vendor>>
}
