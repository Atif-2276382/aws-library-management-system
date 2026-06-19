import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';

export default function NotificationsPage() {
  const [members, setMembers] = useState([]);
  const [form, setForm] = useState({ memberId: '', message: '', overdue: false });

  useEffect(() => {
    apiRequest('/api/members').then(setMembers);
  }, []);

  const send = async (e) => {
    e.preventDefault();
    await apiRequest('/api/notifications', {
      method: 'POST',
      body: JSON.stringify({
        memberId: Number(form.memberId),
        message: form.message,
        overdue: form.overdue
      })
    });
    setForm({ memberId: '', message: '', overdue: false });
    alert('Notification sent');
  };

  return (
    <section>
      <h1>Send Notification</h1>
      <form className="card form-grid" onSubmit={send}>
        <select value={form.memberId} onChange={(e) => setForm({ ...form, memberId: e.target.value })} required>
          <option value="">Select Member</option>
          {members.map((m) => <option key={m.memberId} value={m.memberId}>{m.name}</option>)}
        </select>
        <textarea placeholder="Message" value={form.message} onChange={(e) => setForm({ ...form, message: e.target.value })} required />
        <label>
          <input type="checkbox" checked={form.overdue} onChange={(e) => setForm({ ...form, overdue: e.target.checked })} />
          Overdue
        </label>
        <button type="submit">Send</button>
      </form>
    </section>
  );
}
