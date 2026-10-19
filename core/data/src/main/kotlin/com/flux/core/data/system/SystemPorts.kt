package com.flux.core.data.system

import com.flux.core.domain.port.Clock
import com.flux.core.domain.port.IdGenerator
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

class SystemClock @Inject constructor() : Clock {
    override fun now(): Long = System.currentTimeMillis()
    override fun zone(): ZoneId = ZoneId.systemDefault()
}

class UuidIdGenerator @Inject constructor() : IdGenerator {
    override fun newId(): String = UUID.randomUUID().toString()
}
