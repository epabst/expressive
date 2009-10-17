Feature: Addition Using the Calculator
  As a 1st grade student
  I want to check my answers using an RPN calculator
  So that I can recognize my mistakes

  Scenario: 1+1
    Given "1" is entered
    And "1" is entered
    When I push "+"
    Then the result should be "2"

  Scenario: 1+2
    Given "1" is entered
    And "2" is entered
    When I push "+"
    Then the result should be "3"

  Scenario: intentionally expect wrong answer
    Given "4" is entered
    And "3" is entered
    When I push "+"
    Then the result should be "1"