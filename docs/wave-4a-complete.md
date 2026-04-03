# Wave 4a — Completion Evidence

**Date:** 2026-03-30  
**Verified by:** automated check against Wave 4a checklist (`TASKS.md`)

---

## 4a.1 `grimoire-test-kit`: ArchUnit rule catalog ✅

1 class in `com.grimoire.testkit.architecture` — reusable rule constants for consumer `@ArchTest` fields.

### Rules (§7.3 of Unified Plan)

| Rule constant | Enforces |
|---------------|----------|
| `CONTRACTS_MUST_NOT_IMPORT_GRIMOIRE` | `contracts..*` → no `domain..`, `application..`, `infra..` |
| `DOMAIN_MUST_NOT_IMPORT_INFRA` | `domain..*` → no `infra..`, `application..`, `io.netty..`, `io.micronaut..`, `jakarta.persistence..` |
| `APPLICATION_MUST_NOT_IMPORT_INFRA` | `application..*` → no `infra..`, `io.netty..`, `jakarta.persistence..` |
| `NO_JAVA_UTIL_LOGGING` | `com.grimoire..*` → no `java.util.logging..` |

All rules use `allowEmptyShould(true)` for safety in small modules.

---

## 4a.2 `grimoire-test-kit`: test fixtures and fakes ✅

2 fakes in `com.grimoire.testkit.fake`.

| Class | Implements | Key features |
|-------|-----------|--------------|
| `FakeGameEventPort` | `GameEventPort` | Records `DespawnEvent` and `ZoneChangeEvent` in ordered lists; `despawnEvents()`, `zoneChangeEvents()`, `totalEventCount()`, `clear()` |
| `FakeSessionConfig` | `SessionConfig` | Configurable validity (default 30 min); no-arg and `int` constructors |

### Design decisions

- **Hand-written fakes over Mockito:** Fakes record invocations with typed inner records — no reflection, no fragile `verify()` chains, and better IDE navigation.
- **Compile scope:** Fakes are `compile`-scoped in `test-kit` so consumer modules can use them without re-declaring the dependency.

---

## 4a.3 `grimoire-test-kit`: `EngineTestHarness` ✅

1 abstract class in `com.grimoire.testkit.harness` — adapted from the `october` project's harness for Grimoire's server-side ECS (no OpenGL, no DI container).

| Method | Description |
|--------|-------------|
| `setUpEngine()` (`@BeforeEach`) | Creates `EntityManager` → `ComponentManager` → `EcsWorld` → `SystemScheduler`; calls `createSystems()`, `createGameEventPort()`, `createGameConfig()` |
| `createSystems()` | Override to provide systems under test (default: empty list) |
| `createGameEventPort()` | Override to provide custom port (default: `FakeGameEventPort`) |
| `createGameConfig()` | Override to provide custom config (default: all defaults) |
| `tick(float)` / `tick()` | Runs one scheduler tick (default delta: 50 ms = 20 TPS) |

Protected fields: `world`, `scheduler`, `gameEventPort`, `gameConfig` — all accessible in subclass tests.

---

## 4a.4 `grimoire-infra-observability`: SLF4J + Logback config ✅

### `logback.xml` (classpath resource)

```
%d{ISO8601} [%thread] %-5level %logger{36} - %marker%msg%n
```

| Logger | Level |
|--------|-------|
| Root | `INFO` |
| `com.grimoire` | `DEBUG` |
| `io.netty` | `WARN` |
| `io.micronaut` | `INFO` |
| `org.hibernate` | `WARN` |
| `org.apache.fory` | `WARN` |

### `Markers` utility class

6 SLF4J `Marker` constants: `NETWORK`, `SESSION`, `COMBAT`, `PERSISTENCE`, `ENGINE`, `NAVIGATION`.

Usage: `LOG.info(Markers.NETWORK, "Client connected: {}", sessionId);`

---

## 4a.5 `grimoire-infra-network-netty`: Fory codecs ✅

2 classes in `com.grimoire.infra.network.codec`.

| Class | Extends | Description |
|-------|---------|-------------|
| `ForyEncoder` | `MessageToByteEncoder<Object>` | Writes 4-byte length prefix + Fory-serialised payload |
| `ForyDecoder` | `LengthFieldBasedFrameDecoder` | Strips 4-byte length prefix, deserialises via Fory; max frame 10 MB |

Both use `ThreadLocalFory` with `Language.JAVA` and `requireClassRegistration(false)` — safe for multi-threaded Netty pipelines.

### BOM fix

`external-bom` corrected: `org.apache.fury:fury-core` → `org.apache.fory:fory-core` (library rename; version 0.13.1 unchanged).

---

## 4a.6 Tests: 33 new (all unit) ✅

| Test class | Module | Tests | Pass |
|-----------|--------|-------|------|
| `GrimoireLayerRulesTest` | test-kit | 4 | ✓ |
| `FakeGameEventPortTest` | test-kit | 6 | ✓ |
| `FakeSessionConfigTest` | test-kit | 3 | ✓ |
| `EngineTestHarnessTest` | test-kit | 8 | ✓ |
| `MarkersTest` | infra-observability | 6 | ✓ |
| `ForyCodecTest` | infra-network-netty | 6 | ✓ |
| **Total (Wave 4a)** | | **33** | **33 ✓** |

---

## 4a.7 Build verification ✅

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
Grimoire Infra Network Netty ....................... SUCCESS  [6 tests]
Grimoire Infra Security Keycloak ................... SUCCESS
Grimoire Infra Persistence Jpa ..................... SUCCESS
Grimoire Infra Observability ....................... SUCCESS  [6 tests]
Grimoire Test Kit .................................. SUCCESS  [21 tests]
grimoire-modules ................................... SUCCESS
Grimoire Server App ................................ SUCCESS
Grimoire Web App ................................... SUCCESS
Grimoire Client App ................................ SUCCESS
grimoire-applications .............................. SUCCESS

BUILD SUCCESS

Total tests (project-wide): 848
New tests (Wave 4a): 33
Quality gates: Checkstyle ✅ | PMD ✅ | SpotBugs ✅ | JaCoCo ✅ | Formatter ✅
```

---

## New source files (Wave 4a)

| File | Module | Package | Type | Lines |
|------|--------|---------|------|------:|
| `GrimoireLayerRules.java` | test-kit | `architecture` | class | 100 |
| `FakeGameEventPort.java` | test-kit | `fake` | class | 96 |
| `FakeSessionConfig.java` | test-kit | `fake` | class | 43 |
| `EngineTestHarness.java` | test-kit | `harness` | abstract class | 146 |
| `Markers.java` | infra-observability | `observability` | class | 50 |
| `package-info.java` | infra-observability | `observability` | package-info | 20 |
| `logback.xml` | infra-observability | resources | config | 24 |
| `ForyEncoder.java` | infra-network-netty | `codec` | class | 43 |
| `ForyDecoder.java` | infra-network-netty | `codec` | class | 65 |
| **Total** | | | **9 source files** | **587** |

Modified: `external-bom/pom.xml` — Fory coordinates fix (`fury` → `fory`).

---

**Wave 4a is complete. All 23 modules build green. Wave 4b (Infrastructure Adapters) may begin.**

