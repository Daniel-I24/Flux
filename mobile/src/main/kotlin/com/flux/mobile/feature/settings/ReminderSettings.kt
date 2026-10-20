package com.flux.mobile.feature.settings

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.model.AppSettings
import com.flux.core.model.DailyRoutine
import com.flux.core.model.VibrationPattern
import com.flux.mobile.R
import com.flux.mobile.ui.common.formatted
import com.flux.mobile.ui.sound.SoundPicker

@get:StringRes
private val VibrationPattern.label: Int
    get() = when (this) {
        VibrationPattern.SHORT -> R.string.vibration_short
        VibrationPattern.DOUBLE -> R.string.vibration_double
        VibrationPattern.LONG -> R.string.vibration_long
        VibrationPattern.HEARTBEAT -> R.string.vibration_heartbeat
        VibrationPattern.OFF -> R.string.vibration_off
    }

/** Default sound, vibration (felt on selection), snooze length and FLUX quiet hours. */
@Composable
fun ReminderSettings(settings: AppSettings, routine: DailyRoutine?, viewModel: SettingsViewModel) {
    SettingsSection(stringResource(R.string.settings_reminders)) {
        Text(stringResource(R.string.settings_default_sound), style = MaterialTheme.typography.titleSmall)
        SoundPicker(selected = settings.defaultSound, onSelect = { it?.let(viewModel::setDefaultSound) })
        Text(stringResource(R.string.settings_vibration), style = MaterialTheme.typography.titleSmall)
        VibrationChoice(settings.vibration, viewModel::setVibration)
        Text(stringResource(R.string.settings_snooze), style = MaterialTheme.typography.titleSmall)
        SnoozeChoice(settings.snoozeMinutes, viewModel::setSnooze)
        Row(Modifier.fillMaxWidth().heightIn(min = FluxSizes.minTouchTarget), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_quiet_hours), style = MaterialTheme.typography.bodyLarge)
                Text(
                    text =
                    routine?.let { stringResource(R.string.settings_quiet_hours_detail, it.sleep.formatted(), it.wakeUp.formatted()) }
                        ?: stringResource(R.string.settings_quiet_hours_no_routine),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = settings.quietHoursEnabled, onCheckedChange = viewModel::setQuietHours)
        }
    }
}

@Composable
private fun VibrationChoice(selected: VibrationPattern, onSelect: (VibrationPattern) -> Unit) {
    val vibrator = LocalContext.current.getSystemService(Vibrator::class.java)
    Column(Modifier.selectableGroup()) {
        VibrationPattern.entries.forEach { pattern ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = FluxSizes.minTouchTarget).selectable(
                    selected = pattern == selected,
                    role = Role.RadioButton,
                    onClick = {
                        onSelect(pattern)
                        if (pattern != VibrationPattern.OFF) vibrator?.vibrate(VibrationEffect.createWaveform(pattern.timingsMs, NO_REPEAT))
                    },
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = pattern == selected, onClick = null)
                Spacer(Modifier.width(FluxSpacing.sm))
                Text(stringResource(pattern.label), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SnoozeChoice(minutes: Int, onSelect: (Int) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        AppSettings.SNOOZE_OPTIONS.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == minutes,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index, AppSettings.SNOOZE_OPTIONS.size),
            ) { Text(stringResource(R.string.settings_snooze_minutes, option)) }
        }
    }
}

private const val NO_REPEAT = -1
