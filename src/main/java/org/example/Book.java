package org.example;

import java.util.LinkedList;
import java.util.Queue;

public class Book {
    String title;
    String author;

    BookStatus status = BookStatus.AVAILABLE;
    Queue<Borrow> holdQueue = new LinkedList<>();

    Book(String title, String author){
        this.title = title;
        this.author = author;
    }

    public void addHold(Borrow borrower) {
        holdQueue.add(borrower);
    }

    public Queue<Borrow> getHoldQueue() {
        return holdQueue;
    }

    public String getTitle(){
        return title;
    }

    public String getAuthor() { return author; }

    // needed for the test
    public BookStatus getStatus() { return status; }

    // optional, useful later for borrow/return responsibilities
    public void setStatus(BookStatus status) { this.status = status; }
}
