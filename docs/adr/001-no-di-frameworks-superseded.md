# ADR 001: Pure Java Dependency Injection

**Status:** ~~ACCEPTED~~ **SUPERSEDED by ADR-002**
**Date:** 2025-12-28
**Superseded On:** 2026-03-27
**Superseded By:** [ADR-002: Adopt Micronaut Compile-Time DI as the Project Standard](002-adopt-micronaut-di.md)

---

> **NOTE:** This document is preserved for historical context only.
> The decision below was reversed because all three of the "Litmus Test"
> conditions it defined were met during the migration to the Grandparent
> Maven Architecture. Micronaut's compile-time annotation processing
> generates DI code with **zero runtime reflection**, maintaining
> sub-millisecond startup targets while eliminating boilerplate.

---

## Original Decision (Archived)

We will use **Manual Constructor Injection** for all component wiring. We explicitly **REJECT** the inclusion of
Dependency Injection frameworks (Spring Boot, Micronaut, Quarkus, Guice, Dagger).

## The Reasoning (The "Why")

1. **Startup Latency:** Frameworks involve classpath scanning, configuration parsing, and proxy generation. This costs
   500ms+ on startup. Our target is <50ms.
2. **Runtime Overhead:** Many frameworks use Reflection or dynamic proxies, which pollute stack traces and can interfere
   with "Hot Loop" optimization (JIT inlining).
3. **Debuggability:** "Magic" wiring hides the initialization order. Manual `new GameServer(new BootstrapFactory())`
   makes the dependency graph explicit and compiler-checked.
4. **Artifact Size:** A framework adds megabytes of transitive dependencies. `net-bullet` is a micro-kernel; every
   kilobyte matters.

## The Litmus Test (Conditions that were met — triggering supersession)

1. ✅ **Complexity:** The application wiring block exceeded **200 lines** of pure object instantiation.
2. ✅ **Graph Depth:** The dependency graph exceeded **5 layers** of depth.
3. ✅ **Performance Proof:** Micronaut demonstrated **Zero Reflection** at runtime via compile-time AOT and sub-10ms
   startup impact.

**All three conditions were met. ADR-002 was adopted.**

