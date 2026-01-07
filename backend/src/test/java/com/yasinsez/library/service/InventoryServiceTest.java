package com.yasinsez.library.service;

import com.yasinsez.library.model.Book;
import com.yasinsez.library.model.BookCopy;
import com.yasinsez.library.model.enums.CopyStatus;
import com.yasinsez.library.repository.BookCopyRepository;
import com.yasinsez.library.repository.BookRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
public class InventoryServiceTest {

    @Inject
    InventoryService inventoryService;

    @InjectMock
    BookRepository bookRepository;

    @InjectMock
    BookCopyRepository bookCopyRepository;

    private Book book;
    private BookCopy bookCopy;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(1L);

        bookCopy = new BookCopy();
        bookCopy.setId(1L);
        bookCopy.setBook(book);
        bookCopy.setStatus(CopyStatus.AVAILABLE);
    }

    @Test
    void addBookCopy_Success() {
        when(bookRepository.findByIdOptional(anyLong())).thenReturn(Optional.of(book));
        inventoryService.addBookCopy(1L, "Shelf A");

        ArgumentCaptor<BookCopy> bookCopyCaptor = ArgumentCaptor.forClass(BookCopy.class);
        verify(bookCopyRepository).persist(bookCopyCaptor.capture());

        BookCopy persistedCopy = bookCopyCaptor.getValue();
        assertEquals(book, persistedCopy.getBook());
        assertEquals("Shelf A", persistedCopy.getLocation());
        assertEquals(CopyStatus.AVAILABLE, persistedCopy.getStatus());
    }

    @Test
    void addBookCopy_BookNotFound() {
        when(bookRepository.findByIdOptional(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> inventoryService.addBookCopy(1L, "Shelf A"));
    }

    @Test
    void removeBookCopy_Success() {
        when(bookCopyRepository.deleteById(anyLong())).thenReturn(true);
        assertDoesNotThrow(() -> inventoryService.removeBookCopy(1L));
    }

    @Test
    void removeBookCopy_NotFound() {
        when(bookCopyRepository.deleteById(anyLong())).thenReturn(false);
        assertThrows(NotFoundException.class, () -> inventoryService.removeBookCopy(1L));
    }

    @Test
    void updateBookCopyStatus_Success() {
        when(bookCopyRepository.findByIdOptional(anyLong())).thenReturn(Optional.of(bookCopy));
        BookCopy updatedCopy = inventoryService.updateBookCopyStatus(1L, CopyStatus.CHECKED_OUT);
        assertEquals(CopyStatus.CHECKED_OUT, updatedCopy.getStatus());
    }

    @Test
    void updateBookCopyStatus_NotFound() {
        when(bookCopyRepository.findByIdOptional(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> inventoryService.updateBookCopyStatus(1L, CopyStatus.CHECKED_OUT));
    }

    @Test
    void getCopiesForBook_Success() {
        when(bookRepository.findByIdOptional(anyLong())).thenReturn(Optional.of(book));
        when(bookCopyRepository.findByBook(any(Book.class))).thenReturn(Collections.singletonList(bookCopy));

        List<BookCopy> copies = inventoryService.getCopiesForBook(1L);

        assertFalse(copies.isEmpty());
        assertEquals(1, copies.size());
        assertEquals(bookCopy, copies.get(0));
    }

    @Test
    void getCopiesForBook_BookNotFound() {
        when(bookRepository.findByIdOptional(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> inventoryService.getCopiesForBook(1L));
    }

    @Test
    void getAvailableBooks_Success() {
        when(bookRepository.findAvailableBooks()).thenReturn(Collections.singletonList(book));
        List<Book> books = inventoryService.getAvailableBooks();
        assertFalse(books.isEmpty());
        assertEquals(1, books.size());
    }
}

