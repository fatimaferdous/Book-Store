package org.example;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class AuthSystem {
    Users users;
    Borrow currentUser;

    public AuthSystem(Users users) {
        this.users = users;
    }

    boolean authenticate(String username, String password) {
        Borrow borrower = users.findByUsername(username);
        if (borrower != null && borrower.passwordMatches(password)) {
            currentUser = borrower;
            return true;
        }
        return false;
    }

    public void promptLogin(Scanner input, PrintWriter output) {

        while (true) {
            output.println("Enter username:");
            if (!input.hasNextLine()) break;
            String username = input.nextLine().trim();

            output.println("Enter password:");
            if (!input.hasNextLine()) break;
            String password = input.nextLine().trim();

            if (authenticate(username, password)) {
                output.println("Login successful. Welcome, " + username + "!");

                checkNotifications(currentUser, output);
                displayMenu(output);
                break;

            } else {
                output.println("Invalid credentials.");
            }
        }
    }

    List<Book> getNotificationBooks(Borrow borrower) {
        List<Book> notifications = new java.util.ArrayList<>();

        if (borrower == null) {
            return notifications;
        }

        List<Book> heldBooks = borrower.getHolds();
        if (heldBooks.isEmpty()) {
            return notifications;
        }

        for (Book book : heldBooks) {
            if (book.getStatus() == BookStatus.ON_HOLD &&
                    book.getHoldQueue().peek() == borrower) {
                notifications.add(book);
            }
        }

        return notifications;
    }

    public void checkNotifications(Borrow borrower, PrintWriter output) {
        List<Book> notifications = getNotificationBooks(borrower);

        if (notifications.isEmpty()) {
            output.println("No new notifications.");
            return;
        }

        for (Book book : notifications) {
            output.println("Notification: Book '" + book.getTitle()
                    + "' is now available for you to borrow.");
        }
    }

    void displayMenu(PrintWriter output) {
        output.println("Available operations:");
        output.println("1. Borrow a book");
        output.println("2. Return a book");
        output.println("3. Logout");
    }

    public Borrow getCurrentUser(){
        return currentUser;
    }

    void displayBorrowerBookCount(PrintWriter output, Borrow borrower) {
        output.println("Books currently borrowed: " + borrower.getBorrowedCount());
    }

    public void displayAllBooks(Catalogue catalogue, PrintWriter output) {
        for (int i = 0; i < catalogue.getCatalogueSize(); i++) {
            Book book = catalogue.getBook(i);

            String info = book.getTitle() + " by " + book.getAuthor() + " - " + book.getStatus();

            if (book.getStatus() == BookStatus.CHECKED_OUT) {
                LocalDate dueDate = calculateDueDate();
                info += " (Due: " + dueDate + ")";
            }

            output.println(info);
        }
    }

    Book borrowAvailableBook(Catalogue catalogue, int index) {
        if (catalogue == null) return null;
        if (index < 0 || index >= catalogue.getCatalogueSize()) {
            return null;
        }

        Book book = catalogue.getBook(index);
        if (book.getStatus() == BookStatus.AVAILABLE) {
            return book;
        }
        return null;
    }

    public Book borrowBook(Catalogue catalogue, Scanner input, PrintWriter output) {
        output.println("Select a book to borrow (1-" + catalogue.getCatalogueSize() + "):");
        int choice = Integer.parseInt(input.nextLine()) - 1;

        Book book = borrowAvailableBook(catalogue, choice);
        if (book != null) {
            output.println("You selected: " + book.getTitle());
            output.println("Author: " + book.getAuthor());
            return book;
        } else {
            output.println("Sorry, that book is not available.");
            return null;
        }
    }

    public void confirmBorrowing(Book selectedBook, Scanner input, PrintWriter output) {
        if (selectedBook == null) return;
        output.println("Confirm borrowing? (Y/N)");
        String confirmation = input.nextLine().trim();

        if (confirmation.equalsIgnoreCase("Y")) {
            output.println("You want to confirm borrowing");
        } else {
            output.println("Borrowing cancelled.");
        }
    }

    public boolean canBorrow(Borrow borrower) {
        return borrower.getBorrowedCount() < 3;
    }

    public boolean isBookAvailable(Book book) {
        return book.getStatus() == BookStatus.AVAILABLE;
    }

    public LocalDate calculateDueDate() {
        return LocalDate.now().plusDays(14);
    }

    public void recordBorrowTransaction(Borrow borrower, Book book, LocalDate dueDate) {
        borrower.addBorrowedBook(book, dueDate);
    }

    public void updateAccountStatus(Borrow borrower, Book book) {
        if (borrower != null && book != null) {
            if (!borrower.getBorrowedBooks().contains(book)) {
                borrower.addBorrowedBook(book, calculateDueDate());
            }


            borrower.incrementBorrowCount();
            book.setStatus(BookStatus.CHECKED_OUT);
        }
    }

    public void finalConfirmation(Borrow borrower, Book book, LocalDate dueDate, Scanner input, PrintWriter output) {
        output.println("Borrowing confirmed for " + borrower.getUsername() + "!");
        output.println("Book: " + book.getTitle());
        output.println("Due date: " + dueDate);

        output.println("Press Enter to continue...");
        input.nextLine();
        displayMenu(output);
    }

    boolean unavailableBookStatus(Borrow borrower, Book book) {
        if (borrower == null || book == null) {
            return false;
        }

        // Borrower already has this book checked out → no hold needed/allowed
        if (borrower.getBorrowedBooks().contains(book)) {
            return false;
        }

        // Borrower already has a hold on this book → no extra hold needed
        if (borrower.getHolds().contains(book)) {
            return false;
        }

        // Book is unavailable for others (checked out or on hold) → can offer a hold
        if (book.getStatus() == BookStatus.CHECKED_OUT ||
                book.getStatus() == BookStatus.ON_HOLD) {
            return true;
        }

        // Book is available, no hold needed
        return false;
    }

    public boolean unavailableBook(Borrow borrower, Catalogue catalogue, Book book, PrintWriter output) {
        // If borrower already has the book checked out
        if (borrower.getBorrowedBooks().contains(book)) {
            output.println("You already have this book checked out");
            displayAllBooks(catalogue, output); // return to Step 2
            return false;
        }

        // If borrower already has a hold on the book
        if (borrower.getHolds().contains(book)) {
            output.println("You already have a hold on this book");
            displayAllBooks(catalogue, output); // return to Step 2
            return false;
        }

        // Use core logic to decide if we should offer a hold
        if (unavailableBookStatus(borrower, book)) {
            output.println("Book is unavailable. Would you like to place a hold?");
            return true;
        }

        // Book is available, no hold needed
        return false;
    }

    boolean hold(Borrow borrower, Book book) {
        if (borrower == null || book == null) return false;

        if (borrower.getBorrowedCount() == 3 && book.getStatus() == BookStatus.AVAILABLE) {
            book.addHold(borrower);
            return true; // hold successfully placed
        }

        return false; // conditions not met
    }

    public void offerHold(Borrow borrower, Book book, Catalogue catalogue, Scanner input, PrintWriter output) {
        if (borrower.getBorrowedCount() == 3 && book.getStatus() == BookStatus.AVAILABLE) {
            output.println("You have reached the max borrow limit. Would you like to place a hold on the book? (Y/N)");

            String choice = input.nextLine().trim();
            if (choice.equalsIgnoreCase("Y")) {
                boolean placed = hold(borrower, book);
                if (placed) {
                    output.println("Hold successfully placed for book: " + book.getTitle());
                } else {
                    output.println("Unable to place hold due to rule restrictions.");
                }
            } else {
                output.println("Hold not placed.");
            }

            displayAllBooks(catalogue, output);
        }
    }

    boolean hasReachedBorrowLimit(Borrow borrower) {
        if (borrower == null) return false;
        return borrower.getBorrowedCount() >= 3;
    }

    public void borrowMax(Borrow borrower, PrintWriter output) {
        if (hasReachedBorrowLimit(borrower)) {
            output.println("Maximum borrowing limit reached");
            displayMenu(output);
        }
    }

    public List<Book> displayBorrowedBooks(Borrow borrower, PrintWriter output) {
        List<Book> borrowed = borrower.getBorrowedBooks();
        if (borrowed.isEmpty()) {
            output.println("No books currently borrowed.");
            displayMenu(output);
            return borrowed;
        }

        for (Book book : borrowed) {
            LocalDate dueDate = calculateDueDate();
            output.println(book.getTitle() + " - Due: " + dueDate);
        }
        return borrowed;
    }

    Book selectReturnBookCore(Borrow borrower, int index) {
        if (borrower == null) return null;
        List<Book> borrowed = borrower.getBorrowedBooks();

        if (borrowed.isEmpty()) return null;
        if (index < 0 || index >= borrowed.size()) return null;

        return borrowed.get(index);
    }

    public Book selectReturnBook(Borrow borrower, Scanner input, PrintWriter output) {
        List<Book> borrowed = displayBorrowedBooks(borrower, output);

        if (borrowed.isEmpty()) {
            return null;
        }

        output.println("Select a book to return:");
        for (int i = 0; i < borrowed.size(); i++) {
            output.println((i + 1) + ". " + borrowed.get(i).getTitle());
        }

        int choice = Integer.parseInt(input.nextLine()) - 1;
        return selectReturnBookCore(borrower, choice);
    }

    boolean hasPendingHolds(Book book) {
        if (book == null) return false;
        return !book.getHoldQueue().isEmpty();
    }

    public boolean checkPendingHolds(Book book, PrintWriter output) {
        output.println("Checking for pending holds...");
        return hasPendingHolds(book);
    }

    public void holdUpdate(Borrow borrower, Book book) {
        if (book == null) return;

        if (book.getHoldQueue().isEmpty()) {
            book.setStatus(BookStatus.AVAILABLE);
        } else {
            book.setStatus(BookStatus.ON_HOLD);
            Borrow nextBorrower = book.getHoldQueue().peek();
            if (nextBorrower != null && !nextBorrower.getHolds().contains(book)) {
                nextBorrower.addHold(book);
            }
        }

        borrower.borrowedBooks.remove(book);
        borrower.borrowCount = borrower.getBorrowedBooks().size();
    }

    public void returnConfirmation(Book book, Scanner input, PrintWriter output) {
        output.println("Return confirmed for " + book.getTitle());

        output.println("Press Enter to continue...");
        input.nextLine();

        displayMenu(output);
    }

    void logoutUser() {
        currentUser = null;
    }

    public void logout(Scanner input, PrintWriter output) {
        output.println("You have successfully logged out.");
        logoutUser();

        output.println("Please login with your username and password");
        promptLogin(input, output);
    }


}
