package org.example.project.di

import org.example.project.AiViewModel
import org.example.project.NotesViewModel
import org.example.project.ProfileViewModel
import org.example.project.SettingsViewModel
import org.example.project.data.AiRepository
import org.example.project.data.AiRepositoryImpl
import org.example.project.data.DatabaseDriverFactory
import org.example.project.data.GeminiService
import org.example.project.data.NoteRepository
import org.example.project.data.SettingsRepository
import org.example.project.db.NotesDatabase
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.dsl.module

lateinit var GEMINI_API_KEY: String

// Module 1: Data Layer
val dataModule = module {
    single { NotesDatabase(get<DatabaseDriverFactory>().createDriver()) }
    single { NoteRepository(get()) }
    single { SettingsRepository(get()) }
    single { GeminiService(apiKey = GEMINI_API_KEY) }
    single<AiRepository> { AiRepositoryImpl(get()) }
}

// Module 2: ViewModel Layer
val viewModelModule = module {
    viewModelOf(::NotesViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::AiViewModel)
}

val commonModule: List<Module> = listOf(dataModule, viewModelModule)