# Grimoire — Unified Architecture Plan
## Reference Synthesis & Module Design

**Last revised:** 2026-03-28

---

## 1. Reference Project Summaries

### `references/grimoire` (Primary Source)
Full MMO-lite game; multi-module Maven, Java 21, Micronaut 4.8.3, Netty, Keycloak, PostgreSQL/JPA, JavaFX + LWJGL clients. Key strengths:
- Apache Fory binary serialization (`ForyEncoder`/`ForyDecoder`)
- `GamePacket` record + `PacketType` enum, game protocol design
- `@ConfigurationProperties GameConfig` for all tunable parameters
- `SessionManager` (in-memory with scheduled expiry)
- Keycloak JWT validation via `micronaut-security`
- `Dirty` component for change-detection
- Zone-aware `SpatialGrid`, full A* pathfinder (`AStarPathfinder` + `NavigationGrid`)
- `@Prototype GameLogicHandler` — one Netty handler per connection, Micronaut-managed
- Two client implementations: JavaFX (`grimoire-client`) with OAuth2 auth flow, LWJGL (`grimoire-client-v2`) with OpenGL rendering + scene manager

### `references/net-bullet` (Primary Source)
Java 25 Netty micro-kernel. Key strengths:
- `BootstrapFactory` with Epoll/NIO OS-detection + named thread factory
- `GameServer` as `AutoCloseable` with `CompletableFuture`-based async `start()`/`stop()` and `ReentrantLock` state guards
- Full quality pipeline: SpotBugs + FindSecBugs, PMD + CPD, Checkstyle, merged JaCoCo (unit + IT, 80% line / 60% branch), ArchUnit, jqwik property tests, Formatter
- `*Test` (Surefire) / `*IT` (Failsafe) convention; strict enforcer (Java 25, Maven 3.9.9, dependency convergence, explicit plugin versions)

### `references/ecs-simulation` (Incubator)
Server-side ECS simulation using Artemis-ODB + Micronaut. Notable patterns:
- Delta-capped `GameLoop`, thread-safe `WorldCommandQueue`
- `SpatialHashGrid` using FastUtil, behavior-tree AI
- ArchUnit enforcement of data-only ECS components

### `references/october` (Incubator)
2D OpenGL/LWJGL game engine, Java 21 + Micronaut. Notable patterns:
- `IService` lifecycle interface, stack-based FSM `ApplicationStateService`
- `ComponentRegistry` auto-discovery via Micronaut `BeanIntrospection`
- `EngineTestHarness` abstract base class for live-engine tests
- Composable `ApplicationLoopPolicy` strategy

---

## 2. ADR Compliance

| ADR | Mandate | Plan Compliance |
|-----|---------|-----------------|
| ADR-000 | Java 25 mandatory, enforced by `maven-enforcer` | `grimoire-parent` enforces `[25,)` + Maven 3.9.9 |
| ADR-001 | Superseded (no manual DI) | No manual wiring; all DI via Micronaut |
| ADR-002 | Micronaut compile-time DI + grandparent architecture | `grimoire-parent` / `grimoire-starter-parent` under `grimoire-poms/` |

---

## 3. Decisions Locked by This Plan

1. **No changes under `references/`** — reference projects are read-only inputs. All new work goes under project root modules.
2. **Standards and empty modules first** — quality gates, BOMs, and empty module skeletons must build green before any source code is migrated.
3. **Do not force inclusion of all reference projects** — only adopt components that meet the adoption criteria below.
4. **Prioritize `references/grimoire` and `references/net-bullet`** as primary sources.
5. **Treat `references/ecs-simulation` and `references/october` as incubators** — defer unless hardened by tests and operational evidence.
6. **Single client runtime: LWJGL** — merge both grimoire client codebases into one `grimoire-client-app` using LWJGL rendering from `grimoire-client-v2` and OAuth/login/packet coverage from `grimoire-client`.
7. **JavaFX is retired** — do not carry forward JavaFX dependencies, TestFX, or FXML scenes.

---

## 4. Adoption Criteria

Only adopt a component from a reference project when **all** of the following are true:
- Proven in a running reference app (not only a prototype harness)
- Clear ownership boundary (can be isolated behind ports/interfaces)
- Existing tests or straightforward test seams
- ADR fit (Java 25, Micronaut DI)
- Operational clarity (configurable, observable, and maintainable)

