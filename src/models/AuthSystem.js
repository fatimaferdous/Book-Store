const BookStatus = require('./BookStatus');

class AuthSystem {
  constructor(users) {
    this.users = users;
    this.currentUser = null;
  }

  authenticate(username, password) {
    const borrower = this.users.findByUsername(username);
    if (borrower && borrower.passwordMatches(password)) {
      this.currentUser = borrower;
      return true;
    }
    return false;
  }

  login(username, password) {
    const ok = this.authenticate(username, password);
    if (!ok) {
      return { success: false, message: 'Invalid username or password' };
    }
    return {
      success: true,
      message: `Login successful. Welcome, ${username}!`,
      user: this.currentUser,
    };
  }

  getCurrentUser() {
    return this.currentUser;
  }

  logoutUser() {
    this.currentUser = null;
  }

  logout() {
    this.logoutUser();
    return { success: true, message: 'You have successfully logged out.' };
  }


  getNotificationBooks(borrower) {
    const notifications = [];

    if (!borrower) return notifications;

    const heldBooks = borrower.getHolds();
    if (!heldBooks || heldBooks.length === 0) return notifications;

    for (const book of heldBooks) {
      const queue = book.getHoldQueue();
      if (
        book.getStatus() === BookStatus.ON_HOLD &&
        queue.length > 0 &&
        queue[0] === borrower // peek == borrower
      ) {
        notifications.push(book);
      }
    }

    return notifications;
  }

  checkNotifications(borrower) {
    const books = this.getNotificationBooks(borrower);
    return books.map((b) => b.getTitle());
  }

  displayAllBooks(catalogue) {
    const list = [];
    for (let i = 0; i < catalogue.getCatalogueSize(); i++) {
      const book = catalogue.getBook(i);
      let info = {
        title: book.getTitle(),
        author: book.getAuthor(),
        status: book.getStatus(),
      };


      if (book.getStatus() === BookStatus.CHECKED_OUT) {
        info.dueDate = this.calculateDueDate();
      }

      list.push(info);
    }
    return list;
  }

  borrowAvailableBook(catalogue, index) {
    if (!catalogue) return null;
    if (index < 0 || index >= catalogue.getCatalogueSize()) return null;

    const book = catalogue.getBook(index);
    if (book.getStatus() === BookStatus.AVAILABLE) {
      return book;
    }
    return null;
  }


  canBorrow(borrower) {
    if (!borrower) return false;
    return borrower.getBorrowedCount() < 3;
  }

  isBookAvailable(book) {
    if (!book) return false;
    return book.getStatus() === BookStatus.AVAILABLE;
  }

  calculateDueDate() {
    const d = new Date();
    d.setDate(d.getDate() + 14);
    return d;
  }

  recordBorrowTransaction(borrower, book, dueDate) {
    if (!borrower || !book) return;
    borrower.addBorrowedBook(book, dueDate);
  }

  updateAccountStatus(borrower, book) {
    if (!borrower || !book) return;

    const queue = book.getHoldQueue();
    if (queue.length > 0) {
      const head = queue[0];
      if (head.getUsername() === borrower.getUsername()) {
        queue.shift(); // remove head
      }
    }

    // Ensure borrower has the book in their borrowed list
    const alreadyBorrowed = borrower.getBorrowedBooks().includes(book);
    if (!alreadyBorrowed) {
      borrower.addBorrowedBook(book, this.calculateDueDate());
    }

    //borrower.incrementBorrowCount();
    book.setStatus(BookStatus.CHECKED_OUT);
  }

  borrowBookFor(borrower, book) {
  if (!borrower || !book) {
    return { success: false, message: 'Missing borrower or book' };
  }

  if (!this.canBorrow(borrower)) {
    return {
      success: false,
      reason: 'LIMIT',
      message: 'Borrowing limit reached (3 books).',
    };
  }

  const queue = book.getHoldQueue();
  const isFirstInHoldQueue =
    queue && queue.length > 0 && queue[0] === borrower;

  const canTakeNow =
    this.isBookAvailable(book) ||
    (book.getStatus() === BookStatus.ON_HOLD && isFirstInHoldQueue);

  if (!canTakeNow) {
    return {
      success: false,
      reason: 'UNAVAILABLE',
      message: 'Book is not available to you right now.',
    };
  }

  const dueDate = this.calculateDueDate();
  this.recordBorrowTransaction(borrower, book, dueDate);
  this.updateAccountStatus(borrower, book);

  return {
    success: true,
    message: `Borrowed "${book.getTitle()}" successfully`,
    dueDate,
  };
  }



  // Java: boolean unavailableBookStatus(Borrow borrower, Book book)
  unavailableBookStatus(borrower, book) {
    if (!borrower || !book) return false;

    // Already checked out by this borrower → no hold
    if (borrower.getBorrowedBooks().includes(book)) return false;

    // Already has a hold on this book → no extra hold
    if (borrower.getHolds().includes(book)) return false;

    // Book unavailable (checked out or on hold) → can offer hold
    if (
      book.getStatus() === BookStatus.CHECKED_OUT ||
      book.getStatus() === BookStatus.ON_HOLD
    ) {
      return true;
    }

    // Book is available → no hold needed
    return false;
  }

  // Java: boolean hasReachedBorrowLimit(Borrow borrower)
  hasReachedBorrowLimit(borrower) {
    if (!borrower) return false;
    return borrower.getBorrowedCount() >= 3;
  }

  // Java: boolean hold(Borrow borrower, Book book)
  hold(borrower, book) {
    if (!borrower || !book) return false;

    // Your Java logic: only allow hold if borrowerCount == 3 AND book is AVAILABLE
    if (
      borrower.getBorrowedCount() === 3 &&
      book.getStatus() === BookStatus.AVAILABLE
    ) {
      book.addHold(borrower);
      return true;
    }
    return false;
  }

  // Java: public List<Book> getBorrowedBooksCore(Borrow borrower)
  getBorrowedBooksCore(borrower) {
    if (!borrower) return [];
    return borrower.getBorrowedBooks();
  }

  // Java: Book selectReturnBookCore(Borrow borrower, int index)
  selectReturnBookCore(borrower, index) {
    if (!borrower) return null;

    const borrowed = borrower.getBorrowedBooks();
    if (!borrowed || borrowed.length === 0) return null;

    if (index < 0 || index >= borrowed.length) return null;

    return borrowed[index];
  }

  // Java: boolean hasPendingHolds(Book book)
  hasPendingHolds(book) {
    if (!book) return false;
    const queue = book.getHoldQueue();
    return queue && queue.length > 0;
  }

  // Java: public boolean checkPendingHolds(Book book, PrintWriter output)
  checkPendingHolds(book) {
    return this.hasPendingHolds(book);
  }

  // Java: public void holdUpdate(Borrow borrower, Book book)
  holdUpdate(borrower, book) {
    if (!book || !borrower) return;

    const queue = book.getHoldQueue();

    if (!queue || queue.length === 0) {
      // No more holds → book is available
      book.setStatus(BookStatus.AVAILABLE);
    } else {
      // Still holds → keep ON_HOLD and notify next borrower
      book.setStatus(BookStatus.ON_HOLD);
      const nextBorrower = queue[0];
      if (nextBorrower && !nextBorrower.getHolds().includes(book)) {
        nextBorrower.addHold(book);
      }
    }

    // Remove book from current borrower's borrowedBooks
    borrower.removeBorrowedBook(book);
  }
}

module.exports = AuthSystem;
