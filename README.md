# ConnectionGuard

**Unstable Connection / Ping Guard** — standalone plugin.

**Author:** muvixo

---

## Features

- Detects high-ping players and kicks them after a configurable grace period
- Warns with chat + title + sound
- TPS-aware: skips checks during lag spikes
- Bypass by permission, name list, or in-game command
- Whitelist mode (only check listed players)
- Force a fake ping for testing
- PlaceholderAPI support
- Fully configurable messages

---

## Commands

| Command | Permission | Description |
|---|---|---|
| `/cg help` | `connectionguard.use` | Show help |
| `/cg creator` | `connectionguard.creator` | Show credits |
| `/cg ping [player]` | `connectionguard.check` | Check ping |
| `/cg status` | `connectionguard.admin` | Show plugin status |
| `/cg on` / `/cg off` / `/cg toggle` | `connectionguard.toggle` | Toggle checker |
| `/cg reload` | `connectionguard.reload` | Reload config |
| `/cg set ping <ms>` | `connectionguard.set` | Change ping threshold |
| `/cg set grace <sec>` | `connectionguard.set` | Change grace period |
| `/cg set interval <ticks>` | `connectionguard.set` | Change check interval |
| `/cg bypass <player>` | `connectionguard.admin` | Add bypass |
| `/cg unbypass <player>` | `connectionguard.admin` | Remove bypass |
| `/cg list` | `connectionguard.admin` | List bypassed |
| `/cg forceping <player> <ms>` | `connectionguard.forceping` | Force a ping |
| `/cg clearping <player>` | `connectionguard.forceping` | Remove forced ping |

---

## Permissions

| Permission | Default | Description |
|---|---|---|
| `connectionguard.*` | op | Everything |
| `connectionguard.use` | true | `/cg help`, `/cg ping` |
| `connectionguard.admin` | op | Admin commands |
| `connectionguard.reload` | op | Reload |
| `connectionguard.toggle` | op | Toggle |
| `connectionguard.bypass` | false | Bypass the checker |
| `connectionguard.check` | true | Check ping |
| `connectionguard.creator` | true | See credits |
| `connectionguard.forceping` | op | Force ping |
| `connectionguard.set` | op | Change settings |

---

## Placeholders (PlaceholderAPI)

| Placeholder | Description |
|---|---|
| `%connectionguard_enabled%` | true / false |
| `%connectionguard_threshold%` | Ping threshold |
| `%connectionguard_grace%` | Grace period |
| `%connectionguard_interval%` | Check interval |
| `%connectionguard_bypassed%` | Number of bypassed players |
| `%connectionguard_ping%` | Player's effective ping |
| `%connectionguard_realping%` | Player's real ping |
| `%connectionguard_bypass%` | true / false |
| `%connectionguard_forced%` | Forced ping value (0 if none) |

---

## Build

```bash
mvn clean package
```

Output: `target/ConnectionGuard-1.0.0.jar`

Or push to GitHub — the workflow in `.github/workflows/build.yml` builds and uploads the artifact.

To publish a release: create a tag like `v1.0.0` and push it.

---

## Config

See `src/main/resources/config.yml`.

Key options:
- `ping` — threshold in ms
- `grace-seconds` — how long above threshold before kick
- `check-interval` — how often to check (ticks)
- `advanced.tps-guard` — skip checks during lag
- `bypass.permission` — permission to bypass
- `whitelist.enabled` — only check listed players
