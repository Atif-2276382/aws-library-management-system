import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';

export default function LendingsPage() {
  const [lendings, setLendings] = useState([]);
  const [books, setBooks] = useState([]);
  const [members, setMembers] = useState([]);
  const [issue, setIssue] = useState({ bookId: '', memberId: '' });

  const load = async () => {
    setLendings(await apiRequest('/api/lendings'));
    setBooks(await apiRequest('/api/books'));
    setMembers(await apiRequest('/api/members'));
  };

  useEffect(() => { load(); }, []);

  const issueBook = async (e) => {
    e.preventDefault();
    await apiRequest('/api/lendings', {
      method: 'POST',
      body: JSON.stringify({ bookId: Number(issue.bookId), memberId: Number(issue.memberId) })
    });
    setIssue({ bookId: '', memberId: '' });
    await load();
  };

  return (
    <section>
      <h1>Lending Management</h1>
      <form className="card form-grid" onSubmit={issueBook}>
        <h2>Issue Book</h2>
        <select value={issue.bookId} onChange={(e) => setIssue({ ...issue, bookId: e.target.value })} required>
          <option value="">Select Book</option>
          {books.filter((b) => b.availability).map((b) => (
            <option key={b.bookId} value={b.bookId}>{b.title} ({b.isbn})</option>
          ))}
        </select>
        <select value={issue.memberId} onChange={(e) => setIssue({ ...issue, memberId: e.target.value })} required>
          <option value="">Select Member</option>
          {members.map((m) => <option key={m.memberId} value={m.memberId}>{m.name}</option>)}
        </select>
        <button type="submit">Issue</button>
      </form>
      <div className="table-wrap">
        <table>
          <thead>
            <tr><th>Book</th><th>Member</th><th>Issue</th><th>Due</th><th>Return</th><th>Status</th><th>Action</th></tr>
          </thead>
          <tbody>
            {lendings.map((l) => (
              <tr key={l.lendingId}>
                <td>{l.bookTitle}</td><td>{l.memberName}</td>
                <td>{new Date(l.issueDate).toLocaleDateString()}</td>
                <td>{new Date(l.dueDate).toLocaleDateString()}</td>
                <td>{l.returnDate ? new Date(l.returnDate).toLocaleDateString() : '-'}</td>
                <td>{l.overdue ? 'Overdue' : l.returnDate ? 'Returned' : 'Active'}</td>
                <td>
                  {!l.returnDate && (
                    <button onClick={async () => { await apiRequest(`/api/lendings/${l.lendingId}`, { method: 'PUT' }); await load(); }}>
                      Return
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
