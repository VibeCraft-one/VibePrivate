# Manual Player Smoke Runbook

Purpose: verify the parts that unit tests and headless Paper/Purpur boot cannot prove.

Record pass/fail facts in `docs/LIVE_SMOKE_EVIDENCE_TEMPLATE.md` while running this checklist.

## Scope

Run this on a disposable Paper or Purpur test server with the current `VibeRegionGuard-0.1.2.jar`.

Required testers:

- `PlayerA`: normal player.
- `PlayerB`: normal player for guest/member checks.
- `Admin`: operator or player with `vibeprivate.admin`.

## Preflight

1. Start the server with a clean test world and this plugin installed.
2. Confirm console shows `Enabling VibeRegionGuard v0.1.2`.
3. Confirm `/plugins` lists `VibeRegionGuard`.
4. Confirm `plugins/VibeRegionGuard/config.yml` and `vibeprivate.db` exist.
5. Do not run this on production data.

## Command Smoke

As `PlayerA`:

1. Run `/vp`.
2. Run `/privat`.
3. Run `/home`.
4. Run `/sethome`.

Expected:

- Commands are known.
- Player-only commands do not produce console-only errors.
- Missing HOME/home-location states show a clear player message, not a stacktrace.

As `Admin`:

1. Run `/privatadmin`.

Expected:

- Admin GUI or admin feedback opens without console errors.
- A non-admin player should not get admin access.

## HOME Create And Persistence

As `PlayerA`:

1. Create a HOME region through the normal UI/command flow.
2. Run `/sethome` inside the HOME region.
3. Run `/home` from another location.
4. Stop the server cleanly.
5. Start the server again.
6. Run `/home` again.

Expected:

- HOME is created once.
- HOME survives restart.
- `/home` teleports after restart.
- Console has no plugin `ERROR`, `SEVERE`, or stacktrace.

## FARM Create

As `PlayerA`:

1. Create a FARM region where rules/permissions allow it.
2. Try creating beyond configured limits.

Expected:

- Allowed FARM creation succeeds.
- Limit rejection is clear and does not create extra regions.
- FARM survives restart.

## Protection Smoke

With `PlayerA` owning a HOME and `PlayerB` not added:

1. `PlayerB` tries to break a block inside `PlayerA` HOME.
2. `PlayerB` tries to place a block inside `PlayerA` HOME.
3. `PlayerB` tries to open chest/barrel/furnace/workstation inside `PlayerA` HOME.
4. `PlayerB` tries to damage `PlayerA`, an armor stand, an item frame and a passive mob inside the region.
5. `PlayerB` tries liquid placement near the region boundary.

Expected:

- Guest actions are blocked according to flags.
- Deny messages are rate-limited and readable.
- No protection bypass through containers/entities/liquids.

Then add `PlayerB` as a member and enable one safe flag, such as build.

Expected:

- Enabled member flag allows only that action.
- Other protected actions remain blocked.

## GUI Smoke

As `PlayerA`:

1. Open `/vp` or `/privat`.
2. Open region detail.
3. Toggle one default flag.
4. Open members UI.
5. Add/remove `PlayerB`.
6. Open resource/fuel/deposit UI.

Expected:

- GUI opens without console errors.
- Clicks do not duplicate actions.
- GUI reflects changed flags/members after reopening.
- GUI does not write directly to SQL outside service flow.

As `Admin`:

1. Open `/privatadmin`.
2. Inspect all regions.
3. Inspect a player with many regions if available.
4. Use previous/next pagination.

Expected:

- Pagination works.
- Pages do not skip or duplicate entries.
- No console errors.

## Fuel And Deposit Smoke

As `PlayerA`:

1. Add valid fuel from hand.
2. Try invalid fuel.
3. Add upgrade deposit item from hand.
4. Withdraw excess deposit if possible.
5. Restart the server.

Expected:

- Valid fuel/deposit consumes exactly the intended item count.
- Invalid item is rejected without consuming.
- Withdrawal returns the expected items once.
- Fuel/deposit state survives restart.
- No item duplication after reconnect/restart.

## CLAN Smoke

Use an API caller or test helper plugin if available.

1. Create a CLAN region with a leader.
2. Verify leader can manage it.
3. Add officer/member roles.
4. Remove or sync out an elevated player.

Expected:

- LEADER/OFFICER can manage.
- MEMBER cannot manage elevated actions.
- Removed/synced-out elevated players lose management.

## Final Log Check

After the run:

1. Stop the server cleanly.
2. Open `logs/latest.log`.
3. Search for `ERROR`, `SEVERE`, `Exception`, `Failed`, `Unknown command`.
4. Paste matches or `no matches` into `docs/LIVE_SMOKE_EVIDENCE_TEMPLATE.md`.

Expected:

- No VibeRegionGuard stacktraces.
- Any failed command is a clear user-facing rejection, not a crash.

## Pass Criteria

Manual smoke passes only if:

- Paper or Purpur boot/restart succeeds.
- Core commands are known.
- HOME create, `/sethome`, `/home` and restart persistence work.
- Protection blocks guest actions and allows configured member/owner actions.
- GUI opens and core clicks do not corrupt data.
- Fuel/deposit flows do not duplicate items.
- Logs contain no plugin errors.
