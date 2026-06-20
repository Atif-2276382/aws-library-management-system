package com.library.controller;

import com.library.dto.AuthorDtos;
import com.library.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public List<AuthorDtos.AuthorResponse> getAll(@RequestParam(required = false) String search) {
        return authorService.findAll(search);
    }

    @GetMapping("/{id}")
    public AuthorDtos.AuthorResponse getById(@PathVariable Integer id) {
        return authorService.findById(id);
    }

    @PostMapping
    public ResponseEntity<AuthorDtos.AuthorResponse> create(@Valid @RequestBody AuthorDtos.AuthorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authorService.create(request));
    }

    @PutMapping("/{id}")
    public AuthorDtos.AuthorResponse update(@PathVariable Integer id, @Valid @RequestBody AuthorDtos.AuthorRequest request) {
        return authorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
