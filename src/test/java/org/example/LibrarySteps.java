package org.example;
import io.cucumber.java.en.*;
import static org.junit.jupiter.api.Assertions.*;

public class LibrarySteps {


    Users users;
    Catalogue catalogue;
    AuthSystem auth;
    Borrow currentUser;
    Book currentBook;


    @Given("20 books along with 3 user accounts in a library system")
    public void initializeLibraryAndUsers() {
        users = new InitializeUsers().initializeUsers();
        catalogue = new InitializeLibrary().initializeLibrary();
        auth = new AuthSystem(users);
    }

    @When("{string} logs in with password {string}")
    public void userLogsIn(String username, String password) {
        auth.authenticate(username, password);
        users.findByUsername(username);
    }

    @And("{string} borrows {string}")
    public void userBorrowsBook(String username, String title) {
        Borrow borrower = users.findByUsername(username);
        Book book = catalogue.findBookByTitle(title);

        auth.recordBorrowTransaction(borrower, book, auth.calculateDueDate());
        auth.updateAccountStatus(borrower, book);
        currentBook = book;
    }

    @Then("{string} is unavailable")
    public void bookIsUnavailable(String title) {
        Book book = catalogue.findBookByTitle(title);
        assertNotNull(book, "Book must exist");
        assertEquals(BookStatus.CHECKED_OUT, book.getStatus(), "Book should be unavailable after borrowing");
        assertFalse(auth.isBookAvailable(book), "Book should not be available");
    }

    @When("{string} logs out")
    public void userLogsOut(String username) {
        if (username != null) {
            auth.logoutUser();
        }
    }

    @And("{string} attempts to borrow {string}")
    public void userAttemptsToBorrow(String username, String title) {
        Borrow borrower = users.findByUsername(username);

        Book book = catalogue.findBookByTitle(title);

        boolean available = auth.isBookAvailable(book);

        if (available) {
            auth.recordBorrowTransaction(borrower, book, auth.calculateDueDate());
            auth.updateAccountStatus(borrower, book);
        }
    }

    @Then("the borrow operation fails due to book unavailability")
    public void borrowOperationFails() {
        assertNotNull(currentBook, "Current book must be tracked");
        assertEquals(BookStatus.CHECKED_OUT, currentBook.getStatus(),
                "Borrow should fail when the book is unavailable");
    }

    @And("{string} returns {string}")
    public void userReturnsBook(String username, String title) {
        Borrow borrower = users.findByUsername(username);
        Book book = catalogue.findBookByTitle(title);
        auth.holdUpdate(borrower, book);
    }

    @Then("{string} becomes available again")
    public void bookBecomesAvailable(String title) {
        Book book = catalogue.findBookByTitle(title);
        assertNotNull(book);

        assertEquals(BookStatus.AVAILABLE, book.getStatus(), "Book should become available again after return");
        assertTrue(auth.isBookAvailable(book), "Book must be available again after return");
    }

    @When("{string} places a hold on {string}")
    public void userPlacesHoldOnBook(String username, String title) {
        Borrow borrower = users.findByUsername(username);

        Book book = catalogue.findBookByTitle(title);

        int oldQueueSize = book.getHoldQueue().size();

        book.addHold(borrower);
        borrower.addHold(book);
    }

    @Then("{string} has {string} reserved first as well as {string} second")
    public void holdQueueHasFirstAndSecond(String title, String firstUser, String secondUser) {
        Book book = catalogue.findBookByTitle(title);
        assertNotNull(book, "Book must exist");

        java.util.Queue<Borrow> queue = book.getHoldQueue();
        assertEquals(2, queue.size(), "Hold queue should have exactly two entries");

        Borrow[] arr = queue.toArray(new Borrow[0]);
        assertEquals(firstUser, arr[0].getUsername(),
                "First in hold queue should be " + firstUser);
        assertEquals(secondUser, arr[1].getUsername(),
                "Second in hold queue should be " + secondUser);
    }

    @Then("{string} has a notification for {string}")
    public void userHasNotificationForBook(String username, String title) {
        Borrow borrower = users.findByUsername(username);
        assertNotNull(borrower, "Borrower must exist");

        Book book = catalogue.findBookByTitle(title);
        assertNotNull(book, "Book must exist");

        java.util.List<Book> notifications = auth.getNotificationBooks(borrower);
        assertNotNull(notifications, "Notification list must not be null");
        assertTrue(notifications.contains(book),
                username + " should have a notification for " + title);
    }

    @Then("{string} has no notification for {string}")
    public void userHasNoNotificationForBook(String username, String title) {
        Borrow borrower = users.findByUsername(username);
        assertNotNull(borrower, "Borrower must exist");

        Book book = catalogue.findBookByTitle(title);
        assertNotNull(book, "Book must exist");

        java.util.List<Book> notifications = auth.getNotificationBooks(borrower);
        if (notifications != null) {
            assertFalse(notifications.contains(book),
                    username + " should NOT have a notification for " + title);
        }
    }

