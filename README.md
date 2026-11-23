# KrownTweaks

**KrownTweaks** is an addon for **Marcely's Bedwars (`MBedwars`)** running on Minecraft 1.21.1 (Paper / Spigot / Purpur).

It introduces a dedicated Quick Buy layout editor (`/shopedit`), direct arena player spectating (`/spectate`), full RGB hex color code support, and native MBedwars add-on integration.

---

Features

- **Quick Buy Layout Editor (`/shopedit`)**:
  - Open and configure the Hypixel-style Quick Buy shop layout anywhere (lobby or in-game).
  - Safety protection: Resource spending and accidental purchases are disabled while editing.
  - Staff command: Open the Quick Buy editor for other players (`/shopedit <player>`).
  - Action bar save confirmation: Layout save alerts appear cleanly on the action bar (no chat spam or system indicator lines).

- **Direct Player Spectate (`/spectate <player>`)**:
  - Jump directly into spectating any online player in an active BedWars match.
  - Live checks to prevent alive players from spectating.
  - Smooth spectator transition and cross-arena transfer.

- **Full RGB & Color Gradients**:
  - Supports all modern hex formats: `&#RRGGBB`, `<#RRGGBB>`, `{#RRGGBB}`, `<color:#RRGGBB>`, `#RRGGBB`.
  - Supports Birdflop gradient output directly.
  - Built-in gradient engine: `<gradient:#hex1:#hex2>Your Text</gradient>`.

- **Native MBedwars Add-on**:
  - Automatically loads and saves all configs into `plugins/MBedwars/add-ons/KrownTweaks/`.
  - Registered as an official `BedwarsAddon` visible under `/bw addon`.

---

Commands & Permissions

| Command | Aliases | Permission | Description |
| :--- | :--- | :--- | :--- |
| `/shopedit [player]` | `/quickbuy`, `/qb`, `/shopeditor` | `krowntweaks.shopedit` (default: true) | Opens Quick Buy editor |
| `/shopedit <player>` | — | `krowntweaks.shopedit.admin` (default: op) | Opens editor for another player |
| `/spectate <player>` | `/spec`, `/bwspectate`, `/watch` | `krowntweaks.spectate` (default: true) | Spectate a player in BedWars |
| `/krowntweaks reload`| `/ktweaks`, `/kt` | `krowntweaks.admin` (default: op) | Reload configuration |

---

Building

Requires Java 21+ and Maven.

```bash
mvn clean package
```

The compiled JAR will be output to `target/KrownTweaks-1.0.1.jar`.
