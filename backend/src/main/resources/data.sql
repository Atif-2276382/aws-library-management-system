INSERT IGNORE INTO authors (AuthorID, Name)
VALUES
    (1, 'George Orwell'),
    (2, 'Harper Lee');

INSERT IGNORE INTO books (BookID, Title, ISBN, AuthorID, Genre, Availability)
VALUES
    (1, '1984', 'ISBN-1984-0001', 1, 'Dystopian', b'1'),
    (2, 'Animal Farm', 'ISBN-AF-0002', 1, 'Political Satire', b'1'),
    (3, 'To Kill a Mockingbird', 'ISBN-TKAM-0003', 2, 'Classic', b'1');
