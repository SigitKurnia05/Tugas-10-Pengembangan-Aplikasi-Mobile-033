package org.example.project.data

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

actual class BatteryInfo : KoinComponent {

    private val context: Context get() = get()

    private fun getBatteryIntent(): Intent? =
        context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

    actual fun getBatteryLevel(): Int {
        val batteryManager =
            context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    actual fun isCharging(): Boolean {
        val status = getBatteryIntent()
            ?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    actual fun getBatteryStatus(): String {
        val level     = getBatteryLevel()
        val charging  = isCharging()
        val statusStr = if (charging) "Mengisi daya ⚡" else "Tidak mengisi daya"
        return "$level% — $statusStr"
    }
}