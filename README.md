# LandConverter

**LandConverter** is a production-ready, modern Android application built with **Jetpack Compose**, **Room Persistence Library**, and **Hilt**. It enables users to convert land area measurements between standard SI units (`m²`, `ar`, `ha`) and traditional regional land units (*Dulum*, *Motika*, *Ralo*, *Lanac*, *Jutro / Ral*) used across Balkan countries.

---

##  Key Features

- **SI & Traditional Land Unit Conversion**: Convert seamlessly between square meters (`m²`), ares (`a`), hectares (`ha`), and regional traditional units.
- **Dynamic Multi-Country Localization**: Pre-configured regional conversion formulas and real-time localized terminology for:
  -  **Serbia** (`rs`)
  -  **Bosnia and Herzegovina** (`ba`)
  -  **Croatia** (`hr`)
  -  **Montenegro** (`me`)
  -  **North Macedonia** (`mk`)
- **Unit Management with Deferred State**: Dedicated **Manage Units** screen allowing users to edit unit values, add custom units, or remove units locally in memory. All changes are committed atomically to Room DB only when clicking *"Sačuvaj"*.
- **Atomic Database Operations**: All batch unit changes (Add, Edit, Delete) execute inside a single SQLite `withTransaction` block, guaranteeing zero partial writes or DB desynchronization.
- **Process Survival (`SavedStateHandle`)**: ViewModel state (entered amount, selected country, chosen units) persists across Android system background process death.
- **First-Run Onboarding Dialog**: Automatic first-launch welcome dialog (`AppPreferences`) introducing app features, country selection, and custom unit options.
- **Transactional Seeding**: Transaction-safe initial database callback (`InitialDataCallback`) ensuring preset regional units are seeded on first run without resurrecting deleted custom units on restarts.
- **Material 3 UI**: Edge-to-edge layout, regional flag emojis, rich unit dropdown menus with m² size hints, active checkmarks, smart marquee typography for long unit names, and back-handler confirm dialogs.

---

##  Tech Stack & Architecture

- **Language & Runtime**: Kotlin 2.1+, Java 17 toolchain, Gradle 8+.
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/) & Navigation Compose.
- **Architecture**: MVVM + Clean Architecture pattern (Data, Domain, Presentation layers).
- **Local Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room) v2.8.5 with KSP2 & Room Schema Export enabled.
- **Dependency Injection**: [Hilt (Dagger)](https://dagger.dev/hilt/).
- **Concurrency & Reactive Data**: Kotlin Coroutines, `Flow`, `StateFlow`, `combine`, and `snapshotFlow`.
- **State Preservation**: `SavedStateHandle` for ViewModel process survival.
- **Preferences**: `SharedPreferences` via `AppPreferences` for first-run onboarding state.

---

##  Project Structure

```text
com.djes021.landconverter/
├── data/
│   ├── local/             # Room Database (AppDatabase), DAO (LandUnitDao), Entity (LandUnitEntity), InitialDataCallback, SeedUnit, LandUnitMapper
│   ├── model/             # Data models (BalkanCountry with flag emojis)
│   ├── preferences/       # AppPreferences for first-run onboarding state
│   └── repository/        # LandUnitRepository (atomic withTransaction applyChanges)
├── di/
│   ├── LandConverterApp.kt# Hilt Application class
│   └── DatabaseModule.kt  # Hilt module for Room database
├── domain/
│   ├── model/             # Clean domain model (LandUnit)
│   ├── usecase/           # Business logic (ConvertAreaUseCase)
│   └── util/              # Validation utilities (parsePositiveNumber, UnitValidation)
└── presentation/
    ├── manage/            # Unit management feature (ManageUnitsScreen, ManageUnitsViewModel)
    ├── navigation/        # Compose Navigation graph (NavGraph)
    ├── theme/             # Material 3 color schemes and typography
    ├── ConverterScreen.kt # Primary conversion screen
    ├── ConverterViewModel.kt
    ├── CountryStringProvider.kt # Multi-country UI localization provider
    └── MainActivity.kt    # Single activity entry point
```

---

##  Getting Started

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/djes021/LandConverter.git
   ```
2. **Open in Android Studio**: Open the project in Android Studio (Ladybug / 2024.2+ recommended).
3. **Build & Run**: Sync Gradle and run the `:app` target on an Android emulator or physical device (Android 7.0 / API 24+).
