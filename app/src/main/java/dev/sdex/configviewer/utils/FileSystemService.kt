package dev.sdex.configviewer.utils

import android.content.Intent
import android.os.IBinder
import com.topjohnwu.superuser.ipc.RootService
import com.topjohnwu.superuser.nio.FileSystemManager

class FileSystemService : RootService() {

    override fun onBind(intent: Intent): IBinder {
        return FileSystemManager.getService()
    }
}