Anything failing these criteria is deferred to a future incubator module.

---

## 5. Adopt / Defer Decisions by Reference Project

### 5.1 `references/grimoire` — Primary Source
**Adopt now**
- Protocol shape (`GamePacket`/`PacketType`) and DTO vocabulary
- Session manager policy model
- Keycloak/JWT integration approach
- OAuth2 browser auth flow (from `grimoire-client`, ported to UI-agnostic service)
- LWJGL rendering stack + scene manager (from `grimoire-client-v2`)
- Client-side packet queue model (from `grimoire-client-v2`)
- JPA domain data model and repository intent
- Tunable config pattern (`@ConfigurationProperties`)

**Defer or refactor before adopt**
- Monolithic server module layout (split across new layers)
- Direct coupling between transport handlers and game state mutation
- JavaFX client code (retired)

### 5.2 `references/net-bullet` — Primary Source
**Adopt now**
- Build and quality pipeline baseline (entire plugin chain)
- `checkstyle.xml`, `pmd-ruleset.xml`, `formatter.xml` config files
- Netty bootstrap/server lifecycle patterns (`AutoCloseable`, async start/stop)
- Test naming and IT separation conventions
- Enforcer rules (Java 25, Maven 3.9.9, dependency convergence, explicit plugin versions)

**Defer**
- No-DI composition style (conflicts with ADR-002)
- Pitest mutation testing (skipped in net-bullet itself; evaluate later)

### 5.3 `references/ecs-simulation` — Incubator
**Adopt now (narrow)**
- Pure algorithmic pieces where framework-free (spatial indexing concepts, pathing utilities)

**Defer**
- Artemis-ODB runtime architecture as a default foundation
- YAML/template scanning and behavior-tree runtime

### 5.4 `references/october` — Incubator
**Adopt now (narrow)**
- Lifecycle/FSM design ideas (inform `grimoire-client-app` scene architecture)
- `EngineTestHarness` concept (inform `grimoire-test-kit` engine test support)

