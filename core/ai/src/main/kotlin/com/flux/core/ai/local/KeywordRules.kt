package com.flux.core.ai.local

import com.flux.core.model.DefaultCategory
import com.flux.core.model.Priority

private fun rx(vararg patterns: String) = patterns.map { Regex("\\b(?:$it)\\b") }

/** Accent-free Spanish keywords; each hit adds [KeywordRule.weight] to the category. */
internal val KEYWORD_RULES = listOf(
    KeywordRule(
        DefaultCategory.REMINDERS, 2,
        rx("recuerd(?:a|ame|ale|anos)", "recordar(?:me)?", "recordatorio", "avis(?:a|ame)", "alarma", "no (?:te )?olvid(?:es|ar)"),
    ),
    KeywordRule(
        DefaultCategory.HOMEWORK, 2,
        rx(
            "deberes?", "examen(?:es)?", "parcial(?:es)?", "quiz", "taller", "exposicion", "estudiar", "investigacion",
            "tarea de \\w+", "clase de", "profesor(?:a)?", "universidad", "colegio", "materia", "ensayo", "proyecto de \\w+",
        ),
    ),
    KeywordRule(
        DefaultCategory.SHOPPING, 2,
        rx("comprar", "compras", "supermercado", "mercado", "tienda", "lista de compras", "conseguir"),
    ),
    KeywordRule(
        DefaultCategory.HEALTH, 2,
        rx(
            "medicamentos?", "pastillas?", "cita medica", "medic[oa]", "doctor(?:a)?", "odontologo", "dentista", "vitaminas?",
            "hospital", "dolor", "ejercicio", "gimnasio", "entrenar", "tomar agua", "presion", "terapia",
        ),
    ),
    KeywordRule(
        DefaultCategory.MEALS, 2,
        rx("comer", "almorzar", "almuerzo", "cena", "cenar", "desayuno", "desayunar", "receta", "cocinar", "merienda", "comida"),
    ),
    KeywordRule(
        DefaultCategory.TASKS, 1,
        rx(
            "(?:tengo|tenemos) que", "debo", "debemos", "hay que", "necesito", "llamar", "enviar", "pagar", "hacer", "terminar",
            "entregar", "revisar", "preparar", "llevar", "reservar", "agendar", "escribir", "imprimir", "limpiar", "pendiente",
        ),
    ),
    KeywordRule(
        DefaultCategory.IDEAS, 2,
        rx(
            "idea", "se me ocurrio", "podriamos", "podria", "seria (?:bueno|genial|interesante)",
            "que tal si", "y si", "concepto", "propuesta", "imagina",
        ),
    ),
    KeywordRule(
        DefaultCategory.INFO, 1,
        rx(
            "codigo", "clave", "contrasena", "numero", "direccion", "telefono", "correo", "dato", "placa", "cuenta",
            "queda en", "se llama", "\\d{3,}",
        ),
    ),
)

/** Extra weight for reminders when a concrete date or time was detected. */
internal const val DATE_REMINDER_WEIGHT = 1

/** A custom category whose name appears in the text wins clearly. */
internal const val CUSTOM_NAME_WEIGHT = 4

/** Tie-break: specific categories before generic ones ("comprar pan el viernes" is Shopping). */
internal val CATEGORY_PRIORITY = listOf(
    DefaultCategory.HOMEWORK, DefaultCategory.SHOPPING, DefaultCategory.HEALTH, DefaultCategory.MEALS,
    DefaultCategory.TASKS, DefaultCategory.REMINDERS, DefaultCategory.IDEAS, DefaultCategory.INFO,
)

internal val HIGH_PRIORITY = Regex("\\b(?:urgente|importante|cuanto antes|ya mismo|sin falta|examen)\\b")
internal val LOW_PRIORITY = Regex("\\b(?:algun dia|cuando pueda|si hay tiempo|quizas|tal vez)\\b")

internal fun priorityOf(normalized: String): Priority = when {
    HIGH_PRIORITY.containsMatchIn(normalized) -> Priority.HIGH
    LOW_PRIORITY.containsMatchIn(normalized) -> Priority.LOW
    else -> Priority.NORMAL
}

/** Spoken prefixes removed when building the short title. */
internal val SPOKEN_FILLERS = listOf(
    Regex("^(?:por favor\\s+)?(?:recuerdame|avisame|anota|apunta|guarda)(?:\\s+que)?\\s+"),
    Regex("^(?:tengo|tenemos) que\\s+"),
    Regex("^(?:se me ocurrio(?: una idea)?|una idea|idea)\\s*[:,]?\\s*"),
    Regex("^(?:que|de)\\s+"),
)

/** Dangling connectors left at the end of a title after removing the date phrase. */
internal val TRAILING_CONNECTORS = Regex("(\\s+(?:del|de|el|la|los|las|para|a|al|en|este|esta|hoy|antes|despues|por))+$")
