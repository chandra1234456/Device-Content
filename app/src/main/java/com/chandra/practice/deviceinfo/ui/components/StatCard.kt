package com.chandra.practice.deviceinfo.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chandra.practice.deviceinfo.data.model.QuickStat

@Composable
fun StatCard(stat: QuickStat, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val cardModifier = modifier
        .width(112.dp)
        .semantics { contentDescription = "${stat.label}: ${stat.value}" }
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)

    if (onClick != null) {
        Card(onClick = onClick, modifier = cardModifier, colors = colors, shape = MaterialTheme.shapes.medium) {
            StatCardContent(stat)
        }
    } else {
        Card(modifier = cardModifier, colors = colors, shape = MaterialTheme.shapes.medium) {
            StatCardContent(stat)
        }
    }
}

@Composable
private fun StatCardContent(stat: QuickStat) {
    Column(modifier = Modifier.padding(12.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp)) {
            if (stat.progress != null) {
                CircularProgressIndicator(
                    progress = { stat.progress.coerceIn(0f, 1f) },
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            Icon(
                imageVector = stat.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(if (stat.progress != null) 18.dp else 28.dp),
            )
        }
        Column(modifier = Modifier.padding(top = 8.dp)) {
            Text(
                text = stat.value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stat.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
