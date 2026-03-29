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

- [ ] `MovementSystem` — reads `Velocity`/`Position`/`BoundingBox`; writes `Position`/`Dirty`
- [ ] `CombatSystem` — reads `AttackIntent`/`Stats`; writes `Dead`/`Experience`/`Dirty`; death via `GameEventPort`
- [ ] `LevelUpSystem` — reads `Experience`/`Stats`; delegates to `LevelingRules`; writes `Stats`/`Dirty`
- [ ] `NpcAiSystem` — reads `NpcAi`/`Position`/`Zone`; writes `MovementIntent`/`AttackIntent`; uses `SpatialGrid`+`AStarPathfinder`
- [ ] `PortalCooldownSystem` — reads/writes `PortalCooldown` tick countdown
- [ ] `SpatialGridSystem` — rebuilds `SpatialGrid` from `Position`/`Zone` each tick
- [ ] `ZoneChangeSystem` — reads `Portal`/`Position`/`PortalCooldown`; writes zone transitions; zone-change via `GameEventPort`
- [ ] `GroupService` — create/join/leave/kick orchestration; updates ECS + notifies via `GameEventPort`
- [ ] `GameConfig` port interface in `application-core`
- [ ] `PlayerControlled` extended with `sessionId` field (domain-core)
- [ ] Unit tests for all systems (ports faked/mocked)
- [ ] `mvn clean verify` green (all quality gates)

## Wave 4a — Test Kit, Observability & Codecs

> **Rationale:** Small-scope modules that unblock Wave 4b. Codecs (deferred from Wave 1)
> land in `infra-network-netty` without the full Netty bootstrap.

- [ ] `grimoire-test-kit`: ArchUnit rule catalog enforcing layer boundaries (§7.3 of Unified Plan)
- [ ] `grimoire-test-kit`: test fixtures and fakes for `GameEventPort`, `SessionConfig`
- [ ] `grimoire-test-kit`: `EngineTestHarness` abstract base class (inspired by `october`)
- [ ] `grimoire-infra-observability`: SLF4J + Logback config, structured logging
- [ ] `grimoire-infra-network-netty`: `ForyEncoder`/`ForyDecoder` codecs (deferred from Wave 1)
- [ ] `mvn clean verify` green (all quality gates)

## Wave 4b — Infrastructure Adapters

> **Rationale:** Heavy infra modules that implement application ports. Requires
> Testcontainers (Docker) for integration tests.

- [ ] `grimoire-infra-network-netty`: Netty bootstrap (`GameServer`, `GameChannelInitializer`, `BootstrapFactory`)
- [ ] `grimoire-infra-network-netty`: `GameLogicHandler` (packet dispatch → `GameCommandQueue`)
- [ ] `grimoire-infra-network-netty`: channel map (`ConcurrentHashMap<String, Channel>`) — session-to-channel mapping
- [ ] `grimoire-infra-network-netty`: adapter systems (`NetworkSyncSystem`, `NetworkVisibilitySystem`, `PlayerInputSystem`)
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

- [ ] Evaluate Artemis-ODB ECS behind `SimulationPort`
- [ ] Evaluate `october` lifecycle/FSM patterns for client scene architecture
- [ ] Pitest mutation testing evaluation
