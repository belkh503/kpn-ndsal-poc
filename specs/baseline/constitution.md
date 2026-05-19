<!--
Sync Impact Report
------------------
Version change: 2.1 → 2.2
Modified principles:
  - Principle I: "Initial Analysis Requirement" — expanded with javax→jakarta hard gate
  - Principle II: "Session Manager Is the Base Platform" — renamed "Authoritative Base Platform"; added ADR conflict-resolution rule
  - Principle IV: "Backward Compatibility Mandatory" — expanded with API versioning protocol and deprecation timeline
Added sections:
  - V. Technology Standards (canonical stack table + hard gates)
  - VI. Observability Standards (OpenTelemetry, W3C propagation, SLI/SLO)
  - VII. Testing Standards (unit, integration, contract, coverage, security)
  - Security and API Governance (standalone section)
Removed sections: None
Templates requiring updates:
  ✅ .specify/memory/constitution.md (authoritative copy updated)
  ✅ specs/baseline/plan.md — Constitution Check references updated to v2.2
  ✅ specs/baseline/spec.md — Constitution Check references updated to v2.2
  ⚠ .specify/templates/tasks-template.md — verify task categories reflect Observability and Testing principles
  ⚠ .specify/templates/plan-template.md — verify Constitution Check block covers new principles V–VII
Follow-up TODOs:
  - TODO(RATIFICATION_DATE): Original adoption date unknown; set when team ratifies
-->

# resource-manager-session Constitution

## Core Principles

### I. Initial Analysis Requirement

Implementation MUST NOT begin until:

- Session Manager and Resource Manager repositories are cloned and analyzed
- Complete architecture analysis is performed covering: business logic, module structure,
  dependency graph, Spring Boot compatibility, security, databases, Neo4j schema, Kafka topic
  contracts, Hazelcast topology, API contracts, and CI/CD pipelines
- Dependency, security, and compatibility analyses are complete
- Migration strategy is documented and approved

No direct merge is permitted without complete system understanding.

The `javax` → `jakarta` namespace migration in Resource Manager MUST be treated as a
**hard gate**: no Resource Manager code may be published to a shared module until all
`javax.*` imports are migrated to `jakarta.*`.

### II. Session Manager Is the Authoritative Base Platform

Session Manager is authoritative for:

- authentication and authorization
- session lifecycle management
- security governance and token handling
- observability (OpenTelemetry trace and metrics instrumentation)
- API governance and versioning

Resource Manager MUST adapt to Session Manager standards on all shared integration points.
Conflicts between the two systems MUST be resolved in favor of Session Manager's patterns
unless a documented architectural decision record (ADR) explicitly states otherwise and has
been reviewed by an architect.

### III. Incremental Migration Strategy

Migration MUST be:

- **Phased**: modules migrate independently through defined phases
  (analysis → design → migration → validation)
- **Reversible**: every integration point MUST have a documented rollback procedure before
  it is activated in any environment
- **Feature-toggle driven**: all new integration points MUST be activated behind feature
  flags; toggles MUST be documented in the central toggle registry
- **Independently deployable**: each module MUST remain buildable and deployable in isolation
  at all times

No big-bang migration is permitted. Monolithic cutover is a violation of this principle.

### IV. Backward Compatibility Mandatory

All existing APIs MUST remain functional across all migration phases.

**Protected API paths** (MUST NOT break):

- `/api/v1/sessions/**`

Any breaking change to a protected path MUST follow the API versioning protocol:

1. New path version introduced first (e.g., `/api/v2/sessions/**`)
2. Old path maintained with a documented deprecation timeline (minimum 90-day notice)
3. Migration guide published before deprecation takes effect

Clients MUST NOT be required to change integration code without at least 90 days of advance
notice and a published migration guide.

### V. Technology Standards

The canonical technology stack for the integrated platform is:

| Concern | Standard | Constraint |
|---|---|---|
| Language | Java 17 (LTS) | No Java 8/11 source; Resource Manager MUST upgrade |
| Framework | Spring Boot 3.5.2 | Spring Boot 2 modules MUST upgrade before integration merge |
| Serialization | Protobuf 4.x | Wire-format compatible with 3.x; API evolution field rules MUST be followed |
| Graph DB | Neo4j 5.26.6 | Single schema governance; migrations via Liquibase or Flyway |
| Distributed Cache | Hazelcast 5.5 | Cluster topology owned by Session Manager; Resource Manager joins as client member |
| Messaging | Apache Kafka | Topic contracts versioned; schema registry enforced for Avro/Protobuf schemas |
| Build | Maven multi-module | BOM declared in `platform-parent/`; child modules MUST NOT declare direct dependency versions |
| Container | Docker / Kubernetes | One Dockerfile per deployable module; no host-bound or environment-hardcoded configuration |

