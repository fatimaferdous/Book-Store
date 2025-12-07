const BookStatus = require('./BookStatus');

class Book {
  constructor(title, author) {
    this.title = title;
    this.author = author;

    // default status
    this.status = BookStatus.AVAILABLE;

    this.holdQueue = [];
  }

  addHold(borrower) {
    this.holdQueue.push(borrower);
  }

  getHoldQueue() {
    return this.holdQueue;
  }

  getTitle() {
    return this.title;
  }

  getAuthor() {
    return this.author;
  }

  getStatus() {
    return this.status;
  }

  setStatus(status) {
    this.status = status;
  }
}

module.exports = Book;
