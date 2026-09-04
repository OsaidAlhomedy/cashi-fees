Feature: Transaction fees are priced by transaction type

  Scenario Outline: A settled transaction is charged its percentage fee
    Given a settled transaction of <amount> USD of type "<type>"
    When the fee is requested
    Then the response is 200
    And the quoted fee is <fee>
    And the fee record is settled with a charge id

    Examples:
      | type          | amount | fee  |
      | Mobile Top Up | 1000   | 1.50 |
      | Bill Payment  | 1000   | 2.00 |