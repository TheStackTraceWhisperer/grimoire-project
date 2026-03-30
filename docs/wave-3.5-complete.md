# Wave 3.5 — Completion Evidence

**Date:** 2026-03-29  
**Verified by:** automated check against Wave 3.5 checklist (`TASKS.md`)

---

## 3.5.1 Pure Game Systems ✅

7 systems in `com.grimoire.application.core.system`, totalling 1,091 lines.

### System Summary

| System | Lines | Reads | Writes | External Port |
|--------|------:|-------|--------|---------------|
| `MovementSystem` | 181 | `Velocity`, `Position`, `BoundingBox`, `Zone`, `Solid` | `Position`, `Velocity`, `Dirty` | — |
| `CombatSystem` | 234 | `AttackIntent`, `AttackCooldown`, `Stats`, `Dead`, `Position`, `Zone`, `Monster`, `Experience` | `AttackCooldown`, `Stats`, `Dirty`, `Dead`, `Experience` | `GameEventPort.onEntityDespawn` |
| `LevelUpSystem` | 87 | `Experience`, `Stats` | `Experience`, `Stats`, `Dirty` | — |
| `NpcAiSystem` | 245 | `NpcAi`, `Position`, `Zone`, `PlayerControlled`, `Dead`, `SpawnPoint` | `Velocity`, `AttackIntent` | — |
| `PortalCooldownSystem` | 57 | `PortalCooldown` | `PortalCooldown` (decrement/remove) | — |
| `SpatialGridSystem` | 97 | `Solid`, `Position`, `Zone` | `SpatialGrid` (rebuild) | — |
| `ZoneChangeSystem` | 197 | `PlayerControlled`, `Portal`, `Position`, `BoundingBox`, `Zone`, `PortalCooldown`, `Renderable` | `Zone`, `Position`, `PortalCooldown`, `Dirty` | `GameEventPort.onZoneChange` |

### Design decisions

- **AABB collision detection:** `MovementSystem` and `ZoneChangeSystem` share the same axis-aligned bounding-box overlap logic. Entities with a `BoundingBox` and `Solid` component block movement; portal triggers use `BoundingBox` overlap with `Portal` entities.
- **Spatial grid as a system:** `SpatialGridSystem` rebuilds the `SpatialGrid` each tick from all `Solid`+`Position` entities, then exposes `getGrid()` for sibling systems (`MovementSystem`, `CombatSystem`, `NpcAiSystem`) — all on the same game-loop thread so no synchronisation is needed.
- **NPC leash mechanic:** `NpcAiSystem` stores each NPC's `SpawnPoint` with an optional leash radius (fallback to `GameConfig.npcLeashRadius()`). If an NPC chases a player beyond the leash, it returns to spawn — preventing infinite kiting.
- **Multi-phase combat tick:** `CombatSystem.tick()` executes three ordered phases — cooldown decrement, attack resolution, death processing — so XP awards and despawn notifications happen in the same tick as the killing blow.
- **LevelingRules delegation:** `LevelUpSystem` delegates all threshold/boost logic to `LevelingRules` (domain-combat), keeping the system a pure orchestrator. Multi-level-up per tick is supported via a `while` loop.
- **Portal cooldown:** `PortalCooldownSystem` counts down the `PortalCooldown` component each tick, preventing re-entry after a zone transition. Duration is configurable via `GameConfig.portalCooldownTicks()`.

---

## 3.5.2 `GroupService` — group orchestration ✅

1 class in `com.grimoire.application.core.service` (168 lines).

| Method | Description |
|--------|-------------|
| `createGroup(entityId)` | Creates a new group, auto-leaving any existing group; returns generated `groupId` |
| `joinGroup(groupId, entityId)` | Adds entity to group (auto-leave old); returns `false` if group not found |
| `leaveGroup(entityId)` | Removes entity; auto-disbands empty groups |
| `getMembers(groupId)` | Returns unmodifiable `Set<String>` of member entity IDs |
| `getGroupForEntity(entityId)` | Returns `Optional<String>` group ID |
| `getGroupCount()` | Returns active group count |
| `groupExists(groupId)` | Checks group existence |

### Design decisions

- **Single-group constraint:** An entity can belong to at most one group. `createGroup` and `joinGroup` silently leave the current group first.
- **Auto-disband:** When the last member leaves, the group is automatically removed — no orphan groups.
- **Thread-safety:** Intentionally not thread-safe (`HashMap`-backed). Must be accessed from the game-loop thread only, matching the ECS single-threaded contract.
- **Simplified scope:** The current implementation is a pure in-memory data structure without `EcsWorld` or `GameEventPort` coupling. ECS integration (group membership components) and `GameEventPort` notifications are deferred to Wave 4b when the network adapter needs group broadcast support.

---

## 3.5.3 `GameConfig` port interface ✅

1 interface in `com.grimoire.application.core.port` (87 lines).

```java
public interface GameConfig {
    default double attackRange()        { return 50.0; }
    default double playerSpeed()        { return 5.0; }
    default int    spatialGridCellSize() { return 64; }
    default double npcAggroRange()      { return 100.0; }
    default double npcSpeed()           { return 3.0; }
    default int    attackCooldownTicks() { return 20; }
    default double npcLeashRadius()     { return 200.0; }
    default int    portalCooldownTicks() { return 60; }
}
```

