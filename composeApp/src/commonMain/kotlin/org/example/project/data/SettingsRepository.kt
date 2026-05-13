package org.example.project.data

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class FontSize(val label: String) {
    SMALL("Kecil"),
    MEDIUM("Sedang"),
    LARGE("Besar")
}

class SettingsRepository(private val settings: Settings) {

    private val KEY_DARK_MODE  = "app_dark_mode"
    private val KEY_SORT_ORDER = "app_sort_order"
    private val KEY_HAS_SEEDED = "app_has_seeded"

    private val _isDarkMode = MutableStateFlow(settings.getBoolean(KEY_DARK_MODE, false))
    val isDarkModeFlow: Flow<Boolean> = _isDarkMode.asStateFlow()

    private val _sortOrder = MutableStateFlow(
        SortOrder.valueOf(settings.getString(KEY_SORT_ORDER, SortOrder.NEWEST.name))
    )
    val sortOrderFlow: Flow<SortOrder> = _sortOrder.asStateFlow()

    suspend fun setDarkMode(isDark: Boolean) {
        settings.putBoolean(KEY_DARK_MODE, isDark)
        _isDarkMode.value = isDark
    }

    suspend fun setSortOrder(order: SortOrder) {
        settings.putString(KEY_SORT_ORDER, order.name)
        _sortOrder.value = order
    }

    fun hasSeededNotes(): Boolean = settings.getBoolean(KEY_HAS_SEEDED, false)

    fun markNotesSeeded() { settings.putBoolean(KEY_HAS_SEEDED, true) }
}