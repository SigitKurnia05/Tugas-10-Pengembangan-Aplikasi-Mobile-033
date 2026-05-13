package org.example.project

import android.app.Application
import org.example.project.di.GEMINI_API_KEY
import org.example.project.di.commonModule
import org.example.project.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MyApplication)
            modules(commonModule + platformModule)
        }
    }
}