    @When("{string} borrows the reserved copy of {string}")
    public void userBorrowsReservedCopy(String username, String title) {
        Borrow borrower = users.findByUsername(username);

        Book book = catalogue.findBookByTitle(title);

        Borrow head = book.getHoldQueue().peek();

        auth.recordBorrowTransaction(borrower, book, auth.calculateDueDate());
        auth.updateAccountStatus(borrower, book);
    }

    @Then("{string} has borrowed {string}")
    public void userHasBorrowedBook(String username, String title) {
        Borrow borrower = users.findByUsername(username);
        assertNotNull(borrower, "Borrower must exist");

        Book book = catalogue.findBookByTitle(title);
        assertNotNull(book, "Book must exist");

        assertTrue(borrower.getBorrowedBooks().contains(book),
                username + " should have borrowed " + title);
        assertEquals(BookStatus.CHECKED_OUT, book.getStatus(),
                "Book status should be CHECKED_OUT");
    }

    @Then("{string} is now next in line for {string}")
    public void holdQueueNowHasFirst(String firstUser, String title) {
        Book book = catalogue.findBookByTitle(title);
        assertNotNull(book, "Book must exist");

        java.util.Queue<Borrow> queue = book.getHoldQueue();
        assertFalse(queue.isEmpty(), "Hold queue should not be empty");

        Borrow head = queue.peek();
        assertNotNull(head, "Head of queue must not be null");
        assertEquals(firstUser, head.getUsername(),
                "Head of queue should now be " + firstUser);
    }

    @And("{string} currently has 3 books borrowed")
    public void userHasThreeBooksBorrowed(String username) {
        Borrow borrower = users.findByUsername(username);
        for (int i = 0; i < 3; i++) {
            Book book = catalogue.getBook(i);
            auth.recordBorrowTransaction(borrower, book, auth.calculateDueDate());
            auth.updateAccountStatus(borrower, book);
        }
    }

    @When("{string} tries to borrow {string}")
    public void userTriesToBorrow(String username, String title) {
        currentUser = users.findByUsername(username);
        currentBook = catalogue.findBookByTitle(title);

        if (currentBook != null && auth.isBookAvailable(currentBook) && auth.canBorrow(currentUser)) {
            var due = auth.calculateDueDate();
            auth.recordBorrowTransaction(currentUser, currentBook, due);
            auth.updateAccountStatus(currentUser, currentBook);
        }
    }


    @Then("the borrow operation fails for {string} due to reaching the borrowing limit")
    public void thenBorrowFailsDueToLimit(String username) {
        Borrow borrower = users.findByUsername(username);
        assertNotNull(borrower, "Borrower must exist for this check");

        assertFalse(auth.canBorrow(borrower),
                borrower.getUsername() + " should NOT be allowed to borrow more than 3 books");
    }

    @Then("the hold is successfully recorded for {string}")
    public void holdSuccessfullyRecorded(String username) {
        Borrow borrower = users.findByUsername(username);
        assertFalse(borrower.getHolds().isEmpty(), username + " should have a recorded hold");
    }

    @When("{string} returns one of the books")
    public void userReturnsOneOfTheirBooks(String username) {
        Borrow borrower = users.findByUsername(username);
        Book bookToReturn = borrower.getBorrowedBooks().getFirst();
        auth.holdUpdate(borrower, bookToReturn);
    }

    @Then("{string} has borrowing capacity available")
    public void userNowHasBorrowingCapacity(String username) {
        Borrow borrower = users.findByUsername(username);
        assertTrue(borrower.getBorrowedCount() < 3,
                username + " should have borrowing capacity after returning a book");
    }

    @When("{string} attempts to return a book")
    public void userAttemptsToReturnABook(String username) {
        Borrow borrower = users.findByUsername(username);
        auth.getBorrowedBooksCore(borrower);
    }

    @Then("{string} has no books currently borrowed")
    public void userHasNoBooksCurrentlyBorrowed(String username) {
        Borrow borrower = users.findByUsername(username);
        assertTrue(auth.getBorrowedBooksCore(borrower).isEmpty(),
                username + " should have no books currently borrowed");
    }

    @Then("all books show as available")
    public void allBooksShowAsAvailable() {
        for (int i = 0; i < catalogue.getCatalogueSize(); i++) {
            Book b = catalogue.getBook(i);
            assertEquals(BookStatus.AVAILABLE, b.getStatus(),
                    "Every book should be AVAILABLE when no books are borrowed");
        }
    }

    @Then("the system should have no active user session")
    public void systemHasNoActiveUserSession() {
        assertNull(auth.getCurrentUser(), "There should be no active user after logout");
    }
}
