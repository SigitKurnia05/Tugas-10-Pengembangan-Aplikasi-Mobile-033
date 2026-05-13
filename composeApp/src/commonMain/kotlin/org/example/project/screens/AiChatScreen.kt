package org.example.project.screens

import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.project.AiViewModel
import org.example.project.components.*

// ─── Kategori AI ─────────────────────────────────────────────────────────────
enum class AiCategory(val label: String, val emoji: String, val description: String) {
    SMART_ASSISTANT("Smart Asisten", "✨", "Tanya apa saja, bantu tulis catatan"),
    NUTRITION_ANALYSIS("Analisis Nutrisi", "🥗", "Cek kandungan gizi makanan & minuman"),
    TRANSLATE("Translate", "🌐", "Terjemahkan teks ke berbagai bahasa"),
    IMAGE_ANALYSIS("Analisis Gambar", "🖼️", "Kirim gambar untuk dianalisis AI")
}

@Composable
fun AiChatScreen(
    isDarkMode: Boolean,
    vm: AiViewModel,
    noteContentToSummarize: String? = null
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val backgroundColor = if (isDarkMode) DarkBackground else BackgroundGray
    val cardColor       = if (isDarkMode) DarkCard       else CardWhite
    val textColor       = if (isDarkMode) DarkTextLight  else DarkText
    val subTextColor    = if (isDarkMode) DarkSubText    else SubText
    val accentColor     = if (isDarkMode) DarkBlueAccent else PrimaryBlue

    val listState = rememberLazyListState()
    var inputText        by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AiCategory.SMART_ASSISTANT) }
    var imagePrompt      by remember { mutableStateOf("") }

    // ── Image picker ──────────────────────────────────────────────────────────
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val stream = context.contentResolver.openInputStream(it)
                val bytes = stream?.readBytes() ?: return@let
                stream.close()
                val mimeType = context.contentResolver.getType(it) ?: "image/jpeg"
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                val prompt = if (imagePrompt.isBlank())
                    "Deskripsikan isi gambar ini secara lengkap dan detail dalam bahasa Indonesia."
                else imagePrompt.trim()
                vm.analyzeImage(base64, mimeType, prompt)
                imagePrompt = ""
            } catch (e: Exception) {
                // error handled by VM
            }
        }
    }

    LaunchedEffect(uiState.messages.size, uiState.isStreaming) {
        val count = uiState.messages.size + if (uiState.isStreaming) 1 else 0
        if (count > 0) listState.animateScrollToItem(count - 1)
    }

    LaunchedEffect(noteContentToSummarize) {
        if (!noteContentToSummarize.isNullOrBlank()) {
            vm.summarize(noteContentToSummarize)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // ── Top Bar ──────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardColor)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GeminiStarIcon(size = 38.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "NoteAI Assistant",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor
                    )
                    Text(
                        when {
                            uiState.isStreaming -> "sedang mengetik..."
                            uiState.isLoading   -> "memproses..."
                            else                -> "Gemini 2.5 Flash • Online"
                        },
                        fontSize = 11.sp,
                        color = if (uiState.isLoading || uiState.isStreaming) accentColor else subTextColor
                    )
                }
            }
            if (uiState.messages.isNotEmpty()) {
                IconButton(onClick = { vm.clearChat() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus chat",
                        tint = subTextColor, modifier = Modifier.size(20.dp))
                }
            }
        }

        // ── Category Tabs ────────────────────────
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardColor)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AiCategory.entries) { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) accentColor else backgroundColor)
                        .border(
                            1.dp,
                            if (isSelected) accentColor else subTextColor.copy(alpha = 0.25f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedCategory = category; vm.clearChat() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(category.emoji, fontSize = 13.sp)
                        Text(
                            category.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else subTextColor
                        )
                    }
                }
            }
        }

        // ── Welcome / Suggestion ─────────────────
        AnimatedVisibility(
            visible = uiState.messages.isEmpty() && !uiState.isStreaming,
            enter = fadeIn(), exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GeminiStarIcon(size = 56.dp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Halo! Saya NoteAI 👋", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = textColor, textAlign = TextAlign.Center)
                Text(selectedCategory.description, fontSize = 13.sp,
                    color = subTextColor, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(6.dp))

                val suggestions = when (selectedCategory) {
                    AiCategory.SMART_ASSISTANT    -> listOf(
                        "💡 Tips membuat catatan yang efektif",
                        "📝 Bantu tulis catatan produktivitas",
                        "📊 Apa itu Kotlin Multiplatform?",
                        "✍️ Rangkum teks ini untuk saya"
                    )
                    AiCategory.NUTRITION_ANALYSIS -> listOf(
                        "🍚 Kandungan gizi nasi putih 200g",
                        "🥛 Berapa kalori dalam segelas susu?",
                        "🍌 Manfaat nutrisi pisang untuk tubuh",
                        "🥑 Apakah alpukat baik untuk diet?"
                    )
                    AiCategory.TRANSLATE          -> listOf(
                        "🇬🇧 Terjemahkan ke Inggris: 'Selamat pagi'",
                        "🇯🇵 Terjemahkan ke Jepang: 'Terima kasih'",
                        "🇫🇷 Translate to French: 'Good evening'",
                        "🇸🇦 Terjemahkan ke Arab: 'Semangat belajar'"
                    )
                    AiCategory.IMAGE_ANALYSIS     -> listOf(
                        "🖼️ Pilih gambar lalu klik tombol galeri",
                        "📸 Bisa analisis foto makanan, dokumen, dll",
                        "🔍 Bisa tambah pertanyaan spesifik tentang gambar",
                        "🤖 Powered by Gemini Vision AI"
                    )
                }

                suggestions.forEach { s ->
                    SuggestionChip(
                        onClick = { if (selectedCategory != AiCategory.IMAGE_ANALYSIS) inputText = s.substringAfter(" ") },
                        label   = { Text(s, fontSize = 12.sp, color = textColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors  = SuggestionChipDefaults.suggestionChipColors(containerColor = cardColor),
                        border  = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true, borderColor = accentColor.copy(alpha = 0.3f))
                    )
                }
            }
        }

        // ── Message List ─────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(uiState.messages) { message ->
                ChatBubble(
                    message = message.text, isUser = message.isUser,
                    accentColor = accentColor, cardColor = cardColor,
                    textColor = textColor, subTextColor = subTextColor
                )
            }

            // Streaming bubble — teks muncul bertahap
            if (uiState.isStreaming && uiState.streamingText.isNotEmpty()) {
                item {
                    StreamingBubble(
                        text = uiState.streamingText,
                        cardColor = cardColor, textColor = textColor, accentColor = accentColor
                    )
                }
            }

            if (uiState.isLoading && !uiState.isStreaming) {
                item { TypingIndicator(accentColor = accentColor, cardColor = cardColor) }
            }
        }

        // ── Error Banner with Retry ───────────────
        AnimatedVisibility(
            visible = uiState.error != null,
            enter = slideInVertically { it },
            exit  = slideOutVertically { it }
        ) {
            uiState.error?.let { error ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEBEE))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("⚠️ $error", fontSize = 12.sp,
                            color = Color(0xFFB71C1C), modifier = Modifier.weight(1f))
                        TextButton(onClick = { vm.dismissError() }) {
                            Text("✕", color = Color(0xFFB71C1C), fontSize = 12.sp)
                        }
                    }
                    // Tombol Retry jika ada pesan yang gagal
                    if (uiState.lastFailedMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { vm.retryLastMessage() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("🔄 Coba Lagi", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // ── Input Bar ────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardColor)
        ) {
            // Image prompt field (hanya muncul di kategori IMAGE_ANALYSIS)
            AnimatedVisibility(visible = selectedCategory == AiCategory.IMAGE_ANALYSIS) {
                OutlinedTextField(
                    value = imagePrompt,
                    onValueChange = { imagePrompt = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    placeholder = { Text("Pertanyaan tentang gambar (opsional)...", fontSize = 12.sp, color = subTextColor) },
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = accentColor,
                        unfocusedBorderColor    = subTextColor.copy(alpha = 0.3f),
                        focusedTextColor        = textColor,
                        unfocusedTextColor      = textColor,
                        focusedContainerColor   = backgroundColor,
                        unfocusedContainerColor = backgroundColor
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tombol galeri — hanya di IMAGE_ANALYSIS
                AnimatedVisibility(visible = selectedCategory == AiCategory.IMAGE_ANALYSIS) {
                    FloatingActionButton(
                        onClick = { imagePicker.launch("image/*") },
                        modifier = Modifier.size(48.dp),
                        containerColor = accentColor,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Pilih Gambar",
                            tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }

                // Text input (disembunyikan di IMAGE_ANALYSIS)
                AnimatedVisibility(
                    visible = selectedCategory != AiCategory.IMAGE_ANALYSIS,
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                when (selectedCategory) {
                                    AiCategory.SMART_ASSISTANT    -> "Silahkan ketik yang ingin ditanyakan "
                                    AiCategory.NUTRITION_ANALYSIS -> "Berikan makanan yang ingin dianalisis"
                                    AiCategory.TRANSLATE          -> "Masukkan teks yang ingin diterjemahkan"
                                    else -> ""
                                },
                                color = subTextColor, fontSize = 13.sp
                            )
                        },
                        maxLines = 4,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = accentColor,
                            unfocusedBorderColor    = subTextColor.copy(alpha = 0.3f),
                            focusedTextColor        = textColor,
                            unfocusedTextColor      = textColor,
                            focusedContainerColor   = backgroundColor,
                            unfocusedContainerColor = backgroundColor
                        )
                    )
                }

                // Spacer ketika IMAGE_ANALYSIS (teks input hidden)
                if (selectedCategory == AiCategory.IMAGE_ANALYSIS) {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Send button (hanya untuk non-image)
                AnimatedVisibility(visible = selectedCategory != AiCategory.IMAGE_ANALYSIS) {
                    FloatingActionButton(
                        onClick = {
                            if (inputText.isNotBlank() && !uiState.isLoading) {
                                val prefix = when (selectedCategory) {
                                    AiCategory.NUTRITION_ANALYSIS ->
                                        "[Analisis Nutrisi] Berikan informasi lengkap kandungan gizi, kalori, protein, lemak, karbohidrat, vitamin, dan manfaat atau risiko dari: "
                                    AiCategory.TRANSLATE -> "[Translate] "
                                    else -> ""
                                }
                                vm.sendMessage(prefix + inputText.trim())
                                inputText = ""
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = if (inputText.isNotBlank() && !uiState.isLoading)
                            accentColor else subTextColor.copy(alpha = 0.3f),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Kirim",
                            tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}


// ─── Gemini Star Icon ─────────────────────────────────────────────────────────
@Composable
fun GeminiStarIcon(modifier: Modifier = Modifier, size: Dp = 40.dp) {
    val blueVibrant = Color(0xFF1B73E8)
    val indigoDark  = Color(0xFF161C61)
    val glowColor   = Color(0xFFD2E3FC)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(blueVibrant, indigoDark),
                    center = Offset.Unspecified,
                    radius = Float.POSITIVE_INFINITY
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.12f)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val outerRadius = w * 0.45f
            val innerRadius = w * 0.12f

            val sparkPath = Path().apply {
                moveTo(cx, cy - outerRadius)
                quadraticBezierTo(cx + innerRadius, cy - innerRadius, cx + outerRadius, cy)
                quadraticBezierTo(cx + innerRadius, cy + innerRadius, cx, cy + outerRadius)
                quadraticBezierTo(cx - innerRadius, cy + innerRadius, cx - outerRadius, cy)
                quadraticBezierTo(cx - innerRadius, cy - innerRadius, cx, cy - outerRadius)
                close()
            }
            drawPath(sparkPath, color = Color.White.copy(alpha = 0.7f),
                style = Stroke(width = w * 0.02f, cap = StrokeCap.Round))
            drawPath(sparkPath, brush = Brush.linearGradient(
                colors = listOf(Color.White, glowColor),
                start = Offset(cx, cy - innerRadius), end = Offset(cx, cy + outerRadius)
            ), style = Fill)
        }
    }
}

@Composable
fun GeminiAvatarSmall() {
    val blueVibrant = Color(0xFF1B73E8)
    val indigoDark  = Color(0xFF161C61)
    val glowColor   = Color(0xFFD2E3FC)

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(blueVibrant, indigoDark),
                    center = Offset.Unspecified,
                    radius = Float.POSITIVE_INFINITY
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(28.dp * 0.12f)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val outerRadius = w * 0.45f
            val innerRadius = w * 0.12f

            val sparkPath = Path().apply {
                moveTo(cx, cy - outerRadius)
                quadraticBezierTo(cx + innerRadius, cy - innerRadius, cx + outerRadius, cy)
                quadraticBezierTo(cx + innerRadius, cy + innerRadius, cx, cy + outerRadius)
                quadraticBezierTo(cx - innerRadius, cy + innerRadius, cx - outerRadius, cy)
                quadraticBezierTo(cx - innerRadius, cy - innerRadius, cx, cy - outerRadius)
                close()
            }
            drawPath(sparkPath, color = Color.White.copy(alpha = 0.7f),
                style = Stroke(width = w * 0.02f, cap = StrokeCap.Round))
            drawPath(sparkPath, brush = Brush.linearGradient(
                colors = listOf(Color.White, glowColor),
                start = Offset(cx, cy - innerRadius), end = Offset(cx, cy + outerRadius)
            ), style = Fill)
        }
    }
}


// ─── Streaming Bubble ─────────────────────────────────────────────────────────
@Composable
fun StreamingBubble(
    text: String,
    cardColor: Color,
    textColor: Color,
    accentColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500), repeatMode = RepeatMode.Reverse
        ), label = "cursorAlpha"
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        GeminiAvatarSmall()
        Spacer(modifier = Modifier.width(6.dp))
        Surface(
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp),
            color = cardColor, shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = text + "▋".repeat(1).let { if (cursorAlpha > 0.5f) it else "" },
                fontSize = 14.sp, color = textColor, lineHeight = 21.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}


