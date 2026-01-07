package com.yasinsez.library.service;

import com.yasinsez.library.model.Publisher;
import com.yasinsez.library.repository.PublisherRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class PublisherService {

    @Inject
    PublisherRepository publisherRepository;

    public List<Publisher> getAllPublishers() {
        return publisherRepository.listAll();
    }
}
