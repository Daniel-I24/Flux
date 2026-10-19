package com.flux.mobile.ui.sound

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.core.content.IntentCompat
import com.flux.core.model.SoundRef

/** Android's own ringtone picker; returns the chosen tone as a [SoundRef.System]. */
private class PickSystemSound(private val title: String) : ActivityResultContract<Unit, SoundRef.System?>() {
    override fun createIntent(context: Context, input: Unit): Intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION or RingtoneManager.TYPE_ALARM)
        .putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, title)
        .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
        .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false)

    override fun parseResult(resultCode: Int, intent: Intent?): SoundRef.System? {
        if (resultCode != Activity.RESULT_OK || intent == null) return null
        val uri = IntentCompat.getParcelableExtra(intent, RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java) ?: return null
        return SoundRef.System(uri = uri.toString(), title = "")
    }
}

/** Returns a launcher; [onPicked] receives the tone with its display title resolved. */
@Composable
fun rememberSystemSoundPicker(context: Context, title: String, onPicked: (SoundRef.System) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(PickSystemSound(title)) { picked ->
        picked ?: return@rememberLauncherForActivityResult
        val name = RingtoneManager.getRingtone(context, Uri.parse(picked.uri))?.getTitle(context).orEmpty()
        onPicked(picked.copy(title = name.take(MAX_TITLE)))
    }
    return { launcher.launch(Unit) }
}

private const val MAX_TITLE = 60
