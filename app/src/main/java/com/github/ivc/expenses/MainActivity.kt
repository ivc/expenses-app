package com.github.ivc.expenses

import android.Manifest.permission.READ_SMS
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.github.ivc.expenses.ui.screens.monthly.MonthlyScreen
import com.github.ivc.expenses.ui.theme.ExpensesTheme
import com.github.ivc.expenses.work.ImportSms

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (checkSelfPermission(READ_SMS)) {
            PERMISSION_GRANTED -> {
                scheduleSmsImport()
            }

            else -> {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (isGranted) {
                        scheduleSmsImport()
                    }
                }
                requestPermissions(arrayOf(READ_SMS), 1)
            }
        }

        setContent {
            ExpensesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MonthlyScreen()
                }
            }
        }
    }

    private fun scheduleSmsImport() {
        val workManager = WorkManager.getInstance(applicationContext)
        val importSmsRequest = OneTimeWorkRequestBuilder<ImportSms>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        workManager.enqueue(importSmsRequest)
    }
}
