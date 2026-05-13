package org.example.project.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.example.project.db.NotesDatabase


data class NoteEntity(
    val id: Long,
    val title: String,
    val content: String,
    val isFavorite: Boolean = false,
    val createdAt: Long
)


enum class SortOrder(val label: String) {
    NEWEST("Terbaru"),
    OLDEST("Terlama"),
    A_TO_Z("A → Z"),
    Z_TO_A("Z → A")
}


open class NoteRepository(database: NotesDatabase) {

    private val queries = database.noteQueries


    open val allNotes: Flow<List<NoteEntity>> = queries.selectAll()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { list ->
            list.map { dbNote ->
                NoteEntity(
                    id = dbNote.id,
                    title = dbNote.title,
                    content = dbNote.content,
                    isFavorite = dbNote.is_favorite == 1L,
                    createdAt = dbNote.created_at
                )
            }
        }

    suspend fun getNoteById(id: Long): NoteEntity? {
        return withContext(Dispatchers.IO) {
            val dbNote = queries.selectById(id).executeAsOneOrNull()
            if (dbNote != null) {
                NoteEntity(
                    id = dbNote.id,
                    title = dbNote.title,
                    content = dbNote.content,
                    isFavorite = dbNote.is_favorite == 1L,
                    createdAt = dbNote.created_at
                )
            } else {
                null
            }
        }
    }


    open suspend fun insertNote(title: String, content: String) {
        withContext(Dispatchers.IO) {
            queries.insert(
                title = title.trim(),
                content = content.trim(),
                is_favorite = 0L,
                created_at = System.currentTimeMillis()
            )
        }
    }


    suspend fun updateNote(id: Long, title: String, content: String) {
        withContext(Dispatchers.IO) {
            queries.update(
                title = title.trim(),
                content = content.trim(),
                id = id
            )
        }
    }

    suspend fun toggleFavorite(id: Long, currentStatus: Boolean) {
        withContext(Dispatchers.IO) {
            val newStatus = if (currentStatus) 0L else 1L
            queries.toggleFavorite(is_favorite = newStatus, id = id)
        }
    }


    open suspend fun deleteNote(id: Long) {
        withContext(Dispatchers.IO) {
            queries.delete(id)
        }
    }

    suspend fun deleteAllNotes() {
        withContext(Dispatchers.IO) {
            queries.deleteAll()
        }
    }
}