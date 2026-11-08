package com.flux.mobile.sound

/** A name to show for an imported tone: WhatsApp audios get their date instead of "AUD-…-WA0078". */
sealed interface ToneName {
    data class WhatsApp(val year: Int, val month: Int, val day: Int) : ToneName

    data class Plain(val text: String) : ToneName

    data object Unknown : ToneName
}

/**
 * Which files are worth trying and how to name them. Pure, so every format is tested. The real
 * check is decoding the file (MediaExtractor); this only avoids rejecting audios whose provider
 * reports a vague type, like WhatsApp's .opus files coming as application/ogg or octet-stream.
 */
object ToneFormats {
    private val EXTENSIONS = setOf("mp3", "m4a", "mp4", "aac", "ogg", "oga", "opus", "wav", "amr", "3gp", "flac")
    private val MIME_EXTENSIONS = mapOf(
        "audio/mpeg" to "mp3",
        "audio/mp3" to "mp3",
        "audio/mp4" to "m4a",
        "audio/x-m4a" to "m4a",
        "audio/aac" to "aac",
        "audio/aacp" to "aac",
        "audio/ogg" to "ogg",
        "application/ogg" to "ogg",
        "audio/opus" to "opus",
        "audio/wav" to "wav",
        "audio/x-wav" to "wav",
        "audio/amr" to "amr",
        "audio/flac" to "flac",
    )
    private val WHATSAPP = Regex("""AUD-(\d{4})(\d{2})(\d{2})-WA\d+""", RegexOption.IGNORE_CASE)
    private const val MAX_TITLE = 60
    private const val FALLBACK_EXTENSION = "audio"

    fun accepts(mime: String?, name: String?): Boolean =
        mime?.startsWith("audio/") == true || mime == "application/ogg" || extensionOf(name) in EXTENSIONS

    /** The extension kept on the copy, so the decoder can recognise the container. */
    fun extension(mime: String?, name: String?): String =
        extensionOf(name)?.takeIf { it in EXTENSIONS } ?: MIME_EXTENSIONS[mime] ?: FALLBACK_EXTENSION

    fun name(displayName: String?): ToneName {
        val base = displayName?.substringBeforeLast('.')?.trim().orEmpty()
        WHATSAPP.matchEntire(base)?.destructured?.let { (year, month, day) ->
            return ToneName.WhatsApp(year.toInt(), month.toInt(), day.toInt())
        }
        val text = base.replace('_', ' ').replace(Regex("\\s+"), " ").trim().take(MAX_TITLE)
        return if (text.isEmpty()) ToneName.Unknown else ToneName.Plain(text)
    }

    /** User-typed names: trimmed, single-spaced and short; blank is not a name. */
    fun cleanTitle(title: String): String? = title.replace(Regex("\\s+"), " ").trim().take(MAX_TITLE).ifEmpty { null }

    private fun extensionOf(name: String?): String? = name?.substringAfterLast('.', "")?.lowercase()?.ifEmpty { null }
}
