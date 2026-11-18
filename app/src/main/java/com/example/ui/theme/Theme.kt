package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val StrictLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldSoftBg,
    onPrimaryContainer = TextDarkBlack,
    secondary = StrawberryRed,
    onSecondary = Color.White,
    secondaryContainer = StrawberrySoftBg,
    onSecondaryContainer = TextDarkBlack,
    tertiary = AmberAccent,
    onTertiary = Color.White,
    tertiaryContainer = AmberSoftBg,
    onTertiaryContainer = TextDarkBlack,
    background = BgOffWhite,
    onBackground = TextDarkBlack,
    surface = SurfacePureWhite,
    onSurface = TextDarkBlack,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderSlate,
    outlineVariant = BorderSoft,
    error = StrawberryRed,
    onError = Color.White,
    errorContainer = StrawberrySoftBg,
    onErrorContainer = TextDarkBlack
)

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun StrawberryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StrictLightColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
