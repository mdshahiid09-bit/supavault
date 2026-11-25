package com.supavault.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SupabaseGreen,
    onPrimary = DarkBackground,
    primaryContainer = SupabaseGreenDark,
    onPrimaryContainer = TextPrimary,
    secondary = StatusBlue,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = StatusRed
)

@Composable
fun SupaVaultTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
