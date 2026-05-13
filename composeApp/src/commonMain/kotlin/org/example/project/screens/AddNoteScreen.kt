package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.NotesViewModel
import org.example.project.components.*

@Composable
fun AddNoteScreen(
    onBack: () -> Unit,
    isDarkMode: Boolean,
    vm: NotesViewModel
) {
    val backgroundColor = if (isDarkMode) DarkBackground else BackgroundGray
    val cardColor       = if (isDarkMode) DarkCard       else CardWhite
    val textColor       = if (isDarkMode) DarkTextLight  else DarkText
    val subTextColor    = if (isDarkMode) DarkSubText    else SubText
    val accentColor     = if (isDarkMode) DarkBlueAccent else PrimaryBlue

    var title   by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Text("←", fontSize = 22.sp, color = accentColor)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "Catatan Baru",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul", color = subTextColor) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = subTextColor.copy(alpha = 0.3f),
                        focusedLabelColor = accentColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedContainerColor = backgroundColor,
                        unfocusedContainerColor = backgroundColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Isi catatan...", color = subTextColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = subTextColor.copy(alpha = 0.3f),
                        focusedLabelColor = accentColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedContainerColor = backgroundColor,
                        unfocusedContainerColor = backgroundColor
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (title.isNotBlank()) {
                    vm.addNote(title.trim(), content.trim())
                    onBack()
                }
            },
            enabled = title.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                disabledContainerColor = subTextColor.copy(alpha = 0.3f)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                "💾  Simpan Catatan",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}