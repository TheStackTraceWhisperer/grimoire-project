# Wave 3 — Completion Evidence

**Date:** 2026-03-29  
**Verified by:** automated check against Wave 3 checklist (`TASKS.md`)

---

## 3.1 `grimoire-application-core`: use-case orchestration, command handlers ✅

8 main sources, 6 test sources across 2 packages.

### ECS Engine (`com.grimoire.application.core.ecs`) — 7 classes

| Class | Type | Description |
|-------|------|-------------|
| `ComponentManager` | class | `ComponentType → (EntityId → Component)` storage; add, get, remove, query by type |
| `EcsWorld` | class | Primary game-state façade; delegates to `EntityManager` + `ComponentManager`; prefab registry; tick counter |
| `EntityManager` | class | Entity lifecycle — UUID-based ID generation, alive tracking |
| `GameCommandQueue` | class | Thread-safe `ConcurrentLinkedQueue<Runnable>` bridge between IO threads and the game loop |
| `GameSystem` | interface | `@FunctionalInterface` — `tick(float deltaTime)` contract for all game systems |
| `Prefab` | class | Entity template — ordered list of component templates with optional customiser on instantiation |
| `SystemScheduler` | class | Executes an immutable ordered list of `GameSystem` instances per tick, then increments world tick |

### Outbound Port (`com.grimoire.application.core.port`) — 1 interface

| Class | Type | Methods |
|-------|------|---------|
| `GameEventPort` | interface | `onEntityDespawn(entityId, zoneId)`, `onZoneChange(entityId, newZoneId, x, y)` |

### Design decisions

- **Single-threaded ECS loop:** All entity/component operations are designed for single-threaded access from the game loop. `GameCommandQueue` is the only approved cross-thread bridge (Netty IO → game loop).
- **Prefab pattern:** `EcsWorld.createEntityFromPrefab` supports an optional `Function<Component, Component>` customiser, allowing per-entity overrides while sharing immutable record templates.
- **Port separation:** `GameEventPort` is an outbound port — infrastructure adapters (e.g., network layer) implement it to react to game events without the application layer knowing about transport details.

### Tests: 60 (all unit)

| Test class | Tests | Pass |
|-----------|-------|------|
| `ComponentManagerTest` | 15 | ✓ |
| `EcsWorldTest` | 16 | ✓ |
| `EntityManagerTest` | 9 | ✓ |
| `GameCommandQueueTest` | 7 | ✓ |
| `PrefabTest` | 5 | ✓ |
| `SystemSchedulerTest` | 8 | ✓ |
| **Total** | **60** | **60 ✓** |

### JaCoCo coverage (`grimoire-application-core`)

| Metric | Covered | Total | Percentage |
|--------|---------|-------|-----------|
| Instruction | 408 | 408 | **100.0%** |
| Branch | 27 | 28 | **96.4%** |
| Line | 102 | 102 | **100.0%** |
| Method | 41 | 41 | **100.0%** |
| Class | 6 | 6 | **100.0%** |

---

## 3.2 `grimoire-application-session`: session policies, `SessionManager` ✅

3 main sources, 2 test sources in `com.grimoire.application.session`.

### Classes

| Class | Type | Description |
|-------|------|-------------|
| `Session` | record | Immutable session value: `sessionId`, `accountId`, `username`, `createdAt`, `expiresAt`; compact-constructor null validation; `isExpired()` check; `create()` factory |
| `SessionConfig` | interface | `@FunctionalInterface` configuration port — `sessionValidityMinutes()` |
| `SessionManager` | class | Thread-safe session lifecycle: create (with single-session-per-account enforcement), validate (with lazy expiry), invalidate, cleanup; `ConcurrentHashMap`-backed storage with `accountId → sessionId` reverse index |

### Design decisions

- **Single session per account:** Creating a new session for an account automatically invalidates any existing session, preventing duplicate logins.
- **Lazy expiration:** Expired sessions are cleaned up on access via `validateSession()` and in bulk via `cleanupExpiredSessions()` — scheduling is deferred to the infrastructure layer.
- **Configuration port:** `SessionConfig` is a `@FunctionalInterface` so infrastructure can provide it via `@ConfigurationProperties`, a lambda, or any other mechanism.
- **Thread-safety:** `SessionManager` uses `ConcurrentHashMap` for both maps — safe for concurrent access from IO threads and the game loop.

### Tests: 34 (all unit)

| Test class | Tests | Pass |
|-----------|-------|------|
| `SessionTest` | 12 | ✓ |
| `SessionManagerTest` | 22 | ✓ |
| **Total** | **34** | **34 ✓** |

### JaCoCo coverage (`grimoire-application-session`)

| Metric | Covered | Total | Percentage |
|--------|---------|-------|-----------|
| Instruction | 276 | 276 | **100.0%** |
| Branch | 18 | 18 | **100.0%** |
| Line | 60 | 60 | **100.0%** |
| Method | 12 | 12 | **100.0%** |
| Class | 2 | 2 | **100.0%** |

---

## 3.3 Inter-module deps: `application-*` → `domain-*`, `contracts-api` ✅

### `grimoire-application-core`

```xml
<dependency>
    <groupId>com.grimoire</groupId>
    <artifactId>grimoire-contracts-api</artifactId>
</dependency>
<dependency>
    <groupId>com.grimoire</groupId>
    <artifactId>grimoire-domain-core</artifactId>
</dependency>
<dependency>
    <groupId>com.grimoire</groupId>
    <artifactId>grimoire-domain-combat</artifactId>
</dependency>
<dependency>
    <groupId>com.grimoire</groupId>
    <artifactId>grimoire-domain-navigation</artifactId>
</dependency>
```

### `grimoire-application-session`

```xml
<dependency>
    <groupId>com.grimoire</groupId>
    <artifactId>grimoire-contracts-api</artifactId>
</dependency>
<dependency>
    <groupId>com.grimoire</groupId>
    <artifactId>grimoire-domain-core</artifactId>
</dependency>
```

All dependency versions managed by `internal-bom` (no explicit `<version>` in module POMs).

---

## 3.4 Build verification ✅

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
Grimoire Application Core .......................... SUCCESS  [60 tests]
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

Total new tests (Wave 3): 94 (60 + 34)
Quality gates: Checkstyle ✅ | PMD ✅ | SpotBugs ✅ | JaCoCo ✅
```

---

**Wave 3 is complete. All 23 modules build green. Wave 4 (Infrastructure) may begin.**

