const Users = require('./Users');
const Borrow = require('./Borrow');

class InitializeUsers {
  initializeUsers() {
    const users = new Users();

    users.add(new Borrow('alice', 'pass123'));
    users.add(new Borrow('bob', 'pass456'));
    users.add(new Borrow('charlie', 'pass789'));

    return users;
  }
}

module.exports = InitializeUsers;
