package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

val DarkBgColor = Color(0xFF0F121A)
val DarkCardColor = Color(0xFF1A1D24)
val DarkStatusEmptyColor = Color(0xFF2D323E)
val DarkStatusEmptyTextColor = Color(0xFF8F98A8)

val LightSlate100 = Color(0xFFF1F5F9)
val LightSlate200 = Color(0xFFE2E8F0)
val LightSlate300 = Color(0xFFCBD5E1)
val LightSlate600 = Color(0xFF475569)
val LightSlate900 = Color(0xFF0F172A)
val DarkSlate600 = Color(0xFF475569)

// Dynamic properties

val BgColor: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background

val CardColor: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface

val StatusEmptyColor: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceVariant

val StatusEmptyTextColor: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

val MainTextColor: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface

val Slate200: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface

val Slate300: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface

val Slate400: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

val Slate600: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outline

val Indigo600 = Color(0xFF6366F1)
val Indigo500 = Color(0xFF818CF8)
val Indigo900 = Color(0xFF312E81)

val Emerald500 = Color(0xFF10B981)
val Emerald200 = Color(0xFFA7F3D0)
val Emerald600 = Color(0xFF059669)

val Amber500 = Color(0xFFF59E0B)
val Amber200 = Color(0xFFFDE68A)
val Amber600 = Color(0xFFD97706)

val Rose600 = Color(0xFFE11D48)
val Rose500 = Color(0xFFF43F5E)
val Rose400 = Color(0xFFFB7185)
