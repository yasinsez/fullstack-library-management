package com.yasinsez.library.mapper;

import com.yasinsez.library.dto.LibrarianResponseDTO;
import com.yasinsez.library.model.Librarian;

public class LibrarianMapper {

    public static LibrarianResponseDTO toResponseDTO(Librarian librarian) {
        if (librarian == null) {
            return null;
        }
        return new LibrarianResponseDTO(
                librarian.getId(),
                UserMapper.toResponseDTO(librarian.getUser()),
                librarian.getEmploymentDate(),
                librarian.getEmployeeId());
    }
}
