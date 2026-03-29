# ADR 002: Adopt Micronaut Compile-Time DI as the Project Standard

**Status:** ACCEPTED
**Date:** 2026-03-27
**Supersedes:** [ADR-001: Pure Java Dependency Injection](001-no-di-frameworks-superseded.md)
**Runtime:** Java 25

## The Decision

We adopt **Micronaut Framework** as the official, project-wide standard for all dependency
injection and component wiring across every layer of the Grimoire ecosystem — including the
high-performance network layer, the ECS game loop, and all deployable applications.

The key discriminator: Micronaut performs **all dependency injection at compile time** via
annotation processing. There is **zero runtime reflection, zero classpath scanning, and zero
proxy generation at startup**.

## The Reasoning (The "Why")

### 1. All ADR-001 Litmus Test Conditions Were Met

The original [`ADR-001`](001-no-di-frameworks-superseded.md) specified three conditions under
which a compile-time DI framework would be acceptable. All three conditions have been met:

| Condition | Threshold | Actual |
|-----------|-----------|--------|
| Manual wiring boilerplate | >200 lines | ✅ Exceeded |
| Dependency graph depth | >5 layers | ✅ Exceeded |
| Framework startup overhead | <10ms, zero reflection | ✅ Micronaut AOT: ~3ms impact, zero reflection |

### 2. Compile-Time, Not Runtime, DI

Micronaut's annotation processor (`micronaut-inject-java`) runs at `javac` time and generates
plain Java factory classes. At runtime, the framework reads these pre-generated classes — it
does **not** scan the classpath, use `java.lang.reflect.Method.invoke()`, or generate proxies.
This directly preserves the performance goals of ADR-001.

### 3. Hot Loop Compatibility

The ECS game loop (`GameLoop`, `SystemScheduler`) runs at 20 TPS (50ms tick). Micronaut
`@Scheduled` tasks use a standard `ScheduledExecutorService` under the hood — **no dynamic
dispatch, no reflection per tick**. JIT inlining is unaffected.

### 4. Explicit Startup Graph

Because all DI factories are generated at compile time, the full dependency graph is visible
in the compiled `target/classes` directory and inspectable in the IDE. The transparency
concern from ADR-001 is fully addressed.

### 5. Consolidates the Ecosystem

The `grimoire-server` and `grimoire-web` modules were already using Micronaut. The `net-bullet`
and `ecs-simulation` reference modules were isolated experiments that have now been promoted
into the unified `grimoire-project` reactor. A single framework across all layers eliminates
the cognitive overhead of mixing manual wiring with framework wiring.

## The Architecture This Enables

The Grandparent Maven Architecture (`grimoire-parent` → `grimoire-starter-parent`) codifies
Micronaut as the standard:

* **Library modules** (`grimoire-ecs`, `grimoire-network`, `grimoire-shared`, `grimoire-data`)
  use `grimoire-parent` — they get quality gates and may use Micronaut annotations where
  beneficial (`@Singleton`, `@Scheduled`) but do not require the full application bootstrap.

* **Application modules** (`grimoire-server`, `grimoire-client`, `grimoire-web`) use
  `grimoire-starter-parent` — they inherit `grimoire-parent`'s quality gates AND get the
  full Micronaut runtime, annotation processors, standardized logging, and the
  `micronaut-maven-plugin` for fat-JAR and native-image builds.

## Retained from ADR-000

* Java 25 is still mandatory (enforced by `maven-enforcer-plugin` in `grimoire-parent`).
* All quality gates are retained and **lifted into `grimoire-parent`**: SpotBugs + FindSecBugs,
  PMD, Checkstyle, JaCoCo (80% line / 60% branch coverage minimum).

## Trade-Offs

* **Added build dependency:** `micronaut-inject-java` annotation processor runs at compile time.
  This adds ~1–2 seconds to clean builds on commodity hardware. Accepted.
* **Framework expertise:** Developers must understand Micronaut's compile-time model.
  Documented in `docs/2_DEVELOPMENT_GUIDE.md`.
* **Native image builds:** GraalVM native image compilation requires additional configuration
  for dynamic class loading (e.g., Fory serialization). This is tracked as a separate task.

## Consequences

✅ Eliminates hundreds of lines of manual constructor wiring
✅ Enables `@Scheduled`, `@Value`, `@ConfigurationProperties`, and declarative HTTP clients
✅ Unified framework across all modules
✅ Zero runtime reflection — startup target of <50ms is preserved
✅ All ADR-001 performance invariants honoured

