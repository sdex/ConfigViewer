package dev.sdex.configviewer.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Xml
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import com.topjohnwu.superuser.nio.FileSystemManager
import dev.sdex.configviewer.ConfigViewerApp
import dev.sdex.configviewer.model.Setting
import dev.sdex.configviewer.model.Settings
import dev.sdex.configviewer.model.SettingsFile
import dev.sdex.configviewer.utils.FileSystemService
import dev.sdex.configviewer.utils.PreferencesManager
import dev.sdex.configviewer.utils.getSettingsFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import timber.log.Timber
import java.io.StringReader

class MainViewModel(
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState(isLoading = true))
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private var serviceConnection: ServiceConnection? = null
    private var fileSystemManager: FileSystemManager? = null

    init {
        Shell.getShell {
            val isAppGrantedRoot = Shell.isAppGrantedRoot()
            if (isAppGrantedRoot == true) {
                bindService()
            } else {
                _uiState.value = AppUiState(error = "Root permission is not granted")
            }
        }
    }

    override fun onCleared() {
        fileSystemManager = null
        if (serviceConnection != null) {
            RootService.unbind(serviceConnection!!)
        }
    }

    private fun bindService() {
        serviceConnection = object : ServiceConnection {

            override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
                Timber.d("onServiceConnected: $componentName")
                this@MainViewModel.fileSystemManager = FileSystemManager.getRemote(service)
                val currentFile = preferencesManager.read(
                    PreferencesManager.CURRENT_FILE,
                    SettingsFile.CONFIG.name
                )
                load(SettingsFile.valueOf(currentFile))
            }

            override fun onServiceDisconnected(name: ComponentName) {
                Timber.d("onServiceDisconnected: $name")
            }
        }
        RootService.bind(
            Intent(ConfigViewerApp.INSTANCE, FileSystemService::class.java),
            serviceConnection!!
        )
    }

    fun load(settingsFile: SettingsFile) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (fileSystemManager == null) {
                    return@launch
                }
                val remoteFS = fileSystemManager!!
                val file = getSettingsFile(remoteFS, settingsFile)
                val settings = parse(file)
                preferencesManager.write(PreferencesManager.CURRENT_FILE, settingsFile.name)
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        items = settings,
                        currentSettingsFile = settingsFile,
                        title = settingsFile.file,
                    )
                }
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.value = AppUiState(error = e.message)
            }
        }
    }

    private fun parse(content: String): List<Settings> {
        val parser: XmlPullParser = Xml.newPullParser()
        parser.setInput(StringReader(content))
        var eventType = parser.eventType
        val settings = mutableMapOf<String, MutableList<Setting>>()
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "setting") {
                val name = parser.getAttributeValue(null, "name")
                val value = parser.getAttributeValue(null, "value")
                val packageName = parser.getAttributeValue(null, "package")
                settings.getOrPut(packageName) { mutableListOf() }.add(
                    Setting(name, value)
                )
            }
            eventType = parser.next()
        }
        settings.forEach {
            it.value.sortBy { setting -> setting.name }
        }
        return settings.map {
            Settings(it.key, it.value)
        }.toList().sortedBy { it.packageName }
    }

    fun showSettingDetail(setting: Setting?) {
        _uiState.update { currentState ->
            currentState.copy(
                currentSetting = setting,
            )
        }
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {

            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = extras[APPLICATION_KEY] as ConfigViewerApp
                val preferencesManager = PreferencesManager(application)
                return MainViewModel(preferencesManager) as T
            }
        }
    }
}