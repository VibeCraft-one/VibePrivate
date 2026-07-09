# VibeRegionGuard RC Readiness Matrix

Purpose: map the release-candidate acceptance list to current evidence and the smallest remaining checks.

Status legend:

- `PROVEN`: covered by build, automated smoke or recorded server bootstrap evidence.
- `PARTIAL`: foundation exists, but live server behavior or edge cases still need proof.
- `OPEN`: not proven enough for tester-ready RC.

## Acceptance Matrix

| Requirement | Status | Evidence | Next check |
| --- | --- | --- | --- |
| Server starts without plugin errors | `PROVEN` | Paper 1.21.11 build 132 and Purpur 1.21.11 build 2568 boot/restart evidence recorded in `docs/IMPLEMENTATION_EVIDENCE.md`; current jar bootstrap refresh passed on both after the latest code change. | Re-run during final manual smoke with a real player. |
| `plugin.yml` loads `VibeRegionGuard` | `PROVEN` | Headless Paper/Purpur startup enabled `VibeRegionGuard` v0.1.1. | Confirm `/plugins` on live smoke server. |
| `/vp`, `/privat`, `/privatadmin`, `/home`, `/sethome` are registered | `PARTIAL` | Bootstrap command probes passed; latest Paper/Purpur bootstrap claimed `/home` and `/sethome`; `PluginDescriptorSmokeTest` locks `plugin.yml` command declarations. | Run commands as real player/admin in `docs/MANUAL_PLAYER_SMOKE_RUNBOOK.md`. |
| Player can create a HOME region | `OPEN` | Core region services exist; no live player creation proof yet. | Manual HOME create, `/sethome`, `/home`, restart persistence. |
| Player can create a FARM region when allowed | `OPEN` | Region type exists; no live FARM command/UI proof yet. | Manual FARM create and limit rejection smoke. |
| CLAN region has clear identity/roles model | `PARTIAL` | CLAN role foundation and stale role access fix are committed and tested. | Live/API helper smoke for leader/officer/member management and role removal. |
| Block/container/damage/teleport protection work | `PARTIAL` | SQLite-backed protection service smoke covers HOME owner/member/guest, bypass, environment flags, ADMIN priority and cached-HOME versus later ADMIN overlap priority. | Manual Bukkit event smoke for blocks, containers, entities, damage, liquids and teleport. |
| Fuel/deposit flows do not duplicate items | `PARTIAL` | Persistence hardening and storage cascade tests cover fail-closed paths, delete cascade and rollback of spawned deposit items when deletion fails after drop start. SQLite schema smoke verifies required tables, indexes and foreign-key enforcement. | Manual inventory click/restart/reconnect smoke. |
| GUI does not corrupt data or write directly to SQL | `PARTIAL` | GUI code uses services/API boundaries; admin pagination is implemented and reviewed. | Manual GUI click smoke for region detail, members, fuel/deposit and admin pagination. |
| Admin can inspect player regions with pagination | `PARTIAL` | Admin GUI pagination is implemented, reviewed and committed. | Live 46+ region/owner pagination smoke. |
| API lets external transfer plugin read/move/relocate without DB/reflection | `PROVEN` | `VibePrivateAPI` and thin `VibeRegionGuardApi` facade expose reads, validation, move/relocate primitives and events. `VibeRegionGuardApiTest` locks the external transfer/read method surface and behaviorally verifies delegated world reads, bounds validation and same-bounds world move. Architecture docs ban SQL/reflection for integrations. | Keep transfer orchestration outside this plugin; add integration helper only if manual smoke needs it. |
| Packaged config/messages are loadable and aligned | `PROVEN` | `ResourceYamlSmokeTest` verifies config runtime keys, required message keys and RU/EN message key parity. | Live smoke still verifies readability in real commands/GUI. |
| `.\gradlew.bat clean build --no-daemon` passes | `PROVEN` | Latest recorded full build passed on 2026-07-09 with 77/77 tests. | Re-run only after another code/resource change or immediately before tagging RC. |
| Manual Paper/Purpur smoke passes | `OPEN` | Runbook and evidence template exist. Live player execution is not recorded yet. | Execute `docs/MANUAL_PLAYER_SMOKE_RUNBOOK.md` and record pass/fail evidence in `docs/LIVE_SMOKE_EVIDENCE_TEMPLATE.md`. |

## Storage Notes

- SQLite migration is covered by automated smoke tests, including additive upgrade coverage for legacy `regions` tables.
- MySQL migration still requires a disposable MySQL database before public release; do not use production credentials or production data for this check.

## Current Release Gate

The project is close to tester-ready, but it should not be called a release candidate until the manual player smoke has real pass/fail evidence. The remaining risk is not mostly Java compilation; it is live Bukkit behavior: player commands, inventory clicks, protection events and persistence after writes.

## RU note for owner

Полные тесты можно не гонять после каждого маленького документационного прохода. Безопасный путь сейчас: меньше автотестов на промежуточных шагах, но перед финальным RC обязательно один полный `.\gradlew.bat clean build --no-daemon` и живой Paper/Purpur smoke по runbook.
