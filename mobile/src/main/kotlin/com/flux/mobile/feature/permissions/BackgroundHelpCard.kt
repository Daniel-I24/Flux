package com.flux.mobile.feature.permissions

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.mobile.R

private val STEPS = listOf(R.string.background_help_battery, R.string.background_help_autostart, R.string.background_help_sleeping)

/**
 * Battery savers of some brands stop FLUX and the watch app in the background, so reminders arrive
 * late. The steps are generic; the button opens FLUX's app info, which every Android phone has.
 */
@Composable
fun BackgroundHelpCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(FluxSpacing.lg), verticalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
            Text(stringResource(R.string.background_help_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.background_help_intro), style = MaterialTheme.typography.bodyMedium)
            STEPS.forEach { Text(stringResource(it), style = MaterialTheme.typography.bodyMedium) }
            Text(
                stringResource(R.string.background_help_where),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = { context.openAppInfo() }) { Text(stringResource(R.string.background_help_open)) }
        }
    }
}

private fun Context.openAppInfo() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri())
    runCatching { startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}
