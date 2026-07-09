# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

A "Hunt the Wumpus" text game in Java, built with Gradle. The codebase is early-stage: the cave system (a dodecahedron graph) and hazard placement are implemented and tested; the actual gameplay loop (movement, shooting, smell/draft hints, win/lose) is not yet written — `App.main()` currently only prints the intro and waits for Enter.

## Commands

All commands use the Gradle wrapper:

- Build: `./gradlew build`
- Run the game: `./gradlew run`
- Run all tests: `./gradlew test`
- Run a single test class: `./gradlew test --tests "com.example.wumpus.CavesServiceTest"`
- Run a single test method: `./gradlew test --tests "com.example.wumpus.CavesServiceTest.oneCaveShouldHaveTheWumpus"`
- Checkstyle (runs as part of `build`): `./gradlew checkstyleMain checkstyleTest`

Test reports: `build/reports/tests/test/index.html`. Checkstyle reports: `build/reports/checkstyle/`.

## Toolchain notes

- **Java 25** is required (`build.gradle.kts` sets a Java 25 toolchain). `App.java` uses recent Java features: an instance `void main()` entry point and the `java.lang.IO` console class (`IO.println(...)`) — there are no `static main`, no class-level `public`, and no explicit `IO` import.
- Checkstyle 10.26.1 enforces `severity=error` (any violation fails the build), with a 120-char line limit. Config lives in `config/checkstyle/checkstyle.xml`.
- JUnit 5 (Jupiter) via the JUnit BOM is the test framework.

## Architecture

Single package `com.example.wumpus` under `src/main/java` with mirrored tests in `src/test/java`.

- **`CavesService`** — the domain logic and the main thing under test.
  - `ADJACENCY` is a hardcoded 20×3 dodecahedron adjacency table (0-based). Every cave has exactly 3 neighbours, edges are symmetric, no self-loops. `CaveTest` enforces these graph invariants (20 vertices, 30 unique edges).
  - `buildCaves()` constructs the 20 `Cave` objects wired to that table.
  - `initializeCaves(caves, RandomGenerator)` places hazards (`PIT_COUNT` pits, `BAT_COUNT` bats, `WUMPUS_COUNT` wumpus, `PLAYER_COUNT` player start) using a **partial Fisher-Yates shuffle** over a pool of cave indices. This samples distinct caves without replacement, so hazards never collide and no retry loop is needed — `CavesServiceTest` specifically verifies this holds even with a degenerate RNG that always returns 0.
  - The `RandomGenerator` is injected for testability; the no-arg overload uses the platform default.
- **`Cave`** — plain data holder: its three `linkedCaves` plus boolean flags (`isBottomLessPit`, `hasBats`, `hasWumpus`, `hasPlayer`).
- **`App`** — entry point and (eventually) the game loop.

When extending gameplay, keep randomness injectable via `RandomGenerator` (as `initializeCaves` does) so behaviour stays deterministically testable.
