# Android Chess Application

A modern, fully functional Chess application built from scratch for Android using Jetpack Compose and **Clean Architecture** principles.

## Features

- **Custom Core Engine**: The entire chess rule set (move generation, check, checkmate, stalemate validation) is written purely in Kotlin within the Domain layer.
- **Single-Player AI**: Play against the computer! The app includes a custom chess AI powered by the **Minimax Algorithm** with **Alpha-Beta Pruning** to calculate optimal moves.
- **Modern UI**: The user interface is built entirely with declarative **Jetpack Compose**. The interactive chessboard features visual indicators for selected pieces and legal move hints.
- **MVI Architecture**: Built using Unidirectional Data Flow via the **Model-View-Intent (MVI)** design pattern, guaranteeing predictable and easily testable state management.
- **Robust Dependency Injection**: Powered by Dagger Hilt.

## Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Language**: Kotlin 2.0+
- **Architecture**: MVI (Model-View-Intent)
- **Asynchrony**: Kotlin Coroutines & Flows
- **Dependency Injection**: Dagger Hilt
- **Testing**: JUnit 4

## Project Structure

- **Domain Layer**: Contains the core `GameEngine`, `ChessAI` (Minimax algorithm), and data models (`Board`, `Piece`, `GameState`). Completely independent of Android frameworks.
- **Presentation Layer**: Contains the `GameViewModel` (handles Intents and emits ViewStates) and Compose UI components (`GameScreen`, `ChessBoardView`).

## Getting Started

1. Clone the repository: `git clone https://github.com/venugopalraog/ChessApplication.git`
2. Open the project in **Android Studio (Ladybug or newer)**.
3. Build the project to sync Gradle dependencies (AGP 9.0+ compatible).
4. Run the app on an emulator or physical device!

## Roadmap / Future Improvements

- Fully model En Passant, Castling, and Pawn Promotion in the UI.
- Add PGN Export / Game History saving using Room Database.
- Implement an Undo/Redo intent system.
- Polish the UI with custom SVG graphics rather than Unicode approximations.
