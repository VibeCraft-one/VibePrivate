# VibePrivate API/Lifecycle Pass 1 Evidence

## Scope

Implemented only the VibePrivate API/Lifecycle foundation required before BaseTransfer or SeasonArchive work.

Out of scope for this pass:
- VibeBaseTransfer
- VibeSeasonArchive
- snapshot/export
- GUI
- Iris/world generation
- manual relocation commands

## Changed Files

- `build.gradle`
- `src/main/java/com/vibeprivate/VibePrivateServiceFactory.java`
- `src/main/java/com/vibeprivate/VibePrivateServices.java`
- `src/main/java/com/vibeprivate/api/VibePrivateAPI.java`
- `src/main/java/com/vibeprivate/model/RegionLifecycleState.java`
- `src/main/java/com/vibeprivate/model/RegionStatus.java`
- `src/main/java/com/vibeprivate/service/RegionLifecycleRegionStore.java`
- `src/main/java/com/vibeprivate/service/RegionLifecycleService.java`
- `src/main/java/com/vibeprivate/service/RegionManagerLifecycleRegionStore.java`
- `src/main/java/com/vibeprivate/service/UpkeepService.java`
- `src/main/java/com/vibeprivate/storage/DatabaseSchema.java`
- `src/main/java/com/vibeprivate/storage/DatabaseService.java`
- `src/main/java/com/vibeprivate/storage/DatabaseStatements.java`
- `src/main/java/com/vibeprivate/storage/RegionLifecycleRepository.java`

## Acceptance Checks

- Region lifecycle statuses exist: `ACTIVE`, `INACTIVE`, `SEALED`, `ARCHIVED`: PASSED.
- Lifecycle logic is outside `RegionManager`: PASSED.
- Typed API exists in `VibePrivateAPI`: `getRegionStatus`, `setRegionStatus`, `pauseUpkeep`, `resumeUpkeep`: PASSED.
- Separate lifecycle persistence exists via `region_lifecycle`: PASSED.
- Backward compatibility fallback from old `enabled` flag exists: PASSED.
- `SEALED` and `ARCHIVED` set indefinite upkeep pause state through lifecycle service: PASSED.
- `UpkeepService` filters paused lifecycle regions from owner upkeep calculations: PASSED.
- No BaseTransfer/SeasonArchive/snapshot/Iris/relocation scope added: PASSED.
- No new class over 500 lines found in `src/main/java`: PASSED.
- No reflection added by this pass: PASSED.

## Build / Smoke

Command:

```powershell
.\gradlew.bat build
.\gradlew.bat clean build --no-daemon
```

Result: PASSED on 2026-07-06. The clean build reported `BUILD SUCCESSFUL in 9s`.

Note: Gradle printed Java native-access warnings from the wrapper/runtime. They did not fail the build.
`compileTestJava` and `test` reported `NO-SOURCE` because there are no retained tests in this pass.

## Remaining Risks

- Lifecycle behavior has build coverage only in this pass; focused lifecycle unit tests were not retained because the initial draft test broke Gradle execution.
- Runtime Minecraft smoke on a Paper server was not performed in this pass.
- Next pass should add focused tests or a plugin-load smoke before BaseTransfer work starts.

# VibePrivate API/Lifecycle Pass 2 Evidence

## Scope

Added only focused lifecycle tests/smoke for the existing lifecycle layer before any BaseTransfer work.

Out of scope for this pass:
- VibeBaseTransfer
- VibeSeasonArchive
- snapshot/export
- GUI
- Iris/world generation
- manual relocation commands

## Changed Files

- `build.gradle`
- `src/test/java/com/vibeprivate/service/RegionLifecycleServiceTest.java`
- `docs/IMPLEMENTATION_EVIDENCE.md`

## Acceptance Checks

