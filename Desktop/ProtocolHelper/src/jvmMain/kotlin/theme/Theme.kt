package theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import theme.shapes

// Material 3 color schemes
private val darkColorScheme = Colors(
    primary = dark_primary,
    onPrimary = dark_onPrimary,
    secondary = dark_secondary,
    onSecondary = dark_onSecondary,
    error = dark_error,
    onError = dark_onError,
    background = dark_background,
    onBackground = dark_onBackground,
    surface = dark_surface,
    onSurface = dark_onSurface,
    primaryVariant = dark_primaryContainer,
    secondaryVariant = dark_secondaryContainer,
    isLight = false,
)

private val lightColorScheme = Colors(
    primary = light_primary,
    onPrimary = light_onPrimary,
    secondary = light_secondary,
    onSecondary = light_onSecondary,
    error = light_error,
    onError = light_onError,
    background = light_background,
    onBackground = light_onBackground,
    surface = light_surface,
    onSurface = light_onSurface,
    primaryVariant = light_primaryContainer,
    secondaryVariant = light_secondaryContainer,
    isLight = true,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (dynamicColor) {
        if (darkTheme) darkColorScheme else lightColorScheme
    } else {
        lightColorScheme
    }

    MaterialTheme(
        colors = colorScheme,
        shapes = shapes,
        content = content
    )
}
