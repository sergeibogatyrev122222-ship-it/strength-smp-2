# StrengthSMP Plugin — Paper 1.21.1

## How to Build
Requirements: **Java 21+**, **Maven 3.8+**

```bash
cd StrengthSMP
mvn clean package
```

JAR output: `target/StrengthSMP-1.0.0.jar`
Drop into your server's `/plugins/` folder and restart.

---

## Mechanic
1. **Kill a player** → a **Strength shell** (named Nautilus Shell) goes directly into your inventory
2. **Right-click** the shell → it is consumed, you gain **+1 Strength** (max 5)
3. **Die** → lose **1 Strength** (not all of it)
4. Shells **stack** in your inventory — save them for whenever you want

## Anti-Exploit Protection
- Shells are identified by **NBT tag**, not display name — renamed vanilla shells do NOT work
- Creative/Spectator kills give **no shell** (can't farm with creative mode)
- Killing yourself gives **no shell**
- Attribute modifier uses a **unique NamespacedKey** — never stacks or duplicates
- Modifier is **re-applied on login and respawn** — relog cannot remove it
- Values are **clamped 0–5** on load — file edits can't set strength above max

## Commands
| Command | Who | Description |
|---|---|---|
| `/strength` | Player | Check your current strength + bar |
| `/strength withdraw <amount>` | Player | Remove strength levels |
| `/strength reset <player>` | Admin (OP) | Reset a player's strength to 0 |

## Permissions
| Permission | Default |
|---|---|
| `strengthsmp.admin` | OP |

## Strength Levels
Each shell consumed = **+1 flat attack damage** (via `generic.attack_damage` attribute modifier).

| Shells | Attack Damage Bonus |
|---|---|
| 1 | +1 |
| 2 | +2 |
| 3 | +3 |
| 4 | +4 |
| 5 | +5 (max) |
