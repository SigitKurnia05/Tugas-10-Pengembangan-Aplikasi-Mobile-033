package org.example.project.data

// ═══════════════════════════════════════════════
// ACTUAL CLASS — DeviceInfo (Desktop/JVM)
// Mengakses System properties untuk info perangkat
// ═══════════════════════════════════════════════
actual class DeviceInfo {
    actual fun getDeviceName(): String =
        System.getProperty("os.name") ?: "Desktop"

    actual fun getOsVersion(): String =
        "${System.getProperty("os.name")} ${System.getProperty("os.version")}"

    actual fun getAppVersion(): String = "1.0.0"
}