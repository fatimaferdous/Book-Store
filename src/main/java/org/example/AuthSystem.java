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
        output.println("Enter username:");
        String username = input.nextLine().trim();

        output.println("Enter password:");
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
            }

        } else {
            output.println("Invalid credentials.");
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
}
