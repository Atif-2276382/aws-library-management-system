import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';
import { useAuth } from '../context/AuthContext';

const emptyBook = { title: '', isbn: '', authorId: '', genre: '', availability: true };

export default function BooksPage() {
  const { user } = useAuth();
  const isLibrarian = user?.role === 'LIBRARIAN';
  const [books, setBooks] = useState([]);
  const [authors, setAuthors] = useState([]);
  const [search, setSearch] = useState('');
  const [form, setForm] = useState(emptyBook);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');

  const load = async () => {
    const query = search ? `?search=${encodeURIComponent(search)}` : '';
    setBooks(await apiRequest(`/api/books${query}`));
    if (isLibrarian) {
      setAuthors(await apiRequest('/api/authors'));
    }
  };

  useEffect(() => { load().catch((e) => setError(e.message)); }, [search]);

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const payload = { ...form, authorId: Number(form.authorId), availability: form.availability === true || form.availability === 'true' };
    try {
      if (editingId) {
        await apiRequest(`/api/books/${editingId}`, { method: 'PUT', body: JSON.stringify(payload) });
      } else {
        await apiRequest('/api/books', { method: 'POST', body: JSON.stringify(payload) });
      }
      setForm(emptyBook);
      setEditingId(null);
      await load();
    } catch (err) {
      setError(err.message);
    }
  };

  const onEdit = (book) => {
    setEditingId(book.bookId);
    setForm({ title: book.title, isbn: book.isbn, authorId: book.authorId, genre: book.genre, availability: book.availability });
  };

  const onDelete = async (id) => {
    await apiRequest(`/api/books/${id}`, { method: 'DELETE' });
    await load();
  };

  return (
    <section>
      <h1>Book Management</h1>
      <input className="search" placeholder="Search by title or ISBN" value={search} onChange={(e) => setSearch(e.target.value)} />
      {error && <p className="error">{error}</p>}
      {isLibrarian && (
        <form className="card form-grid" onSubmit={onSubmit}>
          <h2>{editingId ? 'Update Book' : 'Add Book'}</h2>
          <input placeholder="Title" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required />
          <input placeholder="ISBN" value={form.isbn} onChange={(e) => setForm({ ...form, isbn: e.target.value })} required />
          <select value={form.authorId} onChange={(e) => setForm({ ...form, authorId: e.target.value })} required>
            <option value="">Select Author</option>
            {authors.map((a) => <option key={a.authorId} value={a.authorId}>{a.name}</option>)}
          </select>
          <input placeholder="Genre" value={form.genre} onChange={(e) => setForm({ ...form, genre: e.target.value })} />
          <select value={form.availability} onChange={(e) => setForm({ ...form, availability: e.target.value })}>
            <option value="true">Available</option>
            <option value="false">Unavailable</option>
          </select>
          <button type="submit">{editingId ? 'Update' : 'Add'}</button>
        </form>
      )}
      <div className="table-wrap">
        <table>
          <thead><tr><th>Title</th><th>ISBN</th><th>Author</th><th>Genre</th><th>Available</th>{isLibrarian && <th>Actions</th>}</tr></thead>
          <tbody>
            {books.map((b) => (
              <tr key={b.bookId}>
                <td>{b.title}</td><td>{b.isbn}</td><td>{b.authorName}</td><td>{b.genre}</td>
                <td>{b.availability ? 'Yes' : 'No'}</td>
                {isLibrarian && (
                  <td className="actions">
                    <button onClick={() => onEdit(b)}>Edit</button>
                    <button onClick={() => onDelete(b.bookId)}>Delete</button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
