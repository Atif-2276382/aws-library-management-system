package com.library.service;

import com.library.dto.AuthorDtos;
import com.library.entity.Author;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private BookRepository bookRepository;
    @InjectMocks
    private AuthorService authorService;

    @Test
    void findById_success() {
        Author author = new Author();
        author.setAuthorId(1);
        author.setName("Jane");
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));
        when(bookRepository.findAll()).thenReturn(List.of());

        AuthorDtos.AuthorResponse response = authorService.findById(1);
        assertEquals("Jane", response.name());
    }
}
