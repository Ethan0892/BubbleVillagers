# BubbleVillagers

Turns off the AI of villagers in trading halls so they stop costing tick time, while they keep trading, restocking and levelling up.

This is a fork of [Ethan0892/BubbleVillagers](https://github.com/Ethan0892/BubbleVillagers), which continues [xGinko/VillagerOptimizer](https://github.com/xGinko/VillagerOptimizer). It targets Paper 26.2 and carries fixes that are proposed upstream but not released yet. See [What this fork changes](#what-this-fork-changes).

## Why

Every tick a villager runs its brain: sensors, pathfinding, job site lookups. Optimizing a villager turns that off with `Mob#setAware(false)`. Measured on Paper 26.2 with 300 villagers:

| | Average tick time |
|---|---|
| 300 villagers with AI | 10.5 ms (21% of the 50 ms tick budget) |
| 300 optimized villagers | 0.5 ms |

An optimized villager still trades, restocks and levels up, and still falls and gets pushed by water, since only the AI is off. It no longer walks, looks around, works, sleeps, breeds, gossips, panics or summons iron golems.

## Requirements

- Paper 26.2, or a Paper fork such as Folia or Purpur. The plugin declares `api-version: 26.2` and will not load on 26.1.x; use 2.1.8 there.
- Java 25.

## Installation

1. Download `BubbleVillagers-2.2.0.jar` from the [releases page](https://github.com/ghiacciolodev/BubbleVillagers/releases).
2. Put it in `plugins/`. When upgrading, remove the old jar first; two copies of the plugin won't load.
3. Restart the server. Don't use `/reload`.
4. Edit `plugins/BubbleVillagers/config.yml`, then run `/villageroptimizer reload`.

### Upgrading from 2.1.x

Your `config.yml` is kept as it is: the plugin only adds options that are missing and never overwrites a value already in the file. Two defaults changed in 2.2.0, so on an upgraded server you have to apply them yourself:

- **The restock chat message stays on**, because your file already says `notify-player: true`. Set `gameplay.restock-optimized-trades.notify-player` to `false` to turn it off.
- **The restock times stay at 07:00 and 19:00**, because your file already lists `1000` and `13000`. For the new 07:00, 12:00 and 17:00 set `restock-times` to `1000`, `6000`, `11000`.

Alternatively, delete the whole `restock-optimized-trades` section and run `/villageroptimizer reload`: the plugin writes it back with the new defaults.

Older versions also shipped a `config.yml` with sections that nothing reads: top-level `language`, `optimization`, `cooldowns`, `methods`, `commands`, `notifications`, `performance`, `advanced` and `integrations` (WorldGuard, Vault, database, sounds). They are harmless and can be deleted. Everything that works is listed under [Configuration](#configuration).

## Optimizing villagers

**Name tag** (on by default). Rename a villager with a name tag called `Optimize` or `DisableAI`, case-insensitive. Using a name tag with any other name on an optimized villager turns its AI back on. Name tags are consumed.

**Block** (off by default). While sneaking, place one of the configured blocks within 2 blocks of a villager: the closest unoptimized one is optimized. Break the block while sneaking to undo it.

**Workstation** (off by default). While sneaking, place a workstation: the closest villager that claims it within 100 ticks is optimized. Break it while sneaking to undo it. Handy while rolling trades.

**Command.** `/optimizevillagers <radius>` and `/unoptimizevillagers <radius>` act on every villager within the radius, up to 100 blocks.

**Automatic** (all off by default):

- `auto-optimize-chunk-population` optimizes the villagers of any chunk holding more than 20. It reacts to chunk loads, villager spawns and clicks, and also rescans every loaded chunk every `check-period-ticks`. On large servers raise that period: the rescan touches every loaded chunk.
- `auto-optimize-trade-hall-population` does the same, counting villagers within 16 blocks instead of per chunk.
- `regional-activity` watches 500-block regions and optimizes the villagers of a region once they pathfind more than 150 times, or are clicked more than 50 times, within 10 seconds.

Each method can be denied per player with its permission, see [Permissions](#permissions).

## Restocking

An optimized villager can't walk to its workstation, so the plugin restocks it instead, at fixed times of the in-game day listed in `gameplay.restock-optimized-trades.restock-times`.

- Each time in the list opens a restock window at that time of day.
- The restock happens when a player opens the villager after the window opened, before the trading screen appears, so the trades are already full when you look at them.
- A window restocks a villager once. Opening a villager whose trades are all full does not use it up.
- Villagers nobody opens are left alone, which costs nothing.

Times are ticks of the day. Tick 0 is 06:00 and every hour is 1000 ticks:

| Ticks | 0 | 1000 | 6000 | 11000 | 13000 | 18000 |
|---|---|---|---|---|---|---|
| Time | 06:00 | 07:00 | 12:00 | 17:00 | 19:00 | 00:00 |

The default `[1000, 6000, 11000]` restocks three times a day, at 07:00, 12:00 and 17:00. Vanilla villagers restock twice a day; to get close to that, use 07:00 and 19:00:

```yaml
gameplay:
  restock-optimized-trades:
    restock-times:
    - 1000
    - 13000
```

The chat message telling players when the next window opens is off by default; turn on `notify-player` to show it. Players with `villageroptimizer.bypass.restockcooldown` get a restock on every click.

## Levelling up

When you close the trading screen of an optimized villager that has enough experience for its next level, it gets its AI back for 5 seconds so the vanilla level-up can run, then it is optimized again. If the chunk unloads or the server stops during those 5 seconds, the villager is optimized again the next time it loads.

## Commands

| Command | Aliases | Permission | What it does |
|---|---|---|---|
| `/optimizevillagers <radius>` | `optvils`, `noai`, `bubbleoptimize`, `bvopt` | `bubblevillagers.optimize` | Optimizes the villagers around you. |
| `/unoptimizevillagers <radius>` | `unoptvils`, `noaiundo`, `bubbleunoptimize`, `bvunopt` | `bubblevillagers.unoptimize` | Turns their AI back on. |
| `/villageroptimizer reload` | `vo`, `voptimizer`, `bv`, `bubblevillagers` | `villageroptimizer.cmd.reload` | Reloads the configuration and language files. |
| `/villageroptimizer version` | | `villageroptimizer.cmd.version` | Shows the plugin version. |
| `/villageroptimizer disable` | | `villageroptimizer.cmd.disable` | Stops every listener and task until the next reload. |
| `/villageroptimizer glow <on\|off>` | | `villageroptimizer.cmd.glow` | Makes optimized villagers glow, by toggling `outline-optimized-villagers`. |
| `/villageroptimizer clearglow` | | `villageroptimizer.cmd.clearglow` | Removes the glow from optimized villagers in loaded chunks. |

`/villageroptimizer` itself also needs `bubblevillagers.admin`.

## Permissions

| Permission | Default | Grants |
|---|---|---|
| `bubblevillagers.*` | op | All three below. |
| `bubblevillagers.admin` | op | `/villageroptimizer` |
| `bubblevillagers.optimize` | op | `/optimizevillagers` |
| `bubblevillagers.unoptimize` | op | `/unoptimizevillagers` |
| `villageroptimizer.cmd.reload`, `.version`, `.disable`, `.glow`, `.clearglow` | op | The matching subcommand. |
| `villageroptimizer.optimize.nametag` | everyone | Optimizing with name tags. |
| `villageroptimizer.optimize.block` | everyone | Optimizing with blocks. |
| `villageroptimizer.optimize.workstation` | everyone | Optimizing with workstations. |

Bypass permissions are given to nobody by default, operators included:

| Permission | Effect |
|---|---|
| `villageroptimizer.bypass.restockcooldown` | Restocks on every click, ignoring the windows. |
| `villageroptimizer.bypass.nametagcooldown` | Skips the 600 s cooldown before a villager can be optimized again with a name tag. |
| `villageroptimizer.bypass.blockcooldown` | Same, for blocks. |
| `villageroptimizer.bypass.workstationcooldown` | Same, for workstations. |
| `villageroptimizer.bypass.commandcooldown` | Same, for `/optimizevillagers`. |
| `villageroptimizer.bypass.tradeprevention` | Trades with unoptimized villagers while `prevent-trading-with-unoptimized` is on. |

## Configuration

The plugin writes every option into `config.yml` on first start, without comments. This section is the reference. Run `/villageroptimizer reload` after editing.

### general

| Option | Default | Description |
|---|---|---|
| `default-language` | `en_us` | Language used when `auto-language` is off or the client's language has no file. |
| `auto-language` | `true` | Show messages in each player's client language. |
| `cache-keep-time-seconds` | `30` | How long a villager stays in the plugin's cache. |
| `support-avl-villagers` | `false` | Read the optimization state written by AntiVillagerLag, so villagers optimized with it stay optimized. |

### optimization-methods

| Option | Default | Description |
|---|---|---|
| `nametag-optimization.enable` | `true` | Optimize with name tags. |
| `nametag-optimization.names` | `[Optimize, DisableAI]` | Name tag names that optimize, case-insensitive. |
| `nametag-optimization.nametags-get-consumed` | `true` | Use up the name tag. |
| `nametag-optimization.optimize-cooldown-seconds` | `600` | Before a villager can be optimized again this way. |
| `nametag-optimization.notify-player` | `true` | Chat message on success. |
| `block-optimization.enable` | `false` | Optimize by placing blocks. |
| `block-optimization.materials` | `[LAPIS_BLOCK, GLOWSTONE, IRON_BLOCK]` | Blocks that optimize. Bukkit `Material` names. |
| `block-optimization.search-radius-in-blocks` | `2.0` | How far the villager can be from the player. |
| `block-optimization.only-when-sneaking` | `true` | Only while sneaking. |
| `block-optimization.optimize-cooldown-seconds` | `600` | Before a villager can be optimized again this way. |
| `block-optimization.notify-player` | `true` | Chat message on success. |
| `workstation-optimization.enable` | `false` | Optimize by placing workstations. |
| `workstation-optimization.check-linger-duration-ticks` | `100` | How long to wait for a villager to claim the workstation. Keep at 100 or more. |
| `workstation-optimization.search-radius-in-blocks` | `2.0` | How far the villager can be from the player. |
| `workstation-optimization.only-when-sneaking` | `true` | Only while sneaking. |
| `workstation-optimization.optimize-cooldown-seconds` | `600` | Before a villager can be optimized again this way. |
| `workstation-optimization.notify-player` | `true` | Chat message on success. |
| `commands.optimizevillagers.max-block-radius` | `100` | Largest radius for `/optimizevillagers`. |
| `commands.optimizevillagers.cooldown-seconds` | `600` | Before a villager can be optimized again by command. |
| `commands.unoptimizevillagers.max-block-radius` | `100` | Largest radius for `/unoptimizevillagers`. |
| `auto-optimize-chunk-population.enable` | `false` | See [Automatic](#optimizing-villagers). |
| `auto-optimize-chunk-population.threshold` | `20` | Villagers per chunk above which they get optimized. |
| `auto-optimize-chunk-population.check-period-ticks` | `200` | How often every loaded chunk is rescanned. |
| `auto-optimize-trade-hall-population.enable` | `false` | See [Automatic](#optimizing-villagers). |
| `auto-optimize-trade-hall-population.population` | `20` | Villagers within the radius that trigger optimization. |
| `auto-optimize-trade-hall-population.radius-blocks` | `16.0` | Size of a trade hall. |
| `auto-optimize-trade-hall-population.check-period-ticks` | `200` | How often loaded chunks are rescanned. |
| `regional-activity.enable` | `false` | See [Automatic](#optimizing-villagers). |
| `regional-activity.check-radius-blocks` | `500.0` | Size of a region. |
| `regional-activity.data-keep-time-millis` | `10000` | Time window for the limits below. |
| `regional-activity.limits.pathfind-event` | `150` | Villager pathfinds per region and window. |
| `regional-activity.limits.interact-event` | `50` | Clicks on villagers per region and window. |
| `regional-activity.notify-players` | `true` | Tell nearby players their villagers were optimized. |

Every method also has a `log` option, off by default, that writes each action to the console.

### gameplay

| Option | Default | Description |
|---|---|---|
| `restock-optimized-trades.restock-times` | `[1000, 6000, 11000]` | Restock windows, 07:00, 12:00 and 17:00. See [Restocking](#restocking). |
| `restock-optimized-trades.notify-player` | `false` | Chat message when a villager is restocked. |
| `restock-optimized-trades.log` | `false` | Log restocks to the console. |
| `level-optimized-profession.level-check-cooldown-seconds` | `5` | How often a villager's level is checked. |
| `level-optimized-profession.notify-player` | `true` | Tell players to wait while a villager levels up. |
| `prevent-trading-with-unoptimized.enable` | `false` | Players can only trade with optimized villagers. |
| `prevent-trading-with-unoptimized.notify-player` | `true` | Tell them why. |
| `unoptimize-on-job-loose.enable` | `true` | An optimized villager that loses its profession gets its AI back. |
| `villagers-can-be-leashed.enable` | `false` | Allow leashing villagers with a lead. |
| `villagers-can-be-leashed.only-optimized` | `false` | Only optimized ones. |
| `villagers-can-be-leashed.log` | `false` | Log leashing to the console. |
| `villagers-spawn-as-adults.enable` | `false` | Villagers spawn as adults instead of babies. |
| `outline-optimized-villagers.enable` | `false` | Optimized villagers glow. |
| `prevent-entities-from-targeting-optimized.enable` | `true` | Mobs ignore optimized villagers, which can't flee. |
| `prevent-damage-to-optimized.enable` | `true` | Optimized villagers don't take the damage listed below. |
| `prevent-damage-to-optimized.prevent-knockback-from-entity` | `true` | Nor knockback from attacks. |
| `prevent-damage-to-optimized.damage-causes-to-cancel` | every cause | Bukkit `DamageCause` names. Remove the ones you want to allow. |
| `rename-optimized-villagers.enable` | `false` | Has no effect: there is no feature behind it. |

### villager-chunk-limit

**This feature deletes villagers**, without drops, when a chunk holds more than the limit. It is off by default.

| Option | Default | Description |
|---|---|---|
| `enable` | `false` | Turn the limit on. |
| `check-period-in-ticks` | `600` | How often loaded chunks are checked. |
| `skip-not-fully-loaded-chunks` | `true` | Skip chunks whose entities aren't loaded yet. |
| `chunk-check-cooldown-seconds` | `5` | Before the same chunk is checked again. |
| `log-removals` | `true` | Log every removal to the console. |
| `whitelist.enable` | `false` | Protect the professions below. |
| `whitelist.professions` | `[NONE, NITWIT]` | Professions never removed while the whitelist is on. |
| `unoptimized.max-per-chunk` | `20` | Limit for villagers with AI. |
| `unoptimized.removal-priority` | all professions | Professions higher in the list are removed first. |
| `optimized.max-per-chunk` | `60` | Limit for optimized villagers. |
| `optimized.removal-priority` | all professions | Professions higher in the list are removed first. |

## Languages

`en_us`, `de_de`, `it_it`, `ko_kr`, `ru_ru`, `zh_cn`. The files are in `plugins/BubbleVillagers/lang/` and use [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting.

## What this fork changes

Compared to Ethan0892/BubbleVillagers 2.1.8. Details in the [changelog](CHANGELOG.md).

- Restocked trades showed the red X until the trading screen was reopened. Proposed upstream in [#1](https://github.com/Ethan0892/BubbleVillagers/pull/1).
- Opening a villager with full trades used up its restock window. Proposed upstream in [#2](https://github.com/Ethan0892/BubbleVillagers/pull/2).
- Optimized villagers came back with their AI on after a chunk unload or a server restart.
- "Next restock in ..." showed the wrong time after the first restock of the day.
- Villagers stopped restocking after a numeric `/time set`.
- Optimized villagers restock three times a day by default, at 07:00, 12:00 and 17:00.
- The restock chat message is off by default.
- `config.yml` no longer ships options that nothing reads.
- Built against Paper 26.2.

## Building

```bash
mvn clean package
```

Needs JDK 25. The jar ends up in `target/BubbleVillagers-2.2.0.jar`. The bundled tests are disabled because MockBukkit doesn't support Paper 26.x yet.

## Credits and license

MIT licensed. Originally written by [xGinko](https://github.com/xGinko) as VillagerOptimizer, continued by Ξthan as BubbleVillagers. This fork is maintained by [ghiacciolodev](https://github.com/ghiacciolodev); report problems in its [issues](https://github.com/ghiacciolodev/BubbleVillagers/issues).
