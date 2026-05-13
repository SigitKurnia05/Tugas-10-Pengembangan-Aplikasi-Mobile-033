package org.example.project

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.graphics.Color
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.example.project.data.NoteEntity
import org.example.project.data.SortOrder
import org.example.project.screens.NotesScreen
import org.example.project.screens.TestTags
import org.junit.Rule
import org.junit.Test

class NotesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockVm = mockk<NotesViewModel>(relaxed = true)
    private val mockSettingsVm = mockk<SettingsViewModel>(relaxed = true)

    private val testNote = NoteEntity(
        id = 1L,
        title = "Test Note",
        content = "Test Content",
        isFavorite = false,
        createdAt = System.currentTimeMillis()
    )

    // UI Test 1: Search input tampil dan bisa menerima input
    @Test
    fun searchInput_isDisplayed() {
        coEvery { mockVm.displayedNotes } returns flowOf(emptyList())
        coEvery { mockVm.searchQuery } returns flowOf("")
        coEvery { mockSettingsVm.sortOrder } returns flowOf(SortOrder.NEWEST)

        composeTestRule.setContent {
            NotesScreen(
                onNoteClick = {},
                onAddClick = {},
                isDarkMode = false,
                vm = mockVm,
                settingsVm = mockSettingsVm
            )
        }

        composeTestRule
            .onNodeWithTag(TestTags.SEARCH_INPUT)
            .assertIsDisplayed()
    }

    // UI Test 2: FAB tampil dan bisa diklik
    @Test
    fun fab_isDisplayedAndClickable() {
        coEvery { mockVm.displayedNotes } returns flowOf(emptyList())
        coEvery { mockVm.searchQuery } returns flowOf("")
        coEvery { mockSettingsVm.sortOrder } returns flowOf(SortOrder.NEWEST)

        composeTestRule.setContent {
            NotesScreen(
                onNoteClick = {},
                onAddClick = {},
                isDarkMode = false,
                vm = mockVm,
                settingsVm = mockSettingsVm
            )
        }

        composeTestRule
            .onNodeWithTag(TestTags.ADD_FAB)
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    // UI Test 3: Empty state tampil saat tidak ada notes
    @Test
    fun emptyState_showsWhenNoNotes() {
        coEvery { mockVm.displayedNotes } returns flowOf(emptyList())
        coEvery { mockVm.searchQuery } returns flowOf("")
        coEvery { mockSettingsVm.sortOrder } returns flowOf(SortOrder.NEWEST)

        composeTestRule.setContent {
            NotesScreen(
                onNoteClick = {},
                onAddClick = {},
                isDarkMode = false,
                vm = mockVm,
                settingsVm = mockSettingsVm
            )
        }

        composeTestRule
            .onNodeWithTag(TestTags.EMPTY_STATE)
            .assertIsDisplayed()
    }
}