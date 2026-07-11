# VibePrivate Architecture

This file is the short architecture entrypoint. For the current file map and first files to open, use `docs/ARCHITECTURE_MAP.md`.

## Current Identity

- External plugin name: `VibeRegionGuard`.
- Bukkit entrypoint: `com.vibeprivate.VibePrivatePlugin`.
- Java package/API compatibility stays under `com.vibeprivate`.
- Preferred external facade: `VibePrivatePlugin#getVibeRegionGuardApi()`.
- Legacy compatibility facade: `VibePrivatePlugin#getApi()`.

Do not mass-rename packages just for branding. Keep compatibility stable until a dedicated migration checkpoint exists.

## Boundaries

- `api`: public integration surface and Bukkit events.
- `model`: region, bounds, flags and lifecycle state.
- `manager`: in-memory region registry and lookup indexes.
- `service`: business rules for lifecycle, selection, relocation, homes, fuel, upkeep and invites.
- `storage`: SQL schema and repositories.
- `protection` / `index`: hot-path protection lookup and region indexes.
- `gui` / `command` / `listener`: player/admin interaction adapters.
- `config` / `message`: configuration and localized text.

## Transfer Rule

Cross-world or season base transfer orchestration stays in a separate plugin. `VibeRegionGuard` only owns region truth, validation, move/relocate primitives, lifecycle state, home remap and typed events.

Other plugins must use the public API. They must not read SQL tables directly and must not use reflection against internals.

## Development Rule

One pass changes one layer. Do not mix GUI, storage, protection, transfer orchestration and language work in one commit.
