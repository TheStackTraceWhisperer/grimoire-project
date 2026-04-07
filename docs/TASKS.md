# Grimoire — Task Backlog

## Wave 0 — Standards & Empty Skeletons

- [x] Java 25 enforcement (`grimoire-parent`: compiler release, enforcer rule)
- [x] Quality config files at project root (`checkstyle.xml`, `pmd-ruleset.xml`, `formatter.xml`)
- [x] `grimoire-parent` POM (compiler, enforcer, surefire, failsafe, JaCoCo, quality profiles)
- [x] `grimoire-starter-parent` POM (extends `grimoire-parent`, Micronaut BOMs, micronaut-maven-plugin)
- [x] `external-bom` populated with external dependency versions
- [x] `internal-bom` populated with all `com.grimoire` module entries
- [x] BOM import chain: `internal-bom` → `external-bom`, `grimoire-starter-parent` → `internal-bom`
- [x] Empty module skeletons for all 12 library + 3 application modules
- [x] Migration policy docs (`REFERENCES_READ_ONLY.md`, `READINESS_GATES.md`)
- [x] `mvn clean verify -DskipQuality` green
- [x] `mvn clean verify` green
- [x] Dependency diagram (`deps.puml`) — compile & runtime scope only

## Wave 1 — Contracts

- [x] `grimoire-contracts-api`: `ComponentDTO` marker interface (`com.grimoire.contracts.api.component`)
- [x] `grimoire-contracts-wire`: protocol, DTOs, component DTOs (`com.grimoire.contracts.wire.*`)
  - [x] `protocol/`: `GamePacket` record, `PacketType` enum (23 packet types)
  - [x] `dto/`: 18 DTO records (auth, character, chat, group, entity, state, zone)
  - [x] `component/`: 4 component DTOs (`PositionDTO`, `RenderableDTO`, `StatsDTO`, `PortalDTO`)
  - [x] Defensive copies on mutable collection fields (SpotBugs clean)
- [x] `contracts-wire` → `contracts-api` compile dependency wired
- [x] `grimoire-parent` imports `internal-bom` (enables inter-module deps without version)
- [x] 45 unit tests passing, branch coverage ≥ 60%
- [x] `mvn clean verify` green (all quality gates)
- [x] `deps.puml` updated with `CWIRE --> CAPI` edge
- [ ] Codecs (`ForyEncoder`/`ForyDecoder`) deferred to Wave 4a (`grimoire-infra-network-netty`)
- [ ] jqwik property tests for DTO serialization round-trips (optional enhancement)

## Wave 2 — Domain

- [x] `grimoire-domain-core`: entities, value objects, domain services
- [x] `grimoire-domain-combat`: combat rules, cooldowns, leveling
- [x] `grimoire-domain-navigation`: pathing abstractions, `AStarPathfinder`, `SpatialGrid`
- [x] Inter-module deps: `domain-*` → `contracts-api`
- [x] jqwik property tests on domain invariants

## Wave 3 — Application

- [x] `grimoire-application-core`: use-case orchestration, command handlers
- [x] `grimoire-application-session`: session policies, `SessionManager`
- [x] Inter-module deps: `application-*` → `domain-*`, `contracts-api`

## Wave 3.5 — Core Completion (Pure Game Systems)

> **Rationale:** The `GameSystem` interface and ECS engine exist but zero concrete systems
> have been built. Pure systems must land in `application-core` before infrastructure
> adapters can wire them. See [ADR-003](adr/003-wave4-architecture-boundaries.md) §3.

- [x] `MovementSystem` — reads `Velocity`/`Position`/`BoundingBox`; writes `Position`/`Dirty`
- [x] `CombatSystem` — reads `AttackIntent`/`Stats`; writes `Dead`/`Experience`/`Dirty`; death via `GameEventPort`
- [x] `LevelUpSystem` — reads `Experience`/`Stats`; delegates to `LevelingRules`; writes `Stats`/`Dirty`
- [x] `NpcAiSystem` — reads `NpcAi`/`Position`/`Zone`; writes `MovementIntent`/`AttackIntent`; uses `SpatialGrid`+
  `AStarPathfinder`
