package com.yasinsez.library.service;

import com.yasinsez.library.model.Book;
import com.yasinsez.library.repository.BookRepository;
import com.yasinsez.library.repository.BookCopyRepository;
import com.yasinsez.library.repository.CategoryRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import jakarta.inject.Inject;
import com.yasinsez.library.dto.BookRequestDTO;
import com.yasinsez.library.model.Category;
import com.yasinsez.library.model.Publisher;
import com.yasinsez.library.model.Author;
import com.yasinsez.library.repository.PublisherRepository;
import com.yasinsez.library.repository.AuthorRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;

@QuarkusTest
class BookServiceTest {

    @InjectMock
    BookRepository bookRepository;

    @InjectMock
    BookCopyRepository bookCopyRepository;

    @InjectMock
    CategoryRepository categoryRepository;

    @Inject
    BookService bookService;

    @InjectMock
    PublisherRepository publisherRepository;

    @InjectMock
    AuthorRepository authorRepository;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setId(1L);
        when(categoryRepository.findByIdOptional(1L)).thenReturn(Optional.of(category));

        Publisher publisher = new Publisher();
        publisher.setId(1L);
        when(publisherRepository.findByIdOptional(1L)).thenReturn(Optional.of(publisher));

        Author author = new Author();
        author.setId(1L);
        when(authorRepository.findByIdOptional(1L)).thenReturn(Optional.of(author));
    }

    @Test
    void testCreateBook() {
        BookRequestDTO requestDTO = new BookRequestDTO("Test Title", "1234567890123", null, "Fiction", "English",
                "cover.jpg", 1L, 1L, Set.of(1L), 10);
        when(bookRepository.findByIsbn("1234567890123")).thenReturn(Optional.empty());

        bookService.createBook(requestDTO);

        verify(bookRepository).persist(any(Book.class));
    }

    @Test
    void testCreateBookDuplicateIsbn() {
        BookRequestDTO requestDTO = new BookRequestDTO("Duplicate ISBN", "duplicate-isbn", null, "Fiction",
                "English", "cover.jpg", 1L, 1L, Set.of(1L), 10);
        when(bookRepository.findByIsbn("duplicate-isbn")).thenReturn(Optional.of(new Book()));

        assertThrows(IllegalArgumentException.class, () -> bookService.createBook(requestDTO));
    }

    @Test
    void testUpdateBook() {
        Book existing = new Book();
        existing.setId(1L);
        existing.setTitle("Old Title");

        BookRequestDTO updated = new BookRequestDTO("New Title", "1234567890123", null, "Fiction", "English",
                "cover.jpg", 1L, 1L, Set.of(1L), 10);

        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(existing));

        Book result = bookService.updateBook(1L, updated);

        assertEquals("New Title", result.getTitle());
    }

    @Test
    void testUpdateBookNotFound() {
        when(bookRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.updateBook(99L,
                new BookRequestDTO("New Title", "1234567890123", null, "Fiction", "English", "cover.jpg", 1L, 1L,
                        Set.of(1L), 10)));
    }

    @Test
    void testDeleteBook() {
        Book book = new Book();
        book.setId(1L);

        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(book));

        bookService.deleteBook(1L);

        // We now rely on JPA cascade + orphanRemoval for copies
        verifyNoInteractions(bookCopyRepository);
        verify(bookRepository).delete(book);
    }

    @Test
    void testGetAllBooks() {
        List<Book> books = new ArrayList<>();
        books.add(new Book());

        when(bookRepository.listAll()).thenReturn(books);

        List<Book> result = bookService.getAllBooks();

        assertEquals(1, result.size());
    }

    @Test
    void testGetBookById() {
        Book book = new Book();
        book.setId(1L);

        when(bookRepository.findByIdOptional(1L)).thenReturn(Optional.of(book));

        Optional<Book> result = bookService.getBookById(1L);

        assertTrue(result.isPresent());
        assertEquals(book, result.get());
    }

    @Test
    void testIsBookAvailable() {
        when(bookCopyRepository.count("book.id = ?1 and status = 'AVAILABLE'", 1L)).thenReturn(1L);

        boolean available = bookService.isBookAvailable(1L);

        assertTrue(available);
    }
}
