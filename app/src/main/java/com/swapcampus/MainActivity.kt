package com.swapcampus

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.swapcampus.ui.SwapCampusApp
import com.swapcampus.ui.theme.SwapCampusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // targetSdk 35 impose l'affichage bord à bord : on garde les barres
        // système transparentes et on adapte la couleur de leurs icônes au thème.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            SwapCampusTheme {
                val vue = LocalView.current
                val sombre = isSystemInDarkTheme()
                DisposableEffect(sombre) {
                    val controleur = WindowCompat.getInsetsController(window, vue)
                    controleur.isAppearanceLightStatusBars = !sombre
                    controleur.isAppearanceLightNavigationBars = !sombre
                    onDispose { }
                }
                SwapCampusApp()
            }
        }
    }
}
