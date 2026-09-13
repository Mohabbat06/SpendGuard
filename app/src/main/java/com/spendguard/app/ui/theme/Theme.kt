package com.spendguard.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF1B5E4A)
private val GreenContainer = Color(0xFFD8F3E8)
private val Cream = Color(0xFFF7F4EE)
private val Ink = Color(0xFF14221C)
private val Alarm = Color(0xFFB3261E)

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = GreenContainer,
    onPrimaryContainer = Ink,
    secondary = Color(0xFF4A6359),
    background = Cream,
    surface = Color.White,
    onBackground = Ink,
    onSurface = Ink,
    error = Alarm,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AD9B8),
    onPrimary = Color(0xFF003828),
    background = Color(0xFF101714),
    surface = Color(0xFF18201C),
    error = Color(0xFFFFB4AB),
)

@Composable
fun SpendGuardTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
