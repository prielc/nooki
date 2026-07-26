# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

Pre-code bootstrap. Product requirements are defined in [`docs/PRD.md`](docs/PRD.md) — read it before building features. No application code exists yet.

**Nooki** is a parent-controlled YouTube viewer for kids on Android TV / Google TV (MVP targets Xiaomi TV Box-style streamers). A parent sets a PIN and builds an approved-channel whitelist; the child can only ever watch, search, or get recommended videos from within that whitelist — never raw YouTube, Shorts, comments, or related-channel content. All content requests flow through a single "Content Engine" chokepoint (see Architecture) so this guarantee holds everywhere in the app, not just on the home feed.

`docs/PRD.md`'s features are organized under `# 8. Core Features`, numbered 1–5 (Home Feed, My Channels, Search, Channel Page, Video Player) in the same order as FR-006–FR-011.

## Tech stack

Not chosen yet. Per PP-005 in the PRD, the product is explicitly **No Backend** — everything (PIN, approved-channel list) is persisted locally on-device; there is no server/database component to design. Still open: the Android TV app framework (native Kotlin/Compose for TV, React Native, etc.) and how it calls the YouTube Data API.

## Commands

None yet — no package manifest, build tooling, or test runner exists in this repo.

## Data model

None yet. Per the PRD (§11 Local Storage), the only persisted state is the PIN and the list of approved channels — no videos, feed, search results, or recommendations are ever cached locally.

## Architecture

None yet, but the PRD (§12–13) specifies the shape: every screen goes through a single **Content Engine**, never YouTube directly.

```text
Nooki UI → Content Engine → YouTube Data API → Official YouTube Player
```

The Content Engine is responsible for building the home feed (latest 10 videos per approved channel, merged, shuffled, capped at 2 consecutive videos from the same channel), scoping search to approved channels only, and loading a channel's video list. This is the enforcement point for every whitelist business rule (BR-001–BR-008) in the PRD.

## Next steps for future sessions

Keep this section current as the project takes shape — update it whenever a new model, route group, or major structural decision is added, per the self-updating `.md` rule below.

- Choose the Android TV app framework/stack and confirm how it authenticates to the YouTube Data API.
- Define local on-device storage for PIN + approved channels (§11 of the PRD).
- Build the Content Engine as the single chokepoint for feed/search/channel calls (§12–13 of the PRD).

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