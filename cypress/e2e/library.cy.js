describe('Library Book Management', () => {

    it('should allow one user to borrow a book, prevent another user from borrowing it, and allow the second user only after the book is returned', () => {
    const bookTitle = 'The Great Gatsby';

    //Alice logs in and borrows the book
    cy.login('alice', 'pass123');

    // Book initially available and borrowable
    cy.contains('tr', bookTitle).within(() => {
      cy.get('[data-testid="book-status"]').should('contain', 'AVAILABLE');
      cy.get('[data-testid="borrow-button"]').click();
    });

    // Success message of book being borrowed + borrowed count + due date all correct
    cy.get('[data-testid="global-message"]')
      .should('contain', `Borrowed "${bookTitle}" successfully`);
    cy.get('[data-testid="borrowed-count"]').should('contain', '1');
    cy.get('#my-borrowed-list')
      .should('contain', bookTitle)
      .and('contain', 'Due:'); // due date is shown

    // Book should now be CHECKED_OUT and show Return button
    // the borrow button should now be replaced with return button
    cy.contains('tr', bookTitle).within(() => {
      cy.get('[data-testid="book-status"]').should('contain', 'CHECKED_OUT');
      cy.get('[data-testid="return-button"]').should('exist');
      cy.get('[data-testid="borrow-button"]').should('not.exist');
    });

    cy.logout();

    //Bob logs in and cannot borrow the book
    cy.login('bob', 'pass456');

    // Bob has not borrowed anything yet
    cy.get('[data-testid="borrowed-count"]').should('contain', '0');

    // Bob should not be able to borrow while Alice has the book
    cy.contains('tr', bookTitle).within(() => {
      cy.get('[data-testid="book-status"]').should('not.contain', 'AVAILABLE');
      cy.get('[data-testid="borrow-button"]').should('not.exist');
    });

    cy.logout();

    //Step 3: Alice returns the book
    cy.login('alice', 'pass123');

    cy.contains('tr', bookTitle).within(() => {
      cy.get('[data-testid="return-button"]').click();
    });

    //book should be returned successfully and borrow count should return to 0
    cy.get('[data-testid="global-message"]').should(
      'contain',
      `Returned "${bookTitle}"`
    );
    cy.get('[data-testid="borrowed-count"]').should('contain', '0');

    // Returned book becomes AVAILABLE again
    cy.contains('tr', bookTitle).within(() => {
      cy.get('[data-testid="book-status"]').should('contain', 'AVAILABLE');
      cy.get('[data-testid="borrow-button"]').should('exist');
    });

    cy.logout();

    //Bob can now borrow the book after Alice returned it
    cy.login('bob', 'pass456');

    cy.contains('tr', bookTitle).within(() => {
      cy.get('[data-testid="borrow-button"]').click();
    });

    // Bob successfully borrows
    cy.get('[data-testid="global-message"]')
      .should('contain', `Borrowed "${bookTitle}" successfully`);
    cy.get('[data-testid="borrowed-count"]').should('contain', '1');
    cy.get('#my-borrowed-list')
      .should('contain', bookTitle)
      .and('contain', 'Due:');

    // Bob now has the book, so Return button should appear
    cy.contains('tr', bookTitle).within(() => {
      cy.get('[data-testid="book-status"]').should('contain', 'CHECKED_OUT');
      cy.get('[data-testid="return-button"]').should('exist');
      cy.get('[data-testid="borrow-button"]').should('not.exist');
    });

    cy.contains('tr', bookTitle).within(() => {
      cy.get('[data-testid="return-button"]').click();
    });

    cy.get('[data-testid="global-message"]').should(
      'contain',
      `Returned "${bookTitle}"`
    );
    cy.get('[data-testid="borrowed-count"]').should('contain', '0');

    // Returned book becomes AVAILABLE again
    cy.contains('tr', bookTitle).within(() => {
      cy.get('[data-testid="book-status"]').should('contain', 'AVAILABLE');
      cy.get('[data-testid="borrow-button"]').should('exist');
    });

    cy.logout();
  });

    it('should process a multi-user hold queue in FIFO order, notifying and allowing only the first user in the queue to borrow when the book becomes available', () => {
    const title = 'The Great Gatsby';

    //Step 1: User A (Alice) borrows the book
    cy.login('alice', 'pass123');

    cy.contains('tr', title).within(() => {
    // Make sure it's available, then click Borrow
    cy.get('[data-testid="book-status"]').should('contain', 'AVAILABLE');
    cy.get('[data-testid="borrow-button"]').click();
  });

  cy.get('[data-testid="global-message"]').should(
    'contain',
    `Borrowed "${title}" successfully`
  );
  cy.get('[data-testid="borrowed-count"]').should('contain', '1');
    cy.logout();

    //Step 2: User B (Bob) places a hold on the unavailable book
    cy.login('bob', 'pass456');

    cy.get('[data-testid="borrowed-count"]').should('contain', '0');

    cy.contains('tr', title).within(() => {
      // Book is checked out by Alice, so Bob should see a Hold button
      cy.get('[data-testid="hold-button"]').click();
    });

    cy.get('[data-testid="global-message"]').should(
      'contain',
      'Hold placed on'
    );

    cy.logout();

    //Step 3: User C (Charlie) places a hold after Bob (second in queue)
    cy.login('charlie', 'pass789');

    cy.contains('tr', title).within(() => {
      cy.get('[data-testid="hold-button"]').click();
    });

    cy.get('[data-testid="global-message"]').should(
      'contain',
      'Hold placed on'
    );

    cy.logout();

    // At this point, hold queue (FIFO) is: [Bob, Charlie]

    //Step 4: Alice returns the book, making it ON_HOLD for the first in queue
    cy.login('alice', 'pass123');

    cy.contains('tr', title).within(() => {
      cy.get('[data-testid="return-button"]').click();
    });

    cy.get('[data-testid="global-message"]').should(
      'contain',
      `Returned "${title}"`
    );
    cy.logout();

    //Step 5: Verify ONLY Bob (first in queue) can borrow; Charlie cannot yet

    //Charlie logs in first: he should NOT be allowed to borrow yet
    cy.login('charlie', 'pass789');

    cy.contains('tr', title).within(() => {
      cy.get('[data-testid="book-status"]').should('contain', 'ON_HOLD');
      cy.get('[data-testid="borrow-button"]').should('not.exist');
      // He's in the queue, but not first, so no Hold or Borrow
      cy.get('[data-testid="hold-button"]').should('not.exist');
    });

    // And he has no notification yet
    cy.get('#notifications-list').should('not.contain', title);

    cy.logout();

    //Bob logs in: he SHOULD be notified and allowed to borrow
    cy.login('bob', 'pass456');

    // Notification for Bob as first in the queue
    cy.get('#notifications-list').should(
      'contain',
      `Your hold on "${title}" is now available.`
    );

    // Book row shows ON_HOLD and a Borrow button for Bob
    cy.contains('tr', title).within(() => {
      cy.get('[data-testid="book-status"]').should('contain', 'ON_HOLD');
      cy.get('[data-testid="borrow-button"]').click();
    });

    cy.get('[data-testid="global-message"]').should(
      'contain',
      `Borrowed "${title}" successfully`
    );
    cy.get('[data-testid="borrowed-count"]').should('contain', '1');
    cy.get('#my-borrowed-list').should('contain', title);

    cy.logout();

    //Step 6: Queue advances – when Bob returns, Charlie becomes next

    // Bob returns the book
    cy.login('bob', 'pass456');

    cy.contains('tr', title).within(() => {
      cy.get('[data-testid="return-button"]').click();
    });

    cy.get('[data-testid="global-message"]').should(
      'contain',
      `Returned "${title}"`
    );
    cy.get('[data-testid="borrowed-count"]').should('contain', '0');
    cy.logout();

    // Now Charlie should be notified and able to borrow
    cy.login('charlie', 'pass789');

    cy.get('#notifications-list').should(
      'contain',
      `Your hold on "${title}" is now available.`
    );

    cy.contains('tr', title).within(() => {
      cy.get('[data-testid="book-status"]').should('contain', 'ON_HOLD');
      cy.get('[data-testid="borrow-button"]').click();
    });

    cy.get('[data-testid="global-message"]').should(
      'contain',
      `Borrowed "${title}" successfully`
    );
    cy.get('[data-testid="borrowed-count"]').should('contain', '1');
    cy.get('#my-borrowed-list').should('contain', title);

    // Clean up: Charlie returns book so future tests start clean
    cy.contains('tr', title).within(() => {
      cy.get('[data-testid="return-button"]').click();
    });
    cy.get('[data-testid="borrowed-count"]').should('contain', '0');

    cy.logout();
  });

  it('should enforce the 3-book limit while still allowing holds, and notify the user when their held book becomes available after they regain capacity', () => {
  const bookA = 'The Great Gatsby';
  const bookB = 'To Kill a Mockingbird';
  const bookC = '1984';
  const holdTarget = 'Harry Potter';
  const extraBook = 'Pride and Prejudice'; 

  // Bob borrows 3 books and hits the limit
  cy.login('bob', 'pass456');

  [bookA, bookB, bookC].forEach(title => {
    cy.contains('tr', title).within(() => {
      cy.get('[data-testid="borrow-button"]').click();
    });
  });

  // Bob now has 3 books
  cy.get('[data-testid="borrowed-count"]').should('contain', '3');

  // Try to borrow a 4th book.
  cy.contains('tr', extraBook).within(() => {
    cy.get('[data-testid="borrow-button"]').click();
  });

  // Bob can't have more than 3 books
  cy.get('[data-testid="borrowed-count"]').should('contain', '3');

  // a message appears saying if the user wants to place a hold or not, bob places a hold
  cy.get('[data-testid="global-message"]').invoke('text').then(text => {
    expect(
      text.includes('Borrowing limit') || text.includes('Hold placed')
    ).to.be.true;
  });

  cy.logout();

  //Alice borrows the hold-target book
  cy.login('alice', 'pass123');

  cy.contains('tr', holdTarget).within(() => {
    cy.get('[data-testid="borrow-button"]').click();
  });

  cy.get('[data-testid="global-message"]')
    .should('contain', `Borrowed "${holdTarget}"`);

  cy.logout();

  //Bob (still at limit) places a hold on that book
  cy.login('bob', 'pass456');

  cy.get('[data-testid="borrowed-count"]').should('contain', '3');

  cy.contains('tr', holdTarget).within(() => {
    cy.get('button').then($btns => {
      const hasHoldButton = $btns.toArray().some(b => b.innerText.includes('Place Hold'));
      if (hasHoldButton) {
        cy.get('[data-testid="hold-button"]').click();
      }
    });
  });

  cy.get('[data-testid="global-message"]').invoke('text').then(text => {
    expect(text.includes('Hold placed') || text.includes('Borrowing limit')).to.be.true;
  });

  // Holds do NOT increase borrowed count
  cy.get('[data-testid="borrowed-count"]').should('contain', '3');

  cy.logout();

  // Bob returns one book → drops below limit
  cy.login('bob', 'pass456');

  cy.contains('tr', bookC).within(() => {
    cy.get('[data-testid="return-button"]').click();
  });

  cy.get('[data-testid="global-message"]')
    .should('contain', `Returned "${bookC}"`);

  // Now under the 3-book limit
  cy.get('[data-testid="borrowed-count"]').should('contain', '2');

  cy.logout();

  // Alice returns the held book → triggers Bob's notification
  cy.login('alice', 'pass123');

  cy.contains('tr', holdTarget).within(() => {
    cy.get('[data-testid="return-button"]').click();
  });

  cy.get('[data-testid="global-message"]')
    .should('contain', `Returned "${holdTarget}"`);

  cy.logout();

  // Bob logs in: notified + can borrow held book
  cy.login('bob', 'pass456');

  // Notification that the held book is now available for Bob
  cy.get('#notifications-list')
    .should('contain', holdTarget);

  // Book should now show Bob-specific borrow permission
  cy.contains('tr', holdTarget).within(() => {
    cy.get('[data-testid="book-status"]').should('contain', 'ON_HOLD');
    cy.get('[data-testid="borrow-button"]').click();
  });

  cy.get('[data-testid="global-message"]')
    .should('contain', `Borrowed "${holdTarget}" successfully`);

  // Borrowed count back to 3, showing he can borrow again once under limit
  cy.get('[data-testid="borrowed-count"]').should('contain', '3');

  cy.logout();
});

});
