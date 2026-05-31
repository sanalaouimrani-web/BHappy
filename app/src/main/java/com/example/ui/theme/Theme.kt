package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SageGreenDarkAccent,
    secondary = SoftLavender,
    tertiary = MutedBlue,
    background = SoftDarkBg,
    surface = SoftDarkSurface,
    onPrimary = SoftDarkBg,
    onSecondary = SoftDarkOnBg,
    onTertiary = SoftDarkOnBg,
    onBackground = SoftDarkOnBg,
    onSurface = SoftDarkOnSurface
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SageGreenPrimary,
    secondary = SoftLavender,
    tertiary = MutedBlue,
    background = SoftLightBg,
    surface = SoftLightSurface,
    onPrimary = SoftLightSurface,
    onSecondary = SoftLightOnBg,
    onTertiary = SoftLightOnBg,
    onBackground = SoftLightOnBg,
    onSurface = SoftLightOnSurface
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic colors to enforce the calming customized pastel palette strictly.
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
