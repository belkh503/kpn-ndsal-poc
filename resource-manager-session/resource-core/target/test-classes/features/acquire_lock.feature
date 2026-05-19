Feature: Lock Resources Allowed in BCPE Domain

  *As a* calling service (can be KPN-NDSAL Service Manager)
  *I want to* submit locking request
  *So that* it will be allowed

  Background: Network Configuration Graph initialized
    Given initiated producers and consumers

  @locking
  Scenario Outline: Successful locking scenario - <Description>
    When A lock request is sent kafka topic on <LockResources>
    Then AcquireLockResponse has status RESOURCES_LOCKED
    When A lock request is sent kafka topic on <RequestedLock>
    Then AcquireLockResponse has status RESOURCES_LOCKED
    Then producers and consumers are closed
    Examples:
      | LockResources                                                               | RequestedLock                                                                                                                    | Description                                                                              |
      | NODE#nl-pbl-cpe-01#false                                                    | NODE#nl-pbl-cpe-01#false                                                                                                         | Allow - shared locks are compatible- Duplicate request                                   |
      | NODE#nl-pbl-cpe-01#true                                                     | NODE#nl-pbl-cpe-02#true                                                                                                          | Allow - Two different NODE with exclusive lock                                            |
      | NODE#nl-pbl-cpe-01#false                                                    | NODE#nl-pbl-cpe-02#false                                                                                                         | Allow - Two different NODE with shared lock                                               |
      | NODE#nl-pbl-cpe-01#false,PORT#nl-pbl-cpe-01:1/1/2#false                    | NODE#nl-pbl-cpe-02#false,PORT#nl-pbl-cpe-02:1/1/2#false                                                                         | Allow - Two different NODE and PORT                                                       |
      | NODE#nl-pbl-cpe-01#false,PORT#nl-pbl-cpe-01:1/1/2#false                    | NODE#nl-pbl-cpe-01#false,PORT#nl-pbl-cpe-01:1/1/2#false                                                                         | Allow - duplicate request with SHARED lock                                                |
      | NODE#nl-pbl-cpe-01#false,PORT#nl-pbl-cpe-01:1/1/2#false                    | NODE#nl-pbl-cpe-01#false,PORT#nl-pbl-cpe-01:1/1/3#false                                                                         | Allow - same node as shared but different port                                            |
      | NODE#nl-pbl-cpe-01#false,PORT#nl-pbl-cpe-01:1/1/2#false,EAS#EAS000001#false | NODE#nl-pbl-cpe-02#false,PORT#nl-pbl-cpe-01:1/1/2#false,EAS#EAS000001#false                                                    | Allow - different NODE                                                                    |
      | NODE#nl-pbl-cpe-01#false,PORT#nl-pbl-cpe-01:1/1/2#false,EAS#EAS000001#false | NODE#nl-pbl-cpe-01#false,PORT#nl-pbl-cpe-01:1/1/2#false,EAS#EAS000001#false                                                    | Allow - duplicate request with shared lock                                                |
      | NODE#nl-pbl-cpe-07#false                                                    | NODE#nl-pbl-cpe-01#false,PORT#nl-pbl-cpe-01:3/1/2#false,EAS#EAS000001#false%NODE#nl-pbl-cpe-01#true,PORT#nl-pbl-cpe-01:1/1/2#true,EAS#EAS000001#false | Allow - two graph in second request for the same NODE |
      | NODE#nl-pbl-cpe-07#false,PORT#nl-pbl-cpe-07:1/1/2#false,EAS#EAS000007#false | NODE#nl-pbl-cpe-01#false,PORT#nl-pbl-cpe-01:1/1/2#false,EAS#EAS000001#false%NODE#nl-pbl-cpe-01#true,PORT#nl-pbl-cpe-01:1/1/2#true,EAS#EAS000001#false%NODE#nl-pbl-cpe-04#true,PORT#nl-pbl-cpe-04:1/1/2#true,EAS#EAS000004#false | Allow - three graph in second request for two different NODE |

  @locking
  Scenario Outline: Successful queuing scenario - <Description>
    When A lock request is sent kafka topic on <LockResources>
    Then AcquireLockResponse has status RESOURCES_LOCKED
    When A lock request is sent kafka topic on <RequestedLock>
    And Resources are locked <LockResources>
    Then A lock request is queued
    Then producers and consumers are closed
    Examples:
      | LockResources           | RequestedLock                                                             | Description                                                |
      | NODE#nl-pbl-cpe-01#true | NODE#nl-pbl-cpe-01#false,PORT#nl-pbl-cpe-01:1/1/2#true,EAS#EAS000001#true | Shared Lock request on ENE is queued after an exclusive lock |

  @locking
  Scenario Outline: Successful queuing scenario - <Description>
    When A lock request is sent kafka topic on <LockResources>
    Then AcquireLockResponse has status RESOURCES_LOCKED
    When A lock request with priority LOW is sent kafka topic on NODE#nl-pbl-cpe-01#false,PORT#nl-pbl-cpe-01:1/1/2#true,EAS#EAS000001#false
    When A lock request with priority HIGH is sent kafka topic on NODE#nl-pbl-cpe-01#false,PORT#nl-pbl-cpe-01:1/1/2#true,EAS#EAS000002#false
    And Resources are locked <LockResources>
    Then Check request queue size is 2
    When A delete lock request is sent via kafka
    Then Check request queue size is 1
    And Check remaining request
    Then producers and consumers are closed
    Examples:
      | LockResources           | Description                                                              |
      | NODE#nl-pbl-cpe-01#true | two lock requests with high and low priority should be queued adequately |

  @locking
  Scenario Outline: Successful queuing scenario - <Description>
    When A lock request with timeout 1-sec is sent kafka topic on NODE#nl-pbl-cpe-01#false,PORT#nl-pbl-cpe-01:1/1/2#true,EAS#EAS000001#false
    And Resources are locked <LockResources>
    When Start lock cleaner after 1 seconds
    Then No locks in database
    Then producers and consumers are closed
    Examples:
      | LockResources           | Description                                                                          |
      | NODE#nl-pbl-cpe-01#true | when timeout exceeds and lock Cleanup is executed no locks should be in db            |

  @locking
  Scenario Outline: Successful queuing scenario - <Description>
    When A lock request is sent kafka topic on <LockResources>
    Then AcquireLockResponse has status RESOURCES_LOCKED
    When A lock request with timeout 1-sec is sent kafka topic on NODE#nl-pbl-cpe-01#false,PORT#nl-pbl-cpe-01:1/1/2#true,EAS#EAS000001#false
    And Resources are locked <LockResources>
    Then Check request queue size is 1
    Then Start queue cleanup after 1 seconds
    Then Check request queue size is 0
    Then producers and consumers are closed
    Examples:
      | LockResources           | Description                                                                                  |
      | NODE#nl-pbl-cpe-01#true | when lock is not released and queue cleanup is executed no requests should remain            |
