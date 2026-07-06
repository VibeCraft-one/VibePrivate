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
