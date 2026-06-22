package com.library.controller;

import com.library.dto.AuthorDtos;
import com.library.service.AuthorService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private static final Logger log = LoggerFactory.getLogger(AuthorController.class);
    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public List<AuthorDtos.AuthorResponse> getAll(@RequestParam(required = false) String search) {
        log.debug("Fetching authors list search={}", search);
        return authorService.findAll(search);
    }

    @GetMapping("/{id}")
    public AuthorDtos.AuthorResponse getById(@PathVariable Integer id) {
        log.debug("Fetching author by id={}", id);
        return authorService.findById(id);
    }

    @PostMapping
    public ResponseEntity<AuthorDtos.AuthorResponse> create(@Valid @RequestBody AuthorDtos.AuthorRequest request) {
        log.info("Creating author name={}", request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(authorService.create(request));
    }

    @PutMapping("/{id}")
    public AuthorDtos.AuthorResponse update(@PathVariable Integer id, @Valid @RequestBody AuthorDtos.AuthorRequest request) {
        log.info("Updating author id={} name={}", id, request.name());
        return authorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        log.info("Deleting author id={}", id);
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
