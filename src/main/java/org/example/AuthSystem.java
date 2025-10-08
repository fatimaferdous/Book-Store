package org.example;

import java.io.PrintWriter;
import java.util.Scanner;

public class AuthSystem {
    Users users;

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
            output.println("Login successful. Welcome, " + username + "!");
        } else {
            output.println("Invalid credentials.");
        }
    }
}
