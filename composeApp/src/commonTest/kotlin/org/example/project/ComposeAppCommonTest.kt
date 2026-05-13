package org.example.project

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull

class ComposeAppCommonTest {

    @Test
    fun `basic arithmetic test`() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun `NoteEntity default isFavorite is false`() {
        val note = createTestNote()
        assertFalse(note.isFavorite)
    }

    @Test
    fun `NoteEntity with custom values`() {
        val note = createTestNote(title = "Test", content = "Content", isFavorite = true)
        assertEquals("Test", note.title)
        assertTrue(note.isFavorite)
    }

    private fun createTestNote(
        id: Long = 1L,
        title: String = "Test Note",
        content: String = "Test Content",
        isFavorite: Boolean = false,
        createdAt: Long = System.currentTimeMillis()
    ) = org.example.project.data.NoteEntity(id, title, content, isFavorite, createdAt)
}