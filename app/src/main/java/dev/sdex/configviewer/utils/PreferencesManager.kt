package dev.sdex.configviewer.utils

import android.content.Context
import androidx.core.content.edit

class PreferencesManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(
        "prefs",
        Context.MODE_PRIVATE
    )

    fun write(key: String, value: String) {
        sharedPreferences.edit {
            putString(key, value)
        }
    }

    fun read(key: String, defaultValue: String? = ""): String =
        sharedPreferences.getString(key, defaultValue) ?: ""

    fun remove(key: String) {
        sharedPreferences.edit {
            remove(key)
        }
    }

    fun clear() {
        sharedPreferences.edit {
            clear()
        }
    }

    companion object {

        const val CURRENT_FILE = "current_file"
    }
}