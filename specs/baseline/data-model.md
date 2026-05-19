# Data Model: Baseline Integration

## Entities

### Session
- sessionId: String
- userId: String
- status: Enum (ACTIVE, INACTIVE, EXPIRED)
- createdAt: Timestamp
- expiresAt: Timestamp
- attributes: Map<String, Object>

### Resource
- resourceId: String
- type: String
- status: Enum (ALLOCATED, FREE, RESERVED)
- ownerSessionId: String
- createdAt: Timestamp
- updatedAt: Timestamp

### Allocation
- allocationId: String
- resourceId: String
- sessionId: String
- allocatedAt: Timestamp
- releasedAt: Timestamp (nullable)

## Relationships
- Session 1---* Allocation *---1 Resource
- Resource 1---* Allocation *---1 Session

## Validation Rules
- Session must be ACTIVE to allocate a resource
- Resource must be FREE to be allocated
- Allocation must reference valid sessionId and resourceId

## State Transitions
- Session: ACTIVE → INACTIVE/EXPIRED
- Resource: FREE → ALLOCATED → FREE
- Allocation: created on allocation, released on resource release
