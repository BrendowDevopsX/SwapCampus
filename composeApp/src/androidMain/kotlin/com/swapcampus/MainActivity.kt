package com.isep.composeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.isep.composeapp.ui.SwapCampusApp
import com.isep.composeapp.ui.theme.SwapCampusTheme

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