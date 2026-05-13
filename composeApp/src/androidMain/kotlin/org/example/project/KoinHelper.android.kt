package org.example.project.di

import android.content.Context
import androidx.preference.PreferenceManager
import com.russhwolf.settings.SharedPreferencesSettings
import org.example.project.BuildConfig
import org.example.project.data.BatteryInfo
import org.example.project.data.DatabaseDriverFactory
import org.example.project.data.DeviceInfo
import org.example.project.data.NetworkMonitor
import org.example.project.data.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseDriverFactory(get()) }

    single<com.russhwolf.settings.Settings> {
        val context: Context = get()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        SharedPreferencesSettings(prefs)
    }

    single { SettingsRepository(get()) }
    single { DeviceInfo() }
    single { NetworkMonitor() }
    single { BatteryInfo() }
}

actual fun initKoin(): KoinApplication {
    return koinApplication {
        modules(commonModule + platformModule)
    }
}

fun initKoin(context: Context): KoinApplication {
    GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY
    return koinApplication {
        androidContext(context)
        modules(commonModule + platformModule)
    }
}