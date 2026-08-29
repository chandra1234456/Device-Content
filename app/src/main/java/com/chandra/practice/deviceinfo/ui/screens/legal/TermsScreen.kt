package com.chandra.practice.deviceinfo.ui.screens.legal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private const val LAST_UPDATED = "August 29, 2026"

private val TERMS_SECTIONS = listOf(
    LegalSection(
        heading = "Acceptance of these terms",
        body = "By installing or using Device Content, you agree to these terms. If you don't agree, " +
            "please don't use the app.",
    ),
    LegalSection(
        heading = "What this app does",
        body = "Device Content is an informational utility app that reads and displays information " +
            "already available on your device — hardware, software, network, sensors, and more — so " +
            "you can view, copy, or export it for your own reference.",
    ),
    LegalSection(
        heading = "Acceptable use",
        body = "Use the app only on devices you own or are authorized to use, and only for lawful " +
            "purposes. Don't attempt to reverse-engineer, decompile, or misuse the app beyond its " +
            "intended purpose.",
    ),
    LegalSection(
        heading = "No warranty",
        body = "The app is provided \"as is,\" without warranties of any kind. While accuracy is the " +
            "goal, some values — like sensor availability or CPU details — depend on information your " +
            "device's manufacturer exposes to Android, and may not always be complete or exact.",
    ),
    LegalSection(
        heading = "Limitation of liability",
        body = "To the fullest extent permitted by law, the developer isn't liable for any damages or " +
            "decisions made based on information displayed by the app.",
    ),
    LegalSection(
        heading = "Children's use",
        body = "Device Content is not directed at children under 13.",
    ),
    LegalSection(
        heading = "Changes to these terms",
        body = "These terms may be updated as the app evolves. Continued use after an update means you " +
            "accept the revised terms.",
    ),
    LegalSection(
        heading = "Contact",
        body = "Questions about these terms can be sent to chandradev3660@gmail.com.",
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
