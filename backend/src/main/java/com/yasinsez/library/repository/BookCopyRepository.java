package com.yasinsez.library.repository;

import com.yasinsez.library.model.Book;
import com.yasinsez.library.model.BookCopy;
import com.yasinsez.library.model.enums.CopyStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BookCopyRepository implements PanacheRepository<BookCopy> {

    public Optional<BookCopy> findAvailableByBook(Book book) {
        return find("book = ?1 and status = ?2", book, CopyStatus.AVAILABLE).firstResultOptional();
    }

    public Optional<BookCopy> findAvailableByBookId(Long bookId) {
        return find("book.id = ?1 and status = ?2", bookId, CopyStatus.AVAILABLE).firstResultOptional();
    }

    public List<BookCopy> findByBook(Book book) {
        return list("book", book);
    }
}
