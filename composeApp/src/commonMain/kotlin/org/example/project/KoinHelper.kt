package org.example.project.di

import org.koin.core.KoinApplication
import org.koin.core.module.Module

expect val platformModule: Module

expect fun initKoin(): KoinApplication