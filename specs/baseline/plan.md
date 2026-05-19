# IMPLEMENTATION PLAN

**Branch**: `[baseline-integration]` | **Date**: 2026-05-11 | **Spec**: [specs/baseline/spec.md]
**Input**: Feature specification from `/specs/baseline/spec.md`

---

## Summary

Integrate Resource Manager into Session Manager using a new modular integration platform (`resource-manager-session`). The goal is to preserve business logic, API compatibility, security, and operational stability while modernizing and unifying the architecture.

---

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.5.2 (target), Protobuf 4.30.2 (target), Neo4j 5.26.6, Hazelcast 5.5, Kafka
**Primary Dependencies**: Spring Boot, Spring Security, Neo4j, Hazelcast, Kafka, Protobuf, Jackson
**Storage**: Neo4j
**Testing**: JUnit, Spring Test, custom regression suites (NEEDS CLARIFICATION)
**Target Platform**: JVM (Linux/Windows), containerized (Docker/K8s)
**Project Type**: Multi-module Maven platform
**Performance Goals**: No regression in throughput/latency; maintain current operational SLAs
**Constraints**: Backward compatibility for /api/v1/sessions/**; no downtime during migration; feature-toggles for all new integration points
**Scale/Scope**: Enterprise-scale, multi-service, 10k+ concurrent sessions

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- No implementation may begin until architecture, dependency, and security analyses are complete and a migration strategy is prepared (see Constitution v2.1, Principle I).
- Session Manager standards for authentication, authorization, session lifecycle, security, observability, and API governance are authoritative (see Constitution v2.1, Principle II).
- Migration must be phased, reversible, feature-toggle driven, and independently deployable (see Constitution v2.1, Principle III).
- All existing APIs, especially /api/v1/sessions/**, must remain functional (see Constitution v2.1, Principle IV).
- All integration points (Neo4j, Kafka, Hazelcast) must be reviewed for compatibility and security.
- Security analysis must precede any implementation.
- Migration plans must include rollback and feature toggle strategies.

---

## Project Structure

### Documentation (this feature)

```text
specs/baseline/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
└── tasks.md
```

### Source Code (repository root)

```text
platform-parent/
session-core/
resource-core/
shared-security/
shared-kafka/
shared-neo4j/
common-utils/
api-gateway/
```

---

# PHASE 0 — Discovery & Local Analysis

## Context Update
Both repositories are already available locally:
- session-manager (cloned)
- resource-manager (cloned)

No further cloning required.

## Tasks
- Analyze Session Manager codebase locally
- Analyze Resource Manager codebase locally
- Compare package structures
- Analyze dependency trees (pom.xml)
- Analyze Spring Boot versions
- Analyze SecurityConfig implementations
- Analyze Neo4j integration layer
- Analyze Kafka producers/consumers
- Analyze REST API contracts
- Analyze CI/CD pipelines (if present)

## Deliverables
- dependency conflict matrix
- architecture compatibility report
- security comparison report
- migration feasibility report

---

# PHASE 1 — Dependency Governance

## Objectives
Centralize dependency management inside: resource-manager-session

## Actions
- Create platform-parent module
- Align Spring Boot version to 3.5.2
- Align Java version to 17
- Align protobuf version to 4.30.2
- Remove Netty version conflicts
- Remove duplicate Jackson dependencies
- Standardize Maven dependencyManagement section

## Validation
- mvn dependency:tree (both modules)
- mvn dependency:analyze
- Fix transitive conflicts

---

# PHASE 2 — Spring Boot 3 Modernization

## Objectives
Make Resource Manager compatible with Spring Boot 3.x ecosystem.

## Actions
- Migrate javax → jakarta packages
- Upgrade Spring Security to v6 standards
- Refactor deprecated Spring APIs
- Update Neo4j Spring Data compatibility
- Validate REST controllers under Boot 3

---

# PHASE 3 — Multi-Module Architecture

## Objectives
Create unified modular system: resource-manager-session

## Target Modules

platform-parent
session-core
resource-core
shared-security
shared-kafka
shared-neo4j
common-utils
api-gateway

---

# PHASE 4 — Integration & Validation

## Actions
- Integrate modules under unified build
- Implement feature toggles for all new integration points
- Run full regression and compatibility test suites
- Document migration and rollback procedures

## Validation
- All tests pass
- No regression in session or resource APIs
- Security and compatibility reviews signed off
- Rollback plan validated
