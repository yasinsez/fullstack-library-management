package com.yasinsez.library.service;

import com.yasinsez.library.repository.BookRepository;
import com.yasinsez.library.model.Book;
import com.yasinsez.library.model.Category;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

@ApplicationScoped
public class CatalogingService {

    private static final Logger logger = Logger.getLogger(CatalogingService.class.getName());

    @Inject
    private BookRepository bookRepository;

    public boolean verifyISBN(String isbn) {
        return isbn != null && (isbn.length() == 10 || isbn.length() == 13);
    }

    @Transactional
    public Book addBookToCatalog(Book book) {
        try {
            if (book == null) {
                throw new IllegalArgumentException("Book cannot be null");
            }

            if (book.getIsbn() != null && bookRepository.find("isbn", book.getIsbn()).count() > 0) {
                throw new IllegalArgumentException("Book with ISBN " + book.getIsbn() + " already exists");
            }

            bookRepository.persist(book);
            logger.info("Book added to catalog: " + book.getTitle());
            return book;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to add book to catalog", e);
            throw new RuntimeException("Failed to add book to catalog", e);
        }
    }

    @Transactional
    public Book updateBookInCatalog(Book book) {
        try {
            if (book == null || book.getId() == null) {
                throw new IllegalArgumentException("Book and book ID cannot be null");
            }

            Book existingBook = bookRepository.findById(book.getId());
            if (existingBook == null) {
                throw new IllegalArgumentException("Book not found with ID: " + book.getId());
            }

            Book updatedBook = bookRepository.getEntityManager().merge(book);
            logger.info("Book updated in catalog: " + book.getTitle());
            return updatedBook;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to update book in catalog", e);
            throw new RuntimeException("Failed to update book in catalog", e);
        }
    }

    @Transactional
    public void removeBookFromCatalog(Long bookId) {
        try {
            if (bookId == null) {
                throw new IllegalArgumentException("Book ID cannot be null");
            }

            boolean deleted = bookRepository.deleteById(bookId);
            if (!deleted) {
                throw new IllegalArgumentException("Book not found with ID: " + bookId);
            }

            logger.info("Book removed from catalog with ID: " + bookId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to remove book from catalog", e);
            throw new RuntimeException("Failed to remove book from catalog", e);
        }
    }

    public static class CatalogingException extends Exception {
        public CatalogingException(String message) {
            super(message);
        }

        public CatalogingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public void assignCategories(Long bookId, List<Category> categories) {
    }
}
