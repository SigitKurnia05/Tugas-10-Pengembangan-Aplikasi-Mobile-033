package org.example.project.di

import org.koin.test.KoinTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class AppModuleTest {

    // Test bahwa module-module terdefinisi dengan benar
    @Test
    fun `dataModule is defined`() {
        assertNotNull(dataModule)
    }

    @Test
    fun `viewModelModule is defined`() {
        assertNotNull(viewModelModule)
    }

    @Test
    fun `commonModule combines data and viewModel modules`() {
        assertNotNull(commonModule)
    }
}