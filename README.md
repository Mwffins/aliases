# Aliases

A lightweight, server-side Fabric mod that lets you create custom command shortcuts and aliases.

If you miss classic Spigot commands like `/gmc`, `/gms`, `/day`, or `/feed`, Aliases lets you add them in seconds. Define your shortcuts in `config/aliases.json` or manage them directly in-game.

## Features
- **100% Server-side**: Clients don't need to install anything.
- **Native Tab-Completion**: Auto-completes in game chat just like vanilla commands.
- **In-Game Commands**: Manage aliases on the fly with `/aliases add`, `/aliases remove`, `/aliases list`, and `/aliases reload`.
- **Arguments & Fallbacks**: Supports `$args` for argument pass-through, positional arguments (`$0`, `$1`), and fallbacks (`${args|@s}`).
- **Loop Protection**: Built-in protection against circular alias loops and infinite recursion.

## Admin Commands
- `/aliases add <name> <target>` - Add or update an alias.
- `/aliases remove <name>` - Delete an alias.
- `/aliases list` - View all active aliases.
- `/aliases reload` - Reload config from disk.

## Building
```bash
./gradlew build
```