- Focused lifecycle test exists for enabled `true` fallback -> `ACTIVE`: PASSED.
- Focused lifecycle test exists for enabled `false` fallback -> `INACTIVE`: PASSED.
- Focused lifecycle test verifies `setRegionStatus(..., SEALED)` creates indefinite upkeep pause: PASSED.
- Focused lifecycle test verifies `resumeUpkeep` is forbidden for `SEALED` and `ARCHIVED`: PASSED.
- Focused lifecycle test verifies `ACTIVE` clears status-created pause: PASSED.
- Tests run without Bukkit/Paper runtime: PASSED.
- No BaseTransfer/SeasonArchive/snapshot/GUI/Iris/relocation scope added: PASSED.
- No production Java code changed in this pass: PASSED.
- No reflection added by this pass: PASSED.
- No giant class over 500 lines added in this pass: PASSED.

## Build / Smoke

Command:

```powershell
.\gradlew.bat clean build --no-daemon
```

Result: PASSED on 2026-07-06. The build reported `BUILD SUCCESSFUL in 11s`.

Focused smoke executed inside build:
- `lifecycleSmokeTest`
- JUnit result: `5 tests found`, `5 tests started`, `5 tests successful`, `0 tests failed`

Note:
- A small build-only seam was added in `build.gradle`: `lifecycleSmokeTest` runs the JUnit class through JUnit ConsoleLauncher and is wired into `check`.
- This was kept scoped because the default Gradle `test` worker in this workspace failed to load compiled test classes, while direct JUnit ConsoleLauncher execution worked correctly on the same compiled outputs.

## Remaining Risks

- The pass verifies lifecycle service behavior only; it does not verify plugin load on a live Paper server.
- The default Gradle `test` worker remains unreliable in this workspace, so build verification currently relies on `lifecycleSmokeTest` instead of the standard `test` task execution path.

# VibePrivate API/Selection Pass 3 Evidence

## Scope

Added only read-only API and validation foundation for region bounds and manual selection checks.

Out of scope for this pass:
- `canRelocateRegion`
- `relocateRegion`
- `canMoveRegionToWorld`
- `moveRegionToWorldSameBounds`
- Bukkit events
- GUI
- `/home`
- persistence changes
- snapshot/export
- SeasonArchive/BaseTransfer implementation

## Changed Files

- `build.gradle`
- `docs/IMPLEMENTATION_EVIDENCE.md`
- `src/main/java/com/vibeprivate/VibePrivateServiceFactory.java`
- `src/main/java/com/vibeprivate/VibePrivateServices.java`
- `src/main/java/com/vibeprivate/api/VibePrivateAPI.java`
- `src/main/java/com/vibeprivate/model/SelectionBounds.java`
- `src/main/java/com/vibeprivate/service/RegionManagerSelectionRegionStore.java`
- `src/main/java/com/vibeprivate/service/RegionSelectionRegionStore.java`
- `src/main/java/com/vibeprivate/service/RegionSelectionValidator.java`
- `src/test/java/com/vibeprivate/service/RegionSelectionValidatorTest.java`

## What Was Added

- Typed API method `RegionBounds getRegionBounds(String regionId)`.
- Typed API method `boolean isAreaInsideRegion(String regionId, SelectionBounds bounds)`.
- Typed model `SelectionBounds` with world name and normalized min/max coordinates.
- Separate read-only service `RegionSelectionValidator` so validation logic stays outside `RegionManager`.

## Validation Rules In This Pass

- selection world must match region world;
- selection must be present;
- selection must be fully inside region bounds.

Chosen behavior for invalid selection input:
- `null` selection returns `false`;
- invalid `SelectionBounds` model input such as blank `worldName` throws a controlled `IllegalArgumentException`.

## Acceptance Checks

- API contains `getRegionBounds` and `isAreaInsideRegion`: PASSED.
- Typed `SelectionBounds` model exists: PASSED.
- Selection inside region returns `true`: PASSED.
- Selection outside region returns `false`: PASSED.
- World mismatch returns `false`: PASSED.
- Invalid selection input has explicit documented behavior: PASSED.
- No move/relocate/events/GUI scope was added: PASSED.
- No reflection added by this pass: PASSED.
- No giant class over 500 lines added in this pass: PASSED.

