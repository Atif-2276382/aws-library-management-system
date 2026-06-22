package com.library.controller;

import com.library.dto.BookDtos;
import com.library.service.BookService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private static final Logger log = LoggerFactory.getLogger(BookController.class);
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookDtos.BookResponse> getAll(@RequestParam(required = false) String search) {
        log.debug("Fetching books list search={}", search);
        return bookService.findAll(search);
    }

    @GetMapping("/{id}")
    public BookDtos.BookResponse getById(@PathVariable Integer id) {
        log.debug("Fetching book by id={}", id);
        return bookService.findById(id);
    }

    @PostMapping
    public ResponseEntity<BookDtos.BookResponse> create(@Valid @RequestBody BookDtos.BookRequest request) {
        log.info("Creating book title={} isbn={}", request.title(), request.isbn());
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request));
    }

    @PutMapping("/{id}")
    public BookDtos.BookResponse update(@PathVariable Integer id, @Valid @RequestBody BookDtos.BookRequest request) {
        log.info("Updating book id={} title={} isbn={}", id, request.title(), request.isbn());
        return bookService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        log.info("Deleting book id={}", id);
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
