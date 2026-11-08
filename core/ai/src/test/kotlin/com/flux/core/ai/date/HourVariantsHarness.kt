package com.flux.core.ai.date

import com.flux.core.ai.local.LocalNoteClassifier
import com.flux.core.domain.port.Clock
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.domain.repository.ProfileRepository
import com.flux.core.domain.usecase.AskReminderTimeUseCase
import com.flux.core.domain.usecase.ClassifyNoteUseCase
import com.flux.core.model.Category
import com.flux.core.model.Classification
import com.flux.core.model.DefaultCategory
import com.flux.core.model.FluxResult
import com.flux.core.model.UserProfile
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.flowOf

/** Classifier wired with a fixed clock, the default categories and an optional profile. */
abstract class HourVariantsHarness {
    protected val zone = ZoneId.of("America/Bogota")

    private class FixedClock(private val at: ZonedDateTime) : Clock {
        override fun now() = at.toInstant().toEpochMilli()
        override fun zone(): ZoneId = at.zone
    }

    private class Categories : CategoryRepository {
        override fun observeCategories() = flowOf(DefaultCategory.all())
        override suspend fun getCategories(): List<Category> = DefaultCategory.all()
        override suspend fun upsert(category: Category) = Unit
        override suspend fun delete(id: String) = Unit
        override suspend fun ensureDefaults() = Unit
    }

    private class Profiles(private val profile: UserProfile?) : ProfileRepository {
        override fun observeProfile() = flowOf(profile)
        override suspend fun getProfile() = profile
        override suspend fun saveProfile(profile: UserProfile) = Unit
        override suspend fun clear() = Unit
    }

    /** Thursday 5 Nov 2026, 10:00. */
    protected val thursday = at(2026, 11, 5, 10)

    protected fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0) =
        ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone)

    protected suspend fun classify(text: String, now: ZonedDateTime = thursday, profile: UserProfile? = null): Classification {
        val clock = FixedClock(now)
        val ask = AskReminderTimeUseCase(SpanishScheduleGapDetector(), clock)
        val useCase = ClassifyNoteUseCase(listOf(LocalNoteClassifier()), Categories(), Profiles(profile), clock, ask)
        return (useCase(text) as FluxResult.Success).value
    }

    protected suspend fun due(text: String, now: ZonedDateTime = thursday) =
        classify(text, now).dueAt?.let { java.time.Instant.ofEpochMilli(it).atZone(zone) }
}
