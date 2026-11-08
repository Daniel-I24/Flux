package com.flux.mobile.sound

import java.io.File
import java.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** A tone the user imported: the private copy and the name they see (and can change). */
@Serializable
data class ImportedTone(val fileName: String, val title: String)

/**
 * The list of imported tones, kept as a small JSON file next to them. Plain files, so it is tested
 * on the JVM. Files without an entry (imported by v1.1.0, before this list existed) are adopted with
 * [orphanTitle]; entries whose file disappeared are dropped.
 */
class ToneIndex(private val directory: File, private val orphanTitle: String) {
    private val file get() = File(directory, INDEX_FILE)
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(ImportedTone.serializer())

    fun list(): List<ImportedTone> {
        val saved = read().filter { File(directory, it.fileName).isFile }
        val orphans = directory.listFiles().orEmpty()
            .filter { it.isFile && it.name.startsWith(FILE_PREFIX) && saved.none { tone -> tone.fileName == it.name } }
            .sortedBy { it.lastModified() }
            .map { ImportedTone(it.name, orphanTitle) }
        return (saved + orphans).also { if (orphans.isNotEmpty()) write(it) }
    }

    fun add(tone: ImportedTone): List<ImportedTone> = (list().filter { it.fileName != tone.fileName } + tone).also(::write)

    fun rename(fileName: String, title: String): List<ImportedTone> =
        list().map { if (it.fileName == fileName) it.copy(title = title) else it }.also(::write)

    /** Removes the entry and the file; reminders that used it ring the fallback tone (SoundCatalog). */
    fun delete(fileName: String): List<ImportedTone> {
        File(directory, File(fileName).name).delete()
        return list().filter { it.fileName != fileName }.also(::write)
    }

    private fun read(): List<ImportedTone> = try {
        if (file.isFile) json.decodeFromString(serializer, file.readText()) else emptyList()
    } catch (_: IOException) {
        emptyList()
    } catch (_: SerializationException) {
        emptyList()
    } catch (_: IllegalArgumentException) {
        emptyList()
    }

    private fun write(tones: List<ImportedTone>) {
        directory.mkdirs()
        val temp = File(directory, "$INDEX_FILE.tmp")
        temp.writeText(json.encodeToString(serializer, tones))
        if (!temp.renameTo(file)) {
            file.delete()
            temp.renameTo(file)
        }
    }

    companion object {
        const val FILE_PREFIX = "tone_"
        private const val INDEX_FILE = "tones.json"
    }
}
