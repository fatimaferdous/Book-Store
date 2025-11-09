package org.example;

import java.util.ArrayList;

public class Catalogue {
    ArrayList<Book> catalogue;

    public Catalogue(){
        catalogue = new ArrayList<Book>();
    }

    public void addBook(Book book){
        catalogue.add(book);
    }

    Book getBook(int index){
        return catalogue.get(index);
    }

    public int getCatalogueSize(){
        return catalogue.size();
    }

    public Book findBookByTitle(String title) {
        if (title == null) return null;
        for (Book book : catalogue) {
            if (book.getTitle().equalsIgnoreCase(title)) {
                return book;
            }
        }
        return null;
    }
}
