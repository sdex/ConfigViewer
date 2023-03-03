package dev.sdex.configviewer.model

enum class SettingsFile(val file: String) {
    CONFIG("config"), GLOBAL("global"), SECURE("secure"), SYSTEM("system")
}