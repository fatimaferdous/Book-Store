package org.example;

import java.io.PrintWriter;
import java.util.Scanner;

public class AuthSystem {
    Users users;
    Borrow currentUser;

    public AuthSystem(Users users) {
        this.users = users;
    }

    public void promptLogin(Scanner input, PrintWriter output) {

        while (true) {
            output.println("Enter username:");
            if (!input.hasNextLine()) break;
            String username = input.nextLine().trim();

            output.println("Enter password:");
            if (!input.hasNextLine()) break;
            String password = input.nextLine().trim();

            Borrow borrower = users.findByUsername(username);
            if (borrower != null && borrower.passwordMatches(password)) {
                currentUser = borrower;
                output.println("Login successful. Welcome, " + username + "!");

                checkNotifications(output);

                displayMenu(output);

                String choice = input.hasNextLine() ? input.nextLine().trim() : "";
                if (choice.equals("3")) {
                    logout(output);
                    break;
                }

            } else {
                output.println("Invalid credentials.");
            }

        }
    }

    void checkNotifications(PrintWriter output) {
        output.println("No new notifications.");
    }

    void displayMenu(PrintWriter output) {
        output.println("Available operations:");
        output.println("1. Borrow a book");
        output.println("2. Return a book");
        output.println("3. Logout");
    }

    void logout(PrintWriter output) {
        output.println("Logging out...");
        currentUser = null;
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
                info += " (Due)";
            }

            output.println(info);
        }
    }

    public Book borrowBook(Catalogue catalogue, Scanner input, PrintWriter output) {
        output.println("Select a book to borrow (1-" + catalogue.getCatalogueSize() + "):");
        int choice = Integer.parseInt(input.nextLine()) - 1;

        Book book = catalogue.getBook(choice);
        if (book.getStatus() == BookStatus.AVAILABLE) {
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
            output.println("Book borrowed successfully.");
        } else {
            output.println("Borrowing cancelled.");
        }
    }



}
