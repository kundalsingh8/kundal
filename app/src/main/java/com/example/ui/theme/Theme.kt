package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = ProfessionalGreen,
    secondary = ProfessionalTeal,
    tertiary = ProfessionalDeepDark,
    background = ProfessionalLightBg,
    surface = ProfessionalCardBg,
    onPrimary = Color.White,
    onSecondary = ProfessionalTextDark,
    onBackground = ProfessionalTextDark,
    onSurface = ProfessionalTextDark,
    surfaceVariant = ProfessionalLightCard,
    onSurfaceVariant = ProfessionalTextSubtle,
    outline = ProfessionalBorder,
    error = ProfessionalAlertText,
    errorContainer = ProfessionalAlertBg,
    onErrorContainer = ProfessionalAlertText
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ProfessionalGreen,
    secondary = ProfessionalTeal,
    tertiary = ProfessionalDeepDark,
    background = ProfessionalLightBg,
    surface = ProfessionalCardBg,
    onPrimary = Color.White,
    onSecondary = ProfessionalTextDark,
    onBackground = ProfessionalTextDark,
    onSurface = ProfessionalTextDark,
    surfaceVariant = ProfessionalLightCard,
    onSurfaceVariant = ProfessionalTextSubtle,
    outline = ProfessionalBorder,
    error = ProfessionalAlertText,
    errorContainer = ProfessionalAlertBg,
    onErrorContainer = ProfessionalAlertText
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Set to false to force our gorgeous brand identity consistently
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false, 
  content: @Composable () -> Unit,
) {
  val colorScheme = LightColorScheme // Force professional light mode forever

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
