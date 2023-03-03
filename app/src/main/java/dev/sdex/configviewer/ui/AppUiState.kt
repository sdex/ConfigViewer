package dev.sdex.configviewer.ui

import dev.sdex.configviewer.model.Setting
import dev.sdex.configviewer.model.Settings
import dev.sdex.configviewer.model.SettingsFile

data class AppUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentSettingsFile: String = SettingsFile.CONFIG.file,
    val items: List<Settings> = emptyList(),
    val currentSetting: Setting? = null,
    val result: OperationResult? = null,
)

data class OperationResult(
    val message: String,
    val actionLabel: String? = null,
    val duration: OperationResultDuration = OperationResultDuration.Short,
    val action: (() -> Unit)? = null,
)

enum class OperationResultDuration {
    Short,
    Long,
    Indefinite
}