### Design decisions

- **Default methods:** Every parameter has a sensible default, so infrastructure can selectively override via `@ConfigurationProperties` (Wave 5) or tests can use `new GameConfig() {}` with zero overrides.
- **Consumed by:** `CombatSystem`, `NpcAiSystem`, `SpatialGridSystem`, `ZoneChangeSystem`.
- **Lives alongside `GameEventPort`:** Both are outbound ports in `com.grimoire.application.core.port`, keeping the application layer's external contracts in one package.

---

## 3.5.4 `PlayerControlled` extended with `sessionId` ✅

**Module:** `grimoire-domain-core`  
**File:** `com.grimoire.domain.core.component.PlayerControlled`

```java
public record PlayerControlled(String sessionId) implements Component { }
```

Previously a pure marker record (no fields). The `sessionId` field links a player entity to a `Session` managed by `grimoire-application-session`, without introducing any infrastructure dependency (no `Channel`, no network references). This is required by `NpcAiSystem` (to identify player targets) and by `ZoneChangeSystem` (to iterate player entities).

---

## 3.5.5 Tests: 75 new (all unit) ✅

| Test class | Tests | Pass |
|-----------|-------|------|
| `MovementSystemTest` | 8 | ✓ |
| `CombatSystemTest` | 14 | ✓ |
| `LevelUpSystemTest` | 8 | ✓ |
| `NpcAiSystemTest` | 9 | ✓ |
| `PortalCooldownSystemTest` | 5 | ✓ |
| `SpatialGridSystemTest` | 6 | ✓ |
| `ZoneChangeSystemTest` | 9 | ✓ |
| `GroupServiceTest` | 16 | ✓ |
| **Total (Wave 3.5)** | **75** | **75 ✓** |

All ports faked/mocked — `GameEventPort` stubbed, `GameConfig` uses anonymous implementation with defaults, `Random` seeded for deterministic NPC AI tests.

`grimoire-application-core` cumulative: 135 tests (60 Wave 3 + 75 Wave 3.5).

---

## 3.5.6 JaCoCo coverage (`grimoire-application-core`) ✅

15 classes analysed (was 6 in Wave 3).

| Metric | Covered | Total | Percentage |
|--------|---------|-------|-----------|
| Instruction | 2,357 | 2,401 | **98.2%** |
| Branch | 184 | 228 | **80.7%** |
| Line | 492 | 512 | **96.1%** |
| Method | 92 | 95 | **96.8%** |
| Class | 15 | 15 | **100.0%** |

Minor uncovered branches are defensive null/empty guards in `NpcAiSystem`, `CombatSystem`, and `MovementSystem` (edge cases where optional components are unexpectedly absent). 3 `GameConfig` default methods are unused at test time (covered only by systems that consume them).

---

## 3.5.7 Build verification ✅

```
mvn clean verify (full reactor — all 23 modules)

grimoire-project ................................... SUCCESS
grimoire-poms ...................................... SUCCESS
Grimoire External BOM .............................. SUCCESS
Grimoire Internal BOM .............................. SUCCESS
Grimoire Parent .................................... SUCCESS
Grimoire Starter Parent ............................ SUCCESS
Grimoire Contracts Api ............................. SUCCESS
Grimoire Contracts Wire ............................ SUCCESS
Grimoire Domain Core ............................... SUCCESS  [50 tests]
Grimoire Domain Combat ............................. SUCCESS  [55 tests]
Grimoire Domain Navigation ......................... SUCCESS  [71 tests]
Grimoire Application Core .......................... SUCCESS  [135 tests]
Grimoire Application Session ....................... SUCCESS  [34 tests]
Grimoire Infra Network Netty ....................... SUCCESS
Grimoire Infra Security Keycloak ................... SUCCESS
Grimoire Infra Persistence Jpa ..................... SUCCESS
Grimoire Infra Observability ....................... SUCCESS
Grimoire Test Kit .................................. SUCCESS
grimoire-modules ................................... SUCCESS
Grimoire Server App ................................ SUCCESS
Grimoire Web App ................................... SUCCESS
Grimoire Client App ................................ SUCCESS
grimoire-applications .............................. SUCCESS

BUILD SUCCESS

Total tests (project-wide): 782
New tests (Wave 3.5): 75
Quality gates: Checkstyle ✅ | PMD ✅ | SpotBugs ✅ | JaCoCo ✅
```

---

## New source files (Wave 3.5)

| File | Package | Type | Lines |
|------|---------|------|------:|
| `MovementSystem.java` | `system` | class | 181 |
| `CombatSystem.java` | `system` | class | 234 |
| `LevelUpSystem.java` | `system` | class | 87 |
| `NpcAiSystem.java` | `system` | class | 245 |
| `PortalCooldownSystem.java` | `system` | class | 57 |
| `SpatialGridSystem.java` | `system` | class | 97 |
| `ZoneChangeSystem.java` | `system` | class | 197 |
| `GroupService.java` | `service` | class | 168 |
| `GameConfig.java` | `port` | interface | 87 |
| **Total** | | **9 files** | **1,353** |

Modified: `PlayerControlled.java` (`domain-core`) — added `sessionId` field to previously empty marker record.

---

**Wave 3.5 is complete. All 23 modules build green. Wave 4a (Test Kit, Observability & Codecs) may begin.**

