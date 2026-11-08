package com.flux.mobile.sound

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import com.flux.core.designsystem.sound.ToneFiles
import com.flux.core.model.SoundRef
import com.flux.mobile.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** The phone's own tones, read with RingtoneManager (alarms and notifications) instead of its picker. */
data class SystemTone(val sound: SoundRef.System)

/** Imported tones (add, rename, delete) and the phone's tones, all for FLUX's own picker. */
@Singleton
class ToneLibrary @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val importer: ToneImporter,
) {
    private val index by lazy { ToneIndex(ToneFiles.directory(context), context.getString(R.string.sound_imported)) }
    private val mutex = Mutex()
    private val imported = MutableStateFlow<List<ImportedTone>>(emptyList())
    private val system = MutableStateFlow<List<SystemTone>>(emptyList())

    val importedTones: StateFlow<List<ImportedTone>> = imported.asStateFlow()
    val systemTones: StateFlow<List<SystemTone>> = system.asStateFlow()

    suspend fun load() = io {
        imported.value = index.list()
        if (system.value.isEmpty()) system.value = readSystemTones()
    }

    /** Null when the file is not usable audio (the picker says so and keeps the current tone). */
    suspend fun import(uri: Uri): ImportedTone? = io {
        importer.import(uri)?.also { imported.value = index.add(it) }
    }

    suspend fun rename(fileName: String, title: String) = io {
        ToneFormats.cleanTitle(title)?.let { imported.value = index.rename(fileName, it) }
    }

    suspend fun delete(fileName: String) = io { imported.value = index.delete(fileName) }

    private fun readSystemTones(): List<SystemTone> = runCatching {
        val manager = RingtoneManager(context).apply { setType(RingtoneManager.TYPE_ALARM or RingtoneManager.TYPE_NOTIFICATION) }
        val cursor = manager.cursor
        buildList {
            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX).orEmpty()
                add(SystemTone(SoundRef.System(manager.getRingtoneUri(cursor.position).toString(), title)))
            }
        }.distinctBy { it.sound.uri }
    }.getOrDefault(emptyList())

    private suspend fun <T> io(block: suspend () -> T): T = withContext(Dispatchers.IO) { mutex.withLock { block() } }
}