## Build / Smoke

Command:

```powershell
.\gradlew.bat clean build --no-daemon
```

Result: PASSED on 2026-07-06.

Focused smoke executed inside build:
- `RegionLifecycleServiceTest`
- `RegionSelectionValidatorTest`

## Remaining Risks

- This pass validates only region-local bounds checks; it does not yet check world minY/maxY policies or overlap with чужой регион.
- Public API foundation is ready, but move/relocate behavior is intentionally not implemented in this pass.

# VibePrivate API/Selection Pass 4 Evidence

## Scope

Added only the remaining selection validation rules needed before any move/relocate work.

Out of scope for this pass:
- move/relocate implementation
- Bukkit events
- GUI
- `/home`
- persistence changes
- snapshot/archive logic
- BaseTransfer/SeasonArchive work

## Changed Files

- `docs/IMPLEMENTATION_EVIDENCE.md`
- `src/main/java/com/vibeprivate/VibePrivateServiceFactory.java`
- `src/main/java/com/vibeprivate/service/BukkitRegionSelectionWorldHeightProvider.java`
- `src/main/java/com/vibeprivate/service/RegionManagerSelectionRegionStore.java`
- `src/main/java/com/vibeprivate/service/RegionSelectionRegionStore.java`
- `src/main/java/com/vibeprivate/service/RegionSelectionValidator.java`
- `src/main/java/com/vibeprivate/service/RegionSelectionWorldHeight.java`
- `src/main/java/com/vibeprivate/service/RegionSelectionWorldHeightProvider.java`
- `src/test/java/com/vibeprivate/service/RegionSelectionValidatorTest.java`

## Validation Rules Added

- selection must stay within world `minY/maxY` policy;
- selection must not intersect another non-admin region;
- overlap with the same region id does not count as foreign overlap.

Implementation note:
- overlap detection reuses existing `RegionBounds.intersects(...)`;
- region overlap lookup stays read-only through the expanded `RegionSelectionRegionStore` seam;
- world height policy is read-only through a dedicated height provider seam.

## Acceptance Checks

- world minY/maxY policy is enforced: PASSED.
- overlap with another non-admin region is rejected: PASSED.
- pass 3 contract for valid inside/world mismatch/outside own bounds is preserved: PASSED.
- no move/relocate/events/GUI/BaseTransfer/SeasonArchive scope added: PASSED.
- no reflection added by this pass: PASSED.

## Build / Smoke

Commands:

```powershell
.\gradlew.bat clean build --no-daemon
```

Result: PASSED on 2026-07-06.

Focused tests covering this pass:
- valid inside selection returns `true`
- world mismatch returns `false`
- outside own bounds returns `false`
- below world min height returns `false`
- above world max height returns `false`
- overlap with another non-admin region returns `false`
- overlap only with own region remains valid

## Remaining Risks

- This pass does not implement move/relocate execution, only validation foundation.
- World height validation depends on runtime world availability through the new height provider seam.

# VibePrivate API/Relocation Foundation Pass 5 Evidence

## Scope

Added only typed API/service foundation for same-bounds world move and radius relocation inside `VibePrivate`.

Out of scope for this pass:
- Bukkit events
- GUI/commands/messages
- snapshot/export/archive logic
- BaseTransfer/SeasonArchive implementation
- SQL migrations
- cuboid/manual relocation geometry support

## Changed Files

