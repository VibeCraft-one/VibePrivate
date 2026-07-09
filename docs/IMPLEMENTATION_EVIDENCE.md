# VibeRegionGuard Validation Log

This is a compact validation summary. Detailed pass history lives in git commits; do not grow this file into a long diary.

## Current State

- Branch used for cleanup: `pr-1-amethyst`.
- Plugin name: `VibeRegionGuard`.
- Java compatibility namespace: `com.vibeprivate`.
- Public integration surface: `VibePrivateAPI` plus thin `VibeRegionGuardApi` facade.
- Latest verified build: `.\gradlew.bat clean build --no-daemon`.
- Latest verified result: passed on 2026-07-09 with 74 tests found, 74 started, 74 successful, 0 failed.
- Latest focused smoke runner: `.\gradlew.bat lifecycleSmokeTest --no-daemon` passed on 2026-07-09 with 74 tests found, 74 started, 74 successful, 0 failed.
- Latest verified server bootstrap: Paper 1.21.11 build 132 loaded and enabled `VibeRegionGuard` v0.1.1 on 2026-07-09 without startup errors.
- Latest verified restart smoke: Paper 1.21.11 build 132 restarted from the same plugin data folder and re-enabled `VibeRegionGuard` v0.1.1 on 2026-07-09.
- Latest verified Purpur bootstrap/restart: Purpur 1.21.11 build 2568 loaded, restarted and re-enabled `VibeRegionGuard` v0.1.1 on 2026-07-09.
- Latest current-jar bootstrap refresh: current `build/libs/VibeRegionGuard-0.1.1.jar` loaded on Paper 1.21.11 build 132 and Purpur 1.21.11 build 2568 on 2026-07-09; logs reached `Done`, plugin data was created and logs had no plugin startup failure patterns.

## Implemented Foundation

- Lifecycle statuses: `ACTIVE`, `INACTIVE`, `SEALED`, `ARCHIVED`.
- Lifecycle service and persistence outside `RegionManager`.
- Typed API for region reads, status, selection validation, relocation/world move foundation and upkeep pause/resume.
- Typed events for status, seal, archive, restore, world move and relocate flows.
- HOME remap support during same-bounds world move with local rollback attempt if home remap fails.
- Conservative CLAN management reads for compatibility.
- Branded `VibeRegionGuardApi` facade over the existing `VibePrivateAPI`.
- Indexed region lookups for world, owner, player/admin regions and upkeep/fuel maintenance paths.

## Architecture Cleanup

- `VibePrivateServices` is an internal package-private wiring container.
- Direct repository and database getters from the plugin are deprecated for external integrations.
- `VibePrivateServiceFactory` is split into focused construction phases.
- Admin/player region GUI reads use indexed `RegionManager` methods instead of grouping all regions in GUI code.
- Admin/player region GUI lists have 45-item pages with previous/next navigation.
- `plugin.yml` has static smoke coverage for public plugin identity, main class, Paper API version, commands and permissions.
- Packaged YAML resources have smoke coverage for config runtime keys, RU/EN message key parity and required user-facing message keys.
- `FuelService` iterates indexed player regions instead of broad all-region reads.
- `UpkeepService` uses indexed owner/player-region reads.
- Fuel, upgrade deposit withdrawal and region deletion resource paths now fail closed around persistence before item grant/drop.
- Region deletion storage cascade is covered for persisted upgrade deposits.
- SQLite migration has smoke coverage for required tables, required indexes, foreign-key enforcement and legacy `regions` table column upgrades.
- Region lookup index has deterministic 120-player-owner synthetic coverage with ADMIN/CLAN noise.
- Protection service has SQLite-backed smoke coverage for HOME owner/member/guest access, op/bypass access, HOME/ADMIN environment flags and ADMIN overlap priority.

## Current Guardrails

- Keep full base-transfer orchestration in a separate plugin.
- Keep this plugin responsible for region truth, lifecycle, validation, home remap, relocation primitives and events.
- Do not add reflection integrations.
- Do not add blocking storage calls to protection hot paths.
- Do not add transfer/snapshot/archive orchestration into `RegionManager`.
- Do not grow `RegionDetailMenu`, `RegionDetailClickHandler` or `PrivateMenuListener` with new large responsibilities.

## Remaining Risks

- Manual player smoke is still required; the Paper bootstrap passed, but unit tests and headless boot do not verify player commands, live Bukkit inventories, GUI clicks, protection behavior or restart persistence after writes.
- Admin GUI pagination still needs live Bukkit click smoke with many regions.
- CLAN identity/roles are still a compatibility surface, not a finished region-backed clan module.
- Fuel maintenance is now narrower but still linear over player regions; add an expiry queue only if scale testing proves it is needed.
- `CommandMapOverrideService` uses reflection for command compatibility; keep it isolated and add config opt-out later.

## RC Readiness

- Current acceptance mapping lives in `docs/RC_READINESS_MATRIX.md`.
- Do not mark the plugin as tester-ready RC until the manual player smoke has real pass/fail evidence.

## Next Useful Checks

1. Run `docs/MANUAL_PLAYER_SMOKE_RUNBOOK.md` on Paper/Purpur: `/vp`, `/privat`, `/privatadmin`, region create, member add, flags, fuel, restart persistence.
2. Record actual pass/fail evidence from `logs/latest.log` into the coordination task.
3. Re-run `.\gradlew.bat clean build --no-daemon` only after another code/resource change or immediately before tagging RC.
