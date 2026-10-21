package com.swapcampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.swapcampus.ui.SwapCampusApp
import com.swapcampus.ui.theme.SwapCampusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwapCampusTheme {
                SwapCampusApp()
            }
        }
    }
}