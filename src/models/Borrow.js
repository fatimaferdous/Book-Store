class Borrow {
  constructor(username, password) {
    this.username = username;
    this.password = password;

    this.borrowCount = 0;
    this.borrowedBooks = new Map();

    this.holds = [];
  }

  getBorrowedCount() {
    return this.borrowCount;
  }

  incrementBorrowCount() {
    this.borrowCount += 1;
  }

  getUsername() {
    return this.username;
  }

  passwordMatches(input) {
    return this.password === input;
  }

  addBorrowedBook(book, dueDate) {
    this.borrowedBooks.set(book, dueDate);
    this.borrowCount = this.borrowedBooks.size;
  }

  getBorrowedBooks() {
    return Array.from(this.borrowedBooks.keys());
  }

  addHold(book) {
    if (!this.holds.includes(book)) {
      this.holds.push(book);
    }
  }

  getHolds() {
    return [...this.holds];
  }

  removeBorrowedBook(book) {
    if (this.borrowedBooks.delete(book)) {
      this.borrowCount = this.borrowedBooks.size;
    }
  }

  getBorrowedBooksWithDueDates() {
  return Array.from(this.borrowedBooks.entries()).map(([book, dueDate]) => ({
    book,
    dueDate,
  }));
}
}

module.exports = Borrow;
