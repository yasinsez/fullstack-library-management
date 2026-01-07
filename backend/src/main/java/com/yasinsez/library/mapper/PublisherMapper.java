package com.yasinsez.library.mapper;

import com.yasinsez.library.dto.PublisherDTO;
import com.yasinsez.library.model.Publisher;

public class PublisherMapper {

    public static PublisherDTO toDTO(Publisher publisher) {
        if (publisher == null) {
            return null;
        }
        return new PublisherDTO(
                publisher.getId(),
                publisher.getName());
    }

    public static Publisher toEntity(PublisherDTO publisherDTO) {
        if (publisherDTO == null) {
            return null;
        }
        Publisher publisher = new Publisher();
        publisher.setId(publisherDTO.id);
        publisher.setName(publisherDTO.name);
        return publisher;
    }
}
