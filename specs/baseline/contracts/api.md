# API Contracts: Baseline Integration

## Session APIs (Protected)
- /api/v1/sessions/**
  - All endpoints must remain backward compatible
  - No changes to authentication/authorization flows

## Resource APIs
- /api/v1/resources/**
  - Expose Resource Manager endpoints via Session Manager
  - Feature toggles must control exposure
  - API contracts must match existing Resource Manager definitions

## Shared APIs
- /api/v1/allocations/**
  - Manage resource allocations
  - Must enforce session/resource validation rules

## Integration Points
- Neo4j: All data access must use unified configuration
- Kafka: Producers/consumers must be compatible with both managers
- Hazelcast: Distributed coordination must not break session/resource logic

## Security
- All endpoints must pass security review before production rollout
- Rollback plan must be documented for all contract changes
