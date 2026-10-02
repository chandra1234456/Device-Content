package com.chandra.practice.deviceinfo.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.chandra.practice.deviceinfo.data.model.InfoCategory
import com.chandra.practice.deviceinfo.data.model.DeviceInfoItem

/**
 * One row of a grouped list. Rows sharing a section are joined into a single rounded card:
 * pass [isFirst]/[isLast] so only the outer corners are rounded. The whole row is tappable to copy.
 */
@Composable
fun InfoRowCard(
    item: DeviceInfoItem,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
    isFirst: Boolean = true,
    isLast: Boolean = true,
) {
    val big = 20.dp
    val shape = RoundedCornerShape(
        topStart = if (isFirst) big else 4.dp,
        topEnd = if (isFirst) big else 4.dp,
        bottomStart = if (isLast) big else 4.dp,
        bottomEnd = if (isLast) big else 4.dp,
    )
    Surface(
        onClick = onCopy,
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { onClick(label = "Copy ${item.label}") { onCopy(); true } },
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (item.copyable) 0.9f else 0.4f),
                modifier = Modifier.padding(start = 12.dp).size(18.dp),
            )
        }
    }
}

/** Header shown above a category's rows: tonal icon badge, title and item count. */
@Composable
fun CategoryHeader(category: InfoCategory, itemCount: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(category.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Column(modifier = Modifier.padding(start = 14.dp)) {
            Text(category.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            if (itemCount > 0) {
                Text(
                    "$itemCount details · tap any row to copy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
