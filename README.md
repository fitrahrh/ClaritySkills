# ClaritySkills

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg?style=for-the-badge)](LICENSE)
[![Platform](https://img.shields.io/badge/Paper-26.3+-red.svg?style=for-the-badge)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-25-orange.svg?style=for-the-badge)](https://openjdk.org)

**ClaritySkills** is a next-generation leveling, attribute allocation, and elemental mastery plugin custom-engineered for **Clarity Network** (Minecraft 1.21.4+ / Paper 26.3+). Inspired by AuraSkills and purpose-built as the foundational stat provider for **ClarityRPG**, it leverages **Paper Native Dialog API** and **Adventure MiniMessage** to deliver a sleek, zero-latency user experience rendered entirely through native client dialogs without chest GUI hacks or virtual inventory tricks.

---

## Features

### 🎮 1. Native ESC / Pause Menu Button ("Skills Menu")
* Adds a native button directly onto Minecraft's Game Menu (ESC screen) via the `#minecraft:pause_screen_additions` dialog tag.
* Seamless client integration registered through Paper's `PluginBootstrap` and lifecycle registry events.
* Allows players to access their skill dashboard, allocate stat points, and view elemental masteries with zero chat commands required.

### ⚡ 2. Command-Only Leveling & Exponential EXP Formula
* Controlled EXP progression designed for server-driven questing, custom mob kills, and events:
  $$\text{Required EXP} = \text{base\_exp} \times (\text{level}^{\text{exp\_exponent}})$$
* Configurable max level, base EXP, exponent, and points awarded per level in `config.yml`.
* **Zero Vanilla Grind Glitches**: EXP is granted through admin commands (`/skilladmin exp give`) or developer API, preventing mob-farm exploits.
* **Audio & Visual Celebrations**: Native Adventure toast sounds, title screens, and subtitles triggered on level up.

### 🚀 3. Dynamic EXP Boost System (Multipliers & Buffs)
* Multiply EXP rewards dynamically with decimal precision (e.g., `x1.5`, `x2.0`, `x3.0`).
* **Personal Boost**: Applied per player with configurable durations (`30m`, `1h`, `1d`) or permanent status (`perm`).
* **Global Server Boost**: Server-wide multiplier with automatic broadcast announcements upon activation and expiration.
* **Seamless Calculation**: If a player with a active `x2.0` boost is given 10 EXP, the engine automatically credits 20 EXP.

### 💎 4. Skill Point Economy & 8 Core Attributes
Players earn Skill Points upon leveling up (or via `/skilladmin point give`) to invest into 8 distinct attributes:
1. **Attack (ATK)**: Increases physical melee and ranged weapon damage (+0.15 damage/pt).
2. **Magic (MATK)**: Increases spell and potion potency (+1% potency/pt) and expands Mana capacity in `ClarityRPG`.
3. **Defense (DEF)**: Reduces incoming damage percentage with diminishing returns and a configurable cap (default: 75% max reduction).
4. **Fire (Api)**: Burns opponents on hit and inflicts explosive burst damage.
5. **Wind (Angin)**: Enhances movement velocity and mitigates/eliminates fall damage.
6. **Water (Air)**: Grants aquatic vitality, Dolphin's Grace, and freezing slowness debuffs.
7. **Earth (Tanah)**: Enhances defensive armor rating, knockback resistance, and emergency barrier shields.
8. **Light (Cahaya)**: Holy radiance, bonus smite damage against undead monsters, and vampiric lifesteal.

### 🌟 5. Milestone Elemental Tiers & Masteries
Reaching milestone levels (default: Level 25 for Tier 1, Level 50 for Tier 2) unlocks unique passive and active elemental masteries:

| Element | Tier 1 (Level 25) | Tier 2 (Level 50) |
| :--- | :--- | :--- |
| **Fire (Api)** | **Blaze**: Ignites targets for 3 seconds on strike. | **Inferno**: Ignites targets for 7 seconds + explosive fire burst (+3 damage). |
| **Wind (Angin)** | **Badai**: 50% fall damage reduction + agility speed. | **Storm**: 100% immunity to fall damage + gale tempest movement speed. |
| **Water (Air)** | **Gelombang**: Dolphin's Grace swimming speed & water breathing. | **Ice**: Freezes enemies (freeze ticks) & inflicts Slowness on hit. |
| **Earth (Tanah)** | **Megalit**: Fortified armor rating & high knockback resistance. | **Bumi**: Emergency Resistance II barrier activates when HP drops below 30%. |
| **Light (Cahaya)** | **Suar**: +25% Holy Smite damage vs Undead monsters (Zombies, Skeletons, Withers). | **Foton**: +50% Holy Smite damage + heals player (+1.5 HP) when striking targets. |

### 🖥️ 6. 100% Paper Native Dialog Interface (Zero Chest GUI)
* **Main Skills Menu**: Displays current level, EXP progress bar, available Skill Points, active EXP boost multiplier, and quick navigation.
* **Stat Allocation Dialog**: Multi-action buttons (`+1`, `+5`, `+10`) with live stat descriptions and current tier preview.
* **Native Custom Amount Text Dialog**: Input custom allocation numbers directly via native text input dialog (`DialogInput.text`).
* **Elemental Masteries Showcase**: Detailed overview of all unlocked tiers, effects, and progression requirements.

### 🌐 7. Multi-Language Support (ClarityLangCore Integration)
* Automatically hooks into **ClarityLangCore** for per-player dynamic language switching (`/lang`).
* Includes 9 bundled language localizations:
  * English (`en_US`)
  * Indonesian (`id_ID`)
  * Brazilian Portuguese (`pt_BR`)
  * Spanish (`es_ES`)
  * Simplified Chinese (`zh_CN`)
  * Japanese (`ja_JP`)
  * German (`de_DE`)
  * French (`fr_FR`)
  * Russian (`ru_RU`)
* Fully self-contained fallback system when running as a standalone plugin without ClarityLangCore.

### 🔌 8. Developer API & Custom Events (Bridge to `ClarityRPG`)
* Direct static access via `ClaritySkillsAPI` for external plugins.
* Full suite of Bukkit custom events:
  * `PlayerSkillLevelUpEvent`: Fired when a player levels up.
  * `PlayerExpGainEvent`: Fired when a player receives EXP (cancellable, supports multiplier modification).
  * `PlayerStatAllocateEvent`: Fired when a player allocates points to any attribute.
  * `PlayerElementalTierUnlockEvent`: Fired when a player reaches a milestone tier.
  * `PlayerExpBoostEvent`: Fired when personal or global EXP boosts are activated.

---

## 🧩 PlaceholderAPI Expansion

ClaritySkills natively registers the `%clarityskills_<placeholder>%` expansion identifier.

| Placeholder | Description | Example Output |
| :--- | :--- | :--- |
| `%clarityskills_level%` | Current player skill level | `42` |
| `%clarityskills_exp%` | Current EXP in the current level | `1450` |
| `%clarityskills_req_exp%` | EXP required to reach the next level | `2700` |
| `%clarityskills_total_exp%` | Lifetime total EXP earned | `38200` |
| `%clarityskills_exp_percent%` | Progress percentage to next level | `53.7` |
| `%clarityskills_points%` | Available unspent Skill Points | `8` |
| `%clarityskills_boost_multiplier%` | Current effective EXP multiplier | `2.0x` |
| `%clarityskills_boost_remaining%` | Remaining personal boost duration | `24m 12s` or `Permanent` |
| `%clarityskills_stat_<stat>%` | Level of a specific stat (`attack`, `magic`, `defense`, `fire`, etc.) | `25` |
| `%clarityskills_tier_<element>%` | Current tier name of an element (`fire`, `wind`, `water`, `earth`, `light`) | `Inferno` |

---

## 📜 Commands & Permissions

### Player Commands
| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/skill` | `/skills` | Open Main Skill Dialog Menu | `clarityskills.use` (default: true) |
| `/skill menu` | - | Open Main Skill Dialog Menu | `clarityskills.use` |
| `/skill info` | - | View skill summary in chat | `clarityskills.use` |
| `/skill stats` | - | List all 8 attributes and current levels | `clarityskills.use` |
| `/skill boost` | - | Check active personal and global EXP boosts | `clarityskills.use` |

### Admin Commands
| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/skilladmin exp give <player> <amount> [boost]` | `/sa`, `/skillsadmin` | Award EXP to player (optional boost flag) | `clarityskills.admin` |
| `/skilladmin exp take <player> <amount>` | - | Deduct EXP from player | `clarityskills.admin` |
| `/skilladmin exp set <player> <amount>` | - | Set current level EXP for player | `clarityskills.admin` |
| `/skilladmin exp reset <player>` | - | Reset player EXP to 0 | `clarityskills.admin` |
| `/skilladmin point give <player> <amount>` | - | Give Skill Points to player | `clarityskills.admin` |
| `/skilladmin point take <player> <amount>` | - | Take Skill Points from player | `clarityskills.admin` |
| `/skilladmin point set <player> <amount>` | - | Set player Skill Points | `clarityskills.admin` |
| `/skilladmin point reset <player>` | - | Reset player Skill Points to 0 | `clarityskills.admin` |
| `/skilladmin boost give <player> <mult> <duration>` | - | Grant personal EXP boost (`30m`, `1h`, `perm`) | `clarityskills.admin` |
| `/skilladmin boost global <mult> <duration>` | - | Grant global server-wide EXP boost | `clarityskills.admin` |
| `/skilladmin boost clear <global\|player>` | - | Clear global or personal boost | `clarityskills.admin` |
| `/skilladmin boost list [player]` | - | Inspect active boost statuses | `clarityskills.admin` |
| `/skilladmin level set <player> <level>` | - | Manually set player skill level | `clarityskills.admin` |
| `/skilladmin reset <player> [all\|stats\|points]` | - | Reset player skill data | `clarityskills.admin` |
| `/skilladmin reload` | - | Reload configuration and language files | `clarityskills.admin` |

---

## ⚙️ Configuration

The primary configuration file is located at `plugins/ClaritySkills/config.yml`.

```yaml
# ==========================================================
# ClaritySkills Configuration
# Paper 26.3 Native Dialog Skill & Stat System
# Addon Foundation for ClarityRPG
# ==========================================================

settings:
  max_level: 100
  base_exp: 100
  exp_exponent: 1.5
  points_per_level: 2
  stat_max_level: 100
  cost_per_stat_point: 1

elements:
  tier_1_threshold: 25
  tier_2_threshold: 50

mechanics:
  enable_builtin_combat_mechanics: true
  enable_builtin_elemental_mechanics: true
  attack_damage_per_point: 0.15
  defense_reduction_per_point: 0.005
  defense_reduction_cap: 0.75
  magic_damage_multiplier_per_point: 0.01

effects:
  play_level_up_sound: true
  level_up_sound: "ui.toast.challenge_complete"
  play_tier_unlock_sound: true
  tier_unlock_sound: "ui.toast.challenge_complete"

gui:
  register_pause_screen: true
```

---

## 🛠️ Building from Source

### Requirements
* **Java 25** (JDK 25 LTS or newer)
* **Gradle 9.5+** (Gradle wrapper included)

### Compile
```powershell
# On Windows
.\gradlew build

# On Linux / macOS
./gradlew build
```

The compiled shaded jar will be generated at:
`build/libs/ClaritySkills-1.0.0.jar`

---

## 📄 License

This project is licensed under the **GNU General Public License v3.0** - see the [LICENSE](LICENSE) file for details.
