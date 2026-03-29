# Wave 0 — Completion Evidence

**Date:** 2026-03-28  
**Verified by:** automated check against Wave 0 checklist (§9 of `grimoire-unified-plan.md`)

---

## 9.1 Java 25 Migration ✅

| Check | Evidence |
|-------|----------|
| JDK version | `openjdk version "25" 2025-09-16 LTS` (Zulu 25.28+85-CA) |
| Maven version | `Apache Maven 3.9.11` |
| `java.version` property | `<java.version>25</java.version>` in `grimoire-parent` |
| `maven.compiler.release` | `<maven.compiler.release>${java.version}</maven.compiler.release>` in `grimoire-parent` |
| Enforcer `requireJavaVersion` | `[25,)` — Rule passed |
| Enforcer `requireMavenVersion` | `[3.9.9,)` — Rule passed |
| Enforcer `banDuplicatePomDependencyVersions` | Rule passed |
| Enforcer `dependencyConvergence` | Rule passed |

```
$ mvn validate -pl grimoire-modules/grimoire-contracts-api
[INFO] --- enforcer:3.5.0:enforce (enforce-build-environment) @ grimoire-contracts-api ---
[INFO] Rule 0: org.apache.maven.enforcer.rules.version.RequireMavenVersion passed
[INFO] Rule 1: org.apache.maven.enforcer.rules.version.RequireJavaVersion passed
[INFO] Rule 2: org.apache.maven.enforcer.rules.BanDuplicatePomDependencyVersions passed
[INFO] Rule 3: org.apache.maven.enforcer.rules.dependency.DependencyConvergence passed
```

---

## 9.2 Quality Config Files ✅

All three config files present at project root:

```
checkstyle.xml
formatter.xml
pmd-ruleset.xml
```

---

## 9.3 Parent POMs ✅

| POM | Exists | Key contents |
|-----|--------|-------------|
| `grimoire-poms/grimoire-parent/pom.xml` | ✓ | Standalone parent (no reactor parent), compiler, enforcer, surefire, failsafe, JaCoCo, quality profiles |
| `grimoire-poms/grimoire-starter-parent/pom.xml` | ✓ | Extends `grimoire-parent`, imports Micronaut platform BOM + Micronaut data BOM, `micronaut-maven-plugin` |

```
grimoire-starter-parent → parent: grimoire-parent
```

---

## 9.4 BOMs ✅

| BOM | Entries | Import chain |
|-----|---------|-------------|
| `external-bom` | 18 `<artifactId>` entries | — |
| `internal-bom` | 15 `<artifactId>` entries (12 modules + external-bom + grimoire-poms + self) | Imports `external-bom` (`<scope>import</scope>`) |
| `grimoire-starter-parent` | — | Imports `internal-bom` (`<scope>import</scope>`) |

```
internal-bom/pom.xml:
  <artifactId>external-bom</artifactId>
  <type>pom</type>
  <scope>import</scope>

grimoire-starter-parent/pom.xml:
  <artifactId>internal-bom</artifactId>
  <type>pom</type>
  <scope>import</scope>
```

---

## 9.5 Empty Module Skeletons ✅

### Library modules (12) — parent: `grimoire-parent`

| Module | `pom.xml` | `src/main/java` | `src/test/java` |
|--------|-----------|-----------------|-----------------|
| grimoire-application-core | ✓ | ✓ | ✓ |
| grimoire-application-session | ✓ | ✓ | ✓ |
| grimoire-contracts-api | ✓ | ✓ | ✓ |
| grimoire-contracts-wire | ✓ | ✓ | ✓ |
| grimoire-domain-combat | ✓ | ✓ | ✓ |
| grimoire-domain-core | ✓ | ✓ | ✓ |
| grimoire-domain-navigation | ✓ | ✓ | ✓ |
| grimoire-infra-network-netty | ✓ | ✓ | ✓ |
| grimoire-infra-observability | ✓ | ✓ | ✓ |
| grimoire-infra-persistence-jpa | ✓ | ✓ | ✓ |
| grimoire-infra-security-keycloak | ✓ | ✓ | ✓ |
| grimoire-test-kit | ✓ | ✓ | ✓ |

### Application modules (3) — parent: `grimoire-starter-parent`

