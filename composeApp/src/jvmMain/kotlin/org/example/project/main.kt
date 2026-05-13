package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.project.data.NoteRepository
import org.example.project.data.SettingsRepository
import org.example.project.di.initKoin
import org.koin.core.context.stopKoin
import org.koin.java.KoinJavaComponent.getKoin

// ═══════════════════════════════════════════════
// DESKTOP ENTRY POINT — refactored pakai Koin
// ═══════════════════════════════════════════════
fun main() = application {

    // 1. Inisialisasi Koin sebelum UI
    val koin = initKoin()

    // 2. Ambil dependencies dari Koin graph
    val noteRepo: NoteRepository = koin.koin.get()
    val settingsRepo: SettingsRepository = koin.koin.get()

    Window(
        onCloseRequest = {
            stopKoin() // bersihkan Koin saat window ditutup
            exitApplication()
        },
        title = "Catatan Saya"
    ) {
        App(
            repository = noteRepo,
            settingsRepository = settingsRepo
        )
    }
}