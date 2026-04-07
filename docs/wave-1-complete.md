# Wave 1 — Completion Evidence

**Date:** 2026-03-29  
**Verified by:** automated check against Wave 1 checklist (`TASKS.md`)

---

## 1.1 `grimoire-contracts-api`: `ComponentDTO` marker interface ✅

```java
// com.grimoire.contracts.api.component.ComponentDTO
public interface ComponentDTO extends Serializable {
}
```

Package: `com.grimoire.contracts.api.component`  
File: `grimoire-modules/grimoire-contracts-api/src/main/java/com/grimoire/contracts/api/component/ComponentDTO.java`

---

## 1.2 `grimoire-contracts-wire`: protocol, DTOs, component DTOs ✅

28 main sources, 28 test sources across 3 packages.

### `protocol/` — `GamePacket` record + `PacketType` enum (23 packet types)

| Class        | Type                               |
|--------------|------------------------------------|
| `GamePacket` | record                             |
| `PacketType` | enum (10 C2S + 13 S2C = 23 values) |

```
C2S_TOKEN_LOGIN_REQUEST, C2S_REQUEST_CHARACTER_LIST, C2S_CHARACTER_SELECTION,
C2S_MOVEMENT_INTENT, C2S_CHAT_MESSAGE, C2S_PRIVATE_MESSAGE, C2S_CREATE_GROUP,
C2S_GROUP_MESSAGE, C2S_JOIN_GROUP, C2S_LEAVE_GROUP,

S2C_CHARACTER_LIST, S2C_CHARACTER_SELECTION_SUCCESS, S2C_LOGIN_FAILURE,
S2C_GAME_STATE_UPDATE, S2C_ENTITY_SPAWN, S2C_ENTITY_DESPAWN, S2C_ZONE_CHANGE,
S2C_CHAT_BROADCAST, S2C_PRIVATE_MESSAGE_BROADCAST, S2C_CREATE_GROUP_RESPONSE,
S2C_GROUP_MESSAGE_BROADCAST, S2C_JOIN_GROUP_RESPONSE, S2C_LEAVE_GROUP_RESPONSE
```

### `dto/` — 22 DTO records (auth, character, chat, group, entity, state, zone)

| DTO                         | Category  |
|-----------------------------|-----------|
| `TokenLoginRequest`         | auth      |
| `LoginFailure`              | auth      |
| `CharacterListResponse`     | character |
| `CharacterSelectionRequest` | character |
| `CharacterSelectionSuccess` | character |
| `ChatMessage`               | chat      |
| `ChatBroadcast`             | chat      |
| `PrivateMessage`            | chat      |
| `PrivateMessageBroadcast`   | chat      |
| `CreateGroup`               | group     |
| `CreateGroupResponse`       | group     |
| `GroupMessage`              | group     |
| `GroupMessageBroadcast`     | group     |
| `JoinGroup`                 | group     |
| `JoinGroupResponse`         | group     |
| `LeaveGroup`                | group     |
| `LeaveGroupResponse`        | group     |
| `EntitySpawn`               | entity    |
| `EntityDespawn`             | entity    |
| `GameStateUpdate`           | state     |
| `MovementIntent`            | state     |
| `ZoneChange`                | zone      |

### `component/` — 4 component DTOs

| DTO             | Implements     |
|-----------------|----------------|
| `PositionDTO`   | `ComponentDTO` |
| `RenderableDTO` | `ComponentDTO` |
| `StatsDTO`      | `ComponentDTO` |
| `PortalDTO`     | `ComponentDTO` |

---

## 1.3 Defensive copies on mutable collection fields ✅

All DTOs with `List` or `Map` fields use `List.copyOf()` / `Map.copyOf()` with null-safe fallbacks:

```java
// CharacterListResponse
characters = characters == null ? List.of() : List.copyOf(characters);

// EntitySpawn
allComponents = allComponents == null ? List.of() : List.copyOf(allComponents);

// GameStateUpdate
entityUpdates = entityUpdates == null ? Map.of() : Map.copyOf(entityUpdates);
```

SpotBugs: clean (no `EI_EXPOSE_REP` / `EI_EXPOSE_REP2` violations).

---

## 1.4 `contracts-wire` → `contracts-api` compile dependency ✅

```xml
<!-- grimoire-modules/grimoire-contracts-wire/pom.xml -->
<dependency>
    <groupId>com.grimoire</groupId>
    <artifactId>grimoire-contracts-api</artifactId>
</dependency>
```

No `<version>` needed — resolved via `internal-bom` import chain.

---

## 1.5 `grimoire-parent` imports `internal-bom` ✅

