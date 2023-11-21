package com.github.ivc.expenses.db

import com.github.ivc.expenses.db.BuiltinCategoryIcon.Default
import com.github.ivc.expenses.db.BuiltinCategoryIcon.Unknown
import com.google.common.truth.Expect
import org.junit.Rule
import org.junit.Test

class CategoryIconConvertersTest {
    @JvmField
    @Rule
    val expect: Expect = Expect.create()
    private val conv = CategoryIconConverters()

    companion object {
        val cases = mapOf(
            "builtin:Default" to CategoryIconRef("builtin:Default", Default),
            "Default" to CategoryIconRef("Default", Default),
            "default" to CategoryIconRef("default", Unknown),
            "other" to CategoryIconRef("other", Unknown),
        )
    }

    @Test
    fun unmarshal() {
        cases.forEach {
            val got = conv.unmarshal(it.key)
            expect.that(got).isEqualTo(it.value)
        }
    }

    @Test
    fun marshal() {
        cases.forEach {
            val got = conv.marshal(it.value)
            expect.that(got).isEqualTo(it.key)
        }
    }
}