package com.djes021.landconverter.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF2F6B3A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8EBC5),
    onPrimaryContainer = Color(0xFF00210A),
    secondary = Color(0xFF7A5900),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDEA3),
    onSecondaryContainer = Color(0xFF261900),
    tertiary = Color(0xFF8B5038),
    tertiaryContainer = Color(0xFFFFDBCD),
    onTertiaryContainer = Color(0xFF33110A),
    background = Color(0xFFFCFDF6),
    surface = Color(0xFFFCFDF6),
    onSurface = Color(0xFF1A1C19),
    error = Color(0xFFBA1A1A)
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF9CD5A0),
    onPrimary = Color(0xFF003914),
    primaryContainer = Color(0xFF1A5129),
    onPrimaryContainer = Color(0xFFB7F2B9),
    secondary = Color(0xFFE9C16C),
    onSecondary = Color(0xFF412D00),
    secondaryContainer = Color(0xFF5D4200),
    onSecondaryContainer = Color(0xFFFFDEA3),
    tertiary = Color(0xFFFFB59B),
    tertiaryContainer = Color(0xFF6C3923),
    onTertiaryContainer = Color(0xFFFFDBCD),
    background = Color(0xFF1A1C19),
    surface = Color(0xFF1A1C19),
    onSurface = Color(0xFFE2E3DD),
    error = Color(0xFFFFB4AB)
)

@Composable
fun LandConverterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
