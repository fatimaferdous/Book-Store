package org.example;

public class InitializeLibrary {

    Catalogue catalogue = new Catalogue();

    public Catalogue initializeLibrary(){
        catalogue.addBook(new Book("Great Gatsby", "F. Scott FitzGerald"));
        catalogue.addBook(new Book("To Kill a Mockingbird", "Harper Lee"));
        catalogue.addBook(new Book("Pride and Prejudice", "Jane Austen"));
        catalogue.addBook(new Book("Moby-Dick", "Herman Melville"));
        catalogue.addBook(new Book("Jane Eyre", "Charlotte Bronte"));
        catalogue.addBook(new Book("The Lord of the Rings", "J. R. R. Tolkien"));
        catalogue.addBook(new Book("Harry Potter and the Philosopher’s Stone", "J. K. Rowling"));
        catalogue.addBook(new Book("Brave New World", "Aldous Huxley"));
        catalogue.addBook(new Book("The Hobbit", "J. R. R. Tolkien"));
        catalogue.addBook(new Book("Little Women", "Louisa May Alcott"));
        catalogue.addBook(new Book("The Adventures of Huckleberry Finn", "Mark Twain"));
        catalogue.addBook(new Book("1984", "George Orwell"));
        catalogue.addBook(new Book("The Alchemist", "Paulo Cole"));
        catalogue.addBook(new Book("Life of Pi", "Yann Martel"));
        catalogue.addBook(new Book("The Chronicles of Narnia", "C. S. Lewis"));
        catalogue.addBook(new Book("Dracula", "Bram Stoker"));
        catalogue.addBook(new Book("Game of Thrones", "George R. R. Martin"));
        catalogue.addBook(new Book("The Hunger Games", "Suzanne Collins"));
        catalogue.addBook(new Book("A Tale of Two Cities", "Charles Dickens"));
        catalogue.addBook(new Book("For Whom the Bell Tolls", "Ernest Hemingway"));

        return catalogue;
    }
}