- [x] `PortalCooldownSystem` — reads/writes `PortalCooldown` tick countdown
- [x] `SpatialGridSystem` — rebuilds `SpatialGrid` from `Position`/`Zone` each tick
- [x] `ZoneChangeSystem` — reads `Portal`/`Position`/`PortalCooldown`; writes zone transitions; zone-change via
  `GameEventPort`
- [x] `GroupService` — create/join/leave/kick orchestration; updates ECS + notifies via `GameEventPort`
- [x] `GameConfig` port interface in `application-core`
- [x] `PlayerControlled` extended with `sessionId` field (domain-core)
- [x] Unit tests for all systems (ports faked/mocked)
- [x] `mvn clean verify` green (all quality gates)

## Wave 3.9 — Primitive-Backed Engine Upgrade

> **Rationale:** The Artemis-ODB ECS migration (Wave 7 evaluation) is permanently abandoned
> — Artemis lacks Java 25 bytecode weaving support and violates [ADR-000](adr/000-java-25-mandate.md).
> Instead, the existing custom ECS in `application-core` is upgraded to achieve AAA-grade
> performance using contiguous primitive arrays. Pure JDK 25 — zero new dependencies.

### Step 1 — Mutable Domain Shift (domain-core, domain-combat, domain-navigation)

- [x] Refactor all 21 ECS components from immutable records to mutable `public class` POJOs
- [x] Public fields for zero-allocation direct access
- [x] No-arg constructors for array pre-allocation
- [x] Zero-allocation `update()` methods on every component (e.g., `Position.update(double, double)`)
- [x] `Position.translate(double, double)` for delta movement
- [x] `AttackCooldown.decrement()` for tick countdown
- [x] `Experience.addXp(int)` for zero-allocation XP gain
- [x] `equals()`/`hashCode()`/`toString()` on all components
- [x] `Component` marker interface Javadoc updated to describe mutable POJO contract

### Step 2 — The Integer World (application-core)

- [x] `EntityManager` — entities are primitive `int` IDs from `AtomicInteger`
- [x] `boolean[] alive` array tracks entity liveness (no `Set<String>`)
- [x] `maxAliveId` high-water mark for bounded iteration
- [x] `EcsWorld.createEntity()` returns `int`; all methods accept `int entityId`
- [x] All `String`/`UUID` entity tracking removed

### Step 3 — The Contiguous ComponentManager (application-core)

- [x] 21 fixed-size contiguous arrays (one per component type, `new T[100_000]`)
- [x] All `HashMap` storage removed; entity ID is the array index
- [x] Direct typed accessors for hot path (`getPositions()`, `getVelocities()`, etc.)
- [x] Generic `addComponent(int, Component)` / `getComponent(int, Class)` via `arrayMap`
- [x] `removeAllComponents(int)` nulls all arrays for an entity
- [x] `MethodReturnsInternalArray` PMD rule globally excluded (by design)

### Step 4 — System Iteration (application-core)

- [x] All 7 `GameSystem` implementations use `for (int i = 0; i < max; i++)` loops
- [x] Systems query `ecsWorld.getMaxEntityId()` and `ecsWorld.getAlive()` directly
- [x] Systems access `ComponentManager` arrays by index — no string-based queries
- [x] `MovementSystem` — contiguous loop over `velocities[]`/`positions[]`
- [x] `CombatSystem` — three-phase tick (cooldowns, attacks, death) via contiguous loops
- [x] `LevelUpSystem` — contiguous loop over `experiences[]`/`stats[]`
- [x] `NpcAiSystem` — contiguous loop over `npcAis[]`, spatial grid for aggro
- [x] `PortalCooldownSystem` — contiguous loop over `portalCooldowns[]`
- [x] `SpatialGridSystem` — contiguous loop rebuilding spatial grid
- [x] `ZoneChangeSystem` — contiguous loops for portal overlap detection
- [x] `FakeGameEventPortTest` updated to `int` entity IDs
- [x] PMD ruleset: `UseVarargs`, `MethodReturnsInternalArray` globally excluded for ECS
- [x] `mvn clean verify` green (all quality gates)

