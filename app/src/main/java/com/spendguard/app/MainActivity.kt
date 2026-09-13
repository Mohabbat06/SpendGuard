package com.spendguard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.spendguard.app.ui.SpendGuardRoot
import com.spendguard.app.ui.theme.SpendGuardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendGuardTheme {
                SpendGuardRoot()
            }
        }
    }
}
