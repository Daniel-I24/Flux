package com.flux.mobile.ui.sound

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.flux.core.designsystem.sound.ToneFiles
import com.flux.core.model.SoundRef
import com.flux.mobile.R
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Copies an audio file chosen with the system picker (ACTION_OPEN_DOCUMENT with an audio MIME type) into the
 * app's private storage, so it keeps ringing if the original is moved or deleted. Files that
 * are too big or contain no audio are rejected.
 */
class ToneImporter(private val context: Context) {
    suspend fun import(uri: Uri): SoundRef.Custom? = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val type = resolver.getType(uri)
        if (type != null && !type.startsWith(AUDIO_PREFIX)) return@withContext null
        val displayName = displayName(uri) ?: context.getString(R.string.sound_custom)
        val extension = type?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) } ?: FALLBACK_EXTENSION
        val target = ToneFiles.file(context, "$FILE_PREFIX${UUID.randomUUID()}.$extension")
        val copied = try {
            resolver.openInputStream(uri)?.use { input -> target.outputStream().use { output -> copyLimited(input, output) } } ?: false
        } catch (_: IOException) {
            false
        } catch (_: SecurityException) {
            false
        }
        if (!copied || !hasAudio(target)) {
            target.delete()
            return@withContext null
        }
        SoundRef.Custom(fileName = target.name, title = displayName.substringBeforeLast('.').take(MAX_TITLE))
    }

    private fun displayName(uri: Uri): String? = context.contentResolver
        .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }

    /** Copies at most [MAX_BYTES]; a bigger file is rejected instead of filling the storage. */
    private fun copyLimited(input: InputStream, output: OutputStream): Boolean {
        val buffer = ByteArray(BUFFER_BYTES)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) return true
            total += read
            if (total > MAX_BYTES) return false
            output.write(buffer, 0, read)
        }
    }

    private fun hasAudio(file: File): Boolean {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.path)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) == YES
        } catch (_: IllegalArgumentException) {
            false
        } catch (_: RuntimeException) {
            false
        } finally {
            retriever.release()
        }
    }

    private companion object {
        const val AUDIO_PREFIX = "audio/"
        const val FILE_PREFIX = "tone_"
        const val FALLBACK_EXTENSION = "audio"
        const val YES = "yes"
        const val MAX_TITLE = 60
        const val BUFFER_BYTES = 8 * 1024
        const val MAX_BYTES = 10L * 1024 * 1024
    }
}
