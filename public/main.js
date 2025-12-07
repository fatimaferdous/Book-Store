//Helper to show messages
function showMessage(text, isError = false) {
  const msgDiv = document.getElementById('message');
  msgDiv.textContent = text || '';
  msgDiv.style.color = isError ? 'red' : 'green';
}

//Toggle sections based on login state
function setLoggedInUI(loggedIn, username = '') {
  const loginSection = document.getElementById('login-section');
  const appSection = document.getElementById('app-section');
  const userSpan = document.getElementById('current-user');

  if (loggedIn) {
    loginSection.style.display = 'none';
    appSection.style.display = 'block';
    userSpan.textContent = username;
  } else {
    loginSection.style.display = 'block';
    appSection.style.display = 'none';
    userSpan.textContent = '';
  }
}

//LOGIN BUTTON
document.getElementById('login-btn').addEventListener('click', async () => {
  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value.trim();

  if (!username || !password) {
    showMessage('Please enter username and password.', true);
    return;
  }

  try {
    const res = await fetch('/api/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    });

    const data = await res.json();

    if (!res.ok || !data.success) {
      showMessage(data.message || 'Login failed', true);
      setLoggedInUI(false);
      return;
    }

    showMessage(data.message);
    setLoggedInUI(true, data.username);
    await refreshMe();
    await refreshBooks();
    await refreshNotifications();
  } catch (err) {
    console.error(err);
    showMessage('Error contacting server', true);
  }
});

//LOGOUT BUTTON
document.getElementById('logout-btn').addEventListener('click', async () => {
  try {
    const res = await fetch('/api/logout', { method: 'POST' });
    const data = await res.json();
    showMessage(data.message || 'Logged out');
  } catch (err) {
    console.error(err);
    showMessage('Error logging out', true);
  } finally {
    setLoggedInUI(false);
  }
});

//FETCH CURRENT USER INFO
async function refreshMe() {
  const res = await fetch('/api/me');
  const data = await res.json();

  if (!data.loggedIn) {
    setLoggedInUI(false);
    return;
  }

  setLoggedInUI(true, data.username);

  // borrowed count
  document.getElementById('borrowed-count').textContent = data.borrowedCount;

  // borrowed books list
  const list = document.getElementById('my-borrowed-list');
  list.innerHTML = '';
  data.borrowedBooks.forEach((item) => {
    const li = document.createElement('li');
    li.textContent = `${item.title} (Due: ${
      item.dueDate || 'N/A'
    })`;
    list.appendChild(li);
  });
}

//FETCH BOOKS AND BUILD TABLE
async function refreshBooks() {
  const res = await fetch('/api/books');
  const books = await res.json();

  const tbody = document.getElementById('books-tbody');
  tbody.innerHTML = '';

  books.forEach((book) => {
    const tr = document.createElement('tr');
    tr.setAttribute('data-testid', 'book-row');
    tr.setAttribute('data-title', book.title);

    const titleTd = document.createElement('td');
    titleTd.textContent = book.title;

    const authorTd = document.createElement('td');
    authorTd.textContent = book.author;

    const statusTd = document.createElement('td');
    statusTd.textContent = book.status;
    statusTd.setAttribute('data-testid', 'book-status');

    const actionTd = document.createElement('td');
    const btn = document.createElement('button');

    if (book.action === 'BORROW') {
      btn.textContent = 'Borrow';
      btn.setAttribute('data-testid', 'borrow-button');
      btn.addEventListener('click', () => borrowBook(book.title));
    } else if (book.action === 'RETURN') {
      btn.textContent = 'Return';
      btn.setAttribute('data-testid', 'return-button');
      btn.addEventListener('click', () => returnBook(book.title));
    } else if (book.action === 'HOLD') {
      btn.textContent = 'Place Hold';
      btn.setAttribute('data-testid', 'hold-button');
      btn.addEventListener('click', () => holdBook(book.title));
    } else {
      btn.textContent = 'Unavailable';
      btn.disabled = true;
    }

    actionTd.appendChild(btn);

    tr.appendChild(titleTd);
    tr.appendChild(authorTd);
    tr.appendChild(statusTd);
    tr.appendChild(actionTd);

    tbody.appendChild(tr);
  });
}

// manual refresh button for books
document
  .getElementById('refresh-books-btn')
  .addEventListener('click', refreshBooks);

async function borrowBook(title) {
  try {
    const res = await fetch('/api/borrow', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title }),
    });

    const data = await res.json();

    if (!res.ok || !data.success) {
      const msg = data.message || 'Could not borrow book';

      const isLimitError =
        msg.toLowerCase().includes('borrowing limit') ||
        msg.toLowerCase().includes('limit reached');

      if (isLimitError) {
        // Show popup asking if user wants to place a hold instead
        const wantsHold = window.confirm(
          `${msg}\n\nWould you like to place a hold on "${title}" instead?`
        );

        if (wantsHold) {
          // Try to place a hold on the same title
          await holdBook(title);
          // holdBook will show its own message + refresh UI
          return;
        } else {
          // User said No just show the limit message
          showMessage(msg, true);
        }
      } else {
        // Some other borrow error
        showMessage(msg, true);
      }
    } else {
      // Borrow succeeded normally
      showMessage(data.message);
    }

    // Refresh user info, books, and notifications
    await refreshMe();
    await refreshBooks();
    await refreshNotifications();
  } catch (err) {
    console.error(err);
    showMessage('Error borrowing book', true);
  }
}


//RETURN ACTION
async function returnBook(title) {
  try {
    const res = await fetch('/api/return', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title }),
    });

    const data = await res.json();

    if (!res.ok || !data.success) {
      showMessage(data.message || 'Could not return book', true);
    } else {
      showMessage(data.message);
    }

    await refreshMe();
    await refreshBooks();
    await refreshNotifications();
  } catch (err) {
    console.error(err);
    showMessage('Error returning book', true);
  }
}

//PLACE HOLD ACTION
async function holdBook(title) {
  try {
    const res = await fetch('/api/hold', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title }),
    });

    const data = await res.json();

    if (!res.ok || !data.success) {
      showMessage(data.message || 'Could not place hold', true);
    } else {
      showMessage(data.message);
    }

    await refreshMe();
    await refreshBooks();
    await refreshNotifications();
  } catch (err) {
    console.error(err);
    showMessage('Error placing hold', true);
  }
}

//NOTIFICATIONS
async function refreshNotifications() {
  const res = await fetch('/api/notifications');
  const data = await res.json();

  const list = document.getElementById('notifications-list');
  list.innerHTML = '';

  data.notifications.forEach((title) => {
    const li = document.createElement('li');
    li.textContent = `Your hold on "${title}" is now available.`;
    list.appendChild(li);
  });
}

// button to manually refresh notifications
document
  .getElementById('refresh-notifications-btn')
  .addEventListener('click', refreshNotifications);

//On initial page load: check if already logged in
window.addEventListener('load', async () => {
  await refreshMe();
  if (document.getElementById('app-section').style.display === 'block') {
    await refreshBooks();
    await refreshNotifications();
  }
});
