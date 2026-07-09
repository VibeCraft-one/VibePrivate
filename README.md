# VibeRegionGuard

## Summary

- Purpose: private regions for VibeCraft players without a heavy external region system.
- Features: regions, members, flags, GUI, `/vp`, `/privat`, `/privatadmin`, `/home`, `/sethome`, SQLite/MySQL.
- Compatibility: internal Java package/API stay as `com.vibeprivate` and `VibePrivateAPI`; external integrations should prefer the thin `VibeRegionGuardApi` facade.
- Risks: protection bugs can allow griefing or block players; DB migrations and command compatibility need separate smoke checks.
- Current state: API/lifecycle/transfer foundation, admin GUI pagination, CLAN role foundation, fuel/deposit hardening, lookup smoke and protection smoke are implemented and covered by focused checks.
- RC gate: live manual Paper/Purpur player smoke is still required before calling this tester-ready.

## Build

```bash
./gradlew build
```

Focused smoke runner:

```bash
./gradlew lifecycleSmokeTest
```

Note: the standard Gradle `test` task is intentionally disabled in this repo. `build` runs `check`, and `check` depends on `lifecycleSmokeTest`.

## Development Start

1. Open `docs/ARCHITECTURE_MAP.md`.
2. Check `git status --short --branch`.
3. Work on one layer per pass: API, lifecycle, storage, GUI, commands or protection.
4. For external integrations, use `getVibeRegionGuardApi()` or `getApi()`, not repositories, SQL or reflection.

## Minimal Manual Check

1. Install the jar on Paper/Purpur 1.21.x.
2. Verify `/vp`, region creation and member add/remove.
3. Verify protection flags: blocks, containers, damage and teleport.
4. Restart the server and verify regions, home and fuel/deposit persistence.
5. Use `docs/MANUAL_PLAYER_SMOKE_RUNBOOK.md` for the full RC smoke.
