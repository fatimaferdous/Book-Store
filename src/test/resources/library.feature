Feature: Library System

  Scenario:A1 basic borrow-return cycle
    Given 20 books along with 3 user accounts in a library system
    When "alice" logs in with password "pass123"
    And "alice" borrows "The Hobbit"
    Then "The Hobbit" is unavailable

    When "alice" logs out
    And "bob" logs in with password "pass456"
    And "bob" attempts to borrow "The Hobbit"
    Then the borrow operation fails due to book unavailability

    When "bob" places a hold on "The Hobbit"
    And "bob" logs out
    And "alice" logs in with password "pass123"
    And "alice" returns "The Hobbit"
    And "alice" logs out

    And "bob" logs in with password "pass456"
    And "bob" borrows "The Hobbit"
    And "bob" returns "The Hobbit"
    Then "The Hobbit" becomes available again

  Scenario: Multiple holds queue processing
    Given 20 books along with 3 user accounts in a library system
    When "alice" logs in with password "pass123"
    And "alice" borrows "Pride and Prejudice"
    Then "Pride and Prejudice" is unavailable
    When "alice" logs out

    And "bob" logs in with password "pass456"
    And "bob" places a hold on "Pride and Prejudice"
    And "bob" logs out

    And "charlie" logs in with password "pass789"
    And "charlie" places a hold on "Pride and Prejudice"
    And "charlie" logs out
    Then "Pride and Prejudice" has "bob" reserved first as well as "charlie" second

    When "alice" logs in with password "pass123"
    And "alice" returns "Pride and Prejudice"
    And "alice" logs out

    And "charlie" logs in with password "pass789"
    Then "charlie" has no notification for "Pride and Prejudice"
    When "charlie" logs out

    And "bob" logs in with password "pass456"
    Then "bob" has a notification for "Pride and Prejudice"
    When "bob" borrows the reserved copy of "Pride and Prejudice"
    Then "bob" has borrowed "Pride and Prejudice"
    When "bob" returns "Pride and Prejudice"
    And "bob" logs out

    And "charlie" logs in with password "pass789"
    Then "charlie" has a notification for "Pride and Prejudice"
    And "charlie" is now next in line for "Pride and Prejudice"
    When "charlie" borrows the reserved copy of "Pride and Prejudice"
    Then "charlie" has borrowed "Pride and Prejudice"
    When "charlie" returns "Pride and Prejudice"
    And "charlie" logs out
    Then "Pride and Prejudice" becomes available again

  Scenario: Borrowing limits and hold interactions
    Given 20 books along with 3 user accounts in a library system
    And "alice" logs in with password "pass123"
    And "alice" currently has 3 books borrowed
    When "alice" tries to borrow "Harry Potter"
    Then the borrow operation fails for "alice" due to reaching the borrowing limit
    When "alice" places a hold on "Harry Potter"
    Then the hold is successfully recorded for "alice"
    When "alice" logs out

    And "bob" logs in with password "pass456"
    And "bob" borrows "Harry Potter"
    Then "Harry Potter" is unavailable
    When "bob" returns "Harry Potter"
    And "bob" logs out

    And "alice" logs in with password "pass123"
    Then "alice" has a notification for "Harry Potter"
    When "alice" returns one of the books
    Then "alice" has borrowing capacity available
    When "alice" borrows "Harry Potter"
    And "alice" returns "Harry Potter"
    And "alice" logs out
    Then "Harry Potter" becomes available again

  Scenario: No books borrowed
    Given 20 books along with 3 user accounts in a library system
    And "alice" logs in with password "pass123"
    When "alice" attempts to return a book
    Then "alice" has no books currently borrowed
    And all books show as available
    When "alice" logs out
    Then the system should have no active user session
