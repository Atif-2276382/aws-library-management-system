package com.library.service;

import com.library.dto.AuthorDtos;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthorService {

    private static final Logger log = LoggerFactory.getLogger(AuthorService.class);
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    public List<AuthorDtos.AuthorResponse> findAll(String search) {
        List<Author> authors = search == null || search.isBlank()
                ? authorRepository.findAll()
                : authorRepository.findByNameContainingIgnoreCase(search);
        return authors.stream().map(this::toResponse).toList();
    }

    public AuthorDtos.AuthorResponse findById(Integer id) {
        return toResponse(getAuthor(id));
    }

    @Transactional
    public AuthorDtos.AuthorResponse create(AuthorDtos.AuthorRequest request) {
        Author author = new Author();
        author.setName(request.name());
        Author saved = authorRepository.save(author);
        log.info("Created author id={} name={}", saved.getAuthorId(), saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public AuthorDtos.AuthorResponse update(Integer id, AuthorDtos.AuthorRequest request) {
        Author author = getAuthor(id);
        author.setName(request.name());
        return toResponse(authorRepository.save(author));
    }

    @Transactional
    public void delete(Integer id) {
        authorRepository.delete(getAuthor(id));
        log.info("Deleted author id={}", id);
    }

    private Author getAuthor(Integer id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));
    }

    private AuthorDtos.AuthorResponse toResponse(Author author) {
        List<String> linked = bookRepository.findAll().stream()
                .filter(b -> b.getAuthor().getAuthorId().equals(author.getAuthorId()))
                .map(Book::getTitle)
                .toList();
        return new AuthorDtos.AuthorResponse(author.getAuthorId(), author.getName(), linked);
    }
}
