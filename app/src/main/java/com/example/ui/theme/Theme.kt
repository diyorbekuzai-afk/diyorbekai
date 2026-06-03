package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.util.Calendar

private val DarkColorScheme = darkColorScheme(
    primary = Emerald500,
    secondary = Indigo500,
    tertiary = Amber500,
    error = Rose500,
    background = DarkBgColor,
    surface = DarkCardColor,
    surfaceVariant = DarkStatusEmptyColor,
    onSurfaceVariant = DarkStatusEmptyTextColor,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onError = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    outline = DarkSlate600
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald600,
    secondary = Indigo600,
    tertiary = Amber600,
    error = Rose600,
    background = LightSlate100,
    surface = Color.White,
    surfaceVariant = LightSlate200,
    onSurfaceVariant = LightSlate600,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onError = Color.White,
    onBackground = LightSlate900,
    onSurface = LightSlate900,
    outline = LightSlate300
)

@Composable
fun isNightTime(): Boolean {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    return hour < 6 || hour >= 18
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isNightTime(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
