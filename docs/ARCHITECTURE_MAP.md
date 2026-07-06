# VibePrivate Architecture Map

## Entry Points

- `src/main/java/com/vibeprivate/VibePrivatePlugin.java` - plugin bootstrap.
- `src/main/java/com/vibeprivate/VibePrivateServiceFactory.java` - wires services and repositories.
- `src/main/java/com/vibeprivate/VibePrivateServices.java` - runtime container and service access point.
- `src/main/java/com/vibeprivate/api/VibePrivateAPI.java` - public typed API for other plugins.

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

- `api/VibePrivateAPI.java` is the safe integration surface.
- Current external-safe reads include region lookup, lifecycle status, world region listing, bounds lookup, selection-inside-region validation, and relocation/world-move foundation methods.

## Selection / Bounds Foundation

- `model/RegionBounds.java` - normalized region bounds.
- `model/SelectionBounds.java` - normalized external selection input.
- `service/RegionSelectionValidator.java` - read-only validation for `SelectionBounds` against a region.
- `service/RegionManagerSelectionRegionStore.java` - narrow adapter so selection logic stays outside `RegionManager`.

## Relocation Foundation

- `service/RegionRelocationService.java` - typed foundation for same-bounds world move and radius relocation checks.
- `service/RegionManagerRelocationRegionStore.java` - read-only/write-narrow adapter around `RegionManager`, allowed worlds config and region home persistence.
- Keep relocation rules in this layer, then let `RegionManager.replaceRegion(...)` remain the only mutation point for region replacement.
- Same-bounds world move may remap `RegionHome.worldName` through the relocation store seam after region replacement.

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
