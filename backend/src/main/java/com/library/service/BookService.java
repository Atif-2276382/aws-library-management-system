package com.library.service;

import com.library.dto.BookDtos;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    public List<BookDtos.BookResponse> findAll(String search) {
        List<Book> books = search == null || search.isBlank()
                ? bookRepository.findAll()
                : bookRepository.findByTitleContainingIgnoreCaseOrIsbnContainingIgnoreCase(search, search);
        return books.stream().map(this::toResponse).toList();
    }

    public BookDtos.BookResponse findById(Integer id) {
        return toResponse(getBook(id));
    }

    @Transactional
    public BookDtos.BookResponse create(BookDtos.BookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new BusinessException("ISBN must be unique");
        }
        Book book = mapRequest(new Book(), request);
        return toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookDtos.BookResponse update(Integer id, BookDtos.BookRequest request) {
        Book book = getBook(id);
        if (!book.getIsbn().equals(request.isbn()) && bookRepository.existsByIsbn(request.isbn())) {
            throw new BusinessException("ISBN must be unique");
        }
        return toResponse(bookRepository.save(mapRequest(book, request)));
    }

    @Transactional
    public void delete(Integer id) {
        bookRepository.delete(getBook(id));
    }

    private Book mapRequest(Book book, BookDtos.BookRequest request) {
        Author author = authorRepository.findById(request.authorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));
        book.setTitle(request.title());
        book.setIsbn(request.isbn());
        book.setAuthor(author);
        book.setGenre(request.genre());
        if (request.availability() != null) {
            book.setAvailability(request.availability());
        }
        return book;
    }

    private Book getBook(Integer id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
    }

    private BookDtos.BookResponse toResponse(Book book) {
        return new BookDtos.BookResponse(
                book.getBookId(),
                book.getTitle(),
                book.getIsbn(),
                book.getAuthor().getAuthorId(),
                book.getAuthor().getName(),
                book.getGenre(),
                book.isAvailability());
    }
}
