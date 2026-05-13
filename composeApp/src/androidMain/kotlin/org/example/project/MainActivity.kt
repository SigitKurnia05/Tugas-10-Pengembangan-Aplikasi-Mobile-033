package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.example.project.data.NoteRepository
import org.example.project.data.SettingsRepository
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {


    private val noteRepo: NoteRepository by inject()
    private val settingsRepo: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(
                repository = noteRepo,
                settingsRepository = settingsRepo
            )
        }
    }
}