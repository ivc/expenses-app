package com.github.ivc.expenses.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

fun AppDb.insert(table: String, block: ContentValues.() -> Unit): Long {
    val values = ContentValues()
    values.apply(block)
    return openHelper.writableDatabase.insert(
        table = table,
        conflictAlgorithm = SQLiteDatabase.CONFLICT_ABORT,
        values = values,
    )
}

fun AppDb.insertVendor(id: Long? = null, name: String, categoryId: Long? = null): Long {
    return insert("vendor") {
        id?.let { put("id", it) }
        put("name", name)
        categoryId?.let { put("category_id", it) }
    }
}

fun AppDb.insertCategory(id: Long? = null, name: String, icon: String, color: Int): Long {
    return insert("category") {
        id?.let { put("id", it) }
        put("name", name)
        put("icon", icon)
        put("color", color)
    }
}
