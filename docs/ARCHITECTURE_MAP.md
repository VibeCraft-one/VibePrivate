# VibePrivate Architecture Map

## Entry Points

- `src/main/java/com/vibeprivate/VibePrivatePlugin.java` - plugin bootstrap.
- `src/main/java/com/vibeprivate/VibePrivateServiceFactory.java` - wires services and repositories.
- `src/main/java/com/vibeprivate/VibePrivateServices.java` - package-private runtime container for plugin wiring.
- `src/main/java/com/vibeprivate/api/VibePrivateAPI.java` - legacy public typed API for other plugins.
- `src/main/java/com/vibeprivate/api/VibeRegionGuardApi.java` - thin branded facade for external integrations.

## Maintainer Start Order

For a new development or review pass, read only this small set first:

1. `README.md`
2. `docs/ARCHITECTURE_MAP.md`
3. `git status --short --branch`
4. current diff or the one feature file being changed
5. the matching focused test

Do not start by rereading the full evidence history. Use `docs/IMPLEMENTATION_EVIDENCE.md` only for the latest relevant pass and build result.

## Public Integration Boundary

- Preferred external entrypoint: `VibePrivatePlugin#getVibeRegionGuardApi()`.
- Compatibility entrypoint: `VibePrivatePlugin#getApi()`.
- Direct repository and `DatabaseService` getters are legacy-only and should not be used by new integrations.
- Other plugins must not read SQL tables directly and must not use reflection to call internals.
- `VibePrivateServices` is not a public API. It is an internal wiring container used by plugin bootstrap, command registration and listener registration.

## Transfer Ownership

- `VibeRegionGuard` owns region truth: storage, members, flags, lifecycle status, home data, validation, move/relocate primitives and typed events.
- A separate transfer/admin plugin should own transfer orchestration: selecting source/target worlds, batching player bases, admin commands/GUI, progress reports, retries and season-specific rules.
- The transfer plugin must call `VibeRegionGuardApi` / `VibePrivateAPI`; it must not read SQL tables, duplicate region rules or use reflection.
- A tiny internal admin smoke command can be added later if needed, but the full season/base transfer workflow should stay outside this plugin.

## Main Packages

- `api` - public contracts for external use.
- `model` - typed region models, bounds, lifecycle state, selection bounds.
- `manager` - core region registry/index access, centered on `RegionManager`.
- `service` - business logic layers around regions, lifecycle, selection, homes, invites, upkeep.
- `storage` - SQL repositories, schema and DB statements.
- `protection` / `index` - runtime lookup and chunk protection/index support.
- `gui` / `command` / `listener` - player/admin interaction layer.
- `config` / `message` - config loading and localized messages.

## Lifecycle Layer

- `service/RegionLifecycleService.java` - region status and upkeep pause rules.
- `storage/RegionLifecycleRepository.java` - lifecycle persistence.
- `model/RegionStatus.java` and `model/RegionLifecycleState.java` - typed lifecycle state.
- `service/RegionManagerLifecycleRegionStore.java` - narrow adapter from lifecycle service to `RegionManager`.

## API Layer

- `api/VibePrivateAPI.java` remains the legacy safe integration surface.
- `api/VibeRegionGuardApi.java` is the branded thin facade and delegates to `VibePrivateAPI` only.
- `api/event/*` contains typed Bukkit events for lifecycle and transfer flows.
- Current external-safe reads include region lookup, lifecycle status, world region listing, bounds lookup, generic target-bounds validation, selection-inside-region validation, conservative CLAN management compatibility reads, and relocation/world-move foundation methods.
- HOME/FARM/CLAN stay unified as `RegionType` variants inside one public API, not separate subsystems.
- Current CLAN management reads use temporary compatibility semantics; future CLAN work should add region-backed clan identity/roles inside VibeRegionGuard, not an external clan provider.

## Selection / Bounds Foundation

- `model/RegionBounds.java` - normalized region bounds.
- `model/SelectionBounds.java` - normalized external selection input.
- `service/RegionSelectionValidator.java` - read-only validation for generic target `SelectionBounds` and selection-inside-region checks.
- `service/RegionManagerSelectionRegionStore.java` - narrow adapter so selection logic stays outside `RegionManager`.

## Relocation Foundation

- `service/RegionRelocationService.java` - typed foundation for same-bounds world move and radius relocation checks.
- `service/RegionManagerRelocationRegionStore.java` - read-only/write-narrow adapter around `RegionManager`, allowed worlds config and region home persistence.
- `service/RegionEventDispatcher.java` and `service/BukkitRegionEventDispatcher.java` isolate Bukkit event publication away from the facade and stores.
- Keep relocation rules in this layer, then let `RegionManager.replaceRegion(...)` remain the only mutation point for region replacement.
- Same-bounds world move may remap `RegionHome.worldName` through the relocation store seam after region replacement.
- If home remap fails after region replacement, relocation attempts a local rollback to the original region; this is rollback safety, not a full DB transaction.

## Lifecycle / Transfer Events

- `RegionLifecycleService` now emits typed status events after persisted status changes:
  - `RegionStatusChangeEvent`
  - `RegionSealEvent`
  - `RegionArchiveEvent`
  - `RegionRestoreEvent`
- `RegionRelocationService` now emits:
  - `RegionWorldMoveEvent`
  - `RegionRelocateEvent`
- Event publication stays in dedicated services; `VibePrivateAPI` remains a facade and `RegionManager` remains free of transfer/event business logic.

## GUI Layer

- `gui/*` contains menus and handlers only.
- GUI should call services/API, not talk directly to SQL or future transfer logic.

## Storage / Repositories

- `storage/DatabaseService.java` - connection and SQL dialect access.
- `storage/DatabaseSchema.java` - table creation/migrations.
- `storage/DatabaseStatements.java` - vendor-specific upsert SQL.
- `storage/*Repository.java` - data access per concern.

## Safe Place For Future Move / Relocate Work

- Keep public entry points in `api/VibePrivateAPI.java`.
- Put move/relocate rules in dedicated `service` classes like `RegionRelocationService`, not in `RegionManager`.
- Let `RegionManager` stay focused on region state, indexes and lookups.
- Reuse small adapters like lifecycle/selection stores if a new service needs read/write access to regions.

## Useful First Files To Open

1. `src/main/java/com/vibeprivate/api/VibePrivateAPI.java`
2. `src/main/java/com/vibeprivate/VibePrivateServiceFactory.java`
3. `src/main/java/com/vibeprivate/manager/RegionManager.java`
4. `src/main/java/com/vibeprivate/service/RegionLifecycleService.java`
5. `src/main/java/com/vibeprivate/service/RegionSelectionValidator.java`
6. `src/main/java/com/vibeprivate/storage/DatabaseSchema.java`
