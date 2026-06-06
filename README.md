# Hunt the Wumpus

A modern Java implementation of the classic "Hunt the Wumpus" game.

## Overview

You are hunting the Wumpus through a system of 20 caves, each linked to 3 others. The Wumpus is asleep somewhere in these caves. Find it and shoot it before it eats you or you blunder into one of the other hazards.

### Hazards

- **The Wumpus**: If you enter its cave or miss it with an arrow, it might move or eat you.
- **Bottomless Pits**: Two caves contain bottomless pits. Falling into one is fatal.
- **Super Bats**: Two caves contain giant bats that will whisk you away to a random cave.

### Your Equipment

- **Senses**: You can smell the Wumpus, feel cold drafts from pits, or hear bats in adjacent caves.
- **Crooked Arrows**: You have 5 arrows that can fly through up to 5 caves.

## Tech Stack

- **Java 25**: Utilizes modern Java features including unnamed classes and instance main methods.
- **Gradle 9.5.1**: Used for building and running the application.

## Project Structure

- `src/main/java/com/example/wumpus/`: Core game logic.
    - `App.java`: Main entry point (unnamed class).
    - `Hunt.java`: Main game loop and orchestration.
    - `Cave.java`, `CavesService.java`: Cave system management.
    - `Player.java`: Player state management.
    - `actions/`: Player actions like movement and shooting.
    - `io/`: Input/Output abstractions.
- `src/test/java/com/example/wumpus/`: Unit tests for various components.

## How to Run

### Prerequisites

- **Java 25** must be installed and set as your `JAVA_HOME`.
- If you use **SDKMAN!**, you can install and use it with:
  ```bash
  sdk install java 25.0.1-zulu
  sdk use java 25.0.1-zulu
  ```

### Running the Application

To run the game, use the Gradle wrapper:

```bash
./gradlew run
```

### Running Tests

To run the project tests:

```bash
./gradlew test
```

### Checkstyle

To run checkstyle:

```bash
./gradlew checkstyleMain
```