**Defer**
- Full engine runtime as a dependency
- LWJGL rendering subsystem (use grimoire-ref's client-v2 renderer instead, which is simpler and game-specific)

---

## 6. Module Structure

### 6.1 Existing skeleton (already in repo)

```
grimoire/
├── pom.xml                          (reactor, ${revision})
├── grimoire-poms/
│   ├── pom.xml                      (aggregator)
│   ├── external-bom/pom.xml         (empty)
│   └── internal-bom/pom.xml         (empty)
├── grimoire-modules/
│   └── pom.xml                      (aggregator, no modules yet)
├── grimoire-applications/
│   └── pom.xml                      (aggregator, no modules yet)
└── references/                      (READ-ONLY, not modified)
```

### 6.2 Target module topology

```
grimoire/
├── pom.xml                                       (reactor)
├── checkstyle.xml                                (from net-bullet)
├── pmd-ruleset.xml                               (from net-bullet)
├── formatter.xml                                 (from net-bullet)
├── grimoire-poms/
│   ├── pom.xml
│   ├── external-bom/pom.xml                      (all external dep versions)
│   ├── internal-bom/pom.xml                      (all grimoire module versions)
│   ├── grimoire-parent/pom.xml          ← NEW    (quality gates, library parent)
│   └── grimoire-starter-parent/pom.xml  ← NEW    (app parent, extends grimoire-parent)
├── grimoire-modules/
│   ├── pom.xml
│   ├── grimoire-contracts-api/          ← NEW    (port interfaces, events, commands)
│   ├── grimoire-contracts-wire/         ← NEW    (packet DTOs, codecs, wire schema)
│   ├── grimoire-domain-core/            ← NEW    (entities, value objects, domain services)
│   ├── grimoire-domain-combat/          ← NEW    (combat rules, cooldowns, leveling)
│   ├── grimoire-domain-navigation/      ← NEW    (pathing abstractions + pure algorithms)
│   ├── grimoire-application-core/       ← NEW    (use cases, command handlers)
│   ├── grimoire-application-session/    ← NEW    (session policies, orchestration)
│   ├── grimoire-infra-network-netty/    ← NEW    (Netty transport adapters)
│   ├── grimoire-infra-security-keycloak/← NEW    (JWT verification adapter)
│   ├── grimoire-infra-persistence-jpa/  ← NEW    (JPA entities/repos + mappers)
│   ├── grimoire-infra-observability/    ← NEW    (logging, metrics, tracing)
│   └── grimoire-test-kit/              ← NEW    (fixtures, fakes, arch rules, harnesses)
├── grimoire-applications/
│   ├── pom.xml
│   ├── grimoire-server-app/             ← NEW    (assembly only: wiring + config)
│   ├── grimoire-web-app/                ← NEW    (assembly only)
│   └── grimoire-client-app/             ← NEW    (assembly only, LWJGL, merged client)
└── references/                                   (READ-ONLY)
```

### 6.3 Dependency direction rules
- `contracts-*` depends on nothing but JDK
- `domain-*` depends on `contracts-api` only
- `application-*` depends on `domain-*` and `contracts-api` (port interfaces)
- `infra-*` depends on `application-*` ports and external frameworks
- `*-app` modules compose all layers; no business logic in app modules
- `test-kit` may depend on `contracts-*` and `domain-*` only

### 6.4 Parent inheritance
- **Library modules** (`grimoire-modules/*`) → parent = `grimoire-parent`
- **Application modules** (`grimoire-applications/*`) → parent = `grimoire-starter-parent`
- `grimoire-starter-parent` → parent = `grimoire-parent` (grandparent architecture per ADR-002)

---

## 7. Layer Boundaries

### 7.1 Responsibilities
| Layer | Responsibility | Framework annotations allowed? |
|-------|---------------|-------------------------------|
| `contracts-*` | Port interfaces, DTOs, wire schema | No |
| `domain-*` | Game invariants, rules, pure logic | No |
| `application-*` | Use-case orchestration, command dispatch | Minimal (`@Singleton` OK) |
| `infra-*` | External system adapters | Yes (Micronaut, Netty, JPA, etc.) |
| `*-app` | Assembly, config, startup | Yes |

### 7.2 Anti-corruption boundaries
- **Wire → Application**: `grimoire-infra-network-netty` translates packet DTOs to application commands
- **Persistence → Domain**: JPA entities stay in `grimoire-infra-persistence-jpa`; map to domain models via mappers
- **Auth → Application**: Keycloak token objects translate to internal `AuthenticatedPrincipal` model
- **Optional ECS → Application**: if adopted later, expose only `SimulationPort`; keep engine details in adapter

### 7.3 ArchUnit enforcement (in `grimoire-test-kit`)
- `domain.*` must not import from `infra.*`, `application.*`, or any framework package
- `contracts.*` must not import from any other grimoire package
- `application.*` must not import from `infra.*`
- `*-app` packages must not contain business logic classes

---

## 8. Testing Strategy

### 8.1 Test matrix by layer
| Layer | Unit tests | Integration tests | Property tests | Architecture tests |
|-------|-----------|-------------------|----------------|-------------------|
| `contracts-*` | DTO invariants, codec round-trips | — | jqwik on DTOs | — |
| `domain-*` | All rules/logic | — | jqwik on domain invariants | No framework deps |
| `application-*` | Use cases (ports faked) | — | — | No infra deps |
| `infra-*` | — | Testcontainers / embedded Netty | — | — |
| `*-app` | — | Smoke / E2E happy path | — | — |
| `test-kit` | Self-tests on fixtures | — | — | ArchUnit rule catalog |

### 8.2 Cross-cutting controls
- `*Test.java` → Surefire (unit, fast)
- `*IT.java` → Failsafe (integration, may use containers/network)
- JaCoCo merged coverage (unit + IT) with 80% line / 60% branch check
- SpotBugs + FindSecBugs on `verify`
- Checkstyle on `validate`
- PMD + CPD on `verify`
- Formatter validation on `validate`

### 8.3 Quality gate config files (copied from `references/net-bullet` to project root)
- `checkstyle.xml` — rename ruleset to "Grimoire", keep all rules
- `pmd-ruleset.xml` — rename, retarget `com.grimoire.*`
- `formatter.xml` — rename profile to "Grimoire Java Formatter"

---

## 9. Task Tracking & Wave Progress

Implementation checklists and wave-by-wave progress are tracked in [`TASKS.md`](TASKS.md).  
Completion evidence for each wave is recorded in `wave-N-complete.md` files alongside this document.

