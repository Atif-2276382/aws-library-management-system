import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';

export default function AuthorsPage() {
  const [authors, setAuthors] = useState([]);
  const [search, setSearch] = useState('');
  const [name, setName] = useState('');
  const [editingId, setEditingId] = useState(null);

  const load = async () => {
    const query = search ? `?search=${encodeURIComponent(search)}` : '';
    setAuthors(await apiRequest(`/api/authors${query}`));
  };

  useEffect(() => { load(); }, [search]);

  const onSubmit = async (e) => {
    e.preventDefault();
    if (editingId) {
      await apiRequest(`/api/authors/${editingId}`, { method: 'PUT', body: JSON.stringify({ name }) });
    } else {
      await apiRequest('/api/authors', { method: 'POST', body: JSON.stringify({ name }) });
    }
    setName('');
    setEditingId(null);
    await load();
  };

  return (
    <section>
      <h1>Author Management</h1>
      <input className="search" placeholder="Search authors" value={search} onChange={(e) => setSearch(e.target.value)} />
      <form className="card form-grid" onSubmit={onSubmit}>
        <input placeholder="Author name" value={name} onChange={(e) => setName(e.target.value)} required />
        <button type="submit">{editingId ? 'Update' : 'Add'} Author</button>
      </form>
      <div className="table-wrap">
        <table>
          <thead><tr><th>Name</th><th>Linked Books</th><th>Actions</th></tr></thead>
          <tbody>
            {authors.map((a) => (
              <tr key={a.authorId}>
                <td>{a.name}</td>
                <td>{(a.linkedBooks || []).join(', ') || '-'}</td>
                <td className="actions">
                  <button onClick={() => { setEditingId(a.authorId); setName(a.name); }}>Edit</button>
                  <button onClick={async () => { await apiRequest(`/api/authors/${a.authorId}`, { method: 'DELETE' }); await load(); }}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
