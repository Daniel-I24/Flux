package com.flux.mobile.feature.notes

/** A category asked by another tab (Inicio); [token] changes on every request so it is applied once. */
data class NotesRequest(val categoryId: String?, val token: Long)
