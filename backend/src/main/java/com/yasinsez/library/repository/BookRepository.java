package com.yasinsez.library.repository;

import com.yasinsez.library.model.Book;
import com.yasinsez.library.model.Category;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BookRepository implements PanacheRepository<Book> {

    public Optional<Book> findByIsbn(String isbn) {
        return find("isbn", isbn).firstResultOptional();
    }

    public List<Book> findByTitleContaining(String title) {
        return list("LOWER(title) LIKE LOWER(?1)", "%" + title + "%");
    }

    public List<Book> findByCategory(Category category) {
        return list("category", category);
    }

    public List<Book> findByPublicationYear(int year) {
        return list("publicationYear", year);
    }

    public List<Book> findAvailableBooks() {
        return list("SELECT b FROM Book b JOIN b.copies c WHERE c.status = 'AVAILABLE'");
    }

    public List<Book> search(String query) {
        String searchPattern = "%" + query.toLowerCase() + "%";
        return find("SELECT b from Book b LEFT JOIN b.authors a WHERE " +
                "LOWER(b.title) LIKE :query OR " +
                "LOWER(b.isbn) LIKE :query OR " +
                "LOWER(a.name) LIKE :query",
                Parameters.with("query", searchPattern)).list();
    }

    public List<String> findAllGenres() {
        return getEntityManager().createQuery("SELECT DISTINCT b.genre FROM Book b", String.class).getResultList();
    }

    public List<Book> findByGenre(String genre) {
        return list("genre", genre);
    }
}
