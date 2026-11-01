# Mahfaza.co — Future iOS Architecture & Cross-Platform Roadmap

This document outlines the architectural assessment and strategic roadmap for expanding Mahfaza.co from its current native Android (Kotlin + Jetpack Compose) implementation to support Apple iOS devices in future product phases.

## 1. Architectural Assessment of Current Codebase

Mahfaza.co is currently engineered as a high-performance native Android application using:
- **UI Framework**: Jetpack Compose for Android (declarative UI, Material Design 3).
- **Local Persistence**: Room SQLite Database (`AppDatabase`, DAOs, Entities, Mappers).
- **Asynchronous Processing**: Kotlin Coroutines and Flows (`StateFlow`, `collectAsStateWithLifecycle`).
- **Background Scheduling**: AlarmManager, WorkManager, BroadcastReceivers (`ReminderReceiver`, `BootReceiver`).
- **Networking & Authentication**: Local session management with secure offline-first architecture.

## 2. Multiplatform Migration Strategy (Kotlin Multiplatform / Compose Multiplatform)

To support iOS without rewriting business logic from scratch, the recommended future migration path is **Kotlin Multiplatform (KMP) & Compose Multiplatform (CMP)**:

### A. Shared Business Logic & Data Layer (KMP)
- **Repositories & ViewModels**: Business rules, domain models (`Models.kt`), XP policies, and repository orchestration can be extracted into shared common Kotlin modules (`commonMain`).
- **Database Persistence**: Migrate local SQLite storage from Android Room to **SQLDelight** or **Room Multiplatform** (`androidx.room` KMP support), sharing SQL queries and database schemas between Android and iOS.
- **Serialization & Networking**: `kotlinx.serialization` and Ktor network clients are already 100% multiplatform-compatible.

### B. User Interface Layer (Compose Multiplatform)
- **UI Components & Screens**: Jetpack Compose code can be transitioned to Compose Multiplatform (`cmp`), allowing shared UI declarations across Android (`androidMain`) and iOS (`iosMain`).
- **Design System**: Material 3 theme definitions (`Theme.kt`, `Color.kt`, `Type.kt`) are fully compatible with Compose Multiplatform.

### C. Platform-Specific Adaptations
- **Notifications**: Android `AlarmManager` / `WorkManager` and notification channels map directly to iOS `UNUserNotificationCenter` and Background Tasks.
- **Authentication**: Credential Manager / Google Sign-In map to Sign in with Apple and Google Identity Services on iOS.

## 3. Phased Implementation Roadmap

1. **Phase 1 (Current - V31.6)**: Solidify native Android application, ensure zero regressions, achieve Google Play AAB readiness, and stabilize all academic and administrative command centers.
2. **Phase 2 (Refactoring)**: Extract pure Kotlin business logic (domain models, calculators, XP policies, validation rules) into a shared KMP library module.
3. **Phase 3 (Persistence & UI)**: Integrate SQLDelight or Room KMP and evaluate Compose Multiplatform for cross-device parity while retaining native platform wrappers for push notifications and authentication.
