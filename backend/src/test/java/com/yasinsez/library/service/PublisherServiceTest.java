package com.yasinsez.library.service;

import com.yasinsez.library.model.Publisher;
import com.yasinsez.library.repository.PublisherRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@QuarkusTest
class PublisherServiceTest {

    @Inject
    PublisherService publisherService;

    @InjectMock
    PublisherRepository publisherRepository;

    @Test
    void getAllPublishers_shouldReturnListOfPublishers() {
        Publisher publisher = new Publisher();
        publisher.setId(1L);
        publisher.setName("Test Publisher");

        when(publisherRepository.listAll()).thenReturn(Collections.singletonList(publisher));

        List<Publisher> publishers = publisherService.getAllPublishers();

        assertEquals(1, publishers.size());
        assertEquals("Test Publisher", publishers.get(0).getName());
    }
}
