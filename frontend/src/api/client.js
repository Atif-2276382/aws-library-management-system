const API_BASE = import.meta.env.VITE_API_URL || '';

cport async function apiRequest(path, options = {}) {
  const token = localStorage.getItem('token');
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Request failed' }));
    const details = error.errors ? ` (${Object.entries(error.errors).map(([k, v]) => `${k}: ${v}`).join(', ')})` : '';
    throw new Error((error.message || 'Request failed') + details);
  }
  if (response.status === 204) {
    return null;
  }
  return response.json();
}
