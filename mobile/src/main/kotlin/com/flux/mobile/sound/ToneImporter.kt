package com.flux.mobile.sound

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.provider.OpenableColumns
import com.flux.core.designsystem.sound.ToneFiles
import com.flux.mobile.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject

/**
 * Copies an audio chosen with ACTION_OPEN_DOCUMENT into private storage, so it keeps ringing if the
 * original moves. Accepted when it decodes as audio (mp3, m4a, ogg, opus, aac, wav…), whatever MIME
 * type the provider reports; files over [MAX_BYTES] or without an audio track are rejected.
 */
class ToneImporter @Inject constructor(@param:ApplicationContext private val context: Context) {
    /** Blocking: call it off the main thread. */
    fun import(uri: Uri): ImportedTone? {
        val resolver = context.contentResolver
        val type = runCatching { resolver.getType(uri) }.getOrNull()
        val displayName = displayName(uri)
        if (!ToneFormats.accepts(type, displayName)) return null
        val target = ToneFiles.file(context, "${ToneIndex.FILE_PREFIX}${UUID.randomUUID()}.${ToneFormats.extension(type, displayName)}")
        val copied = try {
            resolver.openInputStream(uri)?.use { input -> target.outputStream().use { output -> copyLimited(input, output) } } ?: false
        } catch (_: IOException) {
            false
        } catch (_: SecurityException) {
            false
        }
        if (!copied || !hasAudioTrack(target)) {
            target.delete()
            return null
        }
        return ImportedTone(target.name, title(displayName))
    }

    private fun title(displayName: String?): String = when (val name = ToneFormats.name(displayName)) {
        is ToneName.WhatsApp -> context.getString(R.string.sound_whatsapp, name.day, name.month, name.year)
        is ToneName.Plain -> name.text
        ToneName.Unknown -> context.getString(R.string.sound_imported)
    }

    private fun displayName(uri: Uri): String? = runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
    }.getOrNull() ?: uri.lastPathSegment

    /** Copies at most [MAX_BYTES]; a bigger file is rejected instead of filling the storage. */
    private fun copyLimited(input: InputStream, output: OutputStream): Boolean {
        val buffer = ByteArray(BUFFER_BYTES)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) return total > 0
            total += read
            if (total > MAX_BYTES) return false
            output.write(buffer, 0, read)
        }
    }

    /** MediaExtractor reads the container (Ogg/Opus, MP4, ADTS AAC, WAV…) the same way playback will. */
    private fun hasAudioTrack(file: File): Boolean {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(file.path)
            (0 until extractor.trackCount).any {
                extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            }
        } catch (_: IOException) {
            false
        } catch (_: RuntimeException) {
            false
        } finally {
            extractor.release()
        }
    }

    private companion object {
        const val BUFFER_BYTES = 8 * 1024
        const val MAX_BYTES = 10L * 1024 * 1024
    }
}
