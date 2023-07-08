package com.github.ivc.expenses.screens

sealed class Screen(val route: String) {
    object WIP : Screen("wip")
}
