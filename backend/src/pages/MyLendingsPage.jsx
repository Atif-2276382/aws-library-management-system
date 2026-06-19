import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';

export default function MyLendingsPage() {
  const [lendings, setLendings] = useState([]);

  useEffect(() => {
    apiRequest('/api/lendings/my').then(setLendings);
  }, []);

  return (
    <section>
      <h1>My Lending History</h1>
      <div className="table-wrap">
        <table>
          <thead><tr><th>Book</th><th>Issue</th><th>Due</th><th>Return</th><th>Status</th></tr></thead>
          <tbody>
            {lendings.map((l) => (
              <tr key={l.lendingId}>
                <td>{l.bookTitle}</td>
                <td>{new Date(l.issueDate).toLocaleDateString()}</td>
                <td>{new Date(l.dueDate).toLocaleDateString()}</td>
                <td>{l.returnDate ? new Date(l.returnDate).toLocaleDateString() : '-'}</td>
                <td>{l.overdue ? 'Overdue' : l.returnDate ? 'Returned' : 'Active'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
