import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';

const empty = { name: '', membershipId: '', username: '', password: '' };

export default function MembersPage() {
  const [members, setMembers] = useState([]);
  const [search, setSearch] = useState('');
  const [form, setForm] = useState(empty);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');

  const load = async () => {
    const query = search ? `?search=${encodeURIComponent(search)}` : '';
    setMembers(await apiRequest(`/api/members${query}`));
  };

  useEffect(() => { load().catch((e) => setError(e.message)); }, [search]);

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      if (editingId) {
        await apiRequest(`/api/members/${editingId}`, {
          method: 'PUT',
          body: JSON.stringify({ name: form.name, membershipId: form.membershipId })
        });
      } else {
        await apiRequest('/api/members', { method: 'POST', body: JSON.stringify(form) });
      }
      setForm(empty);
      setEditingId(null);
      await load();
    } catch (err) {
      setError(err.message);
    }
  };

  const onDelete = async (member) => {
    const confirmed = window.confirm(
      `Delete member "${member.name}" (${member.membershipId})? This cannot be undone.`
    );
    if (!confirmed) {
      return;
    }
    setError('');
    try {
      await apiRequest(`/api/members/${member.memberId}`, { method: 'DELETE' });
      await load();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section>
      <h1>Member Management</h1>
      <input className="search" placeholder="Search members" value={search} onChange={(e) => setSearch(e.target.value)} />
      {error && <p className="error">{error}</p>}
      <form className="card form-grid" onSubmit={onSubmit}>
        <input placeholder="Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
        <input placeholder="Membership ID" value={form.membershipId} onChange={(e) => setForm({ ...form, membershipId: e.target.value })} required />
        {!editingId && (
          <>
            <input placeholder="Username" value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} required />
            <input type="password" placeholder="Password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required />
          </>
        )}
        <button type="submit">{editingId ? 'Update' : 'Add'} Member</button>
      </form>
      <div className="table-wrap">
        <table>
          <thead><tr><th>Name</th><th>Membership ID</th><th>Username</th><th>Actions</th></tr></thead>
          <tbody>
            {members.map((m) => (
              <tr key={m.memberId}>
                <td>{m.name}</td><td>{m.membershipId}</td><td>{m.username}</td>
                <td className="actions">
                  <button onClick={() => { setEditingId(m.memberId); setForm({ ...empty, name: m.name, membershipId: m.membershipId }); }}>Edit</button>
                  <button onClick={() => onDelete(m)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
