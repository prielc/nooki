# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

Product requirements are defined in [`docs/PRD.md`](docs/PRD.md) — read it before building features. A minimal buildable Gradle/Compose-for-TV skeleton exists (`MainActivity` just renders "Nooki") — no PRD features (PIN, channel whitelist, Content Engine, feed/search/player) are implemented yet.

**Nooki** is a parent-controlled YouTube viewer for kids on Android TV / Google TV (MVP targets Xiaomi TV Box-style streamers). A parent sets a PIN and builds an approved-channel whitelist; the child can only ever watch, search, or get recommended videos from within that whitelist — never raw YouTube, Shorts, comments, or related-channel content. All content requests flow through a single "Content Engine" chokepoint (see Architecture) so this guarantee holds everywhere in the app, not just on the home feed.

`docs/PRD.md`'s features are organized under `# 8. Core Features`, numbered 1–5 (Home Feed, My Channels, Search, Channel Page, Video Player) in the same order as FR-006–FR-011.

## Tech stack

**Kotlin + Jetpack Compose for TV**, native Android TV/Google TV app. Chosen over React Native for best-of-breed TV-remote input handling and Android TV platform support, matching PP-003 (TV Remote Only) and PP-004 (Simple Before Smart) — tradeoff is slower ramp-up if the team lacks prior Kotlin experience.

Per PP-005 in the PRD, the product is explicitly **No Backend** — everything (PIN, approved-channel list) is persisted locally on-device (likely DataStore/Room, TBD when storage is implemented); there is no server/database component to design. Still open: how the app authenticates to / calls the YouTube Data API.

Local dev environment: this machine had no Android Studio, SDK, or Gradle, and only Java 8 (Compose/AGP need JDK 17+). Resolved headlessly rather than via Android Studio's interactive first-run wizard:
- Android Studio installed via `brew install --cask android-studio` (`/Applications/Android Studio.app`) — used for its bundled JBR (JDK 21) at `/Applications/Android Studio.app/Contents/jbr/Contents/Home`.
- Android SDK command-line tools installed via `brew install --cask android-commandlinetools`, rooted at `/opt/homebrew/share/android-commandlinetools`.
- `platform-tools`, `platforms;android-34`, and `build-tools;34.0.0` installed and licenses accepted via that `sdkmanager`.
- Export before running any Gradle/SDK command in this repo: `export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools` and `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`.

## Commands

Before running any of these, export the SDK/JDK paths noted above (`ANDROID_HOME`, `JAVA_HOME`) — they aren't on this machine's default PATH.

- `./gradlew assembleDebug` — build the debug APK (output at `app/build/outputs/apk/debug/app-debug.apk`).
- `./gradlew installDebug` — build and install onto a connected/running Android TV device or emulator.
- `./gradlew test` — run JVM unit tests (none written yet).
- `./gradlew connectedAndroidTest` — run instrumented tests on a device/emulator (none written yet).

No emulator or physical Android TV device is configured on this machine yet.

## Data model

None yet. Per the PRD (§11 Local Storage), the only persisted state is the PIN and the list of approved channels — no videos, feed, search results, or recommendations are ever cached locally.

## Architecture

Single Gradle module (`:app`, package `com.nooki.app`) — `MainActivity` is currently a bare `ComponentActivity` rendering a Compose `Text("Nooki")` via `androidx.tv.material3`. No Content Engine, storage, or screens exist yet.

The PRD (§12–13) specifies the target shape: every screen goes through a single **Content Engine**, never YouTube directly.

```text
Nooki UI → Content Engine → YouTube Data API → Official YouTube Player
```

The Content Engine is responsible for building the home feed (latest 10 videos per approved channel, merged, shuffled, capped at 2 consecutive videos from the same channel), scoping search to approved channels only, and loading a channel's video list. This is the enforcement point for every whitelist business rule (BR-001–BR-008) in the PRD.

Key build config to know when touching `app/build.gradle.kts`: `minSdk 21`, `compileSdk`/`targetSdk 34`, Kotlin 1.9.24 with Compose compiler extension `1.5.14` (must move together), Compose BOM `2024.06.00`, `androidx.tv:tv-foundation`/`tv-material` for TV-specific components (focus handling, `Text`/`MaterialTheme` come from `androidx.tv.material3`, not the phone `androidx.compose.material3`). Root-level: AGP `8.5.2` and Gradle wrapper `8.7` (`build.gradle.kts`, `gradle/wrapper/gradle-wrapper.properties`) — both require the JDK 17+ `JAVA_HOME` noted above.

## Next steps for future sessions

Keep this section current as the project takes shape — update it whenever a new model, route group, or major structural decision is added, per the self-updating `.md` rule below.

- Confirm how the app authenticates to / calls the YouTube Data API (API key handling, quota).
- Define local on-device storage for PIN + approved channels (§11 of the PRD) — likely Jetpack DataStore.
- Build the Content Engine as the single chokepoint for feed/search/channel calls (§12–13 of the PRD).
- Add launcher icon/banner resources (currently omitted from `AndroidManifest.xml`).

## Git workflow (autonomous)

At the end of every development task (a meaningful chunk of code/config changes complete and working), perform the full git procedure yourself, without asking for confirmation first:

1. `git status` / `git diff` to review what changed.
2. Stage the relevant files (avoid `git add -A`/`.` if it would sweep in unrelated or sensitive files).
3. Commit with a clear, concise message describing the *why*.
4. Push to the remote (once one is configured), including creating/pushing a new branch if needed.

This standing authorization covers commit and push as routine steps of finishing a task. It does **not** cover destructive or history-rewriting operations (force-push, reset --hard, rebase of shared history, deleting branches) — those still require asking first, per standard git safety practice.

## Working process rules

1. Before writing any code, describe your approach and wait for approval. Always ask clarifying questions before writing any code if requirements are ambiguous.
2. If a task requires changes to more than 3 files, stop and break it into smaller tasks first.
3. After writing code, list what could break and suggest tests to cover it.
4. When there's a bug, start by writing a test that reproduces it, then fix it until the test passes.
5. Every time the user corrects you, add a new rule to this file so it never happens again.
6. Whenever you judge that a `.md` file (CLAUDE.md, README.md, or others) is out of date or should be updated, update it directly without asking first.

## Plugins

- `frontend-design@claude-plugins-official` is installed (user scope). Use it for any frontend/UI work in this project — it establishes a design direction (purpose, audience, aesthetic) before coding and helps avoid generic AI-default styling.
- `ui-ux-pro-max@ui-ux-pro-max-skill` is installed (user scope), from third-party marketplace `nextlevelbuilder/ui-ux-pro-max-skill` (github.com/nextlevelbuilder/ui-ux-pro-max-skill). Provides a searchable database of UI styles, color palettes, font pairings, charts, and stack-specific guidance (React, Next.js, Vue, Svelte, Tailwind, shadcn/ui, SwiftUI, Flutter, etc.), plus brand/design-system/logo/banner sub-skills with executable scripts. Note: this is unverified third-party code (not the official Anthropic marketplace) — installed at the user's explicit request despite an anomalous star/fork count relative to the repo's age.