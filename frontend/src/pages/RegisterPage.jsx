import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: '',
    password: '',
    role: 'MEMBER',
    memberName: '',
    membershipId: '',
    emailId: ''
  });
  const [error, setError] = useState('');

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const payload = {
      username: form.username.trim(),
      password: form.password,
      role: form.role
    };
    if (form.role === 'MEMBER') {
      payload.memberName = form.memberName.trim();
      payload.membershipId = form.membershipId.trim();
      payload.emailId = form.emailId.trim();
    }
    try {
      await register(payload);
      navigate('/');
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="auth-page">
      <form className="card" onSubmit={onSubmit}>
        <h1>Register</h1>
        {error && <p className="error">{error}</p>}
        <label>Username<input name="username" value={form.username} onChange={onChange} required /></label>
        <label>Password<input type="password" name="password" value={form.password} onChange={onChange} required /></label>
        <label>Role
          <select name="role" value={form.role} onChange={onChange}>
            <option value="MEMBER">Member</option>
            <option value="LIBRARIAN">Librarian</option>
          </select>
        </label>
        {form.role === 'MEMBER' && (
          <>
            <label>Full Name<input name="memberName" value={form.memberName} onChange={onChange} required /></label>
            <label>Membership ID<input name="membershipId" value={form.membershipId} onChange={onChange} required /></label>
            <label>Email<input type="email" name="emailId" value={form.emailId} onChange={onChange} required /></label>
          </>
        )}
        <button type="submit">Create Account</button>
        <p>Already registered? <Link to="/login">Login</Link></p>
      </form>
    </div>
  );
}
