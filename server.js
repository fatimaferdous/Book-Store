const express = require('express');
const path = require('path');

// Backend models
const InitializeUsers = require('./src/models/InitializeUsers');
const InitializeLibrary = require('./src/models/InitializeLibrary');
const AuthSystem = require('./src/models/AuthSystem');
const BookStatus = require('./src/models/BookStatus');

const app = express();

// Parse JSON and form bodies
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Serve static frontend files from /public
app.use(express.static(path.join(__dirname, 'public')));

const initUsers = new InitializeUsers();
const users = initUsers.initializeUsers();

// Catalogue with all books
const initLibrary = new InitializeLibrary();
const catalogue = initLibrary.initializeLibrary();

// Auth system using the users collection
const auth = new AuthSystem(users);

console.log('System initialized (users + catalogue)');


// LOGIN
app.post('/api/login', (req, res) => {
  const { username, password } = req.body;

  const result = auth.login(username, password);
  if (!result.success) {
    return res.status(401).json(result);
  }

  res.json({
    success: true,
    message: result.message,
    username: result.user.getUsername(),
  });
});


// LOGOUT
app.post('/api/logout', (req, res) => {
  const result = auth.logout();
  res.json(result);
});

app.get('/api/me', (req, res) => {
  const user = auth.getCurrentUser();
  if (!user) {
    return res.json({ loggedIn: false });
  }

  const borrowedBooks = user.getBorrowedBooksWithDueDates().map(
    ({ book, dueDate }) => ({
      title: book.getTitle(),
      dueDate: dueDate ? dueDate.toISOString().slice(0, 10) : null,
    })
  );

  res.json({
    loggedIn: true,
    username: user.getUsername(),
    borrowedCount: user.getBorrowedCount(),
    borrowedBooks,
  });
});

app.get('/api/books', (req, res) => {
  const baseList = auth.displayAllBooks(catalogue); 
  const user = auth.getCurrentUser();

  if (!user) {
    return res.json(baseList);
  }

  const borrowedBooks = user.getBorrowedBooks();
  const holds = user.getHolds();
  const canBorrow = auth.canBorrow(user); 

  const listWithActions = baseList.map(info => {
    const book = catalogue.findBookByTitle(info.title);

    const borrowedByUser = borrowedBooks.includes(book);
    const heldByUser = holds.includes(book);

    const queue = book.getHoldQueue();
    const isFirstInHoldQueue =
      queue && queue.length > 0 && queue[0] === user;

    let action = null;

    if (book.getStatus() === BookStatus.AVAILABLE) {
      action = borrowedByUser ? 'RETURN' : 'BORROW';
    } else {
      if (borrowedByUser) {
        action = 'RETURN';
      } else if (!canBorrow) {
        action = 'HOLD';
      
      } else if (
        book.getStatus() === BookStatus.ON_HOLD &&
        isFirstInHoldQueue
      ) {
        // book is on hold, and this user is first in line -> let them BORROW it
        action = 'BORROW';
      } else if (!heldByUser) {
        // someone else has it, user not first in queue and no hold yet -> can place hold
        action = 'HOLD';
      } else {
        // already on hold by this user but not first -> no action
        action = null;
      }
    }

    return {
      ...info,
      action,
      borrowedByUser,
      heldByUser,
    };
  });

  res.json(listWithActions);
});

// BORROW A BOOK
app.post('/api/borrow', (req, res) => {
  const user = auth.getCurrentUser();
  if (!user) {
    return res
      .status(401)
      .json({ success: false, message: 'Not logged in' });
  }

  const { title } = req.body;
  const book = catalogue.findBookByTitle(title);
  if (!book) {
    return res
      .status(404)
      .json({ success: false, message: 'Book not found' });
  }

  const result = auth.borrowBookFor(user, book);
  if (!result.success) {
    return res.status(400).json(result);
  }

  res.json(result);
});

// PLACE HOLD ON A BOOK
app.post('/api/hold', (req, res) => {
  const user = auth.getCurrentUser();
  if (!user) {
    return res
      .status(401)
      .json({ success: false, message: 'Not logged in' });
  }

  const { title } = req.body;
  const book = catalogue.findBookByTitle(title);
  if (!book) {
    return res
      .status(404)
      .json({ success: false, message: 'Book not found' });
  }

  // Already borrowed by this user
  if (user.getBorrowedBooks().includes(book)) {
    return res.status(400).json({
      success: false,
      message: 'You already have this book borrowed.',
    });
  }

  // Already on hold by this user
  if (user.getHolds().includes(book)) {
    return res.status(400).json({
      success: false,
      message: 'You already have a hold on this book.',
    });
  }

  // Add to book's hold queue and user's holds
  book.addHold(user);
  user.addHold(book);

  // If the book is currently AVAILABLE, mark it as ON_HOLD
  if (book.getStatus() === BookStatus.AVAILABLE) {
    book.setStatus(BookStatus.ON_HOLD);
  }

  return res.json({
    success: true,
    message: `Hold placed on "${title}".`,
  });
});

// RETURN A BOOK
app.post('/api/return', (req, res) => {
  const user = auth.getCurrentUser();
  if (!user) {
    return res
      .status(401)
      .json({ success: false, message: 'Not logged in' });
  }

  const { title } = req.body;
  const book = catalogue.findBookByTitle(title);
  if (!book) {
    return res
      .status(404)
      .json({ success: false, message: 'Book not found' });
  }

  // Ensure this user actually borrowed this book
  const userBorrowedBooks = user.getBorrowedBooks();
  const hasBook = userBorrowedBooks.includes(book);

  if (!hasBook) {
    return res.status(400).json({
      success: false,
      message: 'You cannot return a book you have not borrowed.',
    });
  }

  const hasPending = auth.checkPendingHolds(book);
  auth.holdUpdate(user, book);

  res.json({
    success: true,
    message: hasPending
      ? `Returned "${title}". Next hold has been updated.`
      : `Returned "${title}". Book is now available.`,
  });
});

// NOTIFICATIONS (holds that are ready)
app.get('/api/notifications', (req, res) => {
  const user = auth.getCurrentUser();
  if (!user) {
    return res.json({ notifications: [] });
  }

  const notifications = auth.checkNotifications(user);
  res.json({ notifications });
});

const PORT = 3000;
app.listen(PORT, () => {
  console.log(`Server running at http://localhost:${PORT}`);
});
