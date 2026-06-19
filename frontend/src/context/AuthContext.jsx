import { createContext, useContext, useMemo, useState } from 'react';
import { apiRequest } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user');
    if (!saved) return null;
    try {
      return JSON.parse(saved);
    } catch (e) {
      // corrupted localStorage entry — clear to avoid render-time crash
      console.warn('AuthContext: failed to parse stored user, clearing localStorage');
      localStorage.removeItem('user');
      localStorage.removeItem('token');
      return null;
    }
  });

  const login = async (username, password) => {
    const data = await apiRequest('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password })
    });
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(data));
    setUser(data);
    return data;
  };

  const register = async (payload) => {
    const data = await apiRequest('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(data));
    setUser(data);
    return data;
  };

  const logout = async () => {
    try {
      await apiRequest('/api/auth/logout', { method: 'POST' });
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      setUser(null);
    }
  };

  const value = useMemo(() => ({ user, login, register, logout }), [user]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
