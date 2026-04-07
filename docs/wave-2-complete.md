# Wave 2 — Completion Evidence

**Date:** 2026-03-29  
**Verified by:** automated check against Wave 2 checklist (`TASKS.md`)

---

## 2.1 `grimoire-domain-core`: components (value objects) ✅

16 main sources, 16 test sources.

All domain components are immutable records implementing the `Component` marker interface. In this ECS architecture,
component records serve as value objects, and entity identity is a runtime concern (managed in the application/infra
layer).

### Components (16 records)

| Component        | Fields                             |
|------------------|------------------------------------|
| `BoundingBox`    | `width`, `height`                  |
| `Component`      | marker interface                   |
| `Dead`           | `killerId`                         |
| `Dirty`          | `tick`                             |
| `Experience`     | `currentXp`, `xpToNextLevel`       |
| `MovementIntent` | `targetX`, `targetY`               |
| `Persistent`     | `accountId`                        |
| `Portal`         | `targetZoneId`, `targetPortalId`   |
| `PortalCooldown` | `ticksRemaining`                   |
| `Position`       | `x`, `y`                           |
| `Renderable`     | `name`, `visualId`                 |
| `Solid`          | (marker)                           |
| `SpawnPoint`     | `x`, `y`, `leashRadius`            |
| `Stats`          | `hp`, `maxHp`, `defense`, `attack` |
| `Velocity`       | `dx`, `dy`                         |
| `Zone`           | `zoneId`                           |

### Tests: 50 (36 unit + 14 jqwik property)

- 15 unit test classes (one per component)
- `ComponentPropertyTest` — 14 `@Property` methods validating record construction, value equality, and `Component`
  interface membership

---

## 2.2 `grimoire-domain-combat`: combat rules, cooldowns, leveling ✅

7 main sources, 7 test sources.

### Components (4 records)

| Component        | Fields                                |
|------------------|---------------------------------------|
| `AttackCooldown` | `ticksRemaining`                      |
| `AttackIntent`   | `targetEntityId`                      |
| `Monster`        | `type` (MonsterType enum), `xpReward` |
| `NpcAi`          | `type` (AiType enum)                  |

### Rules (2 static utility classes)

| Class           | Methods                                                                                                   |
|-----------------|-----------------------------------------------------------------------------------------------------------|
| `CombatRules`   | `calculateDamage`, `applyDamage`, `isDead`, `isInRange`                                                   |
| `LevelingRules` | `canLevelUp`, `applyLevelUp`, `boostStatsForLevelUp`, `applyAllLevelUps`, `addXp`, `countPendingLevelUps` |

`LevelingRules` extracts pure domain logic from the reference `LevelUpSystem`:

- XP rollover on level-up
- XP threshold scaling (×1.5, with minimum increment guarantee)
- Stat boosts per level (+10 maxHp, +2 attack, +1 defense)
- HP healing on level-up (capped at new max)

### Tests: 55 (42 unit + 13 jqwik property)

- 4 component test classes + `CombatRulesTest` (11 tests) + `LevelingRulesTest` (17 tests)
- `CombatRulesPropertyTest` — 13 `@Property` methods covering:
  - Damage always ≥ 1
  - `applyDamage` HP ≥ 0 and never increases HP
  - `isDead` consistency with HP sign
  - `isInRange` reflexivity and symmetry
  - Level-up threshold always increases
  - XP rollover is non-negative
  - `applyAllLevelUps` always terminates below threshold
  - Stat boosts never decrease attributes
  - HP never exceeds new max after level-up

---

## 2.3 `grimoire-domain-navigation`: pathing, `AStarPathfinder`, `SpatialGrid` ✅

4 main sources, 6 test sources.

### Classes

| Class             | Description                                                                         |
|-------------------|-------------------------------------------------------------------------------------|
| `AStarPathfinder` | A* algorithm with 8-directional movement, path smoothing, line-of-sight (Bresenham) |
| `NavigationGrid`  | BitSet-based walkable/blocked grid with world↔grid coordinate conversion            |
| `SpatialGrid`     | Spatial partitioning for O(N·k) proximity queries, zone-aware                       |
| `Path`            | Component record for entity navigation waypoints                                    |

### Tests: 71 (56 unit + 15 jqwik property)

- `AStarPathfinderTest` (18 tests) — path finding, obstacles, smoothing, line-of-sight, corner-cutting prevention
- `NavigationGridTest` (15 tests) — grid construction, coordinate conversion, blocked/walkable, area marking
- `SpatialGridTest` (11 tests) — entity tracking, zone isolation, nearby queries, position updates
- `PathTest` (12 tests) — waypoint iteration, immutability, advance, null handling
- `NavigationPropertyTest` (9 `@Property` methods) — grid round-trips, A* always finds path on empty grid, paths never
  traverse blocked cells, same-start-target returns empty, smoothed path ≤ original, line-of-sight symmetry
- `SpatialGridPropertyTest` (6 `@Property` methods) — insert-then-query, remove-then-absent, entity count, zone
  isolation, clear, position update

---

## 2.4 Inter-module deps: `domain-*` → `contracts-api` ✅

| Module                       | Dependencies                                     |
|------------------------------|--------------------------------------------------|
| `grimoire-domain-core`       | `grimoire-contracts-api`                         |
| `grimoire-domain-combat`     | `grimoire-contracts-api`, `grimoire-domain-core` |
| `grimoire-domain-navigation` | `grimoire-contracts-api`, `grimoire-domain-core` |

All dependency versions managed by `internal-bom` (no explicit `<version>` in module POMs).

---

## 2.5 jqwik property tests on domain invariants ✅

| Module                       | Property test class       | `@Property` methods   |
|------------------------------|---------------------------|-----------------------|
| `grimoire-domain-core`       | `ComponentPropertyTest`   | 14                    |
| `grimoire-domain-combat`     | `CombatRulesPropertyTest` | 13                    |
| `grimoire-domain-navigation` | `NavigationPropertyTest`  | 9                     |
| `grimoire-domain-navigation` | `SpatialGridPropertyTest` | 6                     |
| **Total**                    | **4 classes**             | **42 property tests** |

---

## 2.6 Build verification ✅

```
mvn clean verify (domain modules + transitive deps)

Grimoire Parent .................................... SUCCESS
Grimoire Contracts Api ............................. SUCCESS
Grimoire Domain Core ............................... SUCCESS [50 tests]
Grimoire Domain Combat ............................. SUCCESS [55 tests]
Grimoire Domain Navigation ......................... SUCCESS [71 tests]

Total: 176 tests, 0 failures
Quality gates: Checkstyle ✅ | PMD ✅ | SpotBugs ✅ | JaCoCo ✅ | Formatter ✅
```

