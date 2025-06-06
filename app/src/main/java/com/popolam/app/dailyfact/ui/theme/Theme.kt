package com.popolam.app.dailyfact.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = PurpleMedium,
    background = ScreenBackground,
    surface = White,
    onPrimary = White,
    onBackground = TextDark,
    onSurface = TextDark,
)

private val DarkColorScheme = darkColorScheme(
    primary = PurpleMedium,
    background = DarkPurpleStart, // Used by main background gradient
    surface = DarkCardBackground, // For the Card
    onPrimary = White,
    onBackground = TextLight,
    onSurface = TextLight, // For text on the card
)

@Composable
fun DailyFactTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}