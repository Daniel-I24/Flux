package com.flux.core.model

import kotlinx.serialization.Serializable

/**
 * Default categories have a [defaultKey] (their name comes from strings.xml unless
 * the user renamed them via [customName]); user categories only have [customName].
 */
@Serializable
data class Category(
    val id: String,
    val defaultKey: DefaultCategory? = null,
    val customName: String? = null,
    val colorArgb: Long,
    val icon: CategoryIcon,
    val sound: SoundRef? = null,
    val position: Int = 0,
) {
    val isDefault: Boolean get() = defaultKey != null

    companion object {
        const val MAX_NAME_LENGTH = 30
    }
}

@Serializable
enum class DefaultCategory(val id: String, val icon: CategoryIcon, val colorArgb: Long) {
    TASKS("tasks", CategoryIcon.TASK, 0xFF3B82F6),
    HOMEWORK("homework", CategoryIcon.SCHOOL, 0xFF8B5CF6),
    IDEAS("ideas", CategoryIcon.IDEA, 0xFFF59E0B),
    MEALS("meals", CategoryIcon.MEAL, 0xFFF97316),
    HEALTH("health", CategoryIcon.HEALTH, 0xFFEF4444),
    SHOPPING("shopping", CategoryIcon.SHOPPING, 0xFF14B8A6),
    REMINDERS("reminders", CategoryIcon.ALARM, 0xFF10B981),
    INFO("info", CategoryIcon.INFO, 0xFF64748B),
    ;

    fun toCategory(): Category = Category(id = id, defaultKey = this, colorArgb = colorArgb, icon = icon, position = ordinal)

    companion object {
        fun fromId(id: String): DefaultCategory? = entries.firstOrNull { it.id == id }
        fun all(): List<Category> = entries.map { it.toCategory() }
    }
}

@Serializable
enum class CategoryIcon {
    TASK, SCHOOL, IDEA, MEAL, HEALTH, SHOPPING, ALARM, INFO, WORK, HOME, SPORT, MONEY, TRAVEL, STAR, HEART, MUSIC
}
