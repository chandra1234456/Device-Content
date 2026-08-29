package com.chandra.practice.deviceinfo.ui.screens.legal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private const val LAST_UPDATED = "August 29, 2026"

private val PRIVACY_SECTIONS = listOf(
    LegalSection(
        heading = "1. Overview & Commitment to Privacy",
        body = "Device Content is an offline-first diagnostic and system information utility. " +
            "We prioritize your privacy above all else. This app is designed to inspect hardware specs, " +
            "battery metrics, network performance, and system diagnostics locally on your device. " +
            "No personal information, device identifier, or telemetry is ever collected, transmitted, " +
            "or sold to external servers.",
    ),
    LegalSection(
        heading = "2. Explicit Permissions & Just-In-Time Usage",
        body = "The app requests permissions strictly Just-In-Time (JIT) when accessing specific " +
            "diagnostic tools, and operates fully even if permissions are denied:\n\n" +
            "• Camera (CAMERA): Requested only when opening the Camera Diagnostic test to verify front/rear " +
            "camera sensor counts and lens capabilities. No photos or videos are ever recorded or stored.\n" +
            "• Microphone (RECORD_AUDIO): Requested only during the Microphone Diagnostic test to measure real-time " +
            "sound pressure / amplitude levels. Audio buffers are processed in temporary RAM and discarded immediately.\n" +
            "• Location (ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION): Requested during the Wi-Fi Signal Meter " +
            "and GPS Accuracy test as mandated by Android OS to retrieve SSID signal strength and satellite positioning.\n" +
            "• Bluetooth (BLUETOOTH_CONNECT): Requested on Android 12+ solely to inspect local Bluetooth adapter " +
            "availability and state.\n" +
            "• Storage & MediaStore: Uses Android MediaStore queries to present local storage breakdowns (Photos, " +
            "Videos, Audio, Downloads). No files are modified or deleted.",
    ),
    LegalSection(
        heading = "3. On-Device Storage & Local Data Retention",
        body = "Application preferences (such as dark mode settings and onboarding completion flags) are " +
            "saved locally on your device using Android DataStore. Diagnostic test results are stored in local " +
            "application memory. Clearing app data or uninstalling the app permanently erases all saved preferences.",
    ),
    LegalSection(
        heading = "4. Third-Party Services & Analytics",
        body = "Device Content contains zero third-party tracking, advertising SDKs, or external user telemetry.\n\n" +
            "• Google Play In-App Updates: The app utilizes Google Play Core APIs to notify you when a mandatory " +
            "or optional app update is available on the Google Play Store. These update checks are managed " +
            "securely by Google Play Services.\n" +
            "• PDF Report Sharing: Exported PDF diagnostic reports are generated locally and shared exclusively " +
            "via standard Android System Share intents initiated directly by you.",
    ),
    LegalSection(
        heading = "5. User Rights (GDPR & CCPA Compliance)",
        body = "Under international privacy regulations (including GDPR and CCPA):\n" +
            "• Right to Access: You retain full visual access to all inspected system data within the app.\n" +
            "• Right to Portability: You can export a consolidated PDF report of your device health at any time.\n" +
            "• Right to Deletion: Simply clear the app storage or uninstall the app to remove all local preferences.",
    ),
    LegalSection(
        heading = "6. Children's Privacy",
        body = "Device Content does not target children under the age of 13, nor does it collect any personal " +
            "identifiable information from any user of any age.",
    ),
    LegalSection(
        heading = "7. Updates & Policy Modifications",
        body = "We may revise this Privacy Policy periodically to reflect new features or Play Store compliance " +
            "requirements. The date at the top of this document indicates when it was last revised.",
    ),
    LegalSection(
        heading = "8. Contact Information",
        body = "For any questions or privacy inquiries, contact the developer directly at: " +
            "chandradev3660@gmail.com.",
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
