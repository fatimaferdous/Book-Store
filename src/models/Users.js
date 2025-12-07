const Borrow = require('./Borrow');

class Users {
  constructor() {
    this.borrowed = [];
  }

  size() {
    return this.borrowed.length;
  }

  add(borrower) {
    this.borrowed.push(borrower);
  }

  get(index) {
    if (index < 0 || index >= this.borrowed.length) {
      return null;
    }
    return this.borrowed[index];
  }

  findByUsername(username) {
    if (!username) return null;

    const found = this.borrowed.find(
      (b) => b.getUsername() === username
    );

    return found || null;
  }
}

module.exports = Users;
