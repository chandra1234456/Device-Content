package com.chandra.practice.deviceinfo.ui.theme

import androidx.compose.ui.graphics.Color

// Material 3 baseline "purple" tonal palette, matching the app's existing brand primary (#6750A4).
val Primary40 = Color(0xFF6750A4)
val OnPrimary40 = Color(0xFFFFFFFF)
val PrimaryContainer40 = Color(0xFFEADDFF)
val OnPrimaryContainer40 = Color(0xFF21005D)

val Secondary40 = Color(0xFF625B71)
val OnSecondary40 = Color(0xFFFFFFFF)
val SecondaryContainer40 = Color(0xFFE8DEF8)
val OnSecondaryContainer40 = Color(0xFF1D192B)

val Tertiary40 = Color(0xFF7D5260)
val OnTertiary40 = Color(0xFFFFFFFF)
val TertiaryContainer40 = Color(0xFFFFD8E4)
val OnTertiaryContainer40 = Color(0xFF31111D)

val Error40 = Color(0xFFB3261E)
val OnError40 = Color(0xFFFFFFFF)
val ErrorContainer40 = Color(0xFFF9DEDC)
val OnErrorContainer40 = Color(0xFF410E0B)

val Background40 = Color(0xFFFFFBFE)
val OnBackground40 = Color(0xFF1C1B1F)
val Surface40 = Color(0xFFFFFBFE)
val OnSurface40 = Color(0xFF1C1B1F)
val SurfaceVariant40 = Color(0xFFE7E0EC)
val OnSurfaceVariant40 = Color(0xFF49454F)
val Outline40 = Color(0xFF79747E)

val Primary80 = Color(0xFFD0BCFF)
val OnPrimary80 = Color(0xFF381E72)
val PrimaryContainer80 = Color(0xFF4F378B)
val OnPrimaryContainer80 = Color(0xFFEADDFF)

val Secondary80 = Color(0xFFCCC2DC)
val OnSecondary80 = Color(0xFF332D41)
val SecondaryContainer80 = Color(0xFF4A4458)
val OnSecondaryContainer80 = Color(0xFFE8DEF8)

val Tertiary80 = Color(0xFFEFB8C8)
val OnTertiary80 = Color(0xFF492532)
val TertiaryContainer80 = Color(0xFF633B48)
val OnTertiaryContainer80 = Color(0xFFFFD8E4)

val Error80 = Color(0xFFF2B8B5)
val OnError80 = Color(0xFF601410)
val ErrorContainer80 = Color(0xFF8C1D18)
val OnErrorContainer80 = Color(0xFFF9DEDC)

val Background80 = Color(0xFF1C1B1F)
val OnBackground80 = Color(0xFFE6E1E5)
val Surface80 = Color(0xFF1C1B1F)
val OnSurface80 = Color(0xFFE6E1E5)
val SurfaceVariant80 = Color(0xFF49454F)
val OnSurfaceVariant80 = Color(0xFFCAC4D0)
val Outline80 = Color(0xFF938F99)

// Semantic status colors for health/diagnostic scores — layered on top of the M3 scheme above,
// not part of it, and identical in light/dark since "healthy vs. warning vs. danger" shouldn't
// change meaning with the theme.
val HealthGood = Color(0xFF2E9E5B)
val HealthWarning = Color(0xFFB8860B)
val HealthDanger = Color(0xFFC0392B)

fun healthScoreColor(score: Int): Color = when {
    score >= 80 -> HealthGood
    score >= 50 -> HealthWarning
    else -> HealthDanger
}

fun healthScoreLabel(score: Int): String = when {
    score >= 90 -> "Excellent"
    score >= 80 -> "Good"
    score >= 50 -> "Fair"
    else -> "Needs attention"
}
