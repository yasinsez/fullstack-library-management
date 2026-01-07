package com.yasinsez.library.service;

import com.yasinsez.library.model.Book;
import com.yasinsez.library.model.Category;
import com.yasinsez.library.repository.BookRepository;
import com.yasinsez.library.repository.BookCopyRepository;
import com.yasinsez.library.repository.CategoryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.yasinsez.library.dto.BookRequestDTO;
import com.yasinsez.library.mapper.BookMapper;
import com.yasinsez.library.model.Author;
import com.yasinsez.library.model.Publisher;
import com.yasinsez.library.repository.AuthorRepository;
import com.yasinsez.library.repository.PublisherRepository;
import com.yasinsez.library.model.BookCopy;
import com.yasinsez.library.model.enums.CopyStatus;

@ApplicationScoped
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    @Inject
    BookRepository bookRepository;

    @Inject
    BookCopyRepository bookCopyRepository;

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    AuthorRepository authorRepository;

    @Inject
    PublisherRepository publisherRepository;

    @Transactional
    public Book createBook(BookRequestDTO bookRequestDTO) {
        log.debug("Creating a new book with title: {}", bookRequestDTO.title());

        bookRepository.findByIsbn(bookRequestDTO.isbn()).ifPresent(book -> {
            throw new IllegalArgumentException("Book with ISBN " + bookRequestDTO.isbn() + " already exists.");
        });

        Book book = BookMapper.toEntity(bookRequestDTO);

        Publisher publisher = publisherRepository.findByIdOptional(bookRequestDTO.publisherId())
                .orElseThrow(
                        () -> new NotFoundException("Publisher not found with ID: " + bookRequestDTO.publisherId()));
        book.setPublisher(publisher);

        Category category = categoryRepository.findByIdOptional(bookRequestDTO.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with ID: " + bookRequestDTO.categoryId()));
        book.setCategory(category);

        if (bookRequestDTO.authorIds() != null && !bookRequestDTO.authorIds().isEmpty()) {
            Set<Author> authors = bookRequestDTO.authorIds().stream()
                    .map(authorId -> authorRepository.findByIdOptional(authorId)
                            .orElseThrow(() -> new NotFoundException("Author not found with ID: " + authorId)))
                    .collect(Collectors.toSet());
            book.setAuthors(authors);
        }

        bookRepository.persist(book);

        for (int i = 0; i < bookRequestDTO.stock(); i++) {
            BookCopy bookCopy = new BookCopy();
            bookCopy.setBook(book);
            bookCopy.setStatus(CopyStatus.AVAILABLE);
            bookCopyRepository.persist(bookCopy);
            // Keep owning side collection in sync so DTO 'stock' reflects immediately
            if (book.getCopies() != null) {
                book.getCopies().add(bookCopy);
            }
        }

        log.info("Successfully created book with ID: {}", book.getId());
        return book;
    }

    @Transactional
    public Book updateBook(Long bookId, BookRequestDTO bookRequestDTO) {
        log.debug("Updating book: {}", bookId);
        Book existingBook = bookRepository.findByIdOptional(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found with ID: " + bookId));

        existingBook.setTitle(bookRequestDTO.title());
        existingBook.setIsbn(bookRequestDTO.isbn());
        existingBook.setPublicationDate(bookRequestDTO.publicationDate());
        existingBook.setGenre(bookRequestDTO.genre());
        existingBook.setLanguage(bookRequestDTO.language());
        existingBook.setCoverImageUrl(bookRequestDTO.coverImageUrl());

        Publisher publisher = publisherRepository.findByIdOptional(bookRequestDTO.publisherId())
                .orElseThrow(
                        () -> new NotFoundException("Publisher not found with ID: " + bookRequestDTO.publisherId()));
        existingBook.setPublisher(publisher);

        Category category = categoryRepository.findByIdOptional(bookRequestDTO.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with ID: " + bookRequestDTO.categoryId()));
        existingBook.setCategory(category);

        if (bookRequestDTO.authorIds() != null && !bookRequestDTO.authorIds().isEmpty()) {
            Set<Author> authors = bookRequestDTO.authorIds().stream()
                    .map(authorId -> authorRepository.findByIdOptional(authorId)
                            .orElseThrow(() -> new NotFoundException("Author not found with ID: " + authorId)))
                    .collect(Collectors.toSet());
            existingBook.setAuthors(authors);
        }

        int currentStock = existingBook.getCopies().size();
        int newStock = bookRequestDTO.stock();

        if (newStock > currentStock) {
            for (int i = 0; i < newStock - currentStock; i++) {
                BookCopy bookCopy = new BookCopy();
                bookCopy.setBook(existingBook);
                bookCopy.setStatus(CopyStatus.AVAILABLE);
                bookCopyRepository.persist(bookCopy);
                if (existingBook.getCopies() != null) {
                    existingBook.getCopies().add(bookCopy);
                }
            }
        } else if (newStock < currentStock) {
            int toRemove = currentStock - newStock;
            // Remove only AVAILABLE copies, preferring the most recently created ones
            List<BookCopy> removable = existingBook.getCopies().stream()
                    .filter(c -> c.getStatus() == CopyStatus.AVAILABLE)
                    .sorted((a, b) -> {
                        if (a.getCreatedAt() == null && b.getCreatedAt() == null)
                            return 0;
                        if (a.getCreatedAt() == null)
                            return 1;
                        if (b.getCreatedAt() == null)
                            return -1;
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    })
                    .limit(toRemove)
                    .collect(Collectors.toList());

            if (removable.size() < toRemove) {
                throw new BadRequestException("Cannot reduce stock by " + toRemove
                        + ". Only " + removable.size() + " available copies can be removed.");
            }

            // Use orphanRemoval by removing from the collection
            for (BookCopy copy : removable) {
                existingBook.getCopies().remove(copy);
            }
        }

        return existingBook;
    }

    @Transactional
    public void deleteBook(Long bookId) {
        log.debug("Deleting book: {}", bookId);
        Book book = bookRepository.findByIdOptional(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found with ID: " + bookId));

        // Rely on JPA cascade + orphanRemoval defined on Book.copies to remove children
        // Avoid deleting copies directly to prevent stale state/optimistic lock issues
        bookRepository.delete(book);
    }

    public List<Book> getAllBooks() {
        log.debug("Fetching all books");
        return bookRepository.listAll();
    }

    public Optional<Book> getBookById(Long bookId) {
        log.debug("Fetching book by ID: {}", bookId);
        return bookRepository.findByIdOptional(bookId);
    }

    public List<Book> searchBooks(String query) {
        log.debug("Searching books with query: {}", query);
        return bookRepository.search(query);
    }

    public boolean isBookAvailable(Long bookId) {
        return bookCopyRepository.count("book.id = ?1 and status = 'AVAILABLE'", bookId) > 0;
    }

    public List<Book> getBooksByCategory(Long categoryId) {
        Category category = categoryRepository.findByIdOptional(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        return bookRepository.findByCategory(category);
    }
}
