package org.example.project

import kotlinx.coroutines.test.runTest
import org.example.project.data.NoteEntity
import org.example.project.data.SortOrder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Unit test untuk logika NoteEntity dan SortOrder
 * (Repository test penuh ada di androidUnitTest dengan MockK)
 */
class NoteEntityTest {

    private lateinit var testNote: NoteEntity

    @BeforeTest
    fun setup() {
        testNote = NoteEntity(
            id = 1L,
            title = "Test Note",
            content = "Test Content",
            isFavorite = false,
            createdAt = 1000L
        )
    }

    // Test case 1: NoteEntity dibuat dengan benar
    @Test
    fun `note entity is created with correct values`() {
        assertEquals(1L, testNote.id)
        assertEquals("Test Note", testNote.title)
        assertEquals("Test Content", testNote.content)
        assertFalse(testNote.isFavorite)
        assertEquals(1000L, testNote.createdAt)
    }

    // Test case 2: Default isFavorite adalah false
    @Test
    fun `note default isFavorite is false`() {
        val note = NoteEntity(id = 2L, title = "A", content = "B", createdAt = 0L)
        assertFalse(note.isFavorite)
    }

    // Test case 3: Note bisa di-copy dengan nilai baru
    @Test
    fun `note can be toggled to favorite`() {
        val favorited = testNote.copy(isFavorite = true)
        assertTrue(favorited.isFavorite)
        assertFalse(testNote.isFavorite) // original tidak berubah
    }

    // Test case 4: SortOrder memiliki label yang benar
    @Test
    fun `sort order labels are correct`() {
        assertEquals("Terbaru", SortOrder.NEWEST.label)
        assertEquals("Terlama", SortOrder.OLDEST.label)
        assertEquals("A → Z", SortOrder.A_TO_Z.label)
        assertEquals("Z → A", SortOrder.Z_TO_A.label)
    }

    // Test case 5: List notes bisa difilter berdasarkan isFavorite
    @Test
    fun `filtering favorites works correctly`() {
        val notes = listOf(
            NoteEntity(1L, "A", "", true, 0L),
            NoteEntity(2L, "B", "", false, 0L),
            NoteEntity(3L, "C", "", true, 0L),
        )
        val favorites = notes.filter { it.isFavorite }
        assertEquals(2, favorites.size)
        assertTrue(favorites.all { it.isFavorite })
    }

    // Test case 6: List notes bisa dicari berdasarkan title
    @Test
    fun `search by title works correctly`() {
        val notes = listOf(
            NoteEntity(1L, "Shopping List", "", false, 0L),
            NoteEntity(2L, "Meeting Notes", "", false, 0L),
            NoteEntity(3L, "shopping ideas", "", false, 0L),
        )
        val result = notes.filter {
            it.title.contains("shopping", ignoreCase = true)
        }
        assertEquals(2, result.size)
    }

    // Test case 7: Sorting NEWEST benar
    @Test
    fun `sort newest first works correctly`() {
        val notes = listOf(
            NoteEntity(1L, "Old", "", false, 1000L),
            NoteEntity(2L, "New", "", false, 3000L),
            NoteEntity(3L, "Mid", "", false, 2000L),
        )
        val sorted = notes.sortedByDescending { it.createdAt }
        assertEquals("New", sorted[0].title)
        assertEquals("Mid", sorted[1].title)
        assertEquals("Old", sorted[2].title)
    }

    // Test case 8: Sorting A_TO_Z benar
    @Test
    fun `sort A to Z works correctly`() {
        val notes = listOf(
            NoteEntity(1L, "Zebra", "", false, 0L),
            NoteEntity(2L, "Apple", "", false, 0L),
            NoteEntity(3L, "Mango", "", false, 0L),
        )
        val sorted = notes.sortedBy { it.title.lowercase() }
        assertEquals("Apple", sorted[0].title)
        assertEquals("Mango", sorted[1].title)
        assertEquals("Zebra", sorted[2].title)
    }
}