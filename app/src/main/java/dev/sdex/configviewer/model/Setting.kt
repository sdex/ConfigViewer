package dev.sdex.configviewer.model

data class Setting(val name: String, val value: String?) {

    fun getValueSafe(): String = value ?: "null"
}