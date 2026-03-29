# ADR 003: Wave 4 Architecture Boundaries — Systems, Loops, and the PlayerConnection Trap

**Status:** ACCEPTED  
**Date:** 2026-03-29  
**Context:** Pre-Wave-4 retrospective identified four architectural placement decisions that, if made incorrectly, would violate the layer boundary rules established in the Unified Plan (§6.3, §7).

---

## Decision 1: No Infrastructure Types in Domain Components

### The Trap

The reference `PlayerConnection` component holds a Netty `Channel`:

```java
// references/grimoire — DO NOT PORT THIS SHAPE
public record PlayerConnection(Channel channel) implements Component {}
```

Porting this to `grimoire-domain-core` would introduce a `io.netty.channel.Channel` import into the domain layer, instantly breaking the dependency rule: *domain depends on contracts-api only*.

### The Decision

**`PlayerControlled` in `domain-core` remains a pure marker record** (already implemented in Wave 2). It carries zero infrastructure references.

The mapping from domain identity to transport channel is an **infrastructure concern** maintained in `grimoire-infra-network-netty`:

```
┌──────────────────────────┐       ┌──────────────────────────────┐
│  domain-core             │       │  infra-network-netty         │
│                          │       │                              │
│  PlayerControlled()      │       │  ConcurrentHashMap<String,   │
│  Persistent(accountId)   │──────▶│       Channel>               │
│                          │       │  (sessionId → Channel)       │
└──────────────────────────┘       └──────────────────────────────┘
```

When a game system (e.g., combat) needs to notify a player, it calls the `GameEventPort` interface. The Netty adapter implements that port, looks up the session ID in its channel map, and writes the bytes. The domain and application layers never see `Channel`.

### Consequences

- ✅ Domain layer stays pure (zero framework imports)
- ✅ Channel map is testable in isolation with mock channels
- ✅ Transport could be swapped (e.g., WebSocket) without touching domain or application code

---

## Decision 2: GameLoop Decomposition — Scheduler vs. Thread Driver

### The Problem

The reference `GameLoop` conflates two responsibilities:
1. **Tick orchestration** — calling systems in order, incrementing the world tick
2. **Thread lifecycle** — `while(isRunning)`, `Thread.sleep()`, delta-time capping

### The Decision

These responsibilities are split across layers:

| Responsibility | Class | Module | Layer |
|---------------|-------|--------|-------|
| Tick orchestration | `SystemScheduler` | `application-core` | Application |
| Thread lifecycle | (bootstrap code) | `grimoire-server-app` | Assembly |

`SystemScheduler` already exists (Wave 3). It accepts an ordered `List<GameSystem>` and calls `tick(deltaTime)` on each, then increments the world tick. It has no thread awareness.

The thread driver lives in `grimoire-server-app`. At Micronaut startup, the assembly module:
1. Collects all `@Singleton` implementations of `GameSystem` from the DI context
2. Orders them (explicit priority or list)
3. Constructs `SystemScheduler`
4. Launches a dedicated game-loop thread that repeatedly calls `systemScheduler.tick(delta)`

This keeps scheduling policy (system order) in the application layer and thread management (sleep, interruption, shutdown hooks) in the assembly layer where Micronaut controls the lifecycle.

### Consequences

- ✅ `application-core` has no `Thread`, `Runnable`, or `sleep()` calls
- ✅ Thread driver is trivial (~30 lines) and lives in the only module that owns the process lifecycle
- ✅ `SystemScheduler` is already unit-tested (Wave 3: 8 tests)

---

## Decision 3: Game System Placement — Pure vs. Adapter

### The Rule

A system is **pure** if it only reads/writes ECS components via `EcsWorld` and calls port interfaces. It is an **adapter** if it directly uses framework APIs (Netty channels, JPA repositories, etc.).

### The Placement

