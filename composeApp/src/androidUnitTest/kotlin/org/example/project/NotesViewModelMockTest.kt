package org.example.project

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.test.assertTrue
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelMockTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepo: NoteRepository
    private lateinit var mockSettingsRepo: SettingsRepository
    private lateinit var viewModel: NotesViewModel

    private val testNote = NoteEntity(
        id = 1L,
        title = "Test Note",
        content = "Test Content",
        isFavorite = false,
        createdAt = System.currentTimeMillis()
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepo = mockk(relaxed = true)
        mockSettingsRepo = mockk(relaxed = true)

        coEvery { mockRepo.allNotes } returns flowOf(listOf(testNote))
        coEvery { mockSettingsRepo.sortOrderFlow } returns flowOf(SortOrder.NEWEST)
        coEvery { mockSettingsRepo.isDarkModeFlow } returns flowOf(false)

        viewModel = NotesViewModel(mockRepo, mockSettingsRepo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Test case 1: Initial state menampilkan notes
    @Test
    fun `initial state shows notes from repository`() = runTest {
        viewModel.displayedNotes.test {
            skipItems(1) // FIX: Lewati initial state
            val notes = awaitItem()
            assertEquals(1, notes.size)
            assertEquals("Test Note", notes[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Test case 2: Search query memfilter notes
    @Test
    fun `search query filters notes by title`() = runTest {
        viewModel.setSearchQuery("Test")
        advanceUntilIdle()

        viewModel.displayedNotes.test {
            skipItems(1) // FIX: Lewati initial state
            val notes = awaitItem()
            assertTrue(notes.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Test case 3: Search yang tidak match menghasilkan list kosong
    @Test
    fun `search with no match returns empty list`() = runTest {
        advanceUntilIdle()

        viewModel.setSearchQuery("XYZ_NOT_EXIST")
        advanceUntilIdle()

        viewModel.displayedNotes.test {
            val notes = awaitItem()
            assertTrue(notes.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Test case 4: addNote memanggil repository
    @Test
    fun `addNote calls repository insertNote`() = runTest {
        coEvery { mockRepo.insertNote(any(), any()) } just Runs

        viewModel.addNote("New Note", "New Content")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockRepo.insertNote("New Note", "New Content") }
    }

    // Test case 5: deleteNote memanggil repository
    @Test
    fun `deleteNote calls repository deleteNote`() = runTest {
        coEvery { mockRepo.deleteNote(any()) } just Runs

        viewModel.deleteNote(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockRepo.deleteNote(1L) }
    }

    // Test case 6: toggleFavorite memanggil repository
    @Test
    fun `toggleFavorite calls repository toggleFavorite`() = runTest {
        coEvery { mockRepo.toggleFavorite(any(), any()) } just Runs

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.allNotes.collect {}
        }

        advanceUntilIdle()

        viewModel.toggleFavorite(1L)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { mockRepo.toggleFavorite(1L, true) }
    }
}