package com.flux.core.ai.local

import com.flux.core.model.DefaultCategory

/** Each pattern hit adds [weight] to the score of [category]. */
internal data class KeywordRule(val category: DefaultCategory, val weight: Int, val patterns: List<Regex>)