| Module | `pom.xml` | `src/main/java` | `src/test/java` |
|--------|-----------|-----------------|-----------------|
| grimoire-client-app | ✓ | ✓ | ✓ |
| grimoire-server-app | ✓ | ✓ | ✓ |
| grimoire-web-app | ✓ | ✓ | ✓ |

---

## 9.6 Migration Policy Docs ✅

```
docs/migration/READINESS_GATES.md
docs/migration/REFERENCES_READ_ONLY.md
```

---

## 9.7 Verification ✅

### `mvn clean verify -DskipQuality` — BUILD SUCCESS

```
grimoire-project ................................... SUCCESS [  0.196 s]
grimoire-poms ...................................... SUCCESS [  0.008 s]
Grimoire External BOM .............................. SUCCESS [  0.019 s]
Grimoire Internal BOM .............................. SUCCESS [  0.021 s]
Grimoire Parent .................................... SUCCESS [  0.418 s]
Grimoire Starter Parent ............................ SUCCESS [  0.148 s]
Grimoire Contracts Api ............................. SUCCESS [  0.381 s]
Grimoire Contracts Wire ............................ SUCCESS [  0.050 s]
Grimoire Domain Core ............................... SUCCESS [  0.045 s]
Grimoire Domain Combat ............................. SUCCESS [  0.050 s]
Grimoire Domain Navigation ......................... SUCCESS [  0.037 s]
Grimoire Application Core .......................... SUCCESS [  0.038 s]
Grimoire Application Session ....................... SUCCESS [  0.037 s]
Grimoire Infra Network Netty ....................... SUCCESS [  0.038 s]
Grimoire Infra Security Keycloak ................... SUCCESS [  0.036 s]
Grimoire Infra Persistence Jpa ..................... SUCCESS [  0.038 s]
Grimoire Infra Observability ....................... SUCCESS [  0.035 s]
Grimoire Test Kit .................................. SUCCESS [  0.038 s]
grimoire-modules ................................... SUCCESS [  0.005 s]
Grimoire Server App ................................ SUCCESS [  0.118 s]
Grimoire Web App ................................... SUCCESS [  0.105 s]
Grimoire Client App ................................ SUCCESS [  0.115 s]
grimoire-applications .............................. SUCCESS [  0.004 s]
------------------------------------------------------------------------
BUILD SUCCESS (2.377 s)
```

### `mvn clean verify` (full quality gates) — BUILD SUCCESS

```
grimoire-project ................................... SUCCESS [  0.174 s]
grimoire-poms ...................................... SUCCESS [  0.010 s]
Grimoire External BOM .............................. SUCCESS [  0.020 s]
Grimoire Internal BOM .............................. SUCCESS [  0.018 s]
Grimoire Parent .................................... SUCCESS [  2.253 s]
Grimoire Starter Parent ............................ SUCCESS [  0.204 s]
Grimoire Contracts Api ............................. SUCCESS [  1.935 s]
Grimoire Contracts Wire ............................ SUCCESS [  0.533 s]
Grimoire Domain Core ............................... SUCCESS [  0.434 s]
Grimoire Domain Combat ............................. SUCCESS [  0.406 s]
Grimoire Domain Navigation ......................... SUCCESS [  0.374 s]
Grimoire Application Core .......................... SUCCESS [  0.338 s]
Grimoire Application Session ....................... SUCCESS [  0.335 s]
Grimoire Infra Network Netty ....................... SUCCESS [  0.365 s]
Grimoire Infra Security Keycloak ................... SUCCESS [  0.345 s]
Grimoire Infra Persistence Jpa ..................... SUCCESS [  0.348 s]
Grimoire Infra Observability ....................... SUCCESS [  0.319 s]
Grimoire Test Kit .................................. SUCCESS [  0.303 s]
grimoire-modules ................................... SUCCESS [  0.003 s]
Grimoire Server App ................................ SUCCESS [  0.428 s]
Grimoire Web App ................................... SUCCESS [  0.385 s]
Grimoire Client App ................................ SUCCESS [  0.399 s]
grimoire-applications .............................. SUCCESS [  0.005 s]
------------------------------------------------------------------------
BUILD SUCCESS (10.340 s)
```

---

## Additional Artifact

| Item | Status |
|------|--------|
| `deps.puml` — compile & runtime dependency diagram | ✓ Present |

---

**Wave 0 is complete. All 23 modules build green. Wave 1 (Contracts) may begin.**