Deviation from this stack MUST be justified in an ADR and approved before implementation
begins.

### VI. Observability Standards

All application modules MUST instrument with OpenTelemetry:

- Distributed trace propagation MUST use W3C `traceparent` / `tracestate` headers; no
  proprietary trace header formats
- Metrics MUST follow OpenTelemetry semantic conventions (HTTP server/client, DB, messaging)
- Logs MUST include `trace_id` and `span_id` fields for cross-signal correlation
- Health endpoints (`/actuator/health`, `/actuator/info`) MUST be exposed on every deployable
  service
- SLI/SLO definitions MUST be documented per module before production readiness sign-off

Proprietary APM agents MUST NOT replace OTel instrumentation. They MAY augment it if the OTel
layer is preserved and non-removal is enforced in CI.

### VII. Testing Standards

- **Unit tests**: MUST cover all domain logic; the class under test MUST NOT be mocked within
  its own test suite
- **Integration tests**: MUST cover all Neo4j, Kafka, and Hazelcast interaction paths; MUST
  run against real instances via Testcontainers in local and CI environments
- **Contract tests**: MUST be maintained for all inter-module REST and Kafka schema contracts
- **Regression gate**: existing test suites from both legacy systems MUST pass before any
  migration phase is closed
- **Coverage floor**: line coverage MUST NOT decrease from baseline measurements; new code
  MUST achieve ≥80% line coverage
- **Security tests**: OWASP dependency-check MUST run in CI; findings of MEDIUM severity or
  above MUST block merge

Test-first (TDD) is the required workflow for new business logic: tests written and reviewed
before implementation begins. Red-Green-Refactor cycle is enforced.

## Security and API Governance

- Security analysis MUST precede any implementation involving authentication, authorization,
  token handling, or external-facing APIs.
- All integration points (Neo4j, Kafka, Hazelcast) MUST be reviewed for mutual authentication,
  encryption in transit, and least-privilege access before production enablement.
- Credentials, secrets, and API keys MUST NOT appear in source code or version control; use
  environment-injected configuration or an approved secrets manager.
- API error responses MUST NOT expose internal stack traces, class names, or system topology
  details.
- OWASP Top 10 MUST be assessed for every new API endpoint introduced.
- Authorization MUST be enforced server-side; client-supplied role or permission claims MUST
  NOT be trusted without server-side verification.

## Development Workflow

- No implementation phase begins before analysis and design gates are documented and reviewed
  by at least one other engineer.
- All merges require a passing CI pipeline (build, unit test, integration test, contract test,
  OWASP scan, coverage check).
- Code reviews MUST explicitly verify compliance with applicable constitution principles;
  reviewers MUST call out violations as blocking comments.
- The `javax` → `jakarta` migration status MUST be tracked as a P0 task and resolved in Phase 1
  before any shared module is published.
- Feature toggles MUST be registered in the central toggle registry before use in any non-local
  environment.
- CI/CD pipelines MUST enforce all quality gates; pipeline bypasses require architect approval
  and MUST be time-boxed.

## Governance

This constitution is the authoritative governance document for the `resource-manager-session`
integration platform. It supersedes all legacy practices from either source system.

**Amendment procedure**:

1. Propose change via PR with updated constitution and rationale.
2. At least one architect-level review required.
3. Version bumped per the semantic versioning policy below.
4. All dependent templates and docs updated before the PR is merged.
5. Change announced to the team with a summary of what changed and why.

**Versioning policy**:

- **MAJOR**: backward-incompatible governance changes, principle removals, or redefinitions that
  change non-negotiable rules
- **MINOR**: new principles or sections added, material guidance expanded
- **PATCH**: clarifications, wording corrections, non-semantic refinements

**Compliance review**: at the start of every development phase and before each production
deployment, the lead engineer MUST attest that all deliverables comply with this constitution.

**Version**: 2.2 | **Ratified**: TODO(RATIFICATION_DATE) | **Last Amended**: 2026-05-15
