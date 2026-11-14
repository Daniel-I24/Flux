package com.flux.mobile.launch

import android.content.Context
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.flux.mobile.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** "Dictar nota" and "Nueva nota" when the FLUX icon is long-pressed (dynamic, so no package id is hard-coded). */
class AppShortcuts @Inject constructor(@ApplicationContext private val context: Context) {
    fun publish() {
        val shortcuts = listOf(
            shortcut(DICTATE_ID, R.string.shortcut_dictate, R.drawable.ic_shortcut_mic, CaptureLaunchRequests.ACTION_DICTATE),
            shortcut(NEW_NOTE_ID, R.string.shortcut_new_note, R.drawable.ic_shortcut_note, CaptureLaunchRequests.ACTION_NEW_NOTE),
        ).mapIndexed { rank, builder -> builder.setRank(rank).build() }
        runCatching { ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts) }
    }

    private fun shortcut(id: String, label: Int, icon: Int, action: String) = ShortcutInfoCompat.Builder(context, id)
        .setShortLabel(context.getString(label))
        .setIcon(IconCompat.createWithResource(context, icon))
        .setIntent(CaptureLaunchRequests.intent(context, action))

    private companion object {
        const val DICTATE_ID = "dictate"
        const val NEW_NOTE_ID = "new_note"
    }
}
