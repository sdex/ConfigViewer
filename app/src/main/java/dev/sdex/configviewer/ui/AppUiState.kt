package dev.sdex.configviewer.ui

import dev.sdex.configviewer.model.Setting
import dev.sdex.configviewer.model.Settings
import dev.sdex.configviewer.model.SettingsFile

data class AppUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val title: String = "",
    val currentSettingsFile: SettingsFile? = null,
    val items: List<Settings> = emptyList(),
    val currentSetting: Setting? = null,
)
