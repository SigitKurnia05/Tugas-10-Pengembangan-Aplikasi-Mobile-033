package org.example.project

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.project.data.NoteEntity
import org.example.project.data.NoteRepository
import org.example.project.data.SettingsRepository
import org.example.project.data.SortOrder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class FakeNoteRepository : NoteRepository(TODO("tidak dipakai di fake")) {
    private val _notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    override val allNotes: Flow<List<NoteEntity>> = _notes

    private val insertedNotes = mutableListOf<Pair<String, String>>()
    private val deletedIds = mutableListOf<Long>()

    override suspend fun insertNote(title: String, content: String) {
        val newNote = NoteEntity(
            id = (_notes.value.size + 1).toLong(),
            title = title,
            content = content,
            isFavorite = false,
            createdAt = System.currentTimeMillis()
        )
        insertedNotes.add(title to content)
        _notes.value = _notes.value + newNote
    }

    override suspend fun deleteNote(id: Long) {
        deletedIds.add(id)
        _notes.value = _notes.value.filter { it.id != id }
    }

    fun getInsertedNotes() = insertedNotes.toList()
    fun getDeletedIds() = deletedIds.toList()
    fun setNotes(notes: List<NoteEntity>) { _notes.value = notes }
}