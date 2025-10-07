package org.example;

public class Borrow {
    String username;
    String password;
    int borrowCount = 0;
    int getBorrowedCount() { return borrowCount; }

    Borrow(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public boolean passwordMatches(String input) { return password.equals(input); }
}
