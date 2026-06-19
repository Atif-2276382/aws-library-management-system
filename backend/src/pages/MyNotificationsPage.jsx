import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';

export default function MyNotificationsPage() {
  const [items, setItems] = useState([]);

  useEffect(() => {
    apiRequest('/api/notifications/my').then(setItems);
  }, []);

  return (
    <section>
      <h1>My Notifications</h1>
      <div className="grid-cards">
        {items.map((n) => (
          <article key={n.id} className={`card ${n.overdue ? 'overdue' : ''}`}>
            <p>{n.message}</p>
            <small>{new Date(n.sentAt).toLocaleString()} {n.overdue ? '• Overdue' : ''}</small>
          </article>
        ))}
        {!items.length && <p>No notifications yet.</p>}
      </div>
    </section>
  );
}
