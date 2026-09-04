Feature: Invalid fee requests are rejected before any workflow starts

  Scenario: An unpriced transaction type is rejected with the supported list
    Given a settled transaction of 1000 USD of type "Crypto Swap"
    When the fee is requested
    Then the response is 400
    And the error code is VALIDATION_FAILED
    And the message mentions "MOBILE TOP UP, BILL PAYMENT"

  Scenario Outline: An invalid amount is rejected
    Given a settled transaction of <amount> USD of type "Mobile Top Up"
    When the fee is requested
    Then the response is 400
    And the error code is VALIDATION_FAILED

    Examples:
      | amount     |
      | 0          |
      | -5         |
      | 1000.12345 |

  Scenario: A non-FIAT asset type is rejected
    Given a settled transaction of 1000 USD of type "Mobile Top Up"
    And the asset type is "CRYPTO"
    When the fee is requested
    Then the response is 400
    And the message mentions "must be one of: FIAT"

  Scenario: A blank transaction id is rejected
    Given a settled transaction of 1000 USD of type "Mobile Top Up"
    And the transaction id is empty
    When the fee is requested
    Then the response is 400