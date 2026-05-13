package org.example.project.data

import android.os.Build

actual class DeviceInfo {
    actual fun getDeviceName(): String =
        "${Build.MANUFACTURER} ${Build.MODEL}"

    actual fun getOsVersion(): String =
        "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

    actual fun getAppVersion(): String = "1.0.0"
}