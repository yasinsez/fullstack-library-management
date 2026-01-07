package com.yasinsez.library.service;

import com.yasinsez.library.model.Author;
import com.yasinsez.library.repository.AuthorRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@QuarkusTest
class AuthorServiceTest {

    @Inject
    AuthorService authorService;

    @InjectMock
    AuthorRepository authorRepository;

    @Test
    void getAllAuthors_shouldReturnListOfAuthors() {
        Author author = new Author();
        author.setId(1L);
        author.setName("Test Author");

        when(authorRepository.listAll()).thenReturn(Collections.singletonList(author));

        List<Author> authors = authorService.getAllAuthors();

        assertEquals(1, authors.size());
        assertEquals("Test Author", authors.get(0).getName());
    }
}
