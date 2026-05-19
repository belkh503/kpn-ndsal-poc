@HappyFlow
Feature: Sessions can be acquired and released successfully

  Background:
                        Given initiated producers and consumers
                        Given initiated bolt connection

  Scenario: sending acquire request happy flow
    When an acquire request with timeout 10 seconds, priority HIGH
    And on current request a list of SessionInfo:
      | domain | systemType | nodeName | numSessionsWanted |
      | bcpe   | ge104      | test1    | 3                 |
    When a ACQUIRE request with CORRELATION_ID id1 is sent over Kafka
    Then a ACQUIRE response is received after 5000 ms with CORRELATION_ID id1
    Then sessions are acquired with isSessionAcquired is true and a non null value for uuid
    Then producers and consumers are closed

  Scenario: sending two different acquire requests, both are acquired
    When an acquire request with timeout 10 seconds, priority HIGH
    And on current request a list of SessionInfo:
      | domain | systemType | nodeName | numSessionsWanted |
      | bcpe   | ge104      | test2    | 3                 |
    When a ACQUIRE request with CORRELATION_ID id2 is sent over Kafka
    Then a ACQUIRE response is received after 3000 ms with CORRELATION_ID id2
    Then sessions are acquired with isSessionAcquired is true and a non null value for uuid
    When an acquire request with timeout 10 seconds, priority HIGH
    And on current request a list of SessionInfo:
      | domain | systemType | nodeName | numSessionsWanted |
      | bcpe   | ge104      | test3    | 3                 |
    When a ACQUIRE request with CORRELATION_ID id3 is sent over Kafka
    Then a ACQUIRE response is received after 3000 ms with CORRELATION_ID id3
    Then sessions are acquired with isSessionAcquired is true and a non null value for uuid
    Then producers and consumers are closed

  Scenario: sending acquire and release request happy flow
    When an acquire request with timeout 10 seconds, priority HIGH
    And on current request a list of SessionInfo:
      | domain | systemType | nodeName | numSessionsWanted |
      | bcpe   | ge104      | test4    | 3                 |
    When a ACQUIRE request with CORRELATION_ID id4 is sent over Kafka
    Then a ACQUIRE response is received after 3000 ms with CORRELATION_ID id4
    Then sessions are acquired with isSessionAcquired is true and a non null value for uuid
    When a release request with order 1
    When a RELEASE request with CORRELATION_ID id5 is sent over Kafka
    Then a RELEASE response is received after 3000 ms with CORRELATION_ID id5
    Then sessions are released is true
    Then producers and consumers are closed

  Scenario: sending two identical acquire requests, second will be acquired after releasing first
    When an acquire request with timeout 10 seconds, priority HIGH
    And on current request a list of SessionInfo:
      | domain | systemType | nodeName | numSessionsWanted |
      | bcpe   | ge104      | test6    | 3                 |
    When a ACQUIRE request with CORRELATION_ID id8 is sent over Kafka
    When an acquire request with timeout 10 seconds, priority HIGH
    And on current request a list of SessionInfo:
      | domain | systemType | nodeName | numSessionsWanted |
      | bcpe   | ge104      | test6    | 3                 |
    When a ACQUIRE request with CORRELATION_ID id9 is sent over Kafka
    Then a ACQUIRE response is received after 2000 ms with CORRELATION_ID id8
    Then sessions are acquired with isSessionAcquired is true and a non null value for uuid
    Then an acquire response is not received over Kafka after 1000 ms with CORRELATION_ID id9
    When a release request with order 1
    When a RELEASE request with CORRELATION_ID id10 is sent over Kafka
    Then a RELEASE response is received after 2500 ms with CORRELATION_ID id10
    Then sessions are released is true
    Then a ACQUIRE response is received after 1000 ms with CORRELATION_ID id9
    Then sessions are acquired with isSessionAcquired is true and a non null value for uuid
    Then producers and consumers are closed

  Scenario: sending two identical acquire requests, second will be acquired after TimeoutCleaner runs
    When an acquire request with timeout 10 seconds, priority HIGH
    And on current request a list of SessionInfo:
      | domain | systemType | nodeName | numSessionsWanted |
      | bcpe   | ge104      | test7    | 3                 |
    When a ACQUIRE request with CORRELATION_ID id11 is sent over Kafka
    When an acquire request with timeout 10 seconds, priority HIGH
    And on current request a list of SessionInfo:
      | domain | systemType | nodeName | numSessionsWanted |
      | bcpe   | ge104      | test7    | 3                 |
    When a ACQUIRE request with CORRELATION_ID id12 is sent over Kafka
    Then a ACQUIRE response is received after 2000 ms with CORRELATION_ID id11
    Then sessions are acquired with isSessionAcquired is true and a non null value for uuid
    Then an acquire response is not received over Kafka after 1000 ms with CORRELATION_ID id12
    Then a ACQUIRE response is received after 30000 ms with CORRELATION_ID id12
    Then sessions are acquired with isSessionAcquired is true and a non null value for uuid
    Then producers and consumers are closed

  Scenario: sending two identical acquire requests with different priority, the higher priority one will be acquired first
    When an acquire request with timeout 10 seconds, priority HIGH
    And on current request a list of SessionInfo:
      | domain | systemType | nodeName | numSessionsWanted |
      | bcpe   | ge104      | test8    | 3                 |
    When a ACQUIRE request with CORRELATION_ID id13 is sent over Kafka
    Then a ACQUIRE response is received after 2000 ms with CORRELATION_ID id13
    Then sessions are acquired with isSessionAcquired is true and a non null value for uuid
    When an acquire request with timeout 10 seconds, priority LOW
    And on current request a list of SessionInfo:
      | domain | systemType | nodeName | numSessionsWanted |
      | bcpe   | ge104      | test8    | 3                 |
    When a ACQUIRE request with CORRELATION_ID id14 is sent over Kafka
    When an acquire request with timeout 10 seconds, priority HIGH
    And on current request a list of SessionInfo:
      | domain | systemType | nodeName | numSessionsWanted |
      | bcpe   | ge104      | test8    | 3                 |
    When a ACQUIRE request with CORRELATION_ID id15 is sent over Kafka
    Then an acquire response is not received over Kafka after 2000 ms with CORRELATION_ID id14
    Then an acquire response is not received over Kafka after 2000 ms with CORRELATION_ID id15
    When a release request with order 1
    When a RELEASE request with CORRELATION_ID id16 is sent over Kafka
    Then a RELEASE response is received after 2500 ms with CORRELATION_ID id16
    Then sessions are released is true
    Then a ACQUIRE response is received after 1000 ms with CORRELATION_ID id15
    Then sessions are acquired with isSessionAcquired is true and a non null value for uuid
    Then an acquire response is not received over Kafka after 2000 ms with CORRELATION_ID id14
    When a release request with order 2
    When a RELEASE request with CORRELATION_ID id17 is sent over Kafka
    Then a RELEASE response is received after 2500 ms with CORRELATION_ID id17
    Then sessions are released is true
    Then a ACQUIRE response is received after 1000 ms with CORRELATION_ID id14
    Then sessions are acquired with isSessionAcquired is true and a non null value for uuid
    Then producers and consumers are closed

  Scenario: sending acquire and two release requests, second should not release anything
    When an acquire request with timeout 10 seconds, priority HIGH
    And on current request a list of SessionInfo:
      | domain | systemType | nodeName | numSessionsWanted |
      | bcpe   | ge104      | test9    | 3                 |
    When a ACQUIRE request with CORRELATION_ID id18 is sent over Kafka
    Then a ACQUIRE response is received after 5000 ms with CORRELATION_ID id18
    Then sessions are acquired with isSessionAcquired is true and a non null value for uuid
    When a release request with order 1
    When a RELEASE request with CORRELATION_ID id19 is sent over Kafka
    Then a RELEASE response is received after 5000 ms with CORRELATION_ID id19
    Then sessions are released is true
    When a release request with order 1
    When a RELEASE request with CORRELATION_ID id20 is sent over Kafka
    Then a RELEASE response is received after 5000 ms with CORRELATION_ID id20
    Then sessions are released is false
    Then producers and consumers are closed
