package com.flux.core.domain.policy

import com.flux.core.model.Category
import com.flux.core.model.Note
import com.flux.core.model.SoundRef

/** Which sound a reminder plays: its own, then its category's, then the app default. */
object ReminderSoundPolicy {
    fun resolve(note: Note, category: Category?, default: SoundRef): SoundRef = note.sound ?: category?.sound ?: default
}
