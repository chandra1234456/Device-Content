package com.chandra.practice.deviceinfo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.chandra.practice.deviceinfo.data.repository.ThemeMode

private val LightColors = lightColorScheme(
    primary = Primary40,
    onPrimary = OnPrimary40,
    primaryContainer = PrimaryContainer40,
    onPrimaryContainer = OnPrimaryContainer40,
    secondary = Secondary40,
    onSecondary = OnSecondary40,
    secondaryContainer = SecondaryContainer40,
    onSecondaryContainer = OnSecondaryContainer40,
    tertiary = Tertiary40,
    onTertiary = OnTertiary40,
    tertiaryContainer = TertiaryContainer40,
    onTertiaryContainer = OnTertiaryContainer40,
    error = Error40,
    onError = OnError40,
    errorContainer = ErrorContainer40,
    onErrorContainer = OnErrorContainer40,
    background = Background40,
    onBackground = OnBackground40,
    surface = Surface40,
    onSurface = OnSurface40,
    surfaceVariant = SurfaceVariant40,
    onSurfaceVariant = OnSurfaceVariant40,
    outline = Outline40,
)

private val DarkColors = darkColorScheme(
    primary = Primary80,
    onPrimary = OnPrimary80,
    primaryContainer = PrimaryContainer80,
    onPrimaryContainer = OnPrimaryContainer80,
    secondary = Secondary80,
    onSecondary = OnSecondary80,
    secondaryContainer = SecondaryContainer80,
    onSecondaryContainer = OnSecondaryContainer80,
    tertiary = Tertiary80,
    onTertiary = OnTertiary80,
    tertiaryContainer = TertiaryContainer80,
    onTertiaryContainer = OnTertiaryContainer80,
    error = Error80,
    onError = OnError80,
    errorContainer = ErrorContainer80,
    onErrorContainer = OnErrorContainer80,
    background = Background80,
    onBackground = OnBackground80,
    surface = Surface80,
    onSurface = OnSurface80,
    surfaceVariant = SurfaceVariant80,
    onSurfaceVariant = OnSurfaceVariant80,
    outline = Outline80,
)

@Composable
fun DeviceInfoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DeviceInfoTypography,
        shapes = DeviceInfoShapes,
        content = content,
    )
}

@Composable
fun rememberIsDarkTheme(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