- `build.gradle`
- `docs/ARCHITECTURE_MAP.md`
- `docs/IMPLEMENTATION_EVIDENCE.md`
- `src/main/java/com/vibeprivate/VibePrivateServiceFactory.java`
- `src/main/java/com/vibeprivate/VibePrivateServices.java`
- `src/main/java/com/vibeprivate/api/VibePrivateAPI.java`
- `src/main/java/com/vibeprivate/service/RegionManagerRelocationRegionStore.java`
- `src/main/java/com/vibeprivate/service/RegionRelocationRegionStore.java`
- `src/main/java/com/vibeprivate/service/RegionRelocationService.java`
- `src/test/java/com/vibeprivate/service/RegionRelocationServiceTest.java`

## What Was Added

- Typed API method `boolean canMoveRegionToWorld(String regionId, String targetWorld)`.
- Typed API method `Region moveRegionToWorldSameBounds(String regionId, String targetWorld)`.
- Typed API method `boolean canRelocateRegion(String regionId, String targetWorld, int targetCenterX, int targetCenterZ)`.
- Typed API method `Region relocateRegion(String regionId, String targetWorld, int targetCenterX, int targetCenterZ)`.
- Dedicated `RegionRelocationService` outside `RegionManager`.
- Read-only/write-narrow relocation store seam so service reads regions and applies replacement through a narrow adapter.

## Behavior In This Pass

- `moveRegionToWorldSameBounds` rebuilds a region with the same geometry/state and only changes `worldName`.
- `relocateRegion` currently supports only radius regions and rebuilds them with a new `worldName` and new center while preserving radius, `minY/maxY`, owner, type and persisted state fields.
- `can*` methods do not mutate state.
- Unknown region, disallowed world, overlap and unsupported relocation shape/admin path return `false` from `can*` and throw a controlled exception from action methods.

## Acceptance Checks

- Public API contains the 4 relocation methods from the TZ: PASSED.
- Same-bounds world move preserves coordinates and changes only world: PASSED.
- Radius relocation preserves size/state and changes center/world: PASSED.
- `can*` methods do not mutate state: PASSED.
- Overlap/disallowed/unknown/admin cases are covered by focused tests: PASSED.
- No events/GUI/BaseTransfer/SeasonArchive scope added: PASSED.
- No reflection added by this pass: PASSED.
- `RegionManager` was not expanded with relocation business logic: PASSED.

## Build / Smoke

Command:

```powershell
.\gradlew.bat clean build --no-daemon
```

Result: PASSED on 2026-07-07.

Focused smoke executed inside build:
- `RegionLifecycleServiceTest`
- `RegionSelectionValidatorTest`
- `RegionRelocationServiceTest`

## Focused Tests Added

- same-bounds move preserves radius geometry and state
- unknown region -> `canMove false`, action throws
- disallowed world -> `canMove false`, action throws
- foreign overlap blocks same-bounds world move
- radius relocation preserves size/state
- admin region cannot use relocation API
- foreign overlap blocks relocation
- `can*` methods do not mutate store state

## Remaining Risks

- `relocateRegion` intentionally supports only radius regions in this pass; cuboid/manual relocation geometry is deferred rather than implemented unsafely.
- Action methods still rely on `RegionManager.replaceRegion(...)` as the final mutation gate, so runtime chunk-occupancy constraints remain enforced there in addition to the pass 5 overlap checks.

# VibeRegionGuard Brand Rename Evidence

## Scope

Changed only the external/plugin-visible brand from `VibePrivate` to `VibeRegionGuard`.

Kept intentionally unchanged for compatibility:
- Java packages under `com.vibeprivate.*`
- `VibePrivateAPI`
- `VibePrivatePlugin`
- command keys and permission keys

## Changed Files

- `README.md`
- `settings.gradle`
- `docs/IMPLEMENTATION_EVIDENCE.md`
- `src/main/java/com/vibeprivate/command/CommandMapOverrideService.java`
- `src/main/java/com/vibeprivate/service/ChunkProtectionService.java`
- `src/main/resources/messages/en.yml`
- `src/main/resources/messages/ru.yml`
- `src/main/resources/plugin.yml`

## What Changed

