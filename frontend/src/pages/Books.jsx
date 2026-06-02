import { useEffect, useState } from "react";

import {
    getBooks,
    createBook,
    updateBook,
    deleteBook
} from "../services/bookService";

import {
    getAuthors
} from "../services/authorService";

function Books() {

    const [books, setBooks] = useState([]);

    const [authors, setAuthors] =
        useState([]);

    const [editingId, setEditingId] =
        useState(null);

    const [form, setForm] = useState({

        title: "",
        isbn: "",
        genre: "",
        available: true,
        authorId: ""
    });

    useEffect(() => {

        loadBooks();

        loadAuthors();

    }, []);

    const loadBooks = async () => {

        try {

            const response =
                await getBooks();

            setBooks(
                response.data
            );

        } catch (error) {

            console.error(error);
        }
    };

    const loadAuthors = async () => {

        try {

            const response =
                await getAuthors();

            setAuthors(
                response.data
            );

        } catch (error) {

            console.error(error);
        }
    };

    const handleChange = (e) => {

        setForm({

            ...form,

            [e.target.name]:
                e.target.value
        });
    };

    const handleSubmit = async (e) => {

        e.preventDefault();

        try {

const payload = {

    title: form.title,

    isbn: form.isbn,

    genre: form.genre,

    available: form.available,

    author: {

        id:
            Number(
                form.authorId
            )
    }
};

console.log(payload);

            if (editingId) {

                await updateBook(
                    editingId,
                    payload
                );

            } else {

                await createBook(
                    payload
                );
            }

            resetForm();

            loadBooks();

        } catch (error) {

            console.error(error);
        }
    };

    const handleEdit = (book) => {

        setEditingId(book.id);

        setForm({

            title:
                book.title,

            isbn:
                book.isbn,

            genre:
                book.genre,

            available:
                book.available,

            authorId:
                book.author?.id
        });
    };

    const handleDelete = async (id) => {

        try {

            await deleteBook(id);

            loadBooks();

        } catch (error) {

            console.error(error);
        }
    };

    const resetForm = () => {

        setEditingId(null);

        setForm({

            title: "",
            isbn: "",
            genre: "",
            available: true,
            authorId: ""
        });
    };

    return (

        <div className="container mt-4">

            <h2>
                Book Management
            </h2>

            <form
                onSubmit={handleSubmit}
                className="mb-4"
            >

                <input
                    type="text"
                    name="title"
                    placeholder="Title"
                    className="form-control mb-2"
                    value={form.title}
                    onChange={handleChange}
                />

                <input
                    type="text"
                    name="isbn"
                    placeholder="ISBN"
                    className="form-control mb-2"
                    value={form.isbn}
                    onChange={handleChange}
                />

                <input
                    type="text"
                    name="genre"
                    placeholder="Genre"
                    className="form-control mb-2"
                    value={form.genre}
                    onChange={handleChange}
                />

                <select
                    name="authorId"
                    className=
                    "form-control mb-3"

                    value={
                        form.authorId
                    }

                    onChange={
                        handleChange
                    }
                >

                    <option value="">

                        Select Author

                    </option>

                    {
                        authors.map(
                            author => (

                                <option
                                    key={
                                        author.id
                                    }

                                    value={
                                        author.id
                                    }
                                >

                                    {
                                        author.name
                                    }

                                </option>
                            )
                        )
                    }

                </select>

                <button
                    className=
                    "btn btn-primary"
                >

                    {
                        editingId
                            ? "Update Book"
                            : "Add Book"
                    }

                </button>

            </form>

            <table
                className=
                "table table-bordered"
            >

                <thead>

                <tr>

                    <th>ID</th>

                    <th>Title</th>

                    <th>ISBN</th>

                    <th>Genre</th>

                    <th>Author</th>

                    <th>Available</th>

                    <th>Actions</th>

                </tr>

                </thead>

                <tbody>

                {

                    books.map(book => (

                        <tr
                            key={book.id}
                        >

                            <td>
                                {book.id}
                            </td>

                            <td>
                                {book.title}
                            </td>

                            <td>
                                {book.isbn}
                            </td>

                            <td>
                                {book.genre}
                            </td>

                            <td>

                                {
                                    book.author?.name
                                }

                            </td>

                            <td>

                                {
                                    book.available
                                        ? "Yes"
                                        : "No"
                                }

                            </td>

                            <td>

                                <button
                                    className=
                                    "btn btn-warning btn-sm me-2"

                                    onClick={() =>
                                        handleEdit(
                                            book
                                        )
                                    }
                                >

                                    Edit

                                </button>

                                <button
                                    className=
                                    "btn btn-danger btn-sm"

                                    onClick={() =>
                                        handleDelete(
                                            book.id
                                        )
                                    }
                                >

                                    Delete

                                </button>

                            </td>

                        </tr>
                    ))
                }

                </tbody>

            </table>

        </div>
    );
}

export default Books;