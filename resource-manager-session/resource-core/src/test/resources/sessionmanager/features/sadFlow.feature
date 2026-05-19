@SadFlow
Feature: Sessions cannot be acquired or released because of the bad payload

  @AcquireSessions @BadRequest @negativeTimeout
  Scenario: @negativeTimeout: the client tries to acquire sessions for negative timeout
    Given initiated producers and consumers
    When an acquire request with timeout -10 seconds, priority HIGH
    And on current request a list of SessionInfo:
      | domain | systemType | nodeName | numSessionsWanted |
      | bcpe   | ge104      | n1       | 3                 |
      | cc     | asr        | n2       | 3                 |
    When a ACQUIRE request with CORRELATION_ID idsf1 is sent over Kafka
    Then a ACQUIRE response is received after 1000 ms with CORRELATION_ID idsf1
    Then the acquire error message has isSessionAcquired = false, errorDto field is not empty and uuid is null
    Then the acquire error message has "must have a minimum value of 1" in errorDto field
    Then producers and consumers are closed

  @AcquireSessions @BadRequest @tooHighTimeout
  Scenario: @tooHighTimeout the client tries to acquire sessions for too high timeout
    Given initiated producers and consumers
    When an acquire request with timeout 10000 seconds, priority HIGH
    And on current request a list of SessionInfo:
      | domain | systemType | nodeName | numSessionsWanted |
      | bcpe   | ge104      | n1       | 3                 |
      | cc     | asr        | n2       | 3                 |
    When a ACQUIRE request with CORRELATION_ID idsf2 is sent over Kafka
    Then a ACQUIRE response is received after 1000 ms with CORRELATION_ID idsf2
    Then the acquire error message has isSessionAcquired = false, errorDto field is not empty and uuid is null
    Then the acquire error message has "must have a maximum value of 7200" in errorDto field
    Then producers and consumers are closed

  @AcquireSessions @BadRequest @invalidDomain
  Scenario: @invalidDomain the client tries to acquire sessions for invalid domain
    Given initiated producers and consumers
    When an acquire request with timeout 10 seconds, priority HIGH
    And on current request a list of SessionInfo:
      | domain  | systemType | nodeName | numSessionsWanted |
      | garbage | ge104      | n1       | 3                 |
    When a ACQUIRE request with CORRELATION_ID idsf3 is sent over Kafka
    Then a ACQUIRE response is received after 1000 ms with CORRELATION_ID idsf3
    Then the acquire error message has isSessionAcquired = false, errorDto field is not empty and uuid is null
    Then the acquire error message has "Invalid domain and/or system type" in errorDto field
    Then producers and consumers are closed

  @AcquireSessions @BadRequest @invalidSystemType
  Scenario: @invalidSystemType the client tries to acquire sessions for invalid system type
    Given initiated producers and consumers
    When an acquire request with timeout 10 seconds, priority HIGH
    And on current request a list of SessionInfo:
      | domain       | systemType | nodeName | numSessionsWanted |
      | bcpe garbage | garbage    | n1       | 3                 |
    When a ACQUIRE request with CORRELATION_ID idsf4 is sent over Kafka
    Then a ACQUIRE response is received after 1000 ms with CORRELATION_ID idsf4
    Then the acquire error message has isSessionAcquired = false, errorDto field is not empty and uuid is null
    Then the acquire error message has "Invalid domain and/or system type" in errorDto field
    Then producers and consumers are closed

  @AcquireSessions @BadRequest @invalidNumSessions
  Scenario: @invalidNumSessions the client tries to acquire more sessions than the allowed sessions for a given triplet
    Given initiated producers and consumers
    When an acquire request with timeout 10 seconds, priority HIGH
    And on current request a list of SessionInfo:
      | domain | systemType | nodeName | numSessionsWanted |
      | bcpe   | ge104      | n1       | 10                |
    When a ACQUIRE request with CORRELATION_ID idsf5 is sent over Kafka
    Then a ACQUIRE response is received after 1000 ms with CORRELATION_ID idsf5
    Then the acquire error message has isSessionAcquired = false, errorDto field is not empty and uuid is null
    Then the acquire error message has "Invalid numSessionsWanted" in errorDto field
    Then producers and consumers are closed
