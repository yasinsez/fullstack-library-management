package com.yasinsez.library.service;

import com.yasinsez.library.model.Book;
import com.yasinsez.library.model.BookCopy;
import com.yasinsez.library.model.enums.CopyStatus;
import com.yasinsez.library.repository.BookCopyRepository;
import com.yasinsez.library.repository.BookRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApplicationScoped
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    @Inject
    BookRepository bookRepository;

    @Inject
    BookCopyRepository bookCopyRepository;

    @Transactional
    public BookCopy addBookCopy(Long bookId, String location) {
        log.info("Adding a new copy for book {}", bookId);
        Book book = bookRepository.findByIdOptional(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found"));

        BookCopy copy = new BookCopy();
        copy.setBook(book);
        copy.setLocation(location);
        copy.setStatus(CopyStatus.AVAILABLE);
        bookCopyRepository.persist(copy);

        return copy;
    }

    @Transactional
    public void removeBookCopy(Long bookCopyId) {
        log.info("Removing book copy {}", bookCopyId);
        boolean deleted = bookCopyRepository.deleteById(bookCopyId);
        if (!deleted) {
            throw new NotFoundException("Book copy not found");
        }
    }

    @Transactional
    public BookCopy updateBookCopyStatus(Long bookCopyId, CopyStatus status) {
        log.info("Updating status of book copy {} to {}", bookCopyId, status);
        BookCopy copy = bookCopyRepository.findByIdOptional(bookCopyId)
                .orElseThrow(() -> new NotFoundException("Book copy not found"));

        copy.setStatus(status);
        return copy;
    }

    public List<BookCopy> getCopiesForBook(Long bookId) {
        Book book = bookRepository.findByIdOptional(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found"));
        return bookCopyRepository.findByBook(book);
    }

    public List<Book> getAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }
}