```xml
<!-- grimoire-poms/grimoire-parent/pom.xml → dependencyManagement -->
<dependency>
    <groupId>com.grimoire</groupId>
    <artifactId>internal-bom</artifactId>
    <version>${revision}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

Enables inter-module dependencies without explicit version declarations.

---

## 1.6 Unit tests: 46 passing, 100% branch coverage ✅

28 test classes, 46 total test methods. Zero failures, zero errors.

| Test class                      | Tests  | Pass     |
|---------------------------------|--------|----------|
| `PortalDTOTest`                 | 2      | ✓        |
| `PositionDTOTest`               | 3      | ✓        |
| `RenderableDTOTest`             | 3      | ✓        |
| `StatsDTOTest`                  | 2      | ✓        |
| `CharacterListResponseTest`     | 4      | ✓        |
| `CharacterSelectionRequestTest` | 1      | ✓        |
| `CharacterSelectionSuccessTest` | 1      | ✓        |
| `ChatBroadcastTest`             | 1      | ✓        |
| `ChatMessageTest`               | 1      | ✓        |
| `CreateGroupTest`               | 1      | ✓        |
| `CreateGroupResponseTest`       | 1      | ✓        |
| `EntityDespawnTest`             | 1      | ✓        |
| `EntitySpawnTest`               | 2      | ✓        |
| `GameStateUpdateTest`           | 2      | ✓        |
| `GroupMessageTest`              | 1      | ✓        |
| `GroupMessageBroadcastTest`     | 1      | ✓        |
| `JoinGroupTest`                 | 1      | ✓        |
| `JoinGroupResponseTest`         | 1      | ✓        |
| `LeaveGroupTest`                | 1      | ✓        |
| `LeaveGroupResponseTest`        | 1      | ✓        |
| `LoginFailureTest`              | 1      | ✓        |
| `MovementIntentTest`            | 1      | ✓        |
| `PrivateMessageTest`            | 1      | ✓        |
| `PrivateMessageBroadcastTest`   | 1      | ✓        |
| `TokenLoginRequestTest`         | 2      | ✓        |
| `ZoneChangeTest`                | 1      | ✓        |
| `GamePacketTest`                | 4      | ✓        |
| `PacketTypeTest`                | 4      | ✓        |
| **Total**                       | **46** | **46 ✓** |

### JaCoCo coverage (`grimoire-contracts-wire`)

| Metric      | Covered | Total | Percentage |
|-------------|---------|-------|------------|
| Branch      | 6       | 6     | **100.0%** |
| Instruction | 417     | 417   | **100.0%** |

Target was ≥ 60% — achieved 100%.

---

## 1.7 `mvn clean verify` — BUILD SUCCESS ✅

All 23 modules green with full quality gates (checkstyle, PMD, SpotBugs, JaCoCo):

```
grimoire-project ................................... SUCCESS [  0.265 s]
grimoire-poms ...................................... SUCCESS [  0.008 s]
Grimoire External BOM .............................. SUCCESS [  0.019 s]
Grimoire Internal BOM .............................. SUCCESS [  0.022 s]
Grimoire Parent .................................... SUCCESS [  3.018 s]
Grimoire Starter Parent ............................ SUCCESS [  0.328 s]
Grimoire Contracts Api ............................. SUCCESS [  7.319 s]
Grimoire Contracts Wire ............................ SUCCESS [  8.708 s]
Grimoire Domain Core ............................... SUCCESS [  0.643 s]
Grimoire Domain Combat ............................. SUCCESS [  0.549 s]
Grimoire Domain Navigation ......................... SUCCESS [  0.386 s]
Grimoire Application Core .......................... SUCCESS [  0.364 s]
Grimoire Application Session ....................... SUCCESS [  0.382 s]
Grimoire Infra Network Netty ....................... SUCCESS [  0.378 s]
Grimoire Infra Security Keycloak ................... SUCCESS [  0.394 s]
Grimoire Infra Persistence Jpa ..................... SUCCESS [  0.443 s]
Grimoire Infra Observability ....................... SUCCESS [  0.341 s]
Grimoire Test Kit .................................. SUCCESS [  0.344 s]
grimoire-modules ................................... SUCCESS [  0.004 s]
Grimoire Server App ................................ SUCCESS [  0.441 s]
Grimoire Web App ................................... SUCCESS [  0.376 s]
Grimoire Client App ................................ SUCCESS [  0.384 s]
grimoire-applications .............................. SUCCESS [  0.004 s]
------------------------------------------------------------------------
BUILD SUCCESS (25.703 s)
```

---

## 1.8 Dependency diagram updated ✅

`design.puml` contains the `WIRE ..> API : "shared types"` edge reflecting the `contracts-wire → contracts-api` compile
dependency.

---

## Deferred items (not blockers)

| Item                                      | Status   | Target wave                             |
|-------------------------------------------|----------|-----------------------------------------|
| Fury codecs (`FuryEncoder`/`FuryDecoder`) | Deferred | Wave 4 (`grimoire-infra-network-netty`) |
| jqwik property tests for DTO round-trips  | Optional | Backlog                                 |

---

**Wave 1 is complete. 2 contract modules built, 46 tests passing at 100% coverage, all quality gates green. Wave 2 (
Domain) may begin.**

