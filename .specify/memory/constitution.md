
<!--
Sync Impact Report
------------------
Version change: 1.x → 2.1
Modified principles: All replaced with project-specific principles
Added sections: Initial Analysis Requirement, Core Engineering Principles, Governance
Removed sections: Template placeholders
Templates requiring updates: ✅ plan-template.md, ✅ spec-template.md, ✅ tasks-template.md
Follow-up TODOs: RATIFICATION_DATE
-->

# Session Manager + Resource Manager Integration Platform Constitution

## Core Principles

### I. Initial Analysis Requirement
Implementation MUST NOT begin until:
- Session Manager and Resource Manager repositories are cloned
- Complete architecture analysis is performed
- Business logic, module structure, dependency graph, Spring Boot compatibility, security, database, Neo4j, Kafka, Hazelcast, API contracts, and CI/CD pipelines are understood
- Dependency, security, and compatibility analyses are complete
- Migration strategy is prepared
No direct merge is allowed without complete system understanding.

### II. Session Manager Is the Base Platform
Session Manager is authoritative for:
- authentication
- authorization
- session lifecycle
- security governance
- observability
- API governance
Resource Manager MUST adapt to Session Manager standards.

### III. Incremental Migration Strategy
Migration MUST be:
- phased
- reversible
- feature-toggle driven
- independently deployable
No big-bang migration is allowed.

### IV. Backward Compatibility Mandatory
All existing APIs MUST remain functional.
Protected APIs:
	- /api/v1/sessions/**

## Additional Constraints

- All integration points (Neo4j, Kafka, Hazelcast) MUST be reviewed for compatibility and security.
- Security analysis MUST precede any implementation.
- Migration plans MUST include rollback and feature toggle strategies.

## Development Workflow

- No implementation starts before analysis gates are passed.
- All merges require documented analysis and migration plan.
- Code reviews MUST verify compliance with all principles.
- CI/CD pipelines MUST enforce test and security gates.

## Governance

- This constitution supersedes all other practices for this integration platform.
- Amendments require documentation, approval, and a migration plan.
- All PRs/reviews MUST verify compliance with these principles.
- Complexity MUST be justified in writing.
- Use this constitution as the authoritative runtime development guidance.

**Version**: 2.1 | **Ratified**: TODO(RATIFICATION_DATE) | **Last Amended**: 2026-05-11
