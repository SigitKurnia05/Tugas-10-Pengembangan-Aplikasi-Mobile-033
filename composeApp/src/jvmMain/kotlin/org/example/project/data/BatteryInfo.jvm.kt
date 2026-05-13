package org.example.project.data

// ═══════════════════════════════════════════════
// ACTUAL CLASS — BatteryInfo (Desktop/JVM)
// Desktop tidak punya battery API langsung —
// return nilai default yang masuk akal
// ═══════════════════════════════════════════════
actual class BatteryInfo {

    actual fun getBatteryLevel(): Int = 100

    actual fun isCharging(): Boolean = true

    actual fun getBatteryStatus(): String = "Desktop — Terhubung ke daya ⚡"
}