- `plugin.yml` plugin name now resolves to `VibeRegionGuard`.
- Gradle root project name now resolves to `VibeRegionGuard`.
- User-visible help/prefix/GUI/admin strings now use `VibeRegionGuard`.
- Safe log brand strings now use `VibeRegionGuard`.
- Internal Java API/package names remain unchanged on purpose.

## Build / Smoke

Command:

```powershell
.\gradlew.bat clean build --no-daemon
```

Result: PASSED on 2026-07-07.

## Remaining Risks

- Paper plugin data folder name changes from `plugins/VibePrivate` to `plugins/VibeRegionGuard`.
- Existing server data may require manual folder migration and backup before replacing the old branded jar.

# VibePrivate API/Relocation Pass 6 Home World Remap Evidence

## Scope

Added only same-bounds home world remap support in the relocation layer.

Out of scope for this pass:
- GUI/commands/messages/events
- snapshot/BaseTransfer/SeasonArchive
- SQL migrations
- package/class branding changes
- manual `relocateRegion(...)` home offset/remap logic

## Changed Files

- `docs/ARCHITECTURE_MAP.md`
- `docs/IMPLEMENTATION_EVIDENCE.md`
- `src/main/java/com/vibeprivate/VibePrivateServiceFactory.java`
- `src/main/java/com/vibeprivate/service/RegionManagerRelocationRegionStore.java`
- `src/main/java/com/vibeprivate/service/RegionRelocationRegionStore.java`
- `src/main/java/com/vibeprivate/service/RegionRelocationService.java`
- `src/test/java/com/vibeprivate/service/RegionRelocationServiceTest.java`

## What Changed

- `RegionRelocationRegionStore` now exposes narrow home read/save methods for relocation.
- `RegionManagerRelocationRegionStore` now bridges relocation logic to the existing `RegionHomeRepository`.
- `VibePrivateServiceFactory` now passes `regionHomeRepository` into the relocation store adapter.
- `moveRegionToWorldSameBounds(...)` now remaps existing `RegionHome.worldName` to the target world after a successful region replacement.
- Home remap preserves existing `x/y/z/yaw/pitch`.
- No home record means no new home is created.
- `canMoveRegionToWorld(...)` remains read-only and does not mutate home data.

## Acceptance Checks

- Same-bounds move updates home world and keeps coordinates unchanged: PASSED.
- Move without home succeeds and does not create a home: PASSED.
- `can*` methods remain read-only for home data: PASSED.
- Same-world no-op move does not resave home: PASSED.
- No GUI/commands/events/BaseTransfer/SeasonArchive scope added: PASSED.
- No `RegionManager` business logic expansion added by this pass: PASSED.

## Build / Smoke

Command:

```powershell
.\gradlew.bat clean build --no-daemon
```

Result: PASSED on 2026-07-07.

## Focused Tests Added

- same-bounds move remaps existing home world and preserves `x/y/z/yaw/pitch`
- move without home succeeds and does not create a home
- `can*` methods do not mutate home data
- same-world move remains a no-op and does not resave home

## Remaining Risks

- `relocateRegion(...)` still does not remap or offset region home coordinates; safe home translation for manual relocation is deferred instead of guessed.
- Same-bounds world move remaps stored home world only after successful region replacement through `RegionManager.replaceRegion(...)`.

# VibePrivate API/Relocation Pass 7 Home Remap Rollback Safety Evidence

## Scope

Added only local rollback safety for same-bounds home remap during region world move.

Out of scope for this pass:
- GUI/commands/messages/events
- snapshot/BaseTransfer/SeasonArchive
- SQL migrations or shared transaction helpers
- package/class branding changes
- manual `relocateRegion(...)` home offset/remap logic

## Changed Files

- `docs/ARCHITECTURE_MAP.md`
- `docs/IMPLEMENTATION_EVIDENCE.md`
- `src/main/java/com/vibeprivate/service/RegionRelocationService.java`
- `src/test/java/com/vibeprivate/service/RegionRelocationServiceTest.java`

