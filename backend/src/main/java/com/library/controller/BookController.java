package com.library.controller;

import com.library.dto.BookDtos;
import com.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookDtos.BookResponse> getAll(@RequestParam(required = false) String search) {
        return bookService.findAll(search);
    }

    @GetMapping("/{id}")
    public BookDtos.BookResponse getById(@PathVariable Integer id) {
        return bookService.findById(id);
    }

    @PostMapping
    public ResponseEntity<BookDtos.BookResponse> create(@Valid @RequestBody BookDtos.BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request));
    }

    @PutMapping("/{id}")
    public BookDtos.BookResponse update(@PathVariable Integer id, @Valid @RequestBody BookDtos.BookRequest request) {
        return bookService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
