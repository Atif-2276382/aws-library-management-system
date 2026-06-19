import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Layout({ children }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const isLibrarian = user?.role === 'LIBRARIAN';

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="app-shell">
      <header className="topbar">
        <Link to="/" className="brand">LibraryMS</Link>
        <nav className="nav">
          <NavLink to="/books">Books</NavLink>
          {isLibrarian && (
            <>
              <NavLink to="/authors">Authors</NavLink>
              <NavLink to="/members">Members</NavLink>
              <NavLink to="/lendings">Lendings</NavLink>
              <NavLink to="/notifications">Notifications</NavLink>
            </>
          )}
          {!isLibrarian && (
            <>
              <NavLink to="/my-lendings">My Lendings</NavLink>
              <NavLink to="/my-notifications">My Notifications</NavLink>
            </>
          )}
        </nav>
        <div className="user-box">
          <span>{user?.username} ({user?.role})</span>
          <button onClick={handleLogout}>Logout</button>
        </div>
      </header>
      <main className="content">{children}</main>
    </div>
  );
}