| System | Layer | Module | Justification |
|--------|-------|--------|---------------|
| `MovementSystem` | Application | `application-core` | Reads `Velocity`, `Position`, `BoundingBox`; writes `Position`, `Dirty`. Pure ECS. |
| `CombatSystem` | Application | `application-core` | Reads `AttackIntent`, `Stats`; writes `Dead`, `Experience`, `Dirty`. Death notifications go through `GameEventPort`. |
| `LevelUpSystem` | Application | `application-core` | Reads `Experience`, `Stats`; writes `Stats`. Delegates to `LevelingRules`. Pure. |
| `NpcAiSystem` | Application | `application-core` | Reads `NpcAi`, `Position`, `Zone`; writes `MovementIntent`, `AttackIntent`. Uses `SpatialGrid` + `AStarPathfinder` (both in `domain-navigation`). |
| `PortalCooldownSystem` | Application | `application-core` | Reads/writes `PortalCooldown`. Pure tick countdown. |
| `SpatialGridSystem` | Application | `application-core` | Rebuilds `SpatialGrid` from `Position`/`Zone` each tick. Pure ECS. |
| `ZoneChangeSystem` | Application | `application-core` | Reads `Portal`, `Position`, `PortalCooldown`; writes `Position`, `Zone`, `PortalCooldown`. Zone-change notifications go through `GameEventPort`. |
| `NetworkSyncSystem` | Infrastructure | `infra-network-netty` | Reads `Dirty` + component state; serialises to `GameStateUpdate` DTO; writes to Netty channels. |
| `NetworkVisibilitySystem` | Infrastructure | `infra-network-netty` | Reads `Position`, `Zone`; sends `EntitySpawn`/`EntityDespawn` to Netty channels on visibility changes. |
| `PlayerInputSystem` | Infrastructure | `infra-network-netty` | Reads inbound packets from Netty channels; translates to `MovementIntent` components. |
| `PersistenceSystem` | Infrastructure | `infra-persistence-jpa` | Reads `Persistent`, `Stats`, `Position`; writes to JPA `CharacterRepository`. |

### The Wiring Pattern

All systems — pure and adapter — implement `GameSystem` from `application-core`. At assembly time in `grimoire-server-app`, Micronaut discovers all `GameSystem` beans and provides them as an ordered list to `SystemScheduler`:

```
server-app (assembly)
  └── gathers List<GameSystem> from DI context
       ├── MovementSystem        (from application-core)
       ├── CombatSystem          (from application-core)
       ├── LevelUpSystem         (from application-core)
       ├── NpcAiSystem           (from application-core)
       ├── PortalCooldownSystem  (from application-core)
       ├── SpatialGridSystem     (from application-core)
       ├── ZoneChangeSystem      (from application-core)
       ├── NetworkSyncSystem     (from infra-network-netty)
       ├── NetworkVisibilitySystem (from infra-network-netty)
       ├── PlayerInputSystem     (from infra-network-netty)
       └── PersistenceSystem     (from infra-persistence-jpa)
```

### Consequences

- ✅ 7 of 11 systems are pure and fully unit-testable with no mocks for infrastructure
- ✅ Adapter systems are isolated in their infra modules
- ✅ System ordering is a concern of the assembly layer, not hard-coded in any module

---

## Decision 4: Straggler Placement — GroupService, GameConfig, Content

| Class | Module | Rationale |
|-------|--------|-----------|
| `GroupService` | `application-core` | Application-level orchestration (create/join/leave/kick). Updates ECS state and notifies via `GameEventPort`. |
| `GameConfig` | `application-core` (as a port interface) | Pure configuration contract. Infrastructure provides the implementation via Micronaut `@ConfigurationProperties` in `server-app`. |
| `PrefabRegistry` | `grimoire-server-app` | Reads prefab definitions from disk/resources during bootstrap. Feeds data into `EcsWorld.registerPrefab()`. Assembly-only. |
| `NpcFactory` | `grimoire-server-app` | Creates NPC entities from prefabs during zone initialisation. Assembly-only. |
| `ZoneInitializer` | `grimoire-server-app` | Reads zone definitions, builds `NavigationGrid`s, spawns static entities. Assembly-only. |

### Consequences

- ✅ `application-core` gains one service (`GroupService`) and one config port (`GameConfig`)
- ✅ All content/bootstrap classes land in `server-app` (assembly only — no business logic)
- ✅ `GameConfig` as an interface means `application-core` has no Micronaut annotation dependency for config

---

## Summary of Layer Rules (Reinforced)

```
contracts-*     → JDK only. No grimoire deps.
domain-*        → contracts-api only. No framework imports.
application-*   → domain-* + contracts-api. Port interfaces for infra.
                  @Singleton OK. No Channel, no JPA, no HTTP.
infra-*         → application ports + frameworks. Implements ports.
*-app           → Composes all layers. Thread driver. Config binding.
                  Content loading. No business logic.
```

