package com.yasinsez.library.mapper;

import com.yasinsez.library.dto.BookResponseDTO;
import com.yasinsez.library.dto.ShallowBookResponseDTO;
import com.yasinsez.library.model.Book;
import com.yasinsez.library.dto.BookRequestDTO;
import com.yasinsez.library.model.Author;

import java.util.stream.Collectors;

public class BookMapper {

    public static BookResponseDTO toResponseDTO(Book book, boolean isAvailable) {
        BookResponseDTO dto = new BookResponseDTO();
        dto.id = book.getId();
        dto.title = book.getTitle();
        dto.authorName = book.getAuthors() == null ? ""
                : book.getAuthors().stream()
                        .map(author -> author.getName())
                        .collect(Collectors.joining(", "));
        dto.isbn = book.getIsbn();
        dto.publicationYear = book.getPublicationYear() == null ? 0 : book.getPublicationYear();
        dto.description = book.getDescription();
        dto.isAvailable = isAvailable;
        dto.publisher = (book.getPublisher() == null) ? null : book.getPublisher().getName();
        dto.publicationDate = book.getPublicationDate();
        dto.genre = book.getGenre();
        dto.language = book.getLanguage();
        dto.coverImageUrl = book.getCoverImageUrl();
        // expose stock and authors for admin UI needs
        dto.stock = (book.getCopies() == null) ? 0 : book.getCopies().size();
        dto.authors = (book.getAuthors() == null) ? java.util.List.of()
                : book.getAuthors().stream().map(AuthorMapper::toResponseDTO)
                        .collect(java.util.stream.Collectors.toList());
        return dto;
    }

    public static BookResponseDTO toResponseDTO(Book book) {
        return toResponseDTO(book, false);
    }

    public static ShallowBookResponseDTO toShallowResponseDTO(Book book) {
        if (book == null) {
            return null;
        }
        return new ShallowBookResponseDTO(book.getId(), book.getTitle());
    }

    public static Book toEntity(BookRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Book book = new Book();
        book.setTitle(dto.title());
        book.setIsbn(dto.isbn());
        book.setPublicationDate(dto.publicationDate());
        book.setGenre(dto.genre());
        book.setLanguage(dto.language());
        book.setCoverImageUrl(dto.coverImageUrl());

        return book;
    }
}
