package org.example;

public class Book {
    String title;
    String author;

    BookStatus status = BookStatus.AVAILABLE;

    Book(String title, String author){
        this.title = title;
        this.author = author;
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
