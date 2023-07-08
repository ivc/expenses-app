package com.github.ivc.expenses.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.ivc.expenses.ui.theme.ExpensesTheme

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold { innerPadding ->
        NavHost(navController, Screen.WIP.route, Modifier.padding(innerPadding)) {
            composable(Screen.WIP.route) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("⚠ work in progress ⚠")
                }
            }
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    ExpensesTheme {
        MainScreen()
    }
}
