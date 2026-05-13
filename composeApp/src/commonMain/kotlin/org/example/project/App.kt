package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.components.*
import org.example.project.data.FontSize
import org.example.project.data.NoteEntity
import org.example.project.data.NoteRepository
import org.example.project.data.SettingsRepository
import org.example.project.data.SortOrder
import org.example.project.navigation.BottomNavItem
import org.example.project.navigation.Screen
import org.example.project.screens.*
import org.example.project.data.AiError
import org.example.project.data.AiRepository
import org.koin.compose.viewmodel.koinViewModel

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val fontSize: FontSize = FontSize.MEDIUM,
    val sortOrder: SortOrder = SortOrder.NEWEST,
    val showFavoritesOnly: Boolean = false
)

class SettingsViewModel(private val repo: SettingsRepository) : ViewModel() {

    private val _settings = MutableStateFlow(SettingsUiState())
    val settings: StateFlow<SettingsUiState> = _settings.asStateFlow()

    val isDarkMode = repo.isDarkModeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )
    val sortOrder = repo.sortOrderFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), SortOrder.NEWEST
    )

    init {
        viewModelScope.launch {
            combine(repo.isDarkModeFlow, repo.sortOrderFlow) { dark, sort ->
                _settings.value.copy(isDarkMode = dark, sortOrder = sort)
            }.collect { _settings.value = it }
        }
    }

    fun setDarkMode(isDark: Boolean) = viewModelScope.launch {
        repo.setDarkMode(isDark)
        _settings.update { it.copy(isDarkMode = isDark) }
    }

    fun toggleDarkMode() = viewModelScope.launch {
        val newValue = !_settings.value.isDarkMode
        repo.setDarkMode(newValue)
        _settings.update { it.copy(isDarkMode = newValue) }
    }

    fun setSortOrder(order: SortOrder) = viewModelScope.launch {
        repo.setSortOrder(order)
        _settings.update { it.copy(sortOrder = order) }
    }

    fun setFontSize(size: FontSize) {
        _settings.update { it.copy(fontSize = size) }
    }

    fun toggleFavoritesOnly() {
        _settings.update { it.copy(showFavoritesOnly = !it.showFavoritesOnly) }
    }

    fun resetToDefault() = viewModelScope.launch {
        repo.setDarkMode(false)
        repo.setSortOrder(SortOrder.NEWEST)
        _settings.value = SettingsUiState()
    }
}

