package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class LibraryTest {

    @Test
    @DisplayName("Check library catalogue size is 20")
    void RESP_01_test_01(){
        InitializeLibrary library = new InitializeLibrary();
        Catalogue catalogue = library.initializeLibrary();

        int size = catalogue.getCatalogueSize();

        assertEquals(20, size);


    }
    @Test
    @DisplayName("Check library catalogue for valid book - Great Gatsby.")
    void RESP_01_test_02(){

        InitializeLibrary library = new InitializeLibrary();
        Catalogue catalogue = library.initializeLibrary();

        Book book = catalogue.getBook(0);
        String title = book.getTitle();
        assertEquals("Great Gatsby",title);
    }

    @Test
    @DisplayName("Check all book statuses are set to available")
    void RESP_01_test_03(){
        InitializeLibrary init = new InitializeLibrary();
        Catalogue catalogue = init.initializeLibrary();

        for (int i = 0; i < catalogue.getCatalogueSize(); i++) {
            Book b = catalogue.getBook(i);
            assertEquals(BookStatus.AVAILABLE, b.getStatus());
        }
    }

    @Test
    @DisplayName("check initialized 3 borrower accounts")
    void RESP_02_test_01(){
        Users user = new InitializeUsers().initializeUsers();
        assertEquals(3, user.size());
    }

    @Test
    @DisplayName("Check all borrower accounts have zero borrowed books initially")
    void RESP_02_test_02(){
        Users users = new InitializeUsers().initializeUsers();

        int maxBorrowed = -1;
        for (int i = 0; i < users.size(); i++) {
            int c = users.get(i).getBorrowedCount();
            if (c > maxBorrowed) maxBorrowed = c;
        }
        assertEquals(0, maxBorrowed);
    }

    @Test
    @DisplayName("System checks for valid username and password")
    void RESP_03_test_01(){
        String simulatedInput = "CarlTY\n1234Happy\n";
        StringWriter simulatedOutput = new StringWriter();

        Scanner input = new Scanner(simulatedInput);
        PrintWriter output = new PrintWriter(simulatedOutput, true);

        InitializeUsers init = new InitializeUsers();
        Users users = init.initializeUsers();
        AuthSystem auth = new AuthSystem(users);

        auth.promptLogin(input, output);

        String out = simulatedOutput.toString();
        boolean success = out.contains("Login successful");
        assertTrue(success, "Expected successful login message in output");
    }

    @Test
    @DisplayName("checks for session created")
    void RESP_04_test_01() {
        String simulatedInput = "CarlTY\n1234Happy\n";
        StringWriter simulatedOutput = new StringWriter();

        Scanner input = new Scanner(simulatedInput);
        PrintWriter output = new PrintWriter(simulatedOutput, true);

        InitializeUsers init = new InitializeUsers();
        Users users = init.initializeUsers();
        AuthSystem auth = new AuthSystem(users);

        // The borrower logs in successfully
        auth.promptLogin(input, output);

        // Expect the current user to be stored
        Borrow current = auth.getCurrentUser();

        assertNotNull(current, "Expected current user to be set after successful login");
        assertEquals("CarlTY", current.getUsername(), "Expected the current user to be 'alice'");
    }

    @Test
    @DisplayName("System checks for available holds and displays notifications")
    void RESP_04_test_02() {
        String simulatedInput = "CarlTY\n1234Happy\n";
        StringWriter simulatedOutput = new StringWriter();

        Scanner input = new Scanner(simulatedInput);
        PrintWriter output = new PrintWriter(simulatedOutput, true);

        InitializeUsers init = new InitializeUsers();
        Users users = init.initializeUsers();
        AuthSystem auth = new AuthSystem(users);

        auth.promptLogin(input, output);

        String out = simulatedOutput.toString();
        boolean hasNotification = out.contains("notification") || out.contains("No new notifications");
        assertTrue(hasNotification, "Expected system to display notification message");
    }

    @Test
    @DisplayName("System displays borrower operations menu")
    void RESP_04_test_03() {
        String simulatedInput = "CarlTY\n1234Happy\n";
        StringWriter simulatedOutput = new StringWriter();

        Scanner input = new Scanner(simulatedInput);
        PrintWriter output = new PrintWriter(simulatedOutput, true);

        InitializeUsers init = new InitializeUsers();
        Users users = init.initializeUsers();
        AuthSystem auth = new AuthSystem(users);

        auth.promptLogin(input, output);

        String out = simulatedOutput.toString();
        boolean menuDisplayed = out.contains("1. Borrow a book") &&
                out.contains("2. Return a book") &&
                out.contains("3. Logout");

        assertTrue(menuDisplayed, "Expected borrower operations menu to be displayed");
    }

    @Test
    @DisplayName("System checks for session ending after logout")
    void RESP_04_test_04(){
        String simulatedInput = "CarlTY\n1234Happy\n3\n";
        StringWriter simulatedOutput = new StringWriter();

        Scanner input = new Scanner(simulatedInput);
        PrintWriter output = new PrintWriter(simulatedOutput, true);

        InitializeUsers init = new InitializeUsers();
        Users users = init.initializeUsers();
        AuthSystem auth = new AuthSystem(users);

        auth.promptLogin(input, output);

        assertNull(auth.getCurrentUser(), "Expected current user to be cleared after choosing logout");
    }

    @Test
    @DisplayName("System checks for invalid credentials and prompts the user to login again")
    void RESP_05_test_01(){
        String simulatedInput = "wrongUser\nwrongPass\n";
        StringWriter simulatedOutput = new StringWriter();

        Scanner input = new Scanner(simulatedInput);
        PrintWriter output = new PrintWriter(simulatedOutput, true);

        InitializeUsers init = new InitializeUsers();
        Users users = init.initializeUsers();
        AuthSystem auth = new AuthSystem(users);

        auth.promptLogin(input, output);

        String systemOutput = simulatedOutput.toString();

        // Check that after invalid login, system asked for username again
        // (We expect "Enter username:" to appear a second time for retry)
        long usernamePrompts = systemOutput.lines()
                .filter(line -> line.contains("Enter username:"))
                .count();

        assertTrue(usernamePrompts >= 2,
                "Expected system to prompt for username again after invalid credentials");
    }

    @Test
    @DisplayName("Displays borrower's current book count")
    void RESP_06_test_01() {
        Borrow borrower = new Borrow("TestUser", "pass123");

        StringWriter simulatedOutput = new StringWriter();
        PrintWriter output = new PrintWriter(simulatedOutput, true);

        AuthSystem auth = new AuthSystem(new Users());
        auth.displayBorrowerBookCount(output, borrower);

        String systemOutput = simulatedOutput.toString();

        assertTrue(systemOutput.contains("Books currently borrowed: " + borrower.getBorrowedCount()),
                "Expected system to display borrower's current book count");
    }

    @Test
    @DisplayName("checks for book title, author, and status")
    void RESP_06_test_02(){
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();

        StringWriter simulatedOutput = new StringWriter();
        PrintWriter output = new PrintWriter(simulatedOutput, true);

        AuthSystem auth = new AuthSystem(new Users());
        auth.displayAllBooks(catalogue, output);

        String systemOutput = simulatedOutput.toString();

        boolean allDisplayedCorrectly = true;
        for (int i = 0; i < catalogue.getCatalogueSize(); i++) {
            Book book = catalogue.getBook(i);
            String expected = book.getTitle() + " by " + book.getAuthor() + " - " + book.getStatus();
            if (!systemOutput.contains(expected)) {
                allDisplayedCorrectly = false;
                break;
            }
        }
        assertTrue(allDisplayedCorrectly,
                "Expected system to display all books with title, author, and status");
    }

    @Test
    @DisplayName("checks if Due is printed if status of book is checked out")
    void RESP_06_test_03(){
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();

        // Mark one book as checked out
        Book checkedOutBook = catalogue.getBook(0);
        checkedOutBook.setStatus(BookStatus.CHECKED_OUT);

        StringWriter simulatedOutput = new StringWriter();
        PrintWriter output = new PrintWriter(simulatedOutput, true);

        AuthSystem auth = new AuthSystem(new Users());

        auth.displayAllBooks(catalogue, output);

        String systemOutput = simulatedOutput.toString();

        assertTrue(systemOutput.contains("Due"),
                "Expected system to display 'Due' for checked-out books");
    }

    @Test
    @DisplayName("Checks if borrower selects a book and the system is able to display book borrowing info")
    void RESP_07_test_01(){
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();

        Book selectedBook = catalogue.getBook(0);

        StringWriter simulatedOutput = new StringWriter();
        PrintWriter output = new PrintWriter(simulatedOutput, true);
        Scanner simulatedInput = new Scanner("1\n"); // borrower selects book #1

        AuthSystem auth = new AuthSystem(new Users());

        auth.borrowBook(catalogue, simulatedInput, output);

        String systemOutput = simulatedOutput.toString();

        boolean confirmationDisplayed = systemOutput.contains("You selected: " + selectedBook.getTitle()) &&
                systemOutput.contains("Author: " + selectedBook.getAuthor());

        assertTrue(confirmationDisplayed,
                "Expected system to show selected book details and confirmation prompt");
    }

    @Test
    @DisplayName("borrower confirms the borrowing transaction")
    void RESP_07_test_02(){
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();

        StringWriter simulatedOutput = new StringWriter();
        PrintWriter output = new PrintWriter(simulatedOutput, true);

        Scanner simulatedInput = new Scanner("1\nY\n"); // select book 1, confirm

        AuthSystem auth = new AuthSystem(new Users());

        Book selectedBook = auth.borrowBook(catalogue, simulatedInput, output);

        auth.confirmBorrowing(selectedBook, simulatedInput, output);

        String systemOutput = simulatedOutput.toString();

        assertTrue(systemOutput.contains("You want to confirm borrowing"),
                "Expected system to confirm the borrowing transaction");
    }

    @Test
    @DisplayName("Verifies book availability")
    void RESP_08_test_01() {
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();

        AuthSystem auth = new AuthSystem(new Users());

        Book selectedBook = catalogue.getBook(0);

        boolean isAvailable = auth.isBookAvailable(selectedBook);

        assertTrue(isAvailable, "Expected system to verify that the selected book is available");
    }

    @Test
    @DisplayName("Checks if borrower has < 3 books")
    void RESP_08_test_02(){
        Borrow borrower = new Borrow("TestUser", "pass123");
        borrower.borrowCount = 2;

        AuthSystem auth = new AuthSystem(new Users());

        boolean eligible = auth.canBorrow(borrower);

        assertTrue(eligible, "Expected borrower to be eligible to borrow a book when they have fewer than 3 books");
    }

    @Test
    @DisplayName("System calculates due date 14 days from now")
    void RESP_09_test_01(){
        AuthSystem auth = new AuthSystem(new Users());

        LocalDate dueDate = auth.calculateDueDate();

        LocalDate expectedDate = LocalDate.now().plusDays(14);
        assertEquals(expectedDate, dueDate);
    }

    @Test
    @DisplayName("checks for recorded borrowing transaction")
    void RESP_09_test_02(){
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        InitializeUsers initUsers = new InitializeUsers();
        Users users = initUsers.initializeUsers();
        AuthSystem auth = new AuthSystem(users);

        Borrow borrower = users.findByUsername("CarlTY");
        Book book = catalogue.getBook(0);
        LocalDate dueDate = auth.calculateDueDate();

        auth.recordBorrowTransaction(borrower, book, dueDate);

        assertTrue(borrower.getBorrowedBooks().contains(book));
    }

    @Test
    @DisplayName("System checks for book status checked out after borrower account was updated")
    void RESP_09_test_03(){
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        InitializeUsers initUsers = new InitializeUsers();
        Users users = initUsers.initializeUsers();

        AuthSystem auth = new AuthSystem(users);
        Borrow borrower = users.findByUsername("CarlTY");
        Book book = catalogue.getBook(0);
        LocalDate dueDate = auth.calculateDueDate();

        auth.recordBorrowTransaction(borrower, book, dueDate);
        auth.updateAccountStatus(borrower, book);

        assertEquals(BookStatus.CHECKED_OUT, book.getStatus());
    }

    @Test
    @DisplayName("System checks for borrower account gets incremented with number of books after updating")
    void RESP_09_test_04(){
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        InitializeUsers initUsers = new InitializeUsers();
        Users users = initUsers.initializeUsers();

        AuthSystem auth = new AuthSystem(users);
        Borrow borrower = users.findByUsername("CarlTY");
        Book book = catalogue.getBook(0);

        auth.updateAccountStatus(borrower, book);

        assertEquals(1, borrower.getBorrowedCount());
    }

    @Test
    @DisplayName("checks for final confirmation and acknowledgment")
    void RESP_10_test_01(){
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        InitializeUsers initUsers = new InitializeUsers();
        Users users = initUsers.initializeUsers();
        AuthSystem auth = new AuthSystem(users);

        Borrow borrower = users.findByUsername("CarlTY");
        Book book = catalogue.getBook(0);
        LocalDate dueDate = auth.calculateDueDate();

        StringWriter simulatedOutput = new StringWriter();
        PrintWriter output = new PrintWriter(simulatedOutput, true);
        Scanner simulatedInput = new Scanner("\n"); // borrower acknowledges by pressing Enter

        auth.finalConfirmation(borrower, book, dueDate, simulatedInput, output);

        String systemOutput = simulatedOutput.toString();

        assertTrue(systemOutput.contains(book.getTitle()) && systemOutput.contains("Press Enter to continue"));
    }

    @Test
    @DisplayName("checks for system returning to menu after user acknowledgement")
    void RESP_10_test_02(){
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        InitializeUsers initUsers = new InitializeUsers();
        Users users = initUsers.initializeUsers();
        AuthSystem auth = new AuthSystem(users);

        Borrow borrower = users.findByUsername("CarlTY");
        Book book = catalogue.getBook(0);
        LocalDate dueDate = auth.calculateDueDate();

        StringWriter simulatedOutput = new StringWriter();
        PrintWriter output = new PrintWriter(simulatedOutput, true);
        Scanner simulatedInput = new Scanner("\n");

        auth.finalConfirmation(borrower, book, dueDate, simulatedInput, output);

        String systemOutput = simulatedOutput.toString();

        assertTrue(systemOutput.contains("Available operations:"));
    }

    @Test
    @DisplayName("System offers a hold if the book is checked out or on hold by someone else")
    void RESP_11_test_01(){
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        InitializeUsers initUsers = new InitializeUsers();
        Users users = initUsers.initializeUsers();
        AuthSystem auth = new AuthSystem(users);

        Borrow borrower = users.findByUsername("CarlTY");
        Book book = catalogue.getBook(0);

        book.setStatus(BookStatus.CHECKED_OUT);

        StringWriter simulatedOutput = new StringWriter();
        PrintWriter output = new PrintWriter(simulatedOutput, true);
        boolean canPlaceHold = auth.unavailableBook(borrower, catalogue, book, output);

        String systemOutput = simulatedOutput.toString();
        assertTrue(systemOutput.contains("Book is unavailable. Would you like to place a hold?") && canPlaceHold);
    }

    @Test
    @DisplayName("Borrower already has a hold on this book")
    void RESP_11_test_02() {
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        InitializeUsers initUsers = new InitializeUsers();
        Users users = initUsers.initializeUsers();
        AuthSystem auth = new AuthSystem(users);

        Borrow borrower = users.findByUsername("CarlTY");
        Book book = catalogue.getBook(0);

        borrower.addHold(book);

        StringWriter simulatedOutput = new StringWriter();
        PrintWriter output = new PrintWriter(simulatedOutput, true);

        boolean canPlaceHold = auth.unavailableBook(borrower, catalogue, book, output);

        String systemOutput = simulatedOutput.toString();

        assertTrue(
                systemOutput.contains("You already have a hold on this book") && !canPlaceHold,
                "Expected message about existing hold and return to Step 2");
    }

    @Test
    @DisplayName("Borrower already has this book checked out, no hold allowed")
    void RESP_11_test_03() {
        InitializeLibrary initLib = new InitializeLibrary();
        Catalogue catalogue = initLib.initializeLibrary();
        InitializeUsers initUsers = new InitializeUsers();
        Users users = initUsers.initializeUsers();
        AuthSystem auth = new AuthSystem(users);

        Borrow borrower = users.findByUsername("CarlTY");
        Book book = catalogue.getBook(0);

        borrower.addBorrowedBook(book, auth.calculateDueDate());

        StringWriter simulatedOutput = new StringWriter();
        PrintWriter output = new PrintWriter(simulatedOutput, true);

        boolean canPlaceHold = auth.unavailableBook(borrower, catalogue, book, output);

        String systemOutput = simulatedOutput.toString();

        assertTrue(
                systemOutput.contains("You already have this book checked out") && !canPlaceHold,
                "Expected system to detect that borrower already has this book checked out and return to Step 2");
    }



}