## What Changed

- `moveRegionToWorldSameBounds(...)` still replaces the region first and then remaps home world only if needed.
- If `saveHome(...)` fails after a successful region replacement, relocation now attempts `regionStore.replaceRegion(original)` as a local rollback.
- If rollback also fails, rollback failure is attached as a suppressed exception to the primary home remap failure.
- Public API signatures remain unchanged.
- This pass does not add a shared DB transaction layer; it only reduces inconsistency risk in this specific relocation path.

## Acceptance Checks

- Failed home remap after region replace attempts rollback to original region: PASSED.
- Failed rollback is visible via suppressed exception: PASSED.
- Successful same-bounds move path still replaces once and saves home once: PASSED.
- No GUI/commands/events/BaseTransfer/SeasonArchive scope added: PASSED.
- No `RegionManager` business logic expansion added by this pass: PASSED.

## Build / Smoke

Command:

```powershell
.\gradlew.bat clean build --no-daemon
```

Result: PASSED on 2026-07-07.

## Focused Tests Added

- failing home save rolls region back to original world
- rollback failure is suppressed on primary home save failure
- successful path still replaces once and saves home once

## Remaining Risks

- This is local rollback safety, not a full database transaction across region and home writes.
- If rollback fails, runtime still receives the primary remap exception with suppressed rollback failure, but persisted state may already be partially changed.
- `relocateRegion(...)` still does not remap or offset region home coordinates; safe home translation for manual relocation remains deferred.

# VibePrivate API/CLAN Management Read Pass 8 Evidence

## Scope

Added only the two conservative CLAN management read methods required by the TZ on the unified `VibePrivateAPI`.

Out of scope for this pass:
- GUI/commands/messages/events
- snapshot/export/BaseTransfer/SeasonArchive
- broad CLAN/FARM/PRIVATE subsystem split
- clan member management permissions
- full region-backed clan identity, roles, tag and TAB support

## Changed Files

- `build.gradle`
- `docs/ARCHITECTURE_MAP.md`
- `docs/IMPLEMENTATION_EVIDENCE.md`
- `src/main/java/com/vibeprivate/VibePrivateServiceFactory.java`
- `src/main/java/com/vibeprivate/api/VibePrivateAPI.java`
- `src/main/java/com/vibeprivate/service/ClanRegionManagementRegionStore.java`
- `src/main/java/com/vibeprivate/service/ClanRegionManagementService.java`
- `src/main/java/com/vibeprivate/service/RegionManagerClanRegionManagementRegionStore.java`
- `src/test/java/com/vibeprivate/service/ClanRegionManagementServiceTest.java`

## What Changed

- `VibePrivateAPI` now exposes `isClanRegionLeader(String regionId, UUID playerId)`.
- `VibePrivateAPI` now exposes `canManageClanRegion(String regionId, UUID playerId)`.
- Both methods stay inside the unified region API and use only conservative CLAN-region checks.
- Current leader rule is intentionally narrow: returns `true` only when the region exists, has `RegionType.CLAN`, and `ownerId` exactly equals `playerId.toString()`.
- `canManageClanRegion(...)` currently delegates to the same leader rule and does not grant management to ordinary clan members.
- A tiny helper service keeps `VibePrivateAPI` thin; it is not a separate clan subsystem.
- These semantics are compatibility-only for the current data model, not the final CLAN architecture.

## Acceptance Checks

- Missing region returns `false`: PASSED.
- Non-CLAN region returns `false`: PASSED.
- `isClanRegionLeader(...)` returns `true` only for exact CLAN owner UUID match: PASSED.
- `canManageClanRegion(...)` currently matches `isClanRegionLeader(...)` only: PASSED.
- Null inputs follow existing project style and fail fast: PASSED.
- No GUI/events/BaseTransfer/SeasonArchive scope added: PASSED.

## Build / Smoke

Command:

```powershell
.\gradlew.bat clean build --no-daemon
```

