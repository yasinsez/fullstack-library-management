package com.yasinsez.library.repository;

import com.yasinsez.library.model.Publisher;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PublisherRepository implements PanacheRepository<Publisher> {
}
