# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

Product requirements are defined in [`docs/PRD.md`](docs/PRD.md) — read it before building features. A minimal buildable Gradle/Compose-for-TV skeleton exists (`MainActivity` just renders "Nooki"). The local storage layer (PIN + approved channels) is implemented; no UI screens, Content Engine, or YouTube API integration exist yet.

**Nooki** is a parent-controlled YouTube viewer for kids on Android TV / Google TV (MVP targets Xiaomi TV Box-style streamers). A parent sets a PIN and builds an approved-channel whitelist; the child can only ever watch, search, or get recommended videos from within that whitelist — never raw YouTube, Shorts, comments, or related-channel content. All content requests flow through a single "Content Engine" chokepoint (see Architecture) so this guarantee holds everywhere in the app, not just on the home feed.

`docs/PRD.md`'s features are organized under `# 8. Core Features`, numbered 1–5 (Home Feed, My Channels, Search, Channel Page, Video Player) in the same order as FR-006–FR-011.

## Tech stack

**Kotlin + Jetpack Compose for TV**, native Android TV/Google TV app. Chosen over React Native for best-of-breed TV-remote input handling and Android TV platform support, matching PP-003 (TV Remote Only) and PP-004 (Simple Before Smart) — tradeoff is slower ramp-up if the team lacks prior Kotlin experience.

Per PP-005 in the PRD, the product is explicitly **No Backend** — everything (PIN, approved-channel list) is persisted locally on-device via Jetpack DataStore (`ProfileStore`, see Data model below); there is no server/database component to design. Still open: how the app authenticates to / calls the YouTube Data API.

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

Per the PRD (§11 Local Storage), the only persisted state is the PIN and the list of approved channels — no videos, feed, search results, or recommendations are ever cached locally. Implemented in `app/src/main/java/com/nooki/app/data/ProfileStore.kt` via a single `androidx.datastore.preferences` store (`nooki_profile`):

- **PIN** (FR-001/FR-002): stored only as a SHA-256 hash + random salt (both Base64-encoded), never in plaintext. `createPin(pin)` requires exactly 4 numeric digits; `validatePin(pin)` compares hashes with `MessageDigest.isEqual`.
- **Approved channels** (FR-004/FR-005/FR-012): `ApprovedChannel(id, title, thumbnailUrl)` list, serialized as a JSON array string via `org.json` (no extra serialization dependency). `addChannel` dedupes by `id`; `removeChannel` filters by `id`.
- Both exposed as `Flow`s (`isPinSet`, `approvedChannels`) for Compose to collect.

Not yet covered by real tests: `ProfileStore` depends on `android.content.Context`/`android.util.Base64`, so plain JVM `./gradlew test` can't exercise it without Robolectric (not yet a dependency), and no emulator is configured for `connectedAndroidTest` — see Commands.

## Architecture

Single Gradle module (`:app`, package `com.nooki.app`). `MainActivity` (`ComponentActivity`) creates a single `ProfileStore` and, via `collectAsState` on `isPinSet`, switches between `CreatePinScreen` (`ui/pin/CreatePinScreen.kt`) when no PIN exists and a `Text("Nooki")` placeholder once it does — the real home feed doesn't exist yet. The whole Compose tree is forced RTL (`CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)`) since UI copy is Hebrew; `strings.xml` holds the screen text.

`CreatePinScreen` is an on-screen 0–9 keypad (D-pad focusable, since Android TV remotes have no physical number keys — PP-003) driving a two-step enter-then-confirm flow; on confirm it calls `ProfileStore.createPin`, and the screen switch above happens automatically once the `isPinSet` flow updates — no explicit navigation callback needed. Note: `androidx.tv.material3.ColorScheme` has no `outline`/`surfaceVariant`-style role — use `border`/`borderVariant` instead (tripped this up once; see commit history).

`ValidatePinScreen` (FR-002) reuses the same keypad for a single-attempt PIN check via `ProfileStore.validatePin`, calling `onSuccess()` on match or clearing input and showing an error on mismatch. Per PRD §15 it gates parent-only actions (editing the channel list) — **not** routine app launches, so it's intentionally not wired into `MainActivity` yet; there's no "My Channels" edit screen to attach it to. The dots indicator (`PinDots`) and keypad grid (`PinKeypad`) live in `ui/pin/PinKeypad.kt`, shared by both PIN screens.

No Content Engine or YouTube API integration exist yet.

The PRD (§12–13) specifies the target shape: every screen goes through a single **Content Engine**, never YouTube directly.

```text
Nooki UI → Content Engine → YouTube Data API → Official YouTube Player
```

The Content Engine is responsible for building the home feed (latest 10 videos per approved channel, merged, shuffled, capped at 2 consecutive videos from the same channel), scoping search to approved channels only, and loading a channel's video list. This is the enforcement point for every whitelist business rule (BR-001–BR-008) in the PRD.

Key build config to know when touching `app/build.gradle.kts`: `minSdk 21`, `compileSdk`/`targetSdk 34`, Kotlin 1.9.24 with Compose compiler extension `1.5.14` (must move together), Compose BOM `2024.06.00`, `androidx.tv:tv-foundation`/`tv-material` for TV-specific components (focus handling, `Text`/`MaterialTheme` come from `androidx.tv.material3`, not the phone `androidx.compose.material3`). Root-level: AGP `8.5.2` and Gradle wrapper `8.7` (`build.gradle.kts`, `gradle/wrapper/gradle-wrapper.properties`) — both require the JDK 17+ `JAVA_HOME` noted above.

## Next steps for future sessions

Keep this section current as the project takes shape — update it whenever a new model, route group, or major structural decision is added, per the self-updating `.md` rule below.

- Confirm how the app authenticates to / calls the YouTube Data API (API key handling, quota) — needed before "Search First Channel"/"Add Channel" (next step in the First Launch flow, PRD §7) can be built.
- Build the Content Engine as the single chokepoint for feed/search/channel calls (§12–13 of the PRD).
- Build the "My Channels" screen (Feature 2) and wire `ValidatePinScreen` in front of its add/remove actions (FR-002/FR-004/FR-005) — it's implemented but unused until that screen exists.
- Build the real Home Feed to replace the `Text("Nooki")` placeholder in `MainActivity`.
- Add launcher icon/banner resources (currently omitted from `AndroidManifest.xml`).
- Consider adding Robolectric (or similar) so `ProfileStore`/Compose screens can run under `./gradlew test` — see the testing gap noted in Data model. No emulator is set up either, so `CreatePinScreen`'s D-pad focus/keypad behavior is unverified on real TV hardware.

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