## Wave 4a — Test Kit, Observability & Codecs

> **Rationale:** Small-scope modules that unblock Wave 4b. Codecs (deferred from Wave 1)
> land in `infra-network-netty` without the full Netty bootstrap.

- [x] `grimoire-test-kit`: ArchUnit rule catalog enforcing layer boundaries (§7.3 of Unified Plan)
- [x] `grimoire-test-kit`: test fixtures and fakes for `GameEventPort`, `SessionConfig`
- [x] `grimoire-test-kit`: `EngineTestHarness` abstract base class (inspired by `october`)
- [x] `grimoire-infra-observability`: SLF4J + Logback config, structured logging
- [x] `grimoire-infra-network-netty`: `ForyEncoder`/`ForyDecoder` codecs (deferred from Wave 1)
- [x] `grimoire-coverage-report`: JaCoCo aggregate coverage across all modules (`report-aggregate`)
- [x] `mvn clean verify` green (all quality gates)

## Wave 4b — Infrastructure Adapters

> **Rationale:** Heavy infra modules that implement application ports. Requires
> Testcontainers (Docker) for integration tests.

- [ ] `grimoire-infra-network-netty`: Netty bootstrap (`GameServer`, `GameChannelInitializer`, `BootstrapFactory`)
- [ ] `grimoire-infra-network-netty`: `GameLogicHandler` (packet dispatch → `GameCommandQueue`)
- [ ] `grimoire-infra-network-netty`: channel map (`ConcurrentHashMap<String, Channel>`) — session-to-channel mapping
- [ ] `grimoire-infra-network-netty`: adapter systems (`NetworkSyncSystem`, `NetworkVisibilitySystem`,
  `PlayerInputSystem`)
- [ ] `grimoire-infra-security-keycloak`: JWT/Keycloak token validation adapter
- [ ] `grimoire-infra-persistence-jpa`: JPA entities (`Account`, `Character`, `PlayerGroup`, `GroupMembership`)
- [ ] `grimoire-infra-persistence-jpa`: repositories + domain mappers
- [ ] `grimoire-infra-persistence-jpa`: `PersistenceSystem` (adapter `GameSystem`)
- [ ] Testcontainers integration tests for persistence (PostgreSQL) and security (Keycloak)
- [ ] `mvn clean verify` green (all quality gates)

## Wave 5 — Application Assembly

- [ ] `grimoire-server-app`: Micronaut bootstrap, config binding (`@ConfigurationProperties` → `GameConfig`)
- [ ] `grimoire-server-app`: game-loop thread driver (`while(isRunning)` + delta-time + `SystemScheduler.tick()`)
- [ ] `grimoire-server-app`: system ordering — collect all `GameSystem` beans, pass ordered list to `SystemScheduler`
- [ ] `grimoire-server-app`: content bootstrap (`PrefabRegistry`, `NpcFactory`, `ZoneInitializer`)
- [ ] `grimoire-web-app`: wire and configure
- [ ] `grimoire-client-app`: wire and configure

## Wave 6 — Client Merge

- [ ] Merge OAuth2 auth flow (from `grimoire-client`) into `grimoire-client-app`
- [ ] LWJGL rendering stack + scene manager (from `grimoire-client-v2`)
- [ ] Client-side packet queue model

## Wave 7 — Incubator Evaluation

- [x] ~~Evaluate Artemis-ODB ECS behind `SimulationPort`~~ — **ABANDONED.** Artemis lacks Java 25 bytecode weaving;
  violates [ADR-000](adr/000-java-25-mandate.md). Replaced by Wave 3.9 primitive-backed engine upgrade.
- [ ] Evaluate `october` lifecycle/FSM patterns for client scene architecture
- [ ] Pitest mutation testing evaluation
