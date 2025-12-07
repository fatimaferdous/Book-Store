const Book = require('./Book');

class Catalogue {
  constructor() {
    this.catalogue = [];
  }

  addBook(book) {
    this.catalogue.push(book);
  }


  getBook(index) {
    if (index < 0 || index >= this.catalogue.length) {
      return null;
    }
    return this.catalogue[index];
  }

  getCatalogueSize() {
    return this.catalogue.length;
  }

  findBookByTitle(title) {
    if (!title) return null;

    const target = title.toLowerCase();

    const found = this.catalogue.find(
      (book) => book.getTitle().toLowerCase() === target
    );

    return found || null;
  }
}

module.exports = Catalogue;
