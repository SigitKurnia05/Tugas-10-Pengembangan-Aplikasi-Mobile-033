# 📱 My Notes App — **Sigit Kurnia Hartawan** - NIM: 123140033
Tugas 10 Pengembangan Aplikasi Mobile — Testing & Dependency Injection

---

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack_Compose-Multiplatform-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Koin](https://img.shields.io/badge/Koin-DI-FF9800?style=for-the-badge)
![Testing](https://img.shields.io/badge/kotlin.test-MockK-green?style=for-the-badge)
![Turbine](https://img.shields.io/badge/Turbine-Flow_Testing-blue?style=for-the-badge)

</div>

---

## 📋 Deskripsi Aplikasi

**My Notes App** adalah aplikasi catatan digital berbasis **Kotlin Multiplatform (KMP)** dengan **Jetpack Compose Multiplatform**. Pada tugas 10 ini, aplikasi dikembangkan dengan menerapkan **Dependency Injection** menggunakan Koin dan **Testing** menyeluruh menggunakan kotlin.test, MockK, Turbine, dan Compose UI Test.

---

## 🏗️ Dependency Injection — Koin

Implementasi Koin DI menggunakan **2 module terpisah** sesuai layer arsitektur:

```kotlin
// Module 1: Data Layer
val dataModule = module {
    single { NotesDatabase(get<DatabaseDriverFactory>().createDriver()) }
    single { NoteRepository(get()) }
    single { SettingsRepository(get()) }
    single { GeminiService(apiKey = GEMINI_API_KEY) }
    single<AiRepository> { AiRepositoryImpl(get()) }
}

// Module 2: ViewModel Layer
val viewModelModule = module {
    viewModelOf(::NotesViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::AiViewModel)
}

val commonModule: List<Module> = listOf(dataModule, viewModelModule)
```

---

## 🧪 Test Cases

### 1. Unit Test — NoteEntityTest (`commonTest`) — 8 Test Cases

| No | Test Case | Status |
|---|---|:---:|
| 1 | `note entity is created with correct values` | ✅ |
| 2 | `note default isFavorite is false` | ✅ |
| 3 | `note can be toggled to favorite` | ✅ |
| 4 | `sort order labels are correct` | ✅ |
| 5 | `filtering favorites works correctly` | ✅ |
| 6 | `search by title works correctly` | ✅ |
| 7 | `sort newest first works correctly` | ✅ |
| 8 | `sort A to Z works correctly` | ✅ |

### 2. Unit Test — NotesViewModelMockTest (`androidUnitTest`) — 6 Test Cases

| No | Test Case | Status |
|---|---|:---:|
| 1 | `initial state shows notes from repository` | ✅ |
| 2 | `search query filters notes by title` | ✅ |
| 3 | `search with no match returns empty list` | ✅ |
| 4 | `addNote calls repository insertNote` | ✅ |
| 5 | `deleteNote calls repository deleteNote` | ✅ |
| 6 | `toggleFavorite calls repository toggleFavorite` | ✅ |

### 3. Flow Test — NotesFlowTest (`androidUnitTest`) — 2 Test Cases

| No | Test Case | Status |
|---|---|:---:|
| 1 | `displayedNotes flow emits notes correctly` | ✅ |
| 2 | `searchQuery flow updates when setSearchQuery called` | ✅ |

### 4. UI Test — NotesScreenTest (`androidInstrumentedTest`) — 3 Test Cases

| No | Test Case | Status |
|---|---|:---:|
| 1 | `searchInput_isDisplayed` | ✅ |
| 2 | `fab_isDisplayedAndClickable` | ✅ |
| 3 | `emptyState_showsWhenNoNotes` | ✅ |

### 5. Koin Module Test — AppModuleTest (`commonTest`) — 3 Test Cases

| No | Test Case | Status |
|---|---|:---:|
| 1 | `dataModule is defined` | ✅ |
| 2 | `viewModelModule is defined` | ✅ |
| 3 | `commonModule combines data and viewModel modules` | ✅ |

---

## 📊 Test Results & Coverage Report

| Screenshot Test Results | Screenshot Coverage |
|:---:|:---:|
| ![Test Results](https://github.com/user-attachments/assets/8ea96e86-d6c4-4851-8056-7f5ddeb817f9) | ![Coverage](https://github.com/user-attachments/assets/ab662c1c-0422-40c8-8ec8-999e360a5c19) |
| Semua test passed ✅ | Coverage test ✅ |

---

## 🎬 Demo Video

[![Demo Aplikasi](https://img.shields.io/badge/▶%20Tonton%20Demo-Google%20Drive-blue?style=for-the-badge&logo=googledrive)](https://drive.google.com/file/d/13UvpJilIhpbkolkbEWJYTDXxCnFfAvwO/view?usp=sharing)

---

## 🛠️ Tech Stack Testing

| Library | Versi | Kegunaan |
|---|---|---|
| kotlin.test | 2.3.20 | Unit test assertions |
| MockK | 1.13.17 | Mocking dependencies |
| Turbine | 1.2.0 | Flow testing |
| Koin Test | 4.0.0 | DI testing utilities |
| kotlinx-coroutines-test | 1.10.2 | Coroutine & test dispatcher |
| Compose UI Test | - | UI / instrumented testing |

---

## 🛠️ Tech Stack Utama

| Teknologi | Versi | Kegunaan |
|---|---|---|
| Kotlin Multiplatform | 2.3.20 | Cross-platform framework |
| Jetpack Compose Multiplatform | - | UI declarative |
| Google Gemini API | 2.5 Flash | AI / LLM backend |
| Ktor Client | 2.3.12 | HTTP client |
| SQLDelight | 2.0.1 | Local database (SQLite) |
| Koin | 4.0.0 | Dependency injection |
| Multiplatform Settings | 1.1.1 | Persistent key-value storage |
| Kotlinx Coroutines | 1.10.2 | Async & Flow |

---

## 📁 Struktur Test

```
composeApp/src/
├── commonTest/kotlin/org/example/project/
│   ├── NoteRepositoryTest.kt        # 8 unit test NoteEntity & SortOrder
│   ├── NotesViewModelTest.kt        # Fake repository test
│   ├── AppModuleTest.kt             # Koin module verification
│   └── ComposeAppCommonTest.kt      # Basic test
│
├── androidUnitTest/kotlin/org/example/project/
│   ├── NotesViewModelMockTest.kt    # 6 test dengan MockK
│   └── NotesFlowTest.kt             # 2 flow test dengan Turbine
│
└── androidInstrumentedTest/kotlin/org/example/project/
    └── NotesScreenTest.kt           # 3 UI test dengan Compose Test
```

---

## ⚙️ Setup Lokal

1. Clone repository ini
2. Buat file `local.properties` di root project
3. Tambahkan API key Gemini:
```properties
GEMINI_API_KEY=gunakan_API_pribadi_disini
```
4. Sync Gradle dan jalankan aplikasi

---

## ▶️ Cara Menjalankan Test

```bash
# Jalankan semua unit test
./gradlew testDebugUnitTest

# Jalankan common test
./gradlew jvmTest

# Jalankan UI test (butuh emulator/device)
./gradlew connectedAndroidTest
```

---

## 📊 Rubrik Penilaian

| Komponen | Bobot | Status |
|---|:---:|:---:|
| Koin DI Setup (2+ modules) | 20% | ✅ |
| Repository Tests (5+ test cases) | 20% | ✅ 8 test |
| ViewModel Tests MockK (4+ test cases) | 20% | ✅ 6 test |
| Flow Tests Turbine (2+ test cases) | 15% | ✅ 2 test |
| UI Tests Compose (3+ test cases) | 15% | ✅ 3 test |
| Code Quality (AAA pattern) | 10% | ✅ |
| **Bonus Coverage > 80%** | **+10%** | 🎯 |

---

**© 2026 Sigit Kurnia Hartawan — 123140033 — Institut Teknologi Sumatera**
