# LandConverter

**LandConverter** is a modern Android application built with Jetpack Compose that allows users to convert land area measurements between modern standard SI units and traditional regional land units used across the Balkan countries.

---

## Features

- **Traditional & Modern Unit Conversions**: Convert between square meters (`m²`), ares (`a`), hectares (`ha`), and traditional regional units such as *Dulum*, *Motika*, *Ralo*, *Lanac*, and *Jutro / Ral*.
- **Regional Variations**: Traditional land units vary significantly by region in practice. Selecting a specific country/region automatically applies the correct local conversion formula and localized terminology.
- **Supported Regions**:
  - Serbia (`rs`)
  - Bosnia and Herzegovina (`ba`)
  - Croatia (`hr`)
  - Montenegro (`me`)
  - North Macedonia (`mk`)
- **Real-time & Debounced Input**: Instant UI updates with debounced recalculation for smooth user experience.

---

## Tech Stack & Architecture

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/).
- **Architecture**: Clean Architecture / MVVM pattern separating concerns into `data`, `domain`, and `presentation` layers.
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/).
- **Concurrency**: Kotlin Coroutines and `StateFlow`.
- **Language**: Kotlin.

---

## Project Structure

```text
com.example.landconverter/
├── data/
│   ├── model/         # Data models (BalkanCountry, LandUnit, ConverterOptions)
│   └── repository/    # ConversionRepository for area calculation logic
├── domain/
│   └── usecase/       # Business logic (ConvertAreaUseCase)
├── presentation/
│   ├── theme/         # Material 3 theme definitions
│   ├── ConverterScreen.kt       # Composable UI screen
│   ├── ConverterViewModel.kt    # ViewModel managing UI state and user actions
│   └── CountryStringProvider.kt # Presentation-layer UI string localization provider
└── di/
    └── AppModule.kt   # Hilt dependency injection modules
```

---

## Getting Started

1. Clone or open the project in **Android Studio** (Koala or newer recommended).
2. Sync the project with Gradle files.
3. Run the app on an Android emulator or physical device.
