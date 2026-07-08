# VibeRegionGuard Validation Log

This is a compact validation summary. Detailed pass history lives in git commits; do not grow this file into a long diary.

## Current State

- Branch used for cleanup: `pr-1-amethyst`.
- Plugin name: `VibeRegionGuard`.
- Java compatibility namespace: `com.vibeprivate`.
- Public integration surface: `VibePrivateAPI` plus thin `VibeRegionGuardApi` facade.
- Latest verified build: `.\gradlew.bat clean build --no-daemon`.
- Latest verified result: passed on 2026-07-08 with 47 tests found, 47 started, 47 successful, 0 failed.
- Latest verified server bootstrap: Paper 1.21.11 build 132 loaded and enabled `VibeRegionGuard` v0.1.1 on 2026-07-08 without startup errors.

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
- `FuelService` iterates indexed player regions instead of broad all-region reads.
- `UpkeepService` uses indexed owner/player-region reads.

## Current Guardrails

- Keep full base-transfer orchestration in a separate plugin.
- Keep this plugin responsible for region truth, lifecycle, validation, home remap, relocation primitives and events.
- Do not add reflection integrations.
- Do not add blocking storage calls to protection hot paths.
- Do not add transfer/snapshot/archive orchestration into `RegionManager`.
- Do not grow `RegionDetailMenu`, `RegionDetailClickHandler` or `PrivateMenuListener` with new large responsibilities.

## Remaining Risks

- Manual player smoke is still required; the Paper bootstrap passed, but unit tests and headless boot do not verify player commands, Bukkit inventories, GUI clicks, protection behavior or restart persistence after writes.
- Admin GUI list pages still show only the first 45 entries until pagination is added.
- CLAN identity/roles are still a compatibility surface, not a finished region-backed clan module.
- Fuel maintenance is now narrower but still linear over player regions; add an expiry queue only if scale testing proves it is needed.
- `CommandMapOverrideService` uses reflection for command compatibility; keep it isolated and add config opt-out later.

## Next Useful Checks

1. Run manual player smoke on Paper/Purpur: `/vp`, `/privat`, `/privatadmin`, region create, member add, flags, fuel, restart persistence.
2. Add admin/player region pagination before large tester use.
3. Finish CLAN identity and roles as a small region-backed module.
4. Run synthetic region-count smoke before claiming 100+ online readiness.
