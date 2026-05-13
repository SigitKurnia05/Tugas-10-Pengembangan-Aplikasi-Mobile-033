package org.example.project.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.project.NotesViewModel
import org.example.project.SettingsViewModel
import org.example.project.components.*
import org.example.project.data.NoteEntity
import org.example.project.data.SortOrder
import org.example.project.data.NetworkMonitor
import org.koin.compose.koinInject

// TestTags object untuk UI Testing
object TestTags {
    const val NOTES_LIST    = "notes_list"
    const val NOTE_ITEM     = "note_item"
    const val SEARCH_INPUT  = "search_input"
    const val ADD_FAB       = "add_fab"
    const val DELETE_BUTTON = "delete_button"
    const val EMPTY_STATE   = "empty_state"
}

@Composable
fun NotesScreen(
    onNoteClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    isDarkMode: Boolean,
    vm: NotesViewModel,
    settingsVm: SettingsViewModel
) {
    val displayedNotes by vm.displayedNotes.collectAsStateWithLifecycle()
    val searchQuery    by vm.searchQuery.collectAsStateWithLifecycle()
    val sortOrder      by settingsVm.sortOrder.collectAsStateWithLifecycle()

    val backgroundColor = if (isDarkMode) DarkBackground else BackgroundGray
    val cardColor       = if (isDarkMode) DarkCard       else CardWhite
    val textColor       = if (isDarkMode) DarkTextLight  else DarkText
    val subTextColor    = if (isDarkMode) DarkSubText    else SubText
    val accentColor     = if (isDarkMode) DarkBlueAccent else PrimaryBlue

    val networkMonitor: NetworkMonitor = koinInject()
    val isConnected by networkMonitor.observeConnectivity()
        .collectAsStateWithLifecycle(initialValue = true)

    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }

    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Hapus Catatan?") },
            text  = { Text("\"${note.title}\" akan dihapus permanen.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteNote(note.id)
                    noteToDelete = null
                }) {
                    Text("Hapus", color = Color(0xFFE57373), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Batal")
                }
            },
            containerColor = cardColor,
            titleContentColor = textColor,
            textContentColor = subTextColor
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Network Indicator
            NetworkStatusIndicator(isConnected = isConnected)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Search
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { vm.setSearchQuery(it) },
                    placeholder   = { Text("Cari catatan...", color = subTextColor) },
                    leadingIcon   = { Icon(Icons.Default.Search, null, tint = subTextColor) },
                    trailingIcon  = {
                        AnimatedVisibility(visible = searchQuery.isNotBlank()) {
                            IconButton(onClick = { vm.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, "Hapus", tint = subTextColor)
                            }
                        }
                    },
                    singleLine = true,
                    modifier   = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.SEARCH_INPUT),
                    shape      = RoundedCornerShape(14.dp),
                    colors     = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = accentColor,
                        unfocusedBorderColor    = subTextColor.copy(alpha = 0.3f),
                        focusedContainerColor   = cardColor,
                        unfocusedContainerColor = cardColor
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Sorting chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SortOrder.values().forEach { order ->
                        FilterChip(
                            selected = sortOrder == order,
                            onClick  = { settingsVm.setSortOrder(order) },
                            label    = { Text(order.label, fontSize = 12.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accentColor.copy(alpha = 0.2f),
                                selectedLabelColor     = accentColor
                            )
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                if (displayedNotes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(TestTags.EMPTY_STATE),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (searchQuery.isNotBlank()) "Tidak ada hasil."
                            else "Belum ada catatan.",
                            color = subTextColor
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.testTag(TestTags.NOTES_LIST),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(displayedNotes, key = { it.id }) { note ->
                            NoteCard(
                                note         = note,
                                cardColor    = cardColor,
                                textColor    = textColor,
                                subTextColor = subTextColor,
                                accentColor  = accentColor,
                                onClick      = { onNoteClick(note.id) },
                                onFavorite   = { vm.toggleFavorite(note.id) },
                                onDelete     = { noteToDelete = note }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick        = onAddClick,
            modifier       = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag(TestTags.ADD_FAB),
            containerColor = accentColor,
            contentColor   = Color.White
        ) {
            Icon(Icons.Default.Add, "Tambah")
        }
    }
}

@Composable
fun NetworkStatusIndicator(isConnected: Boolean) {
    AnimatedVisibility(
        visible = !isConnected,
        enter   = expandVertically(),
        exit    = shrinkVertically()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color    = Color(0xFFE53935)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CloudOff,
                    contentDescription = "Offline",
                    tint               = Color.White,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Tidak ada koneksi internet",
                    color      = Color.White,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}