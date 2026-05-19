# Tasks: Baseline Integration

**Input**: Design documents from `/specs/baseline/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), data-model.md, contracts/

## Phase 1: Setup (Shared Infrastructure)

- [ ] T001 Create multi-module Maven structure in platform-parent/
- [ ] T002 Initialize session-core/, resource-core/, shared-security/, shared-kafka/, shared-neo4j/, common-utils/, api-gateway/
- [ ] T003 [P] Configure linting and formatting tools in all modules

---

## Phase 2: Foundational (Blocking Prerequisites)

- [ ] T004 Analyze Session Manager local repository (session-core/)
- [ ] T005 Analyze Resource Manager local repository (resource-core/)
- [ ] T006 Compare package structures and map module boundaries
- [ ] T007 Identify overlapping domains and dependencies
- [ ] T008 Analyze Spring Boot versions and CI/CD pipeline structure
- [ ] T009 [P] Align Java version to 17 in all modules
- [ ] T010 [P] Align Spring Boot version to 3.5.2 in all modules
- [ ] T011 [P] Align protobuf version to 4.30.2 in all modules
- [ ] T012 [P] Remove explicit Netty version and resolve Jackson conflicts
- [ ] T013 [P] Standardize dependencyManagement section in platform-parent/pom.xml
- [ ] T014 [P] Run mvn dependency:tree and mvn dependency:analyze in all modules
- [ ] T015 Identify and resolve conflicting transitive dependencies

---

## Phase 3: [US1] Session Integrity (Priority: P1) 🎯 MVP

**Goal**: Ensure all session lifecycle and authentication flows remain unchanged during and after Resource Manager integration.

**Independent Test**: Automated regression tests pass for all /api/v1/sessions/** endpoints.

- [ ] T016 [US1] Preserve /api/v1/sessions/** endpoints in api-gateway/
- [ ] T017 [US1] Validate no regression in authentication/session lifecycle logic (session-core/)
- [ ] T018 [US1] Implement automated regression tests for session flows (tests/)
- [ ] T019 [US1] Document test results and validation steps (docs/)

---

## Phase 4: [US2] Resource Operations Compatibility (Priority: P2)

**Goal**: Expose Resource Manager APIs via Session Manager without breaking existing Resource Manager clients.

**Independent Test**: Resource Manager endpoints are accessible via Session Manager with feature toggles; API contracts remain valid.

- [ ] T020 [US2] Expose /api/v1/resources/** endpoints in api-gateway/
- [ ] T021 [US2] Implement feature toggles for integrated resource operations (shared-security/)
- [ ] T022 [US2] Validate Resource Manager API contracts (contracts/api.md)
- [ ] T023 [US2] Implement regression tests for resource APIs (tests/)
- [ ] T024 [US2] Document compatibility validation (docs/)

---

## Phase 5: [US3] Secure Migration (Priority: P3)

**Goal**: Review and test all new integration points (Neo4j, Kafka, Hazelcast) for security and compatibility before production rollout.

**Independent Test**: Security review and compatibility tests pass for all integration points; rollback plan is documented and tested.

- [ ] T025 [US3] Compare Neo4j repository structures and validate SDN compatibility (shared-neo4j/)
- [ ] T026 [US3] Compare Kafka producers/consumers and validate protobuf serialization (shared-kafka/)
- [ ] T027 [US3] Review Hazelcast configuration for distributed coordination (shared-neo4j/, shared-kafka/)
- [ ] T028 [US3] Conduct security review for all integration points (docs/)
- [ ] T029 [US3] Document and test rollback plan (docs/)

---

## Phase 6: Polish & Cross-Cutting Concerns

- [ ] T030 [P] Unify CORS configuration (shared-security/)
- [ ] T031 [P] Centralize JWT authentication logic (shared-security/)
- [ ] T032 [P] Validate OpenAPI specifications for all APIs (contracts/api.md)
- [ ] T033 [P] Prepare canary deployment and production readiness checklist (docs/)

---

## Dependencies

- Phase 1 → Phase 2 → Phase 3 (US1) → Phase 4 (US2) → Phase 5 (US3) → Phase 6
- User stories are independently testable after foundational phases

## Parallel Execution Examples

- T003, T009–T014, T030–T033 can be executed in parallel
- User story phases (3–5) can be developed/tested independently after foundational tasks

## Implementation Strategy

- MVP: Complete Phase 3 ([US1] Session Integrity)
- Incremental delivery: Each user story phase is independently testable and deployable