Result: PASSED on 2026-07-07.

## Focused Tests Added

- CLAN owner UUID match returns leader/manage `true`
- external clan id owner string does not grant leader/manage access
- missing region and non-CLAN region return `false`
- null inputs fail fast

## Remaining Risks

- Current leader check only works for CLAN regions whose `ownerId` already stores a player UUID string.
- Existing `createClanRegion(...)` still stores trimmed clan identifier in `ownerId`, so leader/manage checks remain compatibility-only until the next CLAN pass adds proper region-backed clan identity, leader, roles and tag data inside VibeRegionGuard.
- This pass intentionally does not grant management rights to ordinary clan members.

# VibePrivate API/Transfer Surface Pass 9 Evidence

## Scope

Added only the remaining generic target-bounds validation read method on the unified public API.

Out of scope for this pass:
- typed lifecycle/move/archive events
- CLAN identity model, roles, tag or TAB state
- GUI/commands/messages/core work
- BaseTransfer/SeasonArchive implementation
- relocation algorithm changes

## Changed Files

- `docs/ARCHITECTURE_MAP.md`
- `docs/IMPLEMENTATION_EVIDENCE.md`
- `src/main/java/com/vibeprivate/api/VibePrivateAPI.java`
- `src/main/java/com/vibeprivate/service/RegionSelectionValidator.java`
- `src/test/java/com/vibeprivate/service/RegionSelectionValidatorTest.java`

## What Changed

- `VibePrivateAPI` now exposes `isTargetBoundsValid(SelectionBounds bounds)`.
- `RegionSelectionValidator` now provides a generic target-bounds validation path separate from `isAreaInsideRegion(...)`.
- Target-bounds validation reuses the existing read-only selection seam and checks only:
  - non-null/non-empty `SelectionBounds`
  - world height policy through `RegionSelectionWorldHeightProvider`
  - no overlap with another non-admin region in the target world
- Existing region-local selection behavior remains separate in `isAreaInsideRegion(...)`.
- This pass does not add relocation, event or CLAN identity logic.

## Acceptance Checks

- Public API now contains an explicit generic target-bounds validation method: PASSED.
- Free-space target bounds inside world height return `true`: PASSED.
- Target bounds overlapping another non-admin region return `false`: PASSED.
- Target bounds outside world height return `false`: PASSED.
- Existing `isAreaInsideRegion(...)` path remains available and unchanged in scope: PASSED.
- No events/GUI/core/BaseTransfer/SeasonArchive scope added: PASSED.

## Build / Smoke

Command:

```powershell
.\gradlew.bat clean build --no-daemon
```

Result: PASSED on 2026-07-07.

## Focused Tests Added

- valid target bounds in free space return `true`
- target bounds overlapping another non-admin region return `false`
- target bounds outside world height return `false`

## Remaining Risks

- This pass closes only the generic target-bounds read API gap from the transfer surface.
- Typed lifecycle/move/archive events are still missing and remain the next separate checkpoint.
- CLAN identity read data is still not exposed beyond temporary leader/manage compatibility methods; full region-backed CLAN identity stays for the later CLAN checkpoint.

# VibePrivate API/Transfer Surface Pass 10 Evidence

## Scope

Added only typed lifecycle/move/archive Bukkit events required by the transfer/admin API direction.

Out of scope for this pass:
- CLAN identity model, roles, tag or TAB state
- GUI/commands/messages/core work
- BaseTransfer/SeasonArchive implementation
- fuel/deposit/core safety logic
- relocation algorithm changes beyond event publication

## Changed Files

