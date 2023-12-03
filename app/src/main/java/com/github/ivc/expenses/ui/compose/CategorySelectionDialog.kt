package com.github.ivc.expenses.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ivc.expenses.db.AppDb
import com.github.ivc.expenses.db.Category
import com.github.ivc.expenses.db.PurchaseEntry
import com.github.ivc.expenses.ui.theme.ExpensesTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

private val categoryButtonColors
    @Composable get() = IconButtonDefaults.iconToggleButtonColors(
        checkedContainerColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelectionDialog(
    entry: PurchaseEntry,
    onDismiss: () -> Unit = {},
    onConfirm: (Category) -> Unit = {},
    model: CategorySelectionModel = viewModel(),
) {
    val categories by model.categories.collectAsState()
    var selected by remember { mutableStateOf(entry.category) }
    Dialog(
        onDismissRequest = { onDismiss() },
    ) {
        Card {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    selected.name,
                )
                FlowRow {
                    for (category in categories) {
                        IconToggleButton(
                            checked = category == selected,
                            colors = categoryButtonColors,
                            onCheckedChange = { selected = category },
                        ) {
                            Icon(
                                painter = category.painter,
                                contentDescription = category.name,
                                tint = Color(category.color),
                            )
                        }
                    }
                }
                TextButton(onClick = { onConfirm(selected) }) {
                    Text("Set Category")
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewCategorySelectorDialog() {
    val context = LocalContext.current
    ExpensesTheme(darkTheme = true) {
        Surface(Modifier.width(412.dp)) {
            CategorySelectionDialog(
                entry = PurchaseEntry.Preview,
                model = CategorySelectionModel(categoriesFlow = flow {
                    emit(Category.defaultCategories(context))
                })
            )
        }
    }
}

class CategorySelectionModel(
    categoriesFlow: Flow<List<Category>> = AppDb.instance.categories.all(),
) : ViewModel() {
    val categories = categoriesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = listOf(),
    )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
