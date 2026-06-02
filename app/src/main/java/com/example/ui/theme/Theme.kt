package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FinanceLightColorScheme = lightColorScheme(
  primary = BrandPrimary,
  secondary = BrandSecondary,
  tertiary = BrandAccent,
  background = BgBase,
  surface = BgSurface,
  surfaceVariant = BgElevated,
  onPrimary = Color.White,
  onSecondary = TextPrimary,
  onTertiary = Color.White,
  onBackground = TextPrimary,
  onSurface = TextPrimary,
  onSurfaceVariant = TextSecondary,
  outline = BorderColor,
  error = ErrorColor
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Force visual premium light financial theme for EarnPulse
  dynamicColor: Boolean = false, // Keep consistent high-end fintech colors
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = FinanceLightColorScheme,
    typography = Typography,
    content = content
  )
}
