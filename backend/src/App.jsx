import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import BooksPage from './pages/BooksPage';
import AuthorsPage from './pages/AuthorsPage';
import MembersPage from './pages/MembersPage';
import LendingsPage from './pages/LendingsPage';
import NotificationsPage from './pages/NotificationsPage';
import MyLendingsPage from './pages/MyLendingsPage';
import MyNotificationsPage from './pages/MyNotificationsPage';
import DashboardPage from './pages/DashboardPage';

function PrivateRoute({ children, roles }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (roles && !roles.includes(user.role)) return <Navigate to="/" replace />;
  return <Layout>{children}</Layout>;
}

export default function App() {
  const { user } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" /> : <LoginPage />} />
      <Route path="/register" element={user ? <Navigate to="/" /> : <RegisterPage />} />
      <Route path="/" element={<PrivateRoute><DashboardPage /></PrivateRoute>} />
      <Route path="/books" element={<PrivateRoute><BooksPage /></PrivateRoute>} />
      <Route path="/authors" element={<PrivateRoute roles={['LIBRARIAN']}><AuthorsPage /></PrivateRoute>} />
      <Route path="/members" element={<PrivateRoute roles={['LIBRARIAN']}><MembersPage /></PrivateRoute>} />
      <Route path="/lendings" element={<PrivateRoute roles={['LIBRARIAN']}><LendingsPage /></PrivateRoute>} />
      <Route path="/notifications" element={<PrivateRoute roles={['LIBRARIAN']}><NotificationsPage /></PrivateRoute>} />
      <Route path="/my-lendings" element={<PrivateRoute roles={['MEMBER']}><MyLendingsPage /></PrivateRoute>} />
      <Route path="/my-notifications" element={<PrivateRoute roles={['MEMBER']}><MyNotificationsPage /></PrivateRoute>} />
    </Routes>
  );
}
