package com.chandra.practice.deviceinfo.ui.screens.legal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private const val LAST_UPDATED = "August 29, 2026"

private val TERMS_SECTIONS = listOf(
    LegalSection(
        heading = "1. Acceptance of Terms",
        body = "By downloading, installing, or accessing Device Content, you agree to be bound by these Terms " +
            "of Service. If you do not agree to all terms outlined herein, please discontinue use of the " +
            "application immediately.",
    ),
    LegalSection(
        heading = "2. Scope of Software Services",
        body = "Device Content provides user-initiated hardware diagnostic tests (Display, Touch, Vibration, " +
            "Flash, Proximity, Speaker, Microphone, Camera, GPS, Wi-Fi), real-time battery analytics, sensor " +
            "exploration, performance benchmarks (CPU, RAM, Storage throughput), and MediaStore storage category " +
            "breakdowns. All information is inspected locally on your Android device.",
    ),
    LegalSection(
        heading = "3. Performance Benchmarks & Hardware Safety",
        body = "The Performance Benchmark tool executes localized multi-threaded CPU computations, RAM buffer " +
            "transfers, and temporary cache storage write/read tests. These stress tests are designed to evaluate " +
            "device capabilities without causing hardware strain or permanent modifications. You agree to run " +
            "benchmarks at your own risk under standard operating temperatures.",
    ),
    LegalSection(
        heading = "4. Diagnostic Disclaimer & No Professional Warranty",
        body = "All diagnostic results (Pass, Fail, or Not Tested) and health scores provided by Device Content " +
            "are for personal informational purposes only. The app relies on hardware capabilities exposed " +
            "by your device manufacturer through standard Android APIs. Diagnostic results do not constitute " +
            "an official manufacturer warranty, certified repair diagnosis, or hardware guarantee.",
    ),
    LegalSection(
        heading = "5. Acceptable Use & Intellectual Property",
        body = "You agree to use Device Content solely on devices you own or are authorized to operate, and " +
            "in compliance with applicable local laws. You may not decompile, reverse-engineer, modify, " +
            "or redistribute the application binary without prior permission.",
    ),
    LegalSection(
        heading = "6. Limitation of Liability",
        body = "To the maximum extent permitted by applicable law, the developer shall not be liable for any " +
            "indirect, incidental, consequential, or special damages arising out of your use or inability " +
            "to use the application or reliance on any diagnostic data displayed.",
    ),
    LegalSection(
        heading = "7. Modifications to Terms",
        body = "We reserve the right to modify these Terms at any time. Continued use of the application " +
            "following any published updates constitutes your acceptance of the revised Terms.",
    ),
    LegalSection(
        heading = "8. Contact & Legal Inquiries",
        body = "For legal questions or inquiries regarding these Terms, contact the developer at: " +
            "chandradev3660@gmail.com.",
    ),
)

@Composable
fun TermsScreen(
    onBack: () -> Unit,
    onViewOnline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LegalDocumentScreen(
        title = "Terms & Services",
        lastUpdated = LAST_UPDATED,
        sections = TERMS_SECTIONS,
        onBack = onBack,
        onViewOnline = onViewOnline,
        modifier = modifier,
    )
}
