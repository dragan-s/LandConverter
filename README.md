# LandConverter

**LandConverter** is a modern Android application built with **Jetpack Compose**, **Room Database**, and **Hilt** that allows users to convert land area measurements between standard SI units (`m²`, `ar`, `ha`) and traditional regional land units (*Dulum*, *Motika*, *Ralo*, *Lanac*, *Jutro / Ral*) used across Balkan countries.

---

##  Key Features

- **Standard & Traditional Unit Conversion**: Seamlessly convert between square meters (`m²`), ares (`a`), hectares (`ha`), and regional traditional units.
- **Regional Variations Support**: Pre-configured regional conversion formulas and localized terminology for:
  -  **Serbia** (`rs`)
  -  **Bosnia and Herzegovina** (`ba`)
  -  **Croatia** (`hr`)
  -  **Montenegro** (`me`)
  -  **North Macedonia** (`mk`)
- **Custom Unit Management**: Dedicated **Manage Units** screen allowing users to edit unit values or add custom regional units per country.
- **Transactional Database Seeding**: Transaction-safe initial database seeding (`InitialDataCallback`) ensuring preset regional units are loaded reliably on first launch without resurrecting deleted custom units on app restarts.
- **Offline Persistence & Migration**: Room Database (v2) pre-populated with historical default units, unique country/unit indexing, and full local database persistence.
- **Modern Material 3 UI**: Clean, edge-to-edge UI with smart text marquee animations for long unit names, full-screen scrolling, and deletion confirmation dialogs.

---

##  Tech Stack & Architecture

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/) & Navigation Compose.
- **Architecture**: Clean Architecture / MVVM (Model-View-ViewModel) pattern.
- **Local Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room) v2.8.5 with [KSP2](https://kotlinlang.org/docs/ksp-overview.html) support.
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/).
- **Concurrency & Reactive Data**: Kotlin Coroutines, `Flow`, and `StateFlow`.
- **Language & Compiler**: Kotlin 2.1 with Gradle 9+.

---

##  Project Structure

```text
com.example.landconverter/
├── data/
│   ├── local/             # Room Database (AppDatabase v2), DAO, LandUnitEntity, InitialDataCallback, SeedUnit
│   ├── model/             # Data models (BalkanCountry)
│   └── repository/        # LandUnitRepository for database queries, SaveUnitResult, and persistence
├── domain/
│   └── usecase/           # Business logic (ConvertAreaUseCase)
├── presentation/
│   ├── manage/            # Unit management feature (ManageUnitsScreen, ManageUnitsViewModel)
│   ├── navigation/        # Compose Navigation graph (NavGraph)
│   ├── theme/             # Material 3 color schemes and typography
│   ├── ConverterScreen.kt # Primary conversion screen
│   ├── ConverterViewModel.kt
│   └── CountryStringProvider.kt # UI localization provider
└── di/
    ├── AppModule.kt       # Application-level Hilt module
    └── DatabaseModule.kt  # Room Database Hilt module
```

---

## ️ Getting Started

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/LandConverter.git
   ```
2. **Open in Android Studio**: Open the project in Android Studio (Ladybug / 2024.2+ recommended).
3. **Build & Run**: Sync Gradle and run `:app` on an Android emulator or physical device (Android 7.0 / API 24+).