// ─── Chat Bubble ─────────────────────────────────────────────────────────────
@Composable
fun ChatBubble(
    message: String,
    isUser: Boolean,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color
) {
    val cleanMessage = if (!isUser) {
        message
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
            .replace(Regex("\\*(.+?)\\*"), "$1")
            .replace(Regex("(?m)^\\* "), "• ")
            .trim()
    } else message

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) { GeminiAvatarSmall(); Spacer(modifier = Modifier.width(6.dp)) }

        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp, topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd   = if (isUser) 4.dp  else 18.dp
            ),
            color = if (isUser) accentColor else cardColor,
            shadowElevation = 1.dp, modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = cleanMessage, fontSize = 14.sp,
                color = if (isUser) Color.White else textColor,
                lineHeight = 21.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier.size(28.dp).align(Alignment.Bottom)
                    .clip(CircleShape).background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) { Text("👤", fontSize = 13.sp) }
        }
    }
}


// ─── Typing Indicator ────────────────────────────────────────────────────────
@Composable
fun TypingIndicator(accentColor: Color, cardColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        GeminiAvatarSmall()
        Spacer(modifier = Modifier.width(6.dp))
        Surface(
            shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp),
            color = cardColor, shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val it = rememberInfiniteTransition(label = "dot$index")
                    val alpha by it.animateFloat(
                        initialValue = 0.2f, targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500, easing = EaseInOut),
                            repeatMode = RepeatMode.Reverse,
                            initialStartOffset = StartOffset(index * 150)
                        ), label = "a$index"
                    )
                    Box(Modifier.size(8.dp).alpha(alpha).background(accentColor, CircleShape))
                }
            }
        }
    }
}