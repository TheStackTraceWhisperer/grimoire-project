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
- [ ] Codecs (`ForyEncoder`/`ForyDecoder`) deferred to Wave 4 (`grimoire-infra-network-netty`)
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

## Wave 4 — Infrastructure

- [ ] `grimoire-infra-network-netty`: Netty bootstrap, `GameLogicHandler`, transport adapters
- [ ] `grimoire-infra-security-keycloak`: JWT verification adapter
- [ ] `grimoire-infra-persistence-jpa`: JPA entities, repositories, domain mappers
- [ ] `grimoire-infra-observability`: logging, metrics, tracing config
- [ ] `grimoire-test-kit`: fixtures, fakes, ArchUnit rule catalog, `EngineTestHarness`
- [ ] Testcontainers integration tests for persistence and security

## Wave 5 — Application Assembly

- [ ] `grimoire-server-app`: wire all layers, Micronaut config, startup
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
