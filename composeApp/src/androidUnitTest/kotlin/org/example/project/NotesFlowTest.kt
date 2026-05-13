package org.example.project

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.project.data.NoteEntity
import org.example.project.data.NoteRepository
import org.example.project.data.SettingsRepository
import org.example.project.data.SortOrder
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class NotesFlowTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepo: NoteRepository
    private lateinit var mockSettingsRepo: SettingsRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepo = mockk(relaxed = true)
        mockSettingsRepo = mockk(relaxed = true)
        coEvery { mockSettingsRepo.sortOrderFlow } returns flowOf(SortOrder.NEWEST)
        coEvery { mockSettingsRepo.isDarkModeFlow } returns flowOf(false)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Flow test 1: Notes flow emit data dengan benar
    @Test
    fun `displayedNotes flow emits notes correctly`() = runTest {
        val note1 = NoteEntity(1L, "First", "Content 1", false, 1000L)
        val note2 = NoteEntity(2L, "Second", "Content 2", false, 2000L)
        coEvery { mockRepo.allNotes } returns flowOf(listOf(note1, note2))

        val vm = NotesViewModel(mockRepo, mockSettingsRepo)

        vm.displayedNotes.test {
            skipItems(1)

            val notes = awaitItem()
            assertEquals(2, notes.size)
            assertEquals("Second", notes[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Flow test 2: searchQuery flow berubah saat setSearchQuery dipanggil
    @Test
    fun `searchQuery flow updates when setSearchQuery called`() = runTest {
        coEvery { mockRepo.allNotes } returns flowOf(emptyList())

        val vm = NotesViewModel(mockRepo, mockSettingsRepo)

        vm.searchQuery.test {
            assertEquals("", awaitItem()) // initial state
            vm.setSearchQuery("hello")
            assertEquals("hello", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}