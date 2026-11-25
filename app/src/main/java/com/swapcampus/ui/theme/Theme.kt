package com.swapcampus.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Thème de l'application : palette indigo / menthe, coins généreusement
 * arrondis, typographie maison.
 *
 * Le mode dynamique de Material You est désactivé par défaut : la marque
 * garde ainsi la même identité sur tous les téléphones, ce qui est plus
 * lisible pour une démonstration notée. Il reste activable via le paramètre.
 */
private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = IndigoContainer,
    onPrimaryContainer = IndigoInk,
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = TealContainer,
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = Amber,
    onTertiary = Color.White,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = Color(0xFF2A1700),
    background = Cloud,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = InkSoft,
    outline = Line,
    outlineVariant = Line,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC6C0FF),
    onPrimary = Color(0xFF241A78),
    primaryContainer = IndigoContainerDark,
    onPrimaryContainer = IndigoContainer,
    secondary = Color(0xFF7DD7C8),
    onSecondary = Color(0xFF00201C),
    secondaryContainer = Color(0xFF075049),
    onSecondaryContainer = TealContainer,
    tertiary = Color(0xFFFFB870),
    onTertiary = Color(0xFF452A00),
    tertiaryContainer = Color(0xFF653E00),
    onTertiaryContainer = AmberContainer,
    background = Night,
    onBackground = Color(0xFFE8E5F2),
    surface = NightSurface,
    onSurface = Color(0xFFE8E5F2),
    surfaceVariant = NightVariant,
    onSurfaceVariant = Color(0xFFC6C2DA),
    outline = NightLine,
    outlineVariant = NightLine,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val SwapCampusShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun SwapCampusTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) {
                androidx.compose.material3.dynamicDarkColorScheme(context)
            } else {
                androidx.compose.material3.dynamicLightColorScheme(context)
            }
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = SwapCampusShapes,
        content = content
    )
}
