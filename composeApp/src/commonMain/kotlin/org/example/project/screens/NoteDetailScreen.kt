package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.project.NotesViewModel
import org.example.project.SettingsViewModel
import org.example.project.components.*
import org.example.project.data.FontSize

@Composable
fun NoteDetailScreen(
    noteId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onSummarize: (String) -> Unit, // Tambahan parameter baru
    isDarkMode: Boolean,
    vm: NotesViewModel,
    settingsVm: SettingsViewModel
) {
    val notes    by vm.allNotes.collectAsStateWithLifecycle()
    val settings by settingsVm.settings.collectAsStateWithLifecycle()
    val note     = notes.find { it.id == noteId }

    val backgroundColor = if (isDarkMode) DarkBackground else BackgroundGray
    val cardColor       = if (isDarkMode) DarkCard       else CardWhite
    val textColor       = if (isDarkMode) DarkTextLight  else DarkText
    val subTextColor    = if (isDarkMode) DarkSubText    else SubText
    val accentColor     = if (isDarkMode) DarkBlueAccent else PrimaryBlue

    val contentFontSize = when (settings.fontSize) {
        FontSize.SMALL  -> 13.sp
        FontSize.MEDIUM -> 15.sp
        FontSize.LARGE  -> 18.sp
    }
    val contentLineHeight = when (settings.fontSize) {
        FontSize.SMALL  -> 20.sp
        FontSize.MEDIUM -> 24.sp
        FontSize.LARGE  -> 28.sp
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Text("←", fontSize = 22.sp, color = accentColor)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("Detail Catatan", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textColor)
            }
            if (note != null) {
                IconButton(onClick = { vm.toggleFavorite(noteId) }) {
                    Icon(
                        imageVector        = if (note.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Bintang",
                        tint               = if (note.isFavorite) Color(0xFFFFB300) else subTextColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (note == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Catatan tidak ditemukan", color = subTextColor)
            }
        } else {
            Card(
                modifier  = Modifier.fillMaxWidth().weight(1f),
                shape     = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors    = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(note.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(thickness = 0.8.dp, color = subTextColor.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text       = note.content.ifBlank { "Tidak ada isi catatan." },
                        fontSize   = contentFontSize,
                        color      = if (note.content.isBlank()) subTextColor else textColor,
                        lineHeight = contentLineHeight
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick   = { onEdit(noteId) },
                modifier  = Modifier.fillMaxWidth().height(50.dp),
                shape     = RoundedCornerShape(14.dp),
                colors    = ButtonDefaults.buttonColors(containerColor = accentColor),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("✏️  Edit Catatan", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    // Menggunakan callback parameter onSummarize alih-alih navController langsung
                    onSummarize(java.net.URLEncoder.encode(note.content, "UTF-8"))
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, accentColor)
            ) {
                Text("✨  Ringkas dengan Gemini AI", fontSize = 15.sp, color = accentColor)
            }
        }
    }
}