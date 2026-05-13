package org.example.project.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.example.project.ProfileViewModel
import org.example.project.components.*
import tugas10pam.composeapp.generated.resources.Res
import tugas10pam.composeapp.generated.resources.profile_photo
import org.jetbrains.compose.resources.painterResource

// isDarkMode sekarang diterima dari App (SettingsViewModel) — bukan dari ProfileUiState
@Composable
fun ProfileScreen(
    vm: ProfileViewModel,
    isDarkMode: Boolean
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    var editName     by remember(uiState.name)     { mutableStateOf(uiState.name) }
    var editBio      by remember(uiState.bio)      { mutableStateOf(uiState.bio) }
    var editEmail    by remember(uiState.email)    { mutableStateOf(uiState.email) }
    var editPhone    by remember(uiState.phone)    { mutableStateOf(uiState.phone) }
    var editLocation by remember(uiState.location) { mutableStateOf(uiState.location) }

    LaunchedEffect(uiState.showSuccessNotif) {
        if (uiState.showSuccessNotif) {
            delay(3000)
            vm.dismissNotif()
        }
    }

    // Warna sekarang dari isDarkMode parameter (sinkron global)
    val backgroundColor = if (isDarkMode) DarkBackground else BackgroundGray
    val cardColor       = if (isDarkMode) DarkCard       else CardWhite
    val textColor       = if (isDarkMode) DarkTextLight  else DarkText
    val subTextColor    = if (isDarkMode) DarkSubText    else SubText
    val accentColor     = if (isDarkMode) DarkBlueAccent else PrimaryBlue
    val iconBgColor     = if (isDarkMode) Color(0xFF1A2A3A) else LightBlue

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Foto + Nama + NIM + Bio ───────────────
            Image(
                painter            = painterResource(Res.drawable.profile_photo),
                contentDescription = "Foto Profil",
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .size(110.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = uiState.name, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = textColor)

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(iconBgColor)
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "NIM: ${uiState.nim}",
                    fontSize = 13.sp, fontWeight = FontWeight.Medium, color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = uiState.bio, fontSize = 14.sp, color = subTextColor,
                textAlign = TextAlign.Center, lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            ActionButton(
                text    = if (uiState.showContactInfo) "✕  Sembunyikan Kontak" else "📋  Tampilkan Kontak",
                color   = if (uiState.showContactInfo) subTextColor else accentColor,
                onClick = { vm.toggleContactInfo() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedVisibility(
                visible = uiState.showContactInfo,
                enter   = fadeIn() + expandVertically(),
                exit    = fadeOut() + shrinkVertically()
            ) {
                ProfileCard(title = "Informasi Kontak", cardColor = cardColor, textColor = textColor) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        InfoItem("📧", "Email",    uiState.email,    textColor, subTextColor, iconBgColor)
                        InfoDivider()
                        InfoItem("📱", "Phone",    uiState.phone,    textColor, subTextColor, iconBgColor)
                        InfoDivider()
                        InfoItem("📍", "Location", uiState.location, textColor, subTextColor, iconBgColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedVisibility(
                visible = uiState.isEditMode,
                enter   = fadeIn() + expandVertically(),
                exit    = fadeOut() + shrinkVertically()
            ) {
                EditForm(
                    name = editName, bio = editBio,
                    onNameChange = { editName = it },
                    onBioChange  = { editBio  = it },
                    email = editEmail, phone = editPhone, location = editLocation,
                    onEmailChange    = { editEmail    = it },
                    onPhoneChange    = { editPhone    = it },
                    onLocationChange = { editLocation = it },
                    onSave       = { vm.saveAll(editName, editBio, editEmail, editPhone, editLocation) },
                    cardColor    = cardColor,
                    textColor    = textColor,
                    subTextColor = subTextColor,
                    accentColor  = accentColor
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick  = { vm.toggleEditMode() },
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = subTextColor),
                border   = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
            ) {
                Text(
                    text = if (uiState.isEditMode) "✕  Batal Edit" else "✏️  Edit Profil & Kontak",
                    fontSize = 13.sp, fontWeight = FontWeight.Medium, color = subTextColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FooterCopyright(subTextColor = subTextColor)
        }

        AnimatedVisibility(
            visible  = uiState.showSuccessNotif,
            enter    = fadeIn(),
            exit     = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 28.dp)
        ) {
            SuccessNotification()
        }
    }
}
