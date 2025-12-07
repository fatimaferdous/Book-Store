const Catalogue = require('./Catalogue');
const Book = require('./Book');

class InitializeLibrary {
  constructor() {
    this.catalogue = new Catalogue();
  }

  initializeLibrary() {
    this.catalogue.addBook(new Book('The Great Gatsby', 'F. Scott Fitzgerald'));
    this.catalogue.addBook(new Book('To Kill a Mockingbird', 'Harper Lee'));
    this.catalogue.addBook(new Book('1984', 'George Orwell'));
    this.catalogue.addBook(new Book('Pride and Prejudice', 'Jane Austen'));
    this.catalogue.addBook(new Book('The Hobbit', 'J. R. R. Tolkien'));
    this.catalogue.addBook(new Book('Harry Potter', 'J. K. Rowling'));
    this.catalogue.addBook(new Book('The Catcher in the Rye', 'J. D. Salinger'));
    this.catalogue.addBook(new Book('Animal Farm', 'George Orwell'));
    this.catalogue.addBook(new Book('Lord of the Flies', 'William Golding'));
    this.catalogue.addBook(new Book('Jane Eyre', 'Charlotte Bronte'));
    this.catalogue.addBook(new Book('Wuthering Heights', 'Emily Brontë'));
    this.catalogue.addBook(new Book('Moby Dick', 'Herman Melville'));
    this.catalogue.addBook(new Book('The Odyssey', 'Homer'));
    this.catalogue.addBook(new Book('Hamlet', 'William Shakespeare'));
    this.catalogue.addBook(new Book('War and Peace', 'Leo Tolstoy'));
    this.catalogue.addBook(new Book('The Divine Comedy', 'Dante Alighieri'));
    this.catalogue.addBook(new Book('Crime and Punishment', 'Fyodor Dostoevsky'));
    this.catalogue.addBook(new Book('Don Quixote', 'Miguel de Cervantes'));
    this.catalogue.addBook(new Book('The Iliad', 'Homer'));
    this.catalogue.addBook(new Book('Ulysses', 'James Joyce'));

    return this.catalogue;
  }
}

module.exports = InitializeLibrary;
