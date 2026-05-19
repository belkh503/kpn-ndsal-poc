Feature: Lock Resources REJECTED in BCPE Domain

  *As a* calling service (can be KPN-NDSAL Service Manager)
  *I want to* submit locking request
  *So that* it will be denied

  Background: Network Configuration Graph initialized

  @locking
  Scenario Outline: Successful get lock status scenario
    Given initiated producers and consumers
    When A get lock status request is sent via kafka
    Then GetLockStatusResponse has status NOT_FOUND
    When A lock request is sent kafka topic on <LockResource>
    Then AcquireLockResponse has status RESOURCES_LOCKED
    When A get lock status request is sent via kafka
    Then GetLockStatusResponse has status ACTIVE
    When A delete lock request is sent via kafka
    Then DeleteLockResponse has status LOCK_REMOVED
    When A get lock status request is sent via kafka
    Then GetLockStatusResponse has status NOT_FOUND
    Examples:
      | LockResource              |
      | NODE#nl-pbl-cpe-01#false  |
