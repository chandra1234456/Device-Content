package com.chandra.practice.deviceinfo.ui.screens.legal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private const val LAST_UPDATED = "August 29, 2026"

private val PRIVACY_SECTIONS = listOf(
    LegalSection(
        heading = "Overview",
        body = "Device Content is an informational utility app. It reads information that is already " +
            "available on your device and displays it to you. This policy explains exactly what it can " +
            "access, how that information is used, and what stays entirely under your control.",
    ),
    LegalSection(
        heading = "Information the app can access",
        body = "Device model, manufacturer and brand; Android version and build details; CPU and " +
            "hardware details; RAM and storage usage; battery level and health; screen and display " +
            "specs; sensors present on your device; the number of cameras and which way they face " +
            "(never photos or video); network connection type and, when available, Wi-Fi details; " +
            "your device's locale settings; and this app's own version and install info.",
    ),
    LegalSection(
        heading = "How this information is used",
        body = "It's shown on-screen in the app's dashboard. It's only ever gathered into a report when " +
            "you tap Copy, Share, or Export — an action you have to take yourself. Nothing is collected, " +
            "logged, or sent anywhere automatically.",
    ),
    LegalSection(
        heading = "What's stored on your device",
        body = "Your theme preference and whether you've completed the first-run screen are saved " +
            "locally using Android's DataStore. This stays on your device and is removed if you " +
            "uninstall the app.",
    ),
    LegalSection(
        heading = "What this app doesn't do",
        body = "No analytics SDK, no advertising SDK, no crash-reporting service that uploads data, no " +
            "account or sign-in, and no server of any kind — there's nowhere for your data to go.",
    ),
    LegalSection(
        heading = "Permissions",
        body = "Network state permissions let the app show your connection type on-screen; they don't " +
            "send any data anywhere by themselves. Camera permission is requested only when you open " +
            "the Camera tab, solely to read how many cameras your device has and which way they face — " +
            "the app never opens a camera session or captures images or video.",
    ),
    LegalSection(
        heading = "Google Play services",
        body = "The app uses Google Play's In-App Update API to check whether a newer version is " +
            "available, and standard Play Store share/rate intents when you tap those buttons in " +
            "Settings. Those requests are handled by the Play Store app itself, not by any server this " +
            "app operates — it doesn't have one.",
    ),
    LegalSection(
        heading = "Children's privacy",
        body = "Device Content is not directed at children under 13. Since the app doesn't collect data " +
            "from anyone, it doesn't knowingly collect data from children either.",
    ),
    LegalSection(
        heading = "Changes to this policy",
        body = "This policy may be updated as the app's features change. The \"last updated\" date above " +
            "reflects the latest revision — check back occasionally if you'd like to stay current.",
    ),
    LegalSection(
        heading = "Contact",
        body = "Questions about this policy can be sent to chandradev3660@gmail.com.",
    ),
)

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    onViewOnline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LegalDocumentScreen(
        title = "Privacy Policy",
        lastUpdated = LAST_UPDATED,
        sections = PRIVACY_SECTIONS,
        onBack = onBack,
        onViewOnline = onViewOnline,
        modifier = modifier,
    )
}
