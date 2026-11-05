package com.flux.mobile.feature.notes

/** What a row can do; [onInOneHour] and [onReschedule] are offered only for overdue notes. */
class NoteRowActions(
    val onToggle: () -> Unit,
    val onOpen: () -> Unit,
    val onDelete: () -> Unit,
    val onInOneHour: (() -> Unit)? = null,
    val onReschedule: (() -> Unit)? = null,
)
