package org.example.project.data

expect class BatteryInfo() {
    fun getBatteryLevel(): Int  // 0-100
    fun isCharging(): Boolean
    fun getBatteryStatus(): String // deskripsi lengkap untuk UI
}