package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.NotesViewModel
import org.example.project.components.*

@Composable
fun FavoritesScreen(
    onNoteClick: (Long) -> Unit,
    isDarkMode: Boolean,
    vm: NotesViewModel
) {
    val notes by vm.allNotes.collectAsStateWithLifecycle()
    val favorites  = notes.filter { it.isFavorite }

    val backgroundColor = if (isDarkMode) DarkBackground else BackgroundGray
    val cardColor       = if (isDarkMode) DarkCard       else CardWhite
    val textColor       = if (isDarkMode) DarkTextLight  else DarkText
    val subTextColor    = if (isDarkMode) DarkSubText    else SubText
    val accentColor     = if (isDarkMode) DarkBlueAccent else PrimaryBlue

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "${favorites.size} catatan berbintang",
            fontSize = 13.sp,
            color = subTextColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⭐", fontSize = 48.sp, color = subTextColor)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum ada catatan berbintang",
                        fontSize = 16.sp, color = subTextColor, fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tekan ikon ⭐ pada catatan untuk menambahkan",
                        fontSize = 13.sp, color = subTextColor
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(favorites, key = { it.id }) { note ->
                    Card(
                        modifier  = Modifier.fillMaxWidth().clickable { onNoteClick(note.id) },
                        shape     = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        colors    = CardDefaults.cardColors(containerColor = cardColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = note.title, fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold, color = textColor,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = note.content, fontSize = 13.sp, color = subTextColor,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}