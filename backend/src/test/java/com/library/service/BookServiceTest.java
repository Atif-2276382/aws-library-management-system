package com.library.service;

import com.library.dto.BookDtos;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.exception.BusinessException;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private AuthorRepository authorRepository;
    @InjectMocks
    private BookService bookService;

    private Author author;

    @BeforeEach
    void setUp() {
        author = new Author();
        author.setAuthorId(1);
        author.setName("Author One");
    }

    @Test
    void createBook_success() {
        BookDtos.BookRequest request = new BookDtos.BookRequest("Title", "ISBN-1", 1, "Fiction", true);
        when(bookRepository.existsByIsbn("ISBN-1")).thenReturn(false);
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setBookId(10);
            return b;
        });

        BookDtos.BookResponse response = bookService.create(request);
        assertEquals("Title", response.title());
        assertEquals("ISBN-1", response.isbn());
    }

    @Test
    void createBook_duplicateIsbn_throws() {
        BookDtos.BookRequest request = new BookDtos.BookRequest("Title", "ISBN-1", 1, "Fiction", true);
        when(bookRepository.existsByIsbn("ISBN-1")).thenReturn(true);
        assertThrows(BusinessException.class, () -> bookService.create(request));
    }
}
