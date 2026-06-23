# VibePrivate Architecture

VibePrivate is the public plugin identity and the forward path for the project.

The old `com.viberegion` package is kept temporarily as prototype code while the
new implementation is built in small, compile-safe passes. It is not the active
plugin entrypoint.

Active plugin entrypoint:

- `com.vibeprivate.VibePrivatePlugin`

Active resources:

- `plugin.yml`
- `config.yml`
- `messages/ru.yml`
- `messages/en.yml`

Current pass responsibilities:

- `config` loads stable configuration values.
- `message` loads visible text from language files.
- `storage` owns SQLite connection and schema migration.
- `command` routes `/vp`, `/privat`, and help.

Next passes should add new production code under `com.vibeprivate.*`.
Old `com.viberegion.*` classes should only be copied from deliberately, then
removed once replacement systems are complete.
