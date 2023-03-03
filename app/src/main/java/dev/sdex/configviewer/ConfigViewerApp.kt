package dev.sdex.configviewer

import android.app.Application
import org.lsposed.hiddenapibypass.HiddenApiBypass
import timber.log.Timber

class ConfigViewerApp: Application() {

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        //if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        //}
        HiddenApiBypass.addHiddenApiExemptions("")
    }

    companion object {

        lateinit var INSTANCE: ConfigViewerApp
    }
}