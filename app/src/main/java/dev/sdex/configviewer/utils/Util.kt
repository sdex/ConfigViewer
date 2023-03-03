package dev.sdex.configviewer.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.util.Xml
import android.widget.Toast
import com.topjohnwu.superuser.nio.FileSystemManager
import dev.sdex.configviewer.model.SettingsFile
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

fun getSettingsFile(
    remoteFS: FileSystemManager,
    settingsFile: SettingsFile
): String {
    val file = remoteFS.getFile("/data/system/users/0/settings_${settingsFile.file}.xml")
    val byteArrayOutputStream = ByteArrayOutputStream()
    val input: XmlPullParser = Xml.newBinaryPullParser()
    val output: XmlSerializer = Xml.newSerializer()
    file.newInputStream().use { inputStream ->
        byteArrayOutputStream.use { os ->
            input.setInput(inputStream, StandardCharsets.UTF_8.name())
            output.setOutput(os, StandardCharsets.UTF_8.name())
            output.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
            Xml.copy(input, output)
            output.flush()
        }
    }
    return byteArrayOutputStream.toString()
}

fun copyText(context: Context, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.primaryClip = ClipData.newPlainText("text", value)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    }
}