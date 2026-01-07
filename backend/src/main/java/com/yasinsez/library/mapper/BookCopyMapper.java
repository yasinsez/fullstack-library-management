package com.yasinsez.library.mapper;

import com.yasinsez.library.dto.BookCopyResponseDTO;
import com.yasinsez.library.model.BookCopy;

public class BookCopyMapper {

    public static BookCopyResponseDTO toResponseDTO(BookCopy bookCopy) {
        if (bookCopy == null) {
            return null;
        }
        return new BookCopyResponseDTO(
                bookCopy.getId(),
                BookMapper.toShallowResponseDTO(bookCopy.getBook()),
                bookCopy.getAcquisitionDate(),
                bookCopy.getCondition(),
                bookCopy.getStatus(),
                bookCopy.getLocation(),
                bookCopy.getCreatedAt(),
                bookCopy.getUpdatedAt()
        );
    }
}
