package com.library.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(unique = true)
    private String isbn;

    private String genre;

    private boolean available = true;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;
}