- `build.gradle`
- `docs/ARCHITECTURE_MAP.md`
- `docs/IMPLEMENTATION_EVIDENCE.md`
- `src/main/java/com/vibeprivate/VibePrivateServiceFactory.java`
- `src/main/java/com/vibeprivate/VibePrivateServices.java`
- `src/main/java/com/vibeprivate/api/event/AbstractRegionEvent.java`
- `src/main/java/com/vibeprivate/api/event/RegionArchiveEvent.java`
- `src/main/java/com/vibeprivate/api/event/RegionRelocateEvent.java`
- `src/main/java/com/vibeprivate/api/event/RegionRestoreEvent.java`
- `src/main/java/com/vibeprivate/api/event/RegionSealEvent.java`
- `src/main/java/com/vibeprivate/api/event/RegionStatusChangeEvent.java`
- `src/main/java/com/vibeprivate/api/event/RegionWorldMoveEvent.java`
- `src/main/java/com/vibeprivate/service/BukkitRegionEventDispatcher.java`
- `src/main/java/com/vibeprivate/service/RegionEventDispatcher.java`
- `src/main/java/com/vibeprivate/service/RegionLifecycleService.java`
- `src/main/java/com/vibeprivate/service/RegionRelocationService.java`
- `src/test/java/com/vibeprivate/service/RegionLifecycleServiceEventTest.java`
- `src/test/java/com/vibeprivate/service/RegionLifecycleServiceTest.java`
- `src/test/java/com/vibeprivate/service/RegionRelocationServiceEventTest.java`
- `src/test/java/com/vibeprivate/service/RegionRelocationServiceTest.java`

## What Changed

- Added typed Bukkit events under `com.vibeprivate.api.event` for required public flows:
  - `RegionStatusChangeEvent`
  - `RegionSealEvent`
  - `RegionArchiveEvent`
  - `RegionRestoreEvent`
  - `RegionWorldMoveEvent`
  - `RegionRelocateEvent`
- Added `RegionEventDispatcher` seam plus `BukkitRegionEventDispatcher` so Bukkit event publication stays outside `VibePrivateAPI`, stores and `RegionManager`.
- `RegionLifecycleService` now dispatches typed lifecycle events only after persisted status changes.
- `RegionRelocationService` now dispatches typed move/relocate events only after successful region mutation and successful HOME world remap when needed.
- `build.gradle` smoke now includes focused event tests, and test runtime explicitly includes Paper API so event classes are executable in smoke.

## Acceptance Checks

- Required typed lifecycle/move/archive events now exist as public Bukkit event classes: PASSED.
- `VibePrivateAPI` remains a facade and does not absorb event business logic: PASSED.
- `RegionManager` still does not own lifecycle/transfer event logic: PASSED.
- Sealing publishes generic status event plus typed seal event: PASSED.
- Archiving publishes generic status event plus typed archive event: PASSED.
- Restoring from archived publishes generic status event plus typed restore event: PASSED.
- Same-bounds world move publishes `RegionWorldMoveEvent` only after successful mutation/remap: PASSED.
- Radius relocate publishes `RegionRelocateEvent` only after successful mutation: PASSED.
- No GUI/core/commands/BaseTransfer/SeasonArchive scope added: PASSED.

## Build / Smoke

Command:

```powershell
.\gradlew.bat clean build --no-daemon
```

Result: PASSED on 2026-07-07.

Smoke summary:

- `42 tests found`
- `42 tests started`
- `42 tests successful`
- `0 tests failed`

## Focused Tests Added

- sealing dispatches `RegionStatusChangeEvent` and `RegionSealEvent`
- archiving dispatches `RegionStatusChangeEvent` and `RegionArchiveEvent`
- restoring from archived dispatches `RegionStatusChangeEvent` and `RegionRestoreEvent`
- unchanged status dispatches no lifecycle events
- world move dispatches `RegionWorldMoveEvent` only after successful remap
- relocate dispatches `RegionRelocateEvent`
- can-checks and no-op move dispatch no transfer events

## Remaining Risks

- This pass adds typed event publication only; listeners that consume these events still need future integration work where required.
- CLAN identity remains compatibility-only and still needs a separate region-backed checkpoint.
- Fuel/deposit/core safety events remain intentionally out of scope until later checkpoints.
