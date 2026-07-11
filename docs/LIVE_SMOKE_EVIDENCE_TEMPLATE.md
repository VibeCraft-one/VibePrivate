# Live Smoke Evidence Template

Purpose: record the manual Paper/Purpur smoke facts needed before calling this tester-ready RC.

Copy this file to a dated note or fill it in-place during a disposable test-server run. Do not use production data.

## Run Metadata

| Field | Value |
| --- | --- |
| Date | |
| Tester | |
| Server type | Paper / Purpur |
| Server version/build | |
| Java version | |
| Plugin jar path | |
| Plugin git commit | |
| Test world/data reset before run | yes / no |

## Required Test Accounts

| Role | Minecraft name | UUID if known | Notes |
| --- | --- | --- | --- |
| PlayerA | | | normal player and region owner |
| PlayerB | | | guest/member checks |
| Admin | | | operator or `vibeprivate.admin` |

## Evidence Checklist

| Area | Result | Evidence note |
| --- | --- | --- |
| Server starts and enables `VibeRegionGuard v0.1.2` | PASS / FAIL | |
| `/plugins` lists `VibeRegionGuard` | PASS / FAIL | |
| `/vp` works for `PlayerA` | PASS / FAIL | |
| `/privat` works for `PlayerA` | PASS / FAIL | |
| `/privatadmin` is admin-only | PASS / FAIL | |
| `/home` shows clear missing-state or teleports after setup | PASS / FAIL | |
| `/sethome` shows clear missing-state or sets home inside HOME | PASS / FAIL | |
| HOME creation creates exactly one HOME | PASS / FAIL | |
| HOME survives clean restart | PASS / FAIL | |
| FARM creation works when allowed | PASS / FAIL | |
| FARM limit rejection does not create extra regions | PASS / FAIL | |
| Guest block break/place is blocked | PASS / FAIL | |
| Guest container use is blocked | PASS / FAIL | |
| Guest damage/entity interactions are blocked as configured | PASS / FAIL | |
| Member flag enables only the intended action | PASS / FAIL | |
| `/vp` or `/privat` GUI opens | PASS / FAIL | |
| Region detail flag toggle persists after reopening | PASS / FAIL | |
| Member add/remove works and persists after reopening | PASS / FAIL | |
| Fuel/deposit GUI opens without errors | PASS / FAIL | |
| Valid fuel consumes exactly expected items | PASS / FAIL | |
| Invalid fuel is rejected without consuming | PASS / FAIL | |
| Deposit add/withdraw does not duplicate items | PASS / FAIL | |
| Fuel/deposit survives restart | PASS / FAIL | |
| Admin region list opens | PASS / FAIL | |
| Admin pagination works with 46+ entries if available | PASS / FAIL / NOT RUN | |
| CLAN leader/officer/member role behavior works if API helper exists | PASS / FAIL / NOT RUN | |

## Final Log Scan

Run from the test server folder after stopping cleanly:

```powershell
Select-String -Path ".\logs\latest.log" -Pattern "ERROR","SEVERE","Exception","Failed","Unknown command"
```

Result:

```text
Paste matching lines here, or write: no matches.
```

## Final Decision

Manual smoke result:

```text
PASS / FAIL
```

Blocking issues:

```text
List exact steps, expected result, actual result and log lines.
```

Non-blocking notes:

```text
List unclear messages, UX rough edges or optional follow-ups.
```
