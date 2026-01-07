package com.yasinsez.library.service;

import com.yasinsez.library.model.Author;
import com.yasinsez.library.repository.AuthorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

import com.yasinsez.library.dto.AuthorDTO;
import com.yasinsez.library.mapper.AuthorMapper;

@ApplicationScoped
public class AuthorService {

    @Inject
    AuthorRepository authorRepository;

    @Transactional
    public Author createAuthor(AuthorDTO authorDTO) {
        var author = AuthorMapper.toEntity(authorDTO);
        authorRepository.persist(author);
        return author;
    }

    @Transactional
    public Author updateAuthor(Long id, AuthorDTO authorDTO) {
        var author = authorRepository.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException("Author not found"));
        author.setName(authorDTO.name);
        author.setBiography(authorDTO.biography);
        author.setBirthDate(authorDTO.dateOfBirth);
        return author;
    }

    @Transactional
    public void deleteAuthor(Long id) {
        var author = authorRepository.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException("Author not found"));
        authorRepository.delete(author);
    }

    public List<Author> getAllAuthors() {
        return authorRepository.listAll();
    }
}
