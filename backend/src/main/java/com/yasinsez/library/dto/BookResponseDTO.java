package com.yasinsez.library.dto;

import java.time.LocalDate;
import java.util.List;

public class BookResponseDTO {
    public Long id;
    public String title;
    public String authorName;
    public String isbn;
    public int publicationYear;
    public String description;
    public boolean isAvailable;
    public String publisher;
    public LocalDate publicationDate;
    public String genre;
    public String language;
    public String coverImageUrl;
    public Integer stock;
    public List<AuthorResponseDTO> authors;

    public Long id() {
        return id;
    }
}
