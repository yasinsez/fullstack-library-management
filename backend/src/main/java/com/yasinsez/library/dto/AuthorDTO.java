package com.yasinsez.library.dto;

import java.time.LocalDate;

public class AuthorDTO {
    public Long id;
    public String name;
    public String biography;
    public LocalDate dateOfBirth;

    public AuthorDTO() {
    }

    public AuthorDTO(Long id, String name, String biography, LocalDate dateOfBirth) {
        this.id = id;
        this.name = name;
        this.biography = biography;
        this.dateOfBirth = dateOfBirth;
    }
}
