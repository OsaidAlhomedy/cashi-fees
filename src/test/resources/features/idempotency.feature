Feature: A transaction is only ever charged once

  Scenario: Submitting the same transaction twice charges it once
    Given a settled transaction of 1000 USD of type "Mobile Top Up"
    When the fee is requested
    And the same transaction is submitted again
    Then both responses are identical
    And exactly one fee record exists