package com.yasinsez.library.controller;

import com.yasinsez.library.model.Book;
import com.yasinsez.library.service.BookService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import com.yasinsez.library.dto.BookRequestDTO;
import io.quarkus.test.security.TestSecurity;
import com.yasinsez.library.model.Publisher;
import com.yasinsez.library.model.Author;

@QuarkusTest
class BookControllerTest {

    @InjectMock
    BookService bookService;

    @Test
    @TestSecurity(user = "librarian", roles = { "LIBRARIAN" })
    void testCreateBookEndpoint() {
        BookRequestDTO bookRequestDTO = new BookRequestDTO("New Book", "1234567890123", null, "Fiction", "English",
                "cover.jpg", 1L, 1L, Set.of(1L), 10);

        given()
                .contentType("application/json")
                .body(bookRequestDTO)
                .when()
                .post("/api/books")
                .then()
                .statusCode(201);
    }

    @Test
    void testGetAllBooksEndpoint() {
        given()
                .when().get("/api/books")
                .then()
                .statusCode(200);
    }

    @Test
    void testGetBookByIdEndpoint_found() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setIsbn("1234567890123");
        Publisher publisher = new Publisher();
        publisher.setName("Test Publisher");
        book.setPublisher(publisher);
        Author author = new Author();
        author.setName("Test Author");
        book.setAuthors(Set.of(author));

        when(bookService.getBookById(1L)).thenReturn(Optional.of(book));

        given()
                .when().get("/api/books/1")
                .then()
                .statusCode(200);
    }

    @Test
    void testGetBookByIdEndpoint_notFound() {
        when(bookService.getBookById(anyLong())).thenReturn(Optional.empty());

        given()
                .when().get("/api/books/1")
                .then()
                .statusCode(404);
    }
}
