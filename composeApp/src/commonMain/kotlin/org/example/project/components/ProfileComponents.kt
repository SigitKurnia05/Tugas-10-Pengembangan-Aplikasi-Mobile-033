package org.example.project.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PrimaryBlue    = Color(0xFF1E88E5)
val LightBlue      = Color(0xFFE3F2FD)
val DarkText       = Color(0xFF1A1A2E)
val SubText        = Color(0xFF6B7280)
val BackgroundGray = Color(0xFFF8F9FA)
val CardWhite      = Color(0xFFFFFFFF)
val SuccessGreen   = Color(0xFF43A047)

val DarkBackground = Color(0xFF121212)
val DarkCard       = Color(0xFF1E1E1E)
val DarkTextLight  = Color(0xFFE0E0E0)
val DarkSubText    = Color(0xFF9E9E9E)
val DarkBlueAccent = Color(0xFF64B5F6)

@Composable
fun ProfileCard(
    title: String, cardColor: Color, textColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
fun InfoItem(
    emoji: String, label: String, text: String,
    textColor: Color, subTextColor: Color, iconBgColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(CircleShape).background(iconBgColor),
            contentAlignment = Alignment.Center
        ) { Text(text = emoji, fontSize = 20.sp) }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = subTextColor, fontWeight = FontWeight.Medium)
            Text(text = text,  fontSize = 14.sp, color = textColor,    fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun InfoDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp),
        thickness = 0.8.dp,
        color = Color(0xFFE0E0E0)
    )
}

@Composable
fun ActionButton(
    text: String, color: Color, onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@Composable
fun DarkModeToggle(
    isDarkMode: Boolean, onToggle: () -> Unit,
    textColor: Color, accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = if (isDarkMode) "🌙" else "☀️", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isDarkMode) "Dark Mode" else "Light Mode",
                fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor
            )
        }
        Switch(
            checked = isDarkMode,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor
            )
        )
    }
}

@Composable
fun FooterCopyright(subTextColor: Color, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth().padding(top = 4.dp)
    ) {
        HorizontalDivider(thickness = 0.8.dp, color = Color(0xFFDDE1E7))
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "© 2026 Sigit Kurnia Hartawan — 123140033",
            fontSize = 12.sp, color = subTextColor,
            fontWeight = FontWeight.Medium, textAlign = TextAlign.Center
        )
        Text(
            text = "Institut Teknologi Sumatera",
            fontSize = 11.sp, color = subTextColor.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SuccessNotification(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.wrapContentWidth(),
        shape = RoundedCornerShape(50.dp),
        color = Color(0xFF2E7D32),
        shadowElevation = 10.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 11.sp,
                    modifier = Modifier.offset(y = (-0.5).dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Profil berhasil diperbarui",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
fun EditForm(
    name: String, bio: String,
    onNameChange: (String) -> Unit, onBioChange: (String) -> Unit,
    email: String, phone: String, location: String,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onSave: () -> Unit,
    cardColor: Color, textColor: Color,
    subTextColor: Color, accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Edit Profil", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
            OutlinedTextField(
                value = name, onValueChange = onNameChange,
                label = { Text("Nama") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor, focusedLabelColor = accentColor, cursorColor = accentColor
                )
            )
            OutlinedTextField(
                value = bio, onValueChange = onBioChange,
                label = { Text("Bio") }, minLines = 3, maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor, focusedLabelColor = accentColor, cursorColor = accentColor
                )
            )
            HorizontalDivider(thickness = 1.dp, color = subTextColor.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 2.dp))
            Text("Edit Kontak", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
            OutlinedTextField(
                value = email, onValueChange = onEmailChange,
                label = { Text("Email") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor, focusedLabelColor = accentColor, cursorColor = accentColor
                )
            )
            OutlinedTextField(
                value = phone, onValueChange = onPhoneChange,
                label = { Text("Phone") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor, focusedLabelColor = accentColor, cursorColor = accentColor
                )
            )
            OutlinedTextField(
                value = location, onValueChange = onLocationChange,
                label = { Text("Location") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor, focusedLabelColor = accentColor, cursorColor = accentColor
                )
            )
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("💾  Simpan Semua Perubahan", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}