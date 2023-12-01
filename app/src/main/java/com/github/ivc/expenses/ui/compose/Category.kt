package com.github.ivc.expenses.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.github.ivc.expenses.db.Category

// TODO: add support for non-builtin icons
val Category.painter: Painter @Composable get() = painterResource(id = this.icon.builtin.id)
