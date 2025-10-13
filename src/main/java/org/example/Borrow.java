package org.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Borrow {
    String username;
    String password;
    int borrowCount = 0;
    Map<Book, LocalDate> borrowedBooks = new HashMap<>();
    List<Book> holds = new ArrayList<>();//THIS
    int getBorrowedCount() { return borrowCount; }

    public void incrementBorrowCount() {
        borrowCount++;
    }

    Borrow(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public boolean passwordMatches(String input) { return password.equals(input); }

    public void addBorrowedBook(Book book, LocalDate dueDate) {
        borrowedBooks.put(book, dueDate);
    }

    public List<Book> getBorrowedBooks() {
        return new ArrayList<>(borrowedBooks.keySet());
    }

    public void addHold(Book book) {
        if (!holds.contains(book)) {
            holds.add(book);
        }
    }

    public List<Book> getHolds() {return new ArrayList<>(holds);}
}
