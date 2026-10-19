package com.flux.core.sync.protocol

/**
 * Remembers recently processed envelope ids so retries (WorkManager, reconnections)
 * never apply the same message twice. Bounded LRU to keep memory constant.
 */
class MessageDeduplicator(private val capacity: Int = DEFAULT_CAPACITY) {
    private val seen = object : LinkedHashMap<String, Unit>(capacity, LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Unit>?) = size > capacity
    }

    /** Returns true the first time [id] is seen. */
    @Synchronized
    fun markIfNew(id: String): Boolean {
        if (seen.containsKey(id)) return false
        seen[id] = Unit
        return true
    }

    private companion object {
        const val DEFAULT_CAPACITY = 500
        const val LOAD_FACTOR = 0.75f
    }
}