class NotesViewModel(
    private val repository: NoteRepository,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly = _showFavoritesOnly.asStateFlow()

    fun toggleFavoritesOnly() { _showFavoritesOnly.update { !it } }

    val displayedNotes: StateFlow<List<NoteEntity>> = combine(
        repository.allNotes,
        _searchQuery,
        settingsRepo.sortOrderFlow,
        _showFavoritesOnly
    ) { notes, query, sortOrder, favOnly ->
        val favFiltered = if (favOnly) notes.filter { it.isFavorite } else notes
        val filtered = if (query.isBlank()) favFiltered else {
            favFiltered.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }
        }
        when (sortOrder) {
            SortOrder.NEWEST -> filtered.sortedByDescending { it.createdAt }
            SortOrder.OLDEST -> filtered.sortedBy { it.createdAt }
            SortOrder.A_TO_Z -> filtered.sortedBy { it.title.lowercase() }
            SortOrder.Z_TO_A -> filtered.sortedByDescending { it.title.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes = repository.allNotes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    fun seedDummyIfEmpty() = viewModelScope.launch {
        if (!settingsRepo.hasSeededNotes()) {
            repository.insertNote(
                "Selamat Datang di My Notes! 👋",
                "Ini catatan pertamamu. Kamu bisa membuat, mengedit, dan menghapus catatan dengan mudah. Tekan ⭐ untuk menandai sebagai favorit, dan 🗑️ untuk menghapus."
            )
            repository.insertNote(
                "Tips Produktivitas 💡",
                "1. Catat ide segera sebelum lupa.\n2. Gunakan fitur favorit untuk catatan penting.\n3. Manfaatkan pencarian untuk menemukan catatan cepat.\n4. Atur dark mode di menu Pengaturan atau Drawer."
            )
            repository.insertNote(
                "Tugas PAM - Semester 6 📚",
                "Fitur yang sudah diimplementasikan:\n✅ CRUD catatan dengan SQLDelight\n✅ Pencarian & sorting real-time\n✅ Favorites\n✅ Dark mode yang sinkron semua screen\n✅ Navigation drawer lengkap\n✅ Settings screen\n✅ Offline-first architecture"
            )
            settingsRepo.markNotesSeeded()
        }
    }

    fun addNote(title: String, content: String) =
        viewModelScope.launch { repository.insertNote(title, content) }

    fun editNote(id: Long, title: String, content: String) =
        viewModelScope.launch { repository.updateNote(id, title, content) }

    fun deleteNote(id: Long) =
        viewModelScope.launch { repository.deleteNote(id) }

    fun toggleFavorite(id: Long) = viewModelScope.launch {
        val currentNote = allNotes.value.find { it.id == id }
        if (currentNote != null) repository.toggleFavorite(id, !currentNote.isFavorite)
    }
}

data class ProfileUiState(
    val name: String = "Sigit Kurnia Hartawan",
    val nim: String = "123140033",
    val bio: String = "Mahasiswa Teknik Informatika Institut Teknologi Sumatera yang tertarik mengeksplorasi Mobile Application Development dan Data Mining.",
    val email: String = "sigit.123140033@student.itera.ac.id",
    val phone: String = "+62 812-3456-7890",
    val location: String = "Lampung Selatan, Indonesia",
    val showContactInfo: Boolean = false,
    val isEditMode: Boolean = false,
    val showSuccessNotif: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun toggleContactInfo() { _uiState.update { it.copy(showContactInfo = !it.showContactInfo) } }
    fun toggleEditMode()    { _uiState.update { it.copy(isEditMode = !it.isEditMode) } }

    fun saveAll(name: String, bio: String, email: String, phone: String, location: String) {
        _uiState.update {
            it.copy(
                name = name, bio = bio, email = email,
                phone = phone, location = location,
                isEditMode = false, showSuccessNotif = true
            )
        }
    }

    fun dismissNotif() { _uiState.update { it.copy(showSuccessNotif = false) } }
}

data class AiMessage(
    val text: String,
    val isUser: Boolean
)

data class AiUiState(
    val messages: List<AiMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isStreaming: Boolean = false,
    val streamingText: String = "",
    val error: String? = null,
    val lastFailedMessage: String? = null   // untuk retry
)

class AiViewModel(private val aiRepository: AiRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AiUiState())
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    // ── Streaming send ──────────────────────────────────────────────────────
    fun sendMessage(message: String) {
        if (message.isBlank() || _uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                messages = it.messages + AiMessage(message, isUser = true),
                isLoading = true,
                isStreaming = true,
                streamingText = "",
                error = null,
                lastFailedMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val buffer = StringBuilder()
                aiRepository.chatStream(message).collect { chunk ->
                    buffer.append(chunk)
                    _uiState.update { it.copy(streamingText = buffer.toString()) }
                }
                // Streaming selesai — pindah ke messages list
                val finalText = buffer.toString()
                if (finalText.isNotBlank()) {
                    _uiState.update {
                        it.copy(
                            messages = it.messages + AiMessage(finalText, isUser = false),
                            isLoading = false,
                            isStreaming = false,
                            streamingText = ""
                        )
                    }
                } else {
                    throw Exception("Response kosong")
                }
            } catch (e: Exception) {
                // Fallback ke non-streaming
                aiRepository.chat(message)
                    .onSuccess { response ->
                        _uiState.update {
                            it.copy(
                                messages = it.messages + AiMessage(response, isUser = false),
                                isLoading = false,
                                isStreaming = false,
                                streamingText = "",
                                lastFailedMessage = null
                            )
                        }
                    }
                    .onFailure { error ->
                        val errorMsg = when (error) {
                            is AiError.RateLimited  -> error.message ?: "Rate limited"
                            is AiError.Unauthorized -> "API key tidak valid. Cek AppModule.kt"
                            is AiError.NetworkError -> "Tidak ada koneksi internet"
                            is AiError.ServerError  -> "Server AI bermasalah, coba lagi"
                            else                    -> error.message ?: "Terjadi kesalahan"
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isStreaming = false,
                                streamingText = "",
                                error = errorMsg,
                                lastFailedMessage = message
                            )
                        }
                    }
            }
        }
    }

    // ── Retry last failed message ───────────────────────────────────────────
    fun retryLastMessage() {
        val last = _uiState.value.lastFailedMessage ?: return
        // Hapus pesan user yang gagal dari list (pesan terakhir user)
        val messages = _uiState.value.messages
        val trimmed = if (messages.lastOrNull()?.isUser == true) messages.dropLast(1) else messages
        _uiState.update { it.copy(messages = trimmed, error = null, lastFailedMessage = null) }
        sendMessage(last)
    }

    // ── Image analysis ──────────────────────────────────────────────────────
    fun analyzeImage(base64: String, mimeType: String = "image/jpeg", customPrompt: String = "") {
        if (_uiState.value.isLoading) return

        val userPrompt = if (customPrompt.isNotBlank()) customPrompt
        else "Analisis gambar ini"
        val displayMsg = "[Gambar] $userPrompt"

        _uiState.update {
            it.copy(
                messages = it.messages + AiMessage(displayMsg, isUser = true),
                isLoading = true,
                isStreaming = false,
                error = null,
                lastFailedMessage = null
            )
        }

        viewModelScope.launch {
            aiRepository.analyzeImage(base64, mimeType, userPrompt)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            messages = it.messages + AiMessage(response, isUser = false),
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Gagal menganalisis gambar",
                            lastFailedMessage = null
                        )
                    }
                }
        }
    }

    // ── Summarize ───────────────────────────────────────────────────────────
    fun summarize(noteContent: String) {
        if (noteContent.isBlank() || _uiState.value.isLoading) return

        val userMsg = "Tolong rangkum catatan ini:\n\n$noteContent"
        _uiState.update {
            it.copy(
                messages = it.messages + AiMessage(userMsg, isUser = true),
                isLoading = true,
                error = null,
                lastFailedMessage = null
            )
        }

        viewModelScope.launch {
            aiRepository.summarize(noteContent)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            messages = it.messages + AiMessage(response, isUser = false),
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Gagal merangkum catatan"
                        )
                    }
                }
        }
    }

    fun dismissError() = _uiState.update { it.copy(error = null, lastFailedMessage = null) }

    fun clearChat() {
        aiRepository.clearHistory()
        _uiState.update { AiUiState() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    repository: NoteRepository,
    settingsRepository: SettingsRepository
) {
    val settingsRepo = remember { settingsRepository }
    val settingsVm: SettingsViewModel = viewModel { SettingsViewModel(settingsRepo) }
    val notesVm: NotesViewModel      = viewModel { NotesViewModel(repository, settingsRepo) }
    val profileVm: ProfileViewModel  = viewModel()

    // Mempertahankan satu inisialisasi AI ViewModel untuk menghindari konflik
    val aiVm: AiViewModel = koinViewModel()

    // Seed dummy notes saat pertama kali
    LaunchedEffect(Unit) { notesVm.seedDummyIfEmpty() }

    val isDarkMode   by settingsVm.isDarkMode.collectAsStateWithLifecycle()
    val profileState by profileVm.uiState.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val currentRoute  = navController.currentBackStackEntryAsState().value?.destination?.route
    val drawerState   = rememberDrawerState(DrawerValue.Closed)
    val scope         = rememberCoroutineScope()

    val backgroundColor = if (isDarkMode) DarkBackground else BackgroundGray
    val cardColor       = if (isDarkMode) DarkCard       else CardWhite
    val accentColor     = if (isDarkMode) DarkBlueAccent else PrimaryBlue
    val subTextColor    = if (isDarkMode) DarkSubText    else SubText
    val textColor       = if (isDarkMode) DarkTextLight  else DarkText

    val bottomNavItems = listOf(
        BottomNavItem.Notes, BottomNavItem.Favorites,
        BottomNavItem.AiChat, BottomNavItem.Profile, BottomNavItem.Settings
    )
    val bottomNavRoutes = bottomNavItems.map { it.route }

    MaterialTheme(colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(drawerContainerColor = cardColor) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Header
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Text("📱  My Notes App", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = accentColor)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(profileState.name, fontSize = 13.sp, color = subTextColor)
                        Text(profileState.email, fontSize = 11.sp, color = subTextColor.copy(alpha = 0.7f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = subTextColor.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Menu items
                    DrawerMenuItem(Icons.Default.Home, "  Catatan",
                        currentRoute == Screen.Notes.route, textColor, accentColor) {
                        navController.navigate(Screen.Notes.route) { launchSingleTop = true }
                        scope.launch { drawerState.close() }
                    }
                    DrawerMenuItem(Icons.Default.Star, "  Favorit",
                        currentRoute == Screen.Favorites.route, textColor, accentColor) {
                        navController.navigate(Screen.Favorites.route) { launchSingleTop = true }
                        scope.launch { drawerState.close() }
                    }
                    DrawerMenuItem(Icons.Default.Person, "  Profil",
                        currentRoute == Screen.Profile.route, textColor, accentColor) {
                        navController.navigate(Screen.Profile.route) { launchSingleTop = true }
                        scope.launch { drawerState.close() }
                    }
                    DrawerMenuItem(Icons.Default.Settings, "  Pengaturan",
                        currentRoute == Screen.Settings.route, textColor, accentColor) {
                        navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                        scope.launch { drawerState.close() }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = subTextColor.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, null, tint = subTextColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                if (isDarkMode) " Dark Mode" else " Light Mode",
                                color = textColor, fontWeight = FontWeight.Medium, fontSize = 14.sp
                            )
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { settingsVm.setDarkMode(it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = accentColor)
                        )
                    }
                }
            }
        ) {
            Scaffold(
                containerColor = backgroundColor,
                topBar = {
                    AnimatedVisibility(visible = currentRoute in bottomNavRoutes, enter = fadeIn(), exit = fadeOut()) {
                        TopAppBar(
                            title = {
                                Text(
                                    when (currentRoute) {
                                        Screen.Notes.route     -> "📝 Catatan"
                                        Screen.Favorites.route -> "⭐ Favorites"
                                        Screen.Profile.route   -> "👤 Profil"
                                        Screen.Settings.route  -> "⚙️ Pengaturan"
                                        else -> ""
                                    },
                                    fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = textColor
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, "Menu", tint = textColor)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = cardColor)
                        )
                    }
                },
                bottomBar = {
                    AnimatedVisibility(visible = currentRoute in bottomNavRoutes, enter = fadeIn(), exit = fadeOut()) {
                        NavigationBar(containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White) {
                            bottomNavItems.forEach { item ->
                                NavigationBarItem(
                                    selected = currentRoute == item.route,
                                    onClick = {
                                        navController.navigate(item.route) {
                                            launchSingleTop = true; restoreState = true
                                        }
                                    },
                                    icon = { Icon(item.icon, contentDescription = null) },
                                    label = { Text(item.label, fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = accentColor, selectedTextColor = accentColor,
                                        unselectedIconColor = subTextColor, indicatorColor = accentColor.copy(alpha = 0.12f)
                                    )
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->
                NavHost(navController = navController, startDestination = Screen.Notes.route, modifier = Modifier.padding(paddingValues)) {
                    composable(Screen.Notes.route) {
                        NotesScreen(
                            onNoteClick = { navController.navigate(Screen.NoteDetail.createRoute(it)) },
                            onAddClick  = { navController.navigate(Screen.AddNote.route) },
                            isDarkMode  = isDarkMode, vm = notesVm, settingsVm = settingsVm
                        )
                    }
                    composable(Screen.Favorites.route) {
                        FavoritesScreen(
                            onNoteClick = { navController.navigate(Screen.NoteDetail.createRoute(it)) },
                            isDarkMode = isDarkMode, vm = notesVm
                        )
                    }
                    composable(Screen.Profile.route) {
                        ProfileScreen(vm = profileVm, isDarkMode = isDarkMode)
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(vm = settingsVm, notesVm = notesVm, isDarkMode = isDarkMode)
                    }
                    composable(route = Screen.NoteDetail.route, arguments = listOf(navArgument("noteId") { type = NavType.LongType })) {
                        NoteDetailScreen(
                            noteId = it.arguments?.getLong("noteId") ?: 0L,
                            onBack = { navController.popBackStack() },
                            onEdit = { id -> navController.navigate(Screen.EditNote.createRoute(id)) },
                            onSummarize = { content -> navController.navigate(Screen.AiChatSummarize.createRoute(content)) }, // Perbaikan parameter
                            isDarkMode = isDarkMode, vm = notesVm,
                            settingsVm = settingsVm
                        )
                    }
                    composable(Screen.AddNote.route) {
                        AddNoteScreen(onBack = { navController.popBackStack() }, isDarkMode = isDarkMode, vm = notesVm)
                    }

                    composable(Screen.AiChat.route) {
                        AiChatScreen(isDarkMode = isDarkMode, vm = aiVm)
                    }
                    composable(
                        route = Screen.AiChatSummarize.route,
                        arguments = listOf(navArgument("noteContent") { type = NavType.StringType })
                    ) {
                        val content = it.arguments?.getString("noteContent") ?: ""
                        AiChatScreen(
                            isDarkMode = isDarkMode,
                            vm = aiVm,
                            noteContentToSummarize = content
                        )
                    }

                    composable(route = Screen.EditNote.route, arguments = listOf(navArgument("noteId") { type = NavType.LongType })) {
                        EditNoteScreen(
                            noteId = it.arguments?.getLong("noteId") ?: 0L,
                            onBack = { navController.popBackStack() },
                            isDarkMode = isDarkMode, vm = notesVm
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    textColor: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    val bg = if (selected) accentColor.copy(alpha = 0.12f) else Color.Transparent
    val fg = if (selected) accentColor else textColor
    Surface(
        onClick = onClick,
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Text(label, color = fg, fontSize = 14.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}