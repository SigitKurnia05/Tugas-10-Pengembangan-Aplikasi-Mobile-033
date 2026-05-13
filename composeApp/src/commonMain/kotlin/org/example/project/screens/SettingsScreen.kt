package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.project.NotesViewModel
import org.example.project.SettingsViewModel
import org.example.project.components.*
import org.example.project.data.BatteryInfo
import org.example.project.data.DeviceInfo
import org.example.project.data.FontSize
import org.example.project.data.SortOrder
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
    vm: SettingsViewModel,
    notesVm: NotesViewModel,
    isDarkMode: Boolean
) {
    val settings          by vm.settings.collectAsStateWithLifecycle()
    val showFavoritesOnly by notesVm.showFavoritesOnly.collectAsStateWithLifecycle()

    val deviceInfo: DeviceInfo   = koinInject()
    val batteryInfo: BatteryInfo = koinInject()

    var batteryLevel by remember { mutableStateOf(batteryInfo.getBatteryLevel()) }
    var isCharging   by remember { mutableStateOf(batteryInfo.isCharging()) }
    var batteryStatus by remember { mutableStateOf(batteryInfo.getBatteryStatus()) }

    LaunchedEffect(Unit) {
        while (true) {
            batteryLevel  = batteryInfo.getBatteryLevel()
            isCharging    = batteryInfo.isCharging()
            batteryStatus = batteryInfo.getBatteryStatus()
            kotlinx.coroutines.delay(5000)
        }
    }

    val backgroundColor = if (isDarkMode) DarkBackground else BackgroundGray
    val cardColor       = if (isDarkMode) DarkCard       else CardWhite
    val textColor       = if (isDarkMode) DarkTextLight  else DarkText
    val subTextColor    = if (isDarkMode) DarkSubText    else SubText
    val accentColor     = if (isDarkMode) DarkBlueAccent else PrimaryBlue
    val iconBgColor     = if (isDarkMode) Color(0xFF1A2A3A) else LightBlue

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        SettingsCard(title = "🎨  Tampilan", cardColor = cardColor, textColor = textColor) {
            SettingsRowSwitch(
                label        = "Dark Mode",
                description  = "Aktifkan tema gelap",
                checked      = settings.isDarkMode,
                onChecked    = { vm.toggleDarkMode() },
                textColor    = textColor,
                subTextColor = subTextColor,
                accentColor  = accentColor
            )

            SettingsDivider(subTextColor)

            SettingsRowLabel(
                label        = "Ukuran Teks",
                description  = "Pilih ukuran font catatan",
                textColor    = textColor,
                subTextColor = subTextColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FontSize.entries.forEach { size ->
                    FilterChip(
                        selected = settings.fontSize == size,
                        onClick  = { vm.setFontSize(size) },
                        label    = { Text(size.label, fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor,
                            selectedLabelColor     = Color.White
                        )
                    )
                }
            }
        }

        SettingsCard(title = "📝  Catatan", cardColor = cardColor, textColor = textColor) {
            SettingsRowLabel(
                label        = "Urutkan Berdasarkan",
                description  = "Atur urutan tampilan catatan",
                textColor    = textColor,
                subTextColor = subTextColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SortOrder.entries.forEach { order ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = settings.sortOrder == order,
                            onClick  = { vm.setSortOrder(order) },
                            colors   = RadioButtonDefaults.colors(selectedColor = accentColor)
                        )
                        Text(
                            text     = order.label,
                            fontSize = 14.sp,
                            color    = textColor,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            SettingsDivider(subTextColor)

            // ── Tampilkan Favorit Saja — terhubung ke NotesViewModel ──
            SettingsRowSwitch(
                label        = "Tampilkan Favorit Saja",
                description  = "Filter hanya catatan berbintang",
                checked      = showFavoritesOnly,
                onChecked    = { notesVm.toggleFavoritesOnly() },
                textColor    = textColor,
                subTextColor = subTextColor,
                accentColor  = accentColor
            )
        }

        SettingsCard(title = "📱  Informasi Perangkat", cardColor = cardColor, textColor = textColor) {
            DeviceInfoRow(
                icon         = Icons.Default.PhoneAndroid,
                label        = "Perangkat",
                value        = deviceInfo.getDeviceName(),
                iconBgColor  = iconBgColor,
                accentColor  = accentColor,
                textColor    = textColor,
                subTextColor = subTextColor
            )
            SettingsDivider(subTextColor)
            DeviceInfoRow(
                icon         = Icons.Default.Android,
                label        = "Sistem Operasi",
                value        = deviceInfo.getOsVersion(),
                iconBgColor  = iconBgColor,
                accentColor  = accentColor,
                textColor    = textColor,
                subTextColor = subTextColor
            )
            SettingsDivider(subTextColor)
            DeviceInfoRow(
                icon         = Icons.Default.Info,
                label        = "Versi Aplikasi",
                value        = deviceInfo.getAppVersion(),
                iconBgColor  = iconBgColor,
                accentColor  = accentColor,
                textColor    = textColor,
                subTextColor = subTextColor
            )
            SettingsDivider(subTextColor)
            BatteryInfoRow(
                batteryLevel = batteryLevel,
                isCharging   = isCharging,
                status       = batteryStatus,
                iconBgColor  = iconBgColor,
                accentColor  = accentColor,
                textColor    = textColor,
                subTextColor = subTextColor
            )
        }

        SettingsCard(title = "⚙️  Lainnya", cardColor = cardColor, textColor = textColor) {
            Button(
                onClick  = { vm.resetToDefault() },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
            ) {
                Text(
                    text       = "🔄  Reset ke Default",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        FooterCopyright(subTextColor = subTextColor)
    }
}


@Composable
fun BatteryInfoRow(
    batteryLevel: Int,
    isCharging: Boolean,
    status: String,
    iconBgColor: Color,
    accentColor: Color,
    textColor: Color,
    subTextColor: Color
) {
    val barColor = when {
        isCharging        -> Color(0xFF43A047)
        batteryLevel > 50 -> Color(0xFF43A047)
        batteryLevel > 20 -> Color(0xFFFFA726)
        else              -> Color(0xFFE53935)
    }

    Row(
        modifier          = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(36.dp)
                .background(iconBgColor, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if (isCharging) "⚡" else "🔋", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Baterai", fontSize = 12.sp, color = subTextColor)
            Text(status, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor)
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress   = { batteryLevel / 100f },
                modifier   = Modifier.fillMaxWidth().height(6.dp),
                color      = barColor,
                trackColor = barColor.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun DeviceInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconBgColor: Color,
    accentColor: Color,
    textColor: Color,
    subTextColor: Color
) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(36.dp)
                .background(iconBgColor, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = accentColor,
                modifier           = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = subTextColor)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor)
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    cardColor: Color,
    textColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors    = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun SettingsRowSwitch(
    label: String,
    description: String,
    checked: Boolean,
    onChecked: () -> Unit,
    textColor: Color,
    subTextColor: Color,
    accentColor: Color
) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor)
            Text(description, fontSize = 12.sp, color = subTextColor)
        }
        Switch(
            checked         = checked,
            onCheckedChange = { onChecked() },
            colors          = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor
            )
        )
    }
}

@Composable
fun SettingsRowLabel(
    label: String,
    description: String,
    textColor: Color,
    subTextColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor)
        Text(description, fontSize = 12.sp, color = subTextColor)
    }
}

@Composable
fun SettingsDivider(subTextColor: Color) {
    HorizontalDivider(
        modifier  = Modifier.padding(vertical = 12.dp),
        thickness = 0.8.dp,
        color     = subTextColor.copy(alpha = 0.2f)
    )
}