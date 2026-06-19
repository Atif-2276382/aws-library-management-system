import { useAuth } from '../context/AuthContext';

export default function DashboardPage() {
  const { user } = useAuth();
  const isLibrarian = user?.role === 'LIBRARIAN';

  return (
    <section>
      <h1>Welcome, {user?.username}</h1>
      <p className="subtitle">Role: {user?.role}</p>
      {isLibrarian ? (
        <div className="grid-cards">
          <article className="card"><h3>Books</h3><p>Manage catalog, ISBN, availability.</p></article>
          <article className="card"><h3>Authors</h3><p>Maintain author records and linked books.</p></article>
          <article className="card"><h3>Members</h3><p>Create and update library members.</p></article>
          <article className="card"><h3>Lendings</h3><p>Issue and return books with business rules.</p></article>
        </div>
      ) : (
        <div className="grid-cards">
          <article className="card"><h3>Browse Books</h3><p>View available titles and details.</p></article>
          <article className="card"><h3>My Lendings</h3><p>Track your issue, due, and return dates.</p></article>
          <article className="card"><h3>Notifications</h3><p>Due date and overdue reminders.</p></article>
        </div>
      )}
    </section>
  );
}
