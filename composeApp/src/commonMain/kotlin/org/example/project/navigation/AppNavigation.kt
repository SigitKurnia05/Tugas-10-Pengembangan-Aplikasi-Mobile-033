package org.example.project.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.ui.graphics.vector.ImageVector


sealed class Screen(val route: String) {
    // Bottom Nav
    object Notes     : Screen("notes")
    object Favorites : Screen("favorit")
    object Profile   : Screen("profile")
    object Settings  : Screen("pengaturan")

    // Notes flow
    object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: Long) = "note_detail/$noteId"
    }
    object AddNote : Screen("add_note")
    object EditNote : Screen("edit_note/{noteId}") {
        fun createRoute(noteId: Long) = "edit_note/$noteId"
    }

    object AiChat : Screen("ai_chat")
    object AiChatSummarize : Screen("ai_chat_summarize/{noteContent}") {
        fun createRoute(noteContent: String) = "ai_chat_summarize/$noteContent"
    }
}


sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Notes     : BottomNavItem(Screen.Notes.route,     Icons.Default.Home,         "Notes")
    object Favorites : BottomNavItem(Screen.Favorites.route, Icons.Default.Star,         "Favorit")
    object AiChat    : BottomNavItem(Screen.AiChat.route,    Icons.Default.AutoAwesome,  "AI")
    object Profile   : BottomNavItem(Screen.Profile.route,   Icons.Default.Person,       "Profile")
    object Settings  : BottomNavItem(Screen.Settings.route,  Icons.Default.Settings,     "Pengaturan")
}