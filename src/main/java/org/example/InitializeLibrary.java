package org.example;

public class InitializeLibrary {

    Catalogue catalogue = new Catalogue();

    public Catalogue initializeLibrary(){
        catalogue.addBook(new Book("book2", "author2"));

        return catalogue;
    }
}
