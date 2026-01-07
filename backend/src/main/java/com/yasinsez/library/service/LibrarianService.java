package com.yasinsez.library.service;

import com.yasinsez.library.dto.BookRequestDTO;
import com.yasinsez.library.dto.LoanResponseDTO;
import com.yasinsez.library.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@ApplicationScoped
public class LibrarianService {

    private static final Logger log = LoggerFactory.getLogger(LibrarianService.class);

    @Inject
    private BookService bookService;

    @Inject
    private UserService userService;

    @Inject
    private LoanService loanService;

    @Inject
    private InventoryService inventoryService;

    @Transactional
    public Book addBookToCatalog(BookRequestDTO bookRequestDTO) {
        log.info("Librarian adding book to catalog: {}", bookRequestDTO.title());
        return bookService.createBook(bookRequestDTO);
    }

    @Transactional
    public Book updateBookInCatalog(Long bookId, BookRequestDTO bookDetails) {
        log.info("Librarian updating book in catalog: {}", bookId);
        return bookService.updateBook(bookId, bookDetails);
    }

    @Transactional
    public void removeBookFromCatalog(Long bookId) {
        log.info("Librarian removing book from catalog: {}", bookId);
        bookService.deleteBook(bookId);
    }

    @Transactional
    public User registerMember(User user, String password) {
        log.info("Librarian registering new member: {}", user.getUsername());
        return userService.createUser(user, password, Set.of(2L));
    }

    @Transactional
    public LoanResponseDTO checkoutBookForMember(Long bookId, Long userId) {
        log.info("Librarian checking out book {} for member {}", bookId, userId);
        return loanService.checkoutBook(bookId, userId);
    }

    @Transactional
    public void returnBookForMember(Long loanId) {
        log.info("Librarian returning book for loan {}", loanId);
        loanService.returnBook(loanId);
    }
}
