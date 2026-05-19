# BASELINE SPECIFICATION

Project: Session Manager + Resource Manager Integration

---

# 1. Objective

Integrate Resource Manager into Session Manager
while preserving:
- existing business logic
- API compatibility
- security behavior
- operational stability

---

# 2. Existing Platform Analysis

## Session Manager
Current Stack:
- Spring Boot 3.5.2
- Java 17
- Neo4j 5.26.6
- Hazelcast 5.5
- Kafka
- OpenTelemetry

Responsibilities:
- session lifecycle
- authentication
- authorization
- distributed coordination

---

## Resource Manager
Current Stack:
- partial Spring Boot 2 compatibility
- explicit Netty dependency
- protobuf 3.x
- Neo4j integration
- Kafka integration

Responsibilities:
- resource allocation
- resource orchestration
- resource scheduling
- resource tracking

---

# 3. Identified Risks

## Critical Risks

### javax → jakarta migration
Resource Manager still contains:
```text
javax.*
```

---

# 4. Constitution Check

- No implementation may begin until architecture, dependency, and security analyses are complete and a migration strategy is prepared (see Constitution v2.1, Principle I).
- Session Manager standards for authentication, authorization, session lifecycle, security, observability, and API governance are authoritative (see Constitution v2.1, Principle II).
- Migration must be phased, reversible, feature-toggle driven, and independently deployable (see Constitution v2.1, Principle III).
- All existing APIs, especially /api/v1/sessions/**, must remain functional (see Constitution v2.1, Principle IV).
- All integration points (Neo4j, Kafka, Hazelcast) must be reviewed for compatibility and security.
- Security analysis must precede any implementation.
- Migration plans must include rollback and feature toggle strategies.

---

# 5. Baseline User Stories & Acceptance Criteria

### User Story 1 - Session Integrity (Priority: P1)

As a platform operator,
I want all session lifecycle and authentication flows to remain unchanged
during and after Resource Manager integration,
so that existing clients experience no disruption.

**Acceptance Criteria:**
- All /api/v1/sessions/** endpoints behave identically before and after integration.
- No regression in authentication or session lifecycle logic.
- Automated regression tests pass for all session flows.

---

### User Story 2 - Resource Operations Compatibility (Priority: P2)

As a developer,
I want Resource Manager APIs to be accessible via Session Manager
without breaking existing Resource Manager clients,
so that integration is seamless and backward compatible.

**Acceptance Criteria:**
- Resource Manager endpoints are exposed via Session Manager.
- Existing Resource Manager API contracts remain valid.
- Feature toggles allow enabling/disabling integrated resource operations.

---

### User Story 3 - Secure Migration (Priority: P3)

As a security engineer,
I want all new integration points (Neo4j, Kafka, Hazelcast) to be reviewed and tested for security and compatibility
before production rollout,
so that the platform remains secure and stable.

**Acceptance Criteria:**
- Security review is completed for all integration points.
- Compatibility tests pass for all new connections.
- Rollback plan is documented and tested.

---

# 6. Out of Scope

- UI/UX changes
- Non-session-related feature additions
- Major refactoring outside integration scope

---

# 7. Open Questions

- What is the timeline for javax → jakarta migration in Resource Manager?
- Are there any undocumented dependencies between Session Manager and Resource Manager?
- What is the rollback procedure if integration causes production issues?
