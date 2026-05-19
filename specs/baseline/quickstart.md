# Quickstart: Baseline Integration

## Prerequisites
- Java 17
- Maven 3.8+
- Docker (for Neo4j, Kafka, Hazelcast)

## Setup
1. Clone both repositories:
   - session-manager
   - resource-manager
2. Ensure both build independently: `mvn clean install`
3. Create new integration platform: `resource-manager-session`
4. Copy baseline plan and data model into `/specs/baseline/`

## Dependency Alignment
- Set Spring Boot version to 3.5.2 in all modules
- Set Java version to 17
- Align protobuf to 4.30.2
- Remove Netty/Jackson conflicts

## Migration Steps
1. Migrate javax → jakarta in Resource Manager
2. Upgrade Spring Security to v6
3. Refactor deprecated Spring APIs
4. Update Neo4j Spring Data usage
5. Validate REST controllers

## Multi-Module Structure
- platform-parent/
- session-core/
- resource-core/
- shared-security/
- shared-kafka/
- shared-neo4j/
- common-utils/
- api-gateway/

## Validation
- Run `mvn dependency:tree` and `mvn dependency:analyze` in all modules
- Run regression and compatibility tests
- Document and test rollback procedures

## References
- See `/specs/baseline/plan.md` for full implementation plan
- See `/specs/baseline/data-model.md` for entities and relationships
