package org.example.project.di

import com.russhwolf.settings.PreferencesSettings
import org.example.project.data.DatabaseDriverFactory
import org.example.project.data.DeviceInfo
import org.example.project.data.SettingsRepository
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.util.prefs.Preferences

// ═══════════════════════════════════════════════
// PLATFORM MODULE — Desktop/JVM specific
// DatabaseDriverFactory untuk Desktop tidak butuh Context
// ═══════════════════════════════════════════════
actual val platformModule: Module = module {
    // DatabaseDriverFactory Desktop — pakai SQLite driver JVM
    single { DatabaseDriverFactory() }

    // Settings — pakai Java Preferences (sudah ada di main.kt sebelumnya)
    single {
        val prefs = Preferences.userRoot().node("org.example.project")
        PreferencesSettings(prefs)
    }

    // Override SettingsRepository dengan Settings yang benar
    single { SettingsRepository(get()) }

    // ✅ DeviceInfo — actual Desktop
    single { DeviceInfo() }

    // ✅ NetworkMonitor — actual Desktop
    single { NetworkMonitor() }

    // ✅ BatteryInfo — actual Desktop (bonus)
    single { BatteryInfo() }
}

// ═══════════════════════════════════════════════
// initKoin — dipanggil dari main.kt Desktop
// ═══════════════════════════════════════════════
actual fun initKoin(): KoinApplication {
    return koinApplication {
        modules(commonModule, platformModule)